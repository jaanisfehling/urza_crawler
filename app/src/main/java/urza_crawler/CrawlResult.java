package urza_crawler;

import java.time.Instant;

public class CrawlResult {
    public String url;
    public String siteName;
    public Instant dateTime;
    public String htmlContent;
    public String listViewUrl;

    public CrawlResult(String url, String siteName, String htmlContent, String listViewUrl) {
        this.url = url;
        this.siteName = siteName;
        this.dateTime = Instant.now();
        this.htmlContent = htmlContent;
        this.listViewUrl = listViewUrl;
    }

    @Override
    public String toString() {
        return url;
    }
}
