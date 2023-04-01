package urza_crawler;

public class CrawlResult {
    public String url;
    public String siteName;
    public String htmlContent;

    public CrawlResult(String url, String siteName, String htmlContent) {
        this.url = url;
        this.siteName = siteName;
        this.htmlContent = htmlContent;
    }

    @Override
    public String toString() {
        return url;
    }
}
