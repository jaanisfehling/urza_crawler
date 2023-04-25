package urza_crawler;

public class CrawlResult {
    public String url;
    public String html;
    public boolean isNew;

    public CrawlResult(String url, String html, boolean isNew) {
        this.url = url;
        this.html = html;
        this.isNew = isNew;
    }

    @Override
    public String toString() {
        return url;
    }
}
