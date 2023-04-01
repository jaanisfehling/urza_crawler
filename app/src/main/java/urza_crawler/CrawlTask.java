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

import static urza_crawler.UrlUtils.getAbsoluteUrl;
import static urza_crawler.UrlUtils.getBaseUrl;

public class CrawlTask implements Runnable {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;

    void updateCrawlTask() {
        String query = "UPDATE \"target\" SET most_recent_article_url=? WHERE list_view_url=?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:32768/", "postgres", "mysecretpassword");) {
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

    public void run() {
        // Scrape List View URL
        Document listViewDoc = null;
        try {
            listViewDoc = Jsoup.connect(listViewUrl).get();
        } catch (IOException e) {
            System.out.println("Error fetching " + listViewUrl);
            return;
        }

        // Get the Name of the List View Site
        Element title = listViewDoc.select("title").first();
        String siteName = (title == null) ? null : title.text();

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        for (Element headline : headlines) {
            String articleUrl = getAbsoluteUrl(getBaseUrl(listViewUrl), headline.attr("href"));

            // If headline was already scraped, we can quit since all other headlines are older
            if (!articleUrl.equals(mostRecentArticleUrl)) {

                // Update the most recent article in the database
                mostRecentArticleUrl = articleUrl;
                updateCrawlTask();

                Document articleDoc = null;
                try {
                    articleDoc = Jsoup.connect(articleUrl).get();

                    // Create Crawl Result
                    String htmlContent = articleDoc.select("*").html();
                    CrawlResult result = new CrawlResult(articleUrl, siteName, htmlContent, listViewUrl);

                    // Send scraped result to Pipeline
                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(Instant.class, new InstantSerializer());
                    Gson gson = builder.create();
                    String json = gson.toJson(result);
                    Main.pipelineClient.send(json);

                } catch (IOException e) {
                    System.out.println("Error fetching " + articleUrl);
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        return listViewUrl;
    }

    private static class InstantSerializer implements JsonSerializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
}
