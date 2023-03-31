package urza_crawler;

import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;

import static urza_crawler.UrlUtils.getAbsoluteUrl;
import static urza_crawler.UrlUtils.getBaseUrl;

public class CrawlTask implements Runnable {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;

    private static class InstantSerializer implements JsonSerializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
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
            String headlineUrl = getAbsoluteUrl(getBaseUrl(listViewUrl), headline.attr("href"));

            // If headline was already scraped, we can quit since all other headlines are older
            if (headlineUrl.equals(mostRecentArticleUrl)) {
                return;
            }

            // If this is a new Headline
            else {
                Document articleDoc = null;
                try {
                    articleDoc = Jsoup.connect(headlineUrl).get();

                    // Create Crawl Result
                    String htmlContent = articleDoc.select("*").html();
                    CrawlResult result = new CrawlResult(headlineUrl, siteName, htmlContent, listViewUrl);

                    // Send scraped result to Pipeline
                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(Instant.class, new InstantSerializer());
                    Gson gson = builder.create();
                    String json = gson.toJson(result);
                    Main.pipelineClient.send(json);

                } catch (IOException e) {
                    System.out.println("Error fetching " + headlineUrl);
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
