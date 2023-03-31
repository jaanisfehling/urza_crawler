package urza_crawler;

public class UrlUtils {
    public static String getBaseUrl(String absoluteUrl) {
        absoluteUrl = absoluteUrl.replace("https://", "");

        StringBuilder siteUrl = new StringBuilder();
        for (int i=0; i<absoluteUrl.length(); i++) {
            char c = absoluteUrl.charAt(i);
            siteUrl.append(c);
            if (c == '/') {
                break;
            }
        }
        return "https://" + siteUrl.toString();
    }

    public static String getAbsoluteUrl(String baseUrl, String relativeUrl) {
        for (int i = 0; i < baseUrl.length(); i++) {
            String baseUrlSubstring = baseUrl.substring(i);
            if (relativeUrl.contains(baseUrlSubstring)) {
                return baseUrl.substring(0, i) + relativeUrl;
            }
        }
        return baseUrl + relativeUrl;
    }
}
