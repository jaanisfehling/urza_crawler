package urza_crawler;

import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.Callable;

import static urza_crawler.UrlUtils.getAbsoluteUrl;
import static urza_crawler.UrlUtils.getBaseUrl;

public class CrawlTask implements Callable<Boolean> {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;

    private void updateMostRecentArticleUrl() {
        String query = "UPDATE \"target\" SET most_recent_article_url=? WHERE list_view_url=?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword")) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, mostRecentArticleUrl);
                stmt.setString(2, listViewUrl);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Document request(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Error fetching " + url);
            return null;
        }
    }

    @Override
    public Boolean call() {
        Document listViewDoc = request(listViewUrl);


        // Get the Name of the List View Site
        Element title = listViewDoc.select("title").first();
        String siteName = (title == null) ? null : title.text();

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        boolean isFirstArticle = true;
        for (Element headline : headlines) {
            String articleUrl = getAbsoluteUrl(getBaseUrl(listViewUrl), headline.attr("href"));

            if (articleUrl.equals(mostRecentArticleUrl) || headline.attr("href").equals(mostRecentArticleUrl)) {
                return true;
            }
            else {
                if (isFirstArticle) {
                    mostRecentArticleUrl = articleUrl;
                    updateMostRecentArticleUrl();
                    isFirstArticle = false;
                }
                Document articleDoc = null;
                articleDoc = request(articleUrl);

                // Create Crawl Result
                String htmlContent = articleDoc.select("*").html();
                CrawlResult result = new CrawlResult(articleUrl, siteName, htmlContent);

                // Send scraped result to Pipeline
                Gson gson = new Gson();
                String json = gson.toJson(result);
                Main.pipelineClient.send(json);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
