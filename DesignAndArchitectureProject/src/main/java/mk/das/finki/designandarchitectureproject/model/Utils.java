package mk.das.finki.designandarchitectureproject.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Document getDocumentWithSSL(String url) throws Exception {
        // Create custom SSL context that trusts all certificates
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        }, new java.security.SecureRandom());

        // Use the custom SSL context
        return Jsoup.connect(url)
                .sslSocketFactory(sslContext.getSocketFactory())
                .get();
    }

    public static List<String> extractDropdownOptions(String url) {
        List<String> dates = new ArrayList<>();

        try {
            // Fetch the HTML content of the webpage
            Document document = getDocumentWithSSL(url);

            // Select the dropdown element by its ID
            Element dropdown = document.selectFirst("#Code");

            if (dropdown != null) {
                // Get all options within the dropdown
                Elements options = dropdown.select("option");

                // Filter out options containing digits
                for (Element option : options) {
                    String optionText = option.text();
                    if (!optionText.matches(".*\\d.*")) { // Regex to check if text contains digits
                        dates.add(optionText);
                    }
                }
            } else {
                System.out.println("Dropdown with ID 'Code' not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to fetch the webpage.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dates;
    }

    public static int getYear(String date){
        return Integer.parseInt(date.split("\\.")[2]);
    }
}

