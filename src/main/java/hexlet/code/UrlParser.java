package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UrlParser {
    public static UrlCheck checkUrl(Url url) {
        try {
            HttpResponse response = Unirest
                    .get(url.getName())
                    .asString();
            int statusCode = response.getStatus();
            Document doc = Jsoup.parse((String) response.getBody());
            String title = doc.title();
            String h1 = doc.getElementsByTag("h1").text();
            String description = getATag(doc, "description");
            return new UrlCheck(statusCode, title, h1, description, url);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getATag(Document document, String attr) {
        Elements elements = document.select("meta[name=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) {
                return s;
            }
        }
        elements = document.select("meta[property=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) {
                return s;
            }
        }
        return "";
    }
}
