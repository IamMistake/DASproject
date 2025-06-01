package mk.das.finki.designandarchitectureproject.repository;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import mk.das.finki.designandarchitectureproject.bootstrap.NewsScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NewsRepository {

    public static List<String> linksOfComapniesMseSites() {
        List<String> links = new ArrayList<>();
        for (String company : NewsScraper.companies.keySet()) {
            String url = "https://www.mse.mk/mk/issuer/" + toSlug(company);
            System.out.println(url);
            links.add(url);
        }

        return links;
    }

    private static String toSlug(String input) {
        String transliterated = input
                .replace("А", "A").replace("Б", "B").replace("В", "V")
                .replace("Г", "G").replace("Д", "D").replace("Ѓ", "Gj")
                .replace("Е", "E").replace("Ж", "Zh").replace("З", "Z")
                .replace("И", "I").replace("Ј", "J").replace("К", "K")
                .replace("Л", "L").replace("М", "M").replace("Н", "N")
                .replace("Њ", "Nj").replace("О", "O").replace("П", "P")
                .replace("Р", "R").replace("С", "S").replace("Т", "T")
                .replace("Ќ", "Kj").replace("У", "U").replace("Ф", "F")
                .replace("Х", "H").replace("Ц", "C").replace("Ч", "C")
                .replace("Џ", "Dz").replace("Ш", "S")
                .replace("а", "a").replace("б", "b").replace("в", "v")
                .replace("г", "g").replace("д", "d").replace("ѓ", "gj")
                .replace("е", "e").replace("ж", "zh").replace("з", "z")
                .replace("и", "i").replace("ј", "j").replace("к", "k")
                .replace("л", "l").replace("м", "m").replace("н", "n")
                .replace("њ", "nj").replace("о", "o").replace("п", "p")
                .replace("р", "r").replace("с", "s").replace("т", "t")
                .replace("ќ", "kj").replace("у", "u").replace("ф", "f")
                .replace("х", "h").replace("ц", "c").replace("ч", "c")
                .replace("џ", "dz").replace("ш", "s");

        String normalized = Normalizer.normalize(transliterated, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase()
                .replace(" ", "-")
                .replace("^", "-")
                .replace("*", "");
//                .replaceAll("[^a-z0-9\\-]+", "");
    }

    public static Map<String, String> scrapeNewsLinks(String url) {
        Map<String, String> newsLinks = new HashMap<>();
        try {
            Document document = Jsoup.connect(url).get();

            Element divOfUl = document.getElementById("seiNetIssuerLatestNews");
            Elements as = divOfUl.select("a");
            Elements h4 = divOfUl.select("h4");

            for (int i = 0; i < as.size(); i++) {
                newsLinks.putIfAbsent(h4.get(i).text(), as.get(i).attr("href"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to fetch the page.");
        }

        return newsLinks;
    }

    public static String getNews(String url) {
        String res = "";

        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

            HtmlPage page = webClient.getPage(url);

            webClient.waitForBackgroundJavaScript(2000);

            String pageSource = page.asXml();
            Document document = Jsoup.parse(pageSource);

            Element text = document.body().getElementsByClass("col").get(3).select("div").get(0);
            Element text1 = document.getElementsByClass("row").get(1).select("div").get(1);
            res = text.text() + "\n" + text1.text();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to fetch the page.");
        }

        return res;
    }

    public static String translateToEnglish(String text) {
        try {
            String url = "https://translate.google.com/m?hl=en&sl=mk&tl=en&q=" +
                    java.net.URLEncoder.encode(text, "UTF-8");

            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(1000)
                    .execute();

            String translatedText = Jsoup.parse(response.body())
                    .select("div.result-container")
                    .text();

            return translatedText;
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation failed.";
        }
    }

    public static String getCompanyUrl(String companySelected) {
        String company = getCompanyName(companySelected);
        String url = "https://www.mse.mk/mk/issuer/" + toSlug(company);
        return url;
    }

    private static String getCompanyName(String companySelected) {
        return NewsScraper.companies.keySet().stream().filter(k -> NewsScraper.companies.get(k).equals(companySelected)).findFirst().orElse("Алкалоид АД Скопје");
    }
}

