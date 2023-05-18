import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import urza_crawler.CrawlTask;

public class TestCrawlTask {

    private final CrawlTask task = new CrawlTask("https://books.toscrape.com/", "h3 > a", "catalogue/sharp-objects_997/index.html", null, null, null);


}
