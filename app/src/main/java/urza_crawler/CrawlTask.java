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

public class CrawlTask implements Callable<Boolean> {
    String listViewUrl;
    String articleSelector;
    String mostRecentArticleUrl;
    String nextPageSelector;
    transient String siteName;

    private void updateMostRecentArticleUrl(String newMostRecentArticleUrl) {
        String query = "UPDATE \"target\" SET most_recent_article_url=? WHERE list_view_url=?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword")) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newMostRecentArticleUrl);
                stmt.setString(2, listViewUrl);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("SQL Exception: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }

    private Document request(String url, String referrerUrl) {

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:111.0) Gecko/20100101 Firefox/111.0")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .referrer(referrerUrl)
                    .timeout(3000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .get();
            return doc;
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
            return null;
        }
    }

    private void scrapeArticle(String url, String referrerUrl) {
        Document articleDoc = request(url, referrerUrl);
        if (articleDoc == null) {
            return;
        }

        String htmlContent = articleDoc.select("*").html();
        CrawlResult result = new CrawlResult(url, siteName, htmlContent);

        Gson gson = new Gson();
        String json = gson.toJson(result);
        Main.pipelineClient.send(json);
    }

    private String crawlPage(String url, String referrerUrl, boolean isFirstPage) {

        Document listViewDoc = request(url, referrerUrl);
        if (listViewDoc == null) {
            return null;
        }

        // Get the Name of the List View Site
        Element title = listViewDoc.select("title").first();
        siteName = (title == null) ? null : title.text();

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        boolean isFirstArticle = isFirstPage;
        for (Element headline : headlines) {
            String absArticleUrl = headline.attr("abs:href");

            // If current Headline matches most recent article, we cancel
            if (absArticleUrl.equals(mostRecentArticleUrl) || headline.attr("href").equals(mostRecentArticleUrl)) {
                return null;
            }
            else {
                if (isFirstArticle) {
                    updateMostRecentArticleUrl(absArticleUrl);
                    isFirstArticle = false;
                }
                scrapeArticle(absArticleUrl, listViewUrl);
            }
        }
        return listViewDoc.select(nextPageSelector).first().attr("abs:href");
    }

    @Override
    public Boolean call() {
        String nextPageUrl = crawlPage(listViewUrl, "www.google.com", true);
        String previousListViewUrl = listViewUrl;
        String currentPageUrl;

        while(nextPageUrl != null) {
            currentPageUrl = nextPageUrl;
            nextPageUrl = crawlPage(nextPageUrl, previousListViewUrl, false);
            previousListViewUrl = currentPageUrl;
        }
        return null;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
