package urza_crawler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlUtilsTest {

    @Test void getBaseUrlTest() {
        String url1 = "https://www.bayer.com/media/";
        String url2 = "https://investor.fb.com/investor-news/default.aspx";
        String baseUrl1 = "https://www.bayer.com/";
        String baseUrl2 = "https://investor.fb.com/";

        assertEquals(UrlUtils.getBaseUrl(url1), baseUrl1);
        assertEquals(UrlUtils.getBaseUrl(url2), baseUrl2);
    }

    @Test void getAbsoluteUrlTest() {
        String baseUrl1 = "https://www.bayer.com/";
        String baseUrl2 = "https://investor.fb.com/";
        String relUrl1 = "//www.bayer.com/media/bayer-kultur-in-berlin-konzert-mit-giorgi-gigashvili--tsduneba/";
        String relUrl2 = "/investor-news/press-release-details/2023/Meta-to-Participate/default.aspx";
        String absUrl1 = "https://www.bayer.com/media/bayer-kultur-in-berlin-konzert-mit-giorgi-gigashvili--tsduneba/";
        String absUrl2 = "https://investor.fb.com/investor-news/press-release-details/2023/Meta-to-Participate/default.aspx";

        assertEquals(UrlUtils.getAbsoluteUrl(baseUrl1, relUrl1), absUrl1);
        assertEquals(UrlUtils.getAbsoluteUrl(baseUrl2, relUrl2), absUrl2);
    }
}
