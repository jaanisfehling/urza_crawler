package urza_crawler;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static urza_crawler.Main.*;


public class CrawlTask implements Callable<CrawlTask> {
    String listViewUrl;
    String articleSelector;
    String mostRecentArticleUrl;
    String nextPageSelector;
    Boolean oldArticlesScraped;
    Integer maxPageDepth;

    transient String oldMostRecentArticleUrl;

    public CrawlTask(String listViewUrl, String articleSelector, String mostRecentArticleUrl, String nextPageSelector, Boolean oldArticlesScraped, Integer maxPageDepth) {
        this.listViewUrl = listViewUrl;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleUrl;
        this.nextPageSelector = nextPageSelector;
        this.oldArticlesScraped = oldArticlesScraped;
        this.maxPageDepth = maxPageDepth;
    }

    private void updateCrawlTask() {
        oldArticlesScraped = true;
        logger.log(Level.INFO, "Sending back updated Crawl Task");
        Gson gson = new Gson();
        queueClient.send(gson.toJson(this));
    }

    public Document request(String url, String referrerUrl) {
        try {
            return Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "identity")
                    .header("Accept-Language", "en-US;q=0.7,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", new URL(url).getHost())
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("TE", "trailers")
                    .header("Upgrade-Insecure-Requests", "1")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/113.0")
                    .referrer(referrerUrl)
                    .timeout(3000)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception: " + e.getMessage());
            return null;
        }
    }

    private void scrapeArticle(String url, String referrerUrl, boolean isNew) {
        logger.log(Level.FINE, "Requesting " + url);
        Document articleDoc = request(url, referrerUrl);
        if (articleDoc == null) {
            return;
        }

        String html = articleDoc.select("*").html();
        CrawlResult result = new CrawlResult(url, html, isNew);

        Gson gson = new Gson();
        String json = gson.toJson(result);
        pipelineClient.send(json);
    }

    private String crawlPage(String url, String referrerUrl, boolean isFirstPage) {
        Document listViewDoc = request(url, referrerUrl);
        if (listViewDoc == null) {
            logger.log(Level.SEVERE, "Empty document for " + url);
            return null;
        }

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        if (headlines.isEmpty()) {
            logger.log(Level.SEVERE, "No headlines for " + url);
            return null;
        }
        boolean isFirstArticle = isFirstPage;
        for (Element headline : headlines) {
            String absArticleUrl = headline.attr("abs:href");

            // If current Headline matches most recent article, we cancel
            if (absArticleUrl.equals(oldMostRecentArticleUrl) || headline.attr("href").equals(oldMostRecentArticleUrl)) {
                return null;
            } else {
                if (isFirstArticle) {
                    mostRecentArticleUrl = absArticleUrl;
                    updateCrawlTask();
                    isFirstArticle = false;
                }
                logger.log(Level.INFO, "New article: " + absArticleUrl);
                scrapeArticle(absArticleUrl, listViewUrl, oldArticlesScraped);
            }
        }
        if (nextPageSelector != null) {
            Element nextPage = listViewDoc.select(nextPageSelector).first();
            if (nextPage == null) {
                logger.log(Level.SEVERE, "No next page for " + url);
                return null;
            }
            return nextPage.attr("abs:href");
        }
        return null;
    }

    @Override
    public CrawlTask call() {
        oldMostRecentArticleUrl = mostRecentArticleUrl;
        String nextPageUrl = crawlPage(listViewUrl, "www.google.com", true);
        String previousListViewUrl = listViewUrl;
        String currentPageUrl;
        int pageIndex = 1;

        while (nextPageUrl != null && pageIndex < maxPageDepth) {
            currentPageUrl = nextPageUrl;
            nextPageUrl = crawlPage(nextPageUrl, previousListViewUrl, false);
            previousListViewUrl = currentPageUrl;
            pageIndex++;
        }
        return this;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
