package urza_crawler;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;

public class CrawlTask {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;

    public CrawlResult crawl() {

        // Scrape List View URL
        Document listViewDoc = null;
        try {
            listViewDoc = Jsoup.connect(listViewUrl).get();
        } catch (IOException e) {
            System.out.println("Error fetching " + listViewUrl);
            return null;
        }

        // Get the Name of the List View Site
        Element title = listViewDoc.select("title").first();
        String siteName = (title == null) ? null : title.text();

        // Iterate over all headlines
        Elements headlines = listViewDoc.select(articleSelector);
        for (Element headline : headlines) {
            String headlineUrl = headline.attr("href");

            // If headline was already scraped, we can quit since all other headlines are older
            if (headlineUrl.equals(mostRecentArticleUrl)) {
                return null;
            }

            // If this is a new Headline
            else {
                Document articleDoc = null;
                try {
                    articleDoc = Jsoup.connect(headlineUrl).get();

                    // Create Crawl Result
                    Instant dateTime = Instant.now();
                    String htmlContent = articleDoc.select("*").html();
                    return new CrawlResult(headlineUrl, siteName, dateTime, htmlContent);

                } catch (IOException e) {
                    System.out.println("Error fetching " + headlineUrl);
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
