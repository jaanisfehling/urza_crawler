package urza_crawler;

public class CrawlResult {
    public String url;
    public String htmlContent;
    public boolean isNew;

    public CrawlResult(String url, String htmlContent, boolean isNew) {
        this.url = url;
        this.htmlContent = htmlContent;
        this.isNew = isNew;
    }

    @Override
    public String toString() {
        return url;
    }
}
