import {queue, server} from "./main";
import axios from "axios";
import * as cheerio from "cheerio";
import { createRequire } from "module";
const require = createRequire(import.meta.url);
const {Worker} = require("worker_threads");

export default class CrawlTask {
    async constructor(listViewUrl, articleSelector, mostRecentArticleUrl, nextPageSelector, oldArticlesScraped, maxPageDepth) {
        this.listViewUrl = listViewUrl;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleUrl;
        this.nextPageSelector = nextPageSelector;
        this.oldArticlesScraped = oldArticlesScraped;
        this.maxPageDepth = maxPageDepth;
        this.oldMostRecentArticleUrl;
        this.baseUrl = new URL(listViewUrl).hostname;
        await this.run();
    }

    updateCrawlTask() {
        console.log("Sending back updated Crawl Task");
        queue.send(JSON.stringify({
            listViewUrl: this.listViewUrl,
            articleSelector: this.articleSelector,
            mostRecentArticleUrl: this.mostRecentArticleUrl,
            nextPageSelector: this.nextPageSelector,
            oldArticlesScraped: true,
            maxPageDepth: this.maxPageDepth
        }));
    }

    async request(url, referer) {
        try {
            const response = await axios.get(url, {
                headers: {
                    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                    "Accept-Encoding": "identity",
                    "Accept-Language": "en-US;q=0.7,en;q=0.3",
                    "Connection": "keep-alive",
                    "Host": this.baseUrl,
                    "Sec-Fetch-Dest": "document",
                    "Sec-Fetch-Mode": "navigate",
                    "Sec-Fetch-Site": "none",
                    "Sec-Fetch-User": "?1",
                    "TE": "trailers",
                    "Upgrade-Insecure-Requests": "1",
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/113.0",
                    "Referer": referer
                }
            });
            return response.data;
        } catch(e) {
            console.error("Error fetching Url: " + url + "\n" + e.message);
        }
    }

    async scrapeArticle(url, referer, isNew) {
        console.log("Requesting " + url);
        const html = await this.request(url, referer);
        if (html === null) {
            console.error("Empty document for " + url);
            return;
        }
        const article = {url: url, html: html, isNew: isNew}

        const worker = new Worker("./parse.js", {
            workerData: {
                article: article
            }
        });

        worker.once("message", async article => {
            if (article !== null) {
                console.log("Valid Article");
                server.send(JSON.stringify(article));
            } else {
                console.error("Invalid Article");
            }
        });

        worker.on("error", (e) => {
            console.error("Error in worker for " + article.url + ":\n" + e.message);
        });
    }

    async crawlPage(url, referer, isFirstPage) {
        const html = await this.request(url, referer);
        const $ = cheerio.load(html);
        if (html === null || $ === null) {
            console.error("Empty document for " + url);
            return null;
        }

        // Iterate over all headlines
        const headlines = $(this.articleSelector);
        if (headlines === null) {
            console.log("No headlines for " + url);
            return null;
        }
        let isFirstArticle = isFirstPage;
        for (const headline of headlines) {
            const relArticleUrl = $(headline).attr("href");
            const absArticleUrl = new URL(relArticleUrl, this.baseUrl);

            // If current Headline matches most recent article, we cancel
            if (absArticleUrl === this.oldMostRecentArticleUrl || relArticleUrl === this.oldMostRecentArticleUrl) {
                return null;
            } else {
                if (isFirstArticle) {
                    this.mostRecentArticleUrl = absArticleUrl;
                    this.updateCrawlTask();
                    isFirstArticle = false;
                }
                console.log("New article: " + absArticleUrl);
                await this.scrapeArticle(absArticleUrl, this.listViewUrl, this.oldArticlesScraped);
            }
        }
        if (this.nextPageSelector != null) {
            const nextPage = $(this.nextPageSelector).first();
            if (nextPage == null) {
                console.error("No next page for " + url);
                return null;
            }
            return nextPage.attr("abs:href");
        }
        return null;
    }

    async run() {
        this.oldMostRecentArticleUrl = this.mostRecentArticleUrl;
        let nextPageUrl = this.crawlPage(this.listViewUrl, "www.google.com", true);
        let previousListViewUrl = this.listViewUrl;
        let currentPageUrl;
        let pageIndex = 1;

        while (nextPageUrl != null && pageIndex < this.maxPageDepth) {
            currentPageUrl = nextPageUrl;
            nextPageUrl = this.crawlPage(nextPageUrl, previousListViewUrl, false);
            previousListViewUrl = currentPageUrl;
            pageIndex++;
        }
    }
}