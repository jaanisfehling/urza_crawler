import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import urza_crawler.CrawlTask;

public class CrawlTaskTest {

    @Test
    void testRequest() {
        CrawlTask task = new CrawlTask("https://books.toscrape.com", "h3 > a", "catalogue/sharp-objects_997/index.html", null, null, null);
        Document doc = task.request("https://www.bayer.com/media/", "google.com");
        System.out.println(doc);
    }
}
