import org.junit.jupiter.api.Test;
import urza_crawler.CrawlTask;

import static org.mockito.Mockito.spy;

public class CrawlTaskTest {

    @Test
    void testNoNewArticles() {
        CrawlTask task = new CrawlTask("https://books.toscrape.com/", "h3 > a", "catalogue/sharp-objects_997/index.html", null, null, null);
        CrawlTask spiedTask = spy(task);
    }
}
