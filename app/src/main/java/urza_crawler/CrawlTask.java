package urza_crawler;

public class CrawlTask {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;

    public CrawlTask(String listViewUrl, String articleSelector, String mostRecentArticleUrl) {
        this.listViewUrl = listViewUrl;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleUrl;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }
}
