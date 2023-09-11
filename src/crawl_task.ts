import {apiToken, newArticleEndpoint, queue, scraperKey} from "./main.js";
import axios, { AxiosError } from "axios";
import {parseHTML} from "linkedom";
import {createRequire} from "module";

const require = createRequire(import.meta.url);
const {Worker} = require("worker_threads");

export default async function crawl(task) {
    let ticker = task.ticker;
    let listViewUrl = task.listViewUrl;
    let articleSelector = task.articleSelector;
    let mostRecentArticleUrl = task?.mostRecentArticleUrl;
    let nextPageSelector = task?.nextPageSelector;
    let oldArticlesScraped = task?.oldArticlesScraped;
    let maxPageDepth = task?.maxPageDepth;
    let baseUrl = new URL(listViewUrl);

    let oldMostRecentArticleUrl = mostRecentArticleUrl;
    let nextPageUrl = await crawlPage(listViewUrl, "www.google.com", true);
    let previousListViewUrl = listViewUrl;
    let currentPageUrl;
    let pageIndex = 1;

    while (nextPageUrl !== null && pageIndex < maxPageDepth) {
        currentPageUrl = nextPageUrl;
        nextPageUrl = await crawlPage(nextPageUrl, previousListViewUrl, false);
        previousListViewUrl = currentPageUrl;
        pageIndex++;
    }

    function updateCrawlTask() {
        console.log("Sending back updated Crawl Task");
        queue.connection.send(JSON.stringify({
            listViewUrl: listViewUrl,
            articleSelector: articleSelector,
            mostRecentArticleUrl: mostRecentArticleUrl,
            nextPageSelector: nextPageSelector,
            oldArticlesScraped: true,
            maxPageDepth: maxPageDepth
        }));
    }

    async function request(url, referer) {
        try {
            const response = await axios.get(url, {
                headers: {
                    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                    "Accept-Encoding": "identity",
                    "Accept-Language": "en-US;q=0.7,en;q=0.3",
                    "Connection": "keep-alive",
                    "Host": baseUrl.hostname,
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
        } catch (e: unknown) {
            const error = e as AxiosError;
            if (error.response) {
                if (error.response.data) {
                    console.error(error.response.data);
                } else {
                    console.error("Bad request: " + url);
                }
            } else if (error.request) {
                console.error("Cannot establish connection: " + url);
            } else {
                console.error("Client error: " + url);
            }
        }
    }

    async function scrapeArticle(url, referer, isNew) {
        const html = await request(url, referer);
        if (html === null) {
            console.error("Empty document for " + url);
            return;
        }
        const article = {url: url, ticker: ticker, html: html, isNew: isNew}

        const worker = new Worker("./src/parse.js", {
            workerData: {
                article: article
            }
        });

        worker.once("message", async article => {
            if (article !== null) {
                console.log("Valid Article");
                try {
                    await axios.post(newArticleEndpoint, JSON.stringify(article), {headers: {
                        "Authorization": "APIToken " + apiToken,
                        "Scraper-Key": scraperKey,
                        "Content-Type": "application/json"
                    }});
                } catch (e: unknown) {
                    const error = e as AxiosError;
                    if (error.response) {
                        if (error.response.data) {
                            console.error("(Django) " + error.response.data);
                        } else {
                            console.error("(Django) Bad request");
                        }
                    } else if (error.request) {
                        console.error("(Django) Cannot establish connection");
                    } else {
                        console.error("(Django) Client error");
                    }
                }
            } else {
                console.error("Invalid article");
            }
        });

        worker.on("error", (e) => {
            console.error("Error in worker for " + article.url + ":\n" + e.message);
        });
    }

    async function crawlPage(url, referer, isFirstPage) {
        const html = await request(url, referer);
        const {document} = parseHTML(html);
        if (document === null) {
            console.error("Empty document for " + url);
            return null;
        }

        // Iterate over all headlines
        const headlines = document.querySelectorAll(articleSelector);
        if (headlines === null) {
            console.log("No headlines for " + url);
            return null;
        }
        let isFirstArticle = isFirstPage;
        for (const headline of headlines) {
            const relArticleUrl = headline.href;
            const absArticleUrl = new URL(relArticleUrl, baseUrl.href).href;

            // If current Headline matches most recent article, we cancel
            if (absArticleUrl === oldMostRecentArticleUrl || relArticleUrl === oldMostRecentArticleUrl) {
                return null;
            } else {
                if (isFirstArticle) {
                    mostRecentArticleUrl = absArticleUrl;
                    updateCrawlTask();
                    isFirstArticle = false;
                }
                console.log("New article: " + absArticleUrl);
                await scrapeArticle(absArticleUrl, listViewUrl, oldArticlesScraped);
            }
        }
        if (nextPageSelector != null) {
            const nextPage = document.querySelector(nextPageSelector);
            if (nextPage === null) {
                console.error("No next page for " + url);
                return null;
            }
            return new URL(nextPage.href, baseUrl.href).href;
        }
        return null;
    }
}