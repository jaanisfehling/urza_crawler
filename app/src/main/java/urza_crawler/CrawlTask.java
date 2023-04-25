package urza_crawler;

import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static urza_crawler.Main.*;

public class CrawlTask implements Callable<CrawlTask> {
    String listViewUrl;
    String articleSelector;
    String mostRecentArticleUrl;
    String nextPageSelector;
    boolean oldArticlesScraped;
    int maxPageDepth;

    private void updateCrawlTask() {
        oldArticlesScraped = true;
        logger.log(Level.INFO, "Sending back updated Crawl Task");
        Gson gson = new Gson();
        queueClient.send(gson.toJson(this));

        String query = "UPDATE target SET most_recent_article_url=?, old_articles_scraped=? WHERE list_view_url=?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword")) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, mostRecentArticleUrl);
                stmt.setBoolean(2, oldArticlesScraped);
                stmt.setString(3, listViewUrl);
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Exception: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception: " + e.getMessage());
        }
    }

    private Document request(String url, String referrerUrl) {

        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:111.0) Gecko/20100101 Firefox/111.0")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .referrer(referrerUrl)
                    .timeout(3000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception: " + e.getMessage());
            return null;
        }
    }

    private void scrapeArticle(String url, String referrerUrl, boolean isNew) {
        logger.log(Level.INFO, "Requesting " + url);
        Document articleDoc = request(url, referrerUrl);
        if (articleDoc == null) {
            return;
        }

        String html = articleDoc.select("*").html();
        CrawlResult result = new CrawlResult(url, html, isNew);

        Gson gson = new Gson();
        String json = gson.toJson(result);
        Main.pipelineClient.send(json);
    }

    private String crawlPage(String url, String referrerUrl, boolean isFirstPage) {
        Document listViewDoc = request(url, referrerUrl);
        if (listViewDoc == null) {
            return null;
        }

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        if (headlines.isEmpty()) {
            logger.log(Level.SEVERE, "No headlines for " + url);
        }
        boolean isFirstArticle = isFirstPage;
        for (Element headline : headlines) {
            String absArticleUrl = headline.attr("abs:href");

            // If current Headline matches most recent article, we cancel
            if (absArticleUrl.equals(mostRecentArticleUrl) || headline.attr("href").equals(mostRecentArticleUrl)) {
                return null;
            }
            else {
                if (isFirstArticle) {
                    mostRecentArticleUrl = absArticleUrl;
                    updateCrawlTask();
                    isFirstArticle = false;
                }
                scrapeArticle(absArticleUrl, listViewUrl, oldArticlesScraped);
            }
        }
        Element nextPage = listViewDoc.select(nextPageSelector).first();
        return (nextPage == null) ? null : nextPage.attr("abs:href");
    }

    @Override
    public CrawlTask call() {
        String nextPageUrl = crawlPage(listViewUrl, "www.google.com", true);
        String previousListViewUrl = listViewUrl;
        String currentPageUrl;
        int pageIndex = 1;

        while(nextPageUrl != null && pageIndex <= maxPageDepth) {
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
