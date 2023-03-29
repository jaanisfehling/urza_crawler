package urza_crawler;

import java.time.Instant;

public class CrawlResult {
    public String url;
    public String siteName;
    public Instant dateTime;
    public String htmlContent;

    public CrawlResult(String url, String siteName, Instant dateTime, String htmlContent) {
        this.url = url;
        this.siteName = siteName;
        this.dateTime = dateTime;
        this.htmlContent = htmlContent;
    }

    @Override
    public String toString() {
        return url;
    }
}
