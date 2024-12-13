package backend.main.src;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class JavaWebScraper {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the zip code: ");
        String zipCode = scanner.nextLine().trim();

        System.out.print("Enter the item to search for: ");
        String searchQuery = scanner.nextLine().trim();

        String url = "https://www.bestbuy.com/site/searchpage.jsp?st=" + searchQuery + "&zip=" + zipCode;

        JSONArray productsArray = new JSONArray();

        try {
            Document document = Jsoup.connect(url).get();
            Elements products = document.select(".sku-item");

            for (Element product : products) {
                String name = product.select(".sku-title").text();
                String priceText = product.select(".priceView-customer-price span").text();

                //extract only the current price
                String price = priceText.replaceAll("Your price for this item is ", "").trim();

                //Ensure to take the first occurrence of the price if it appears > 1 times
                if (price.contains(" ")) {
                    price = price.split(" ")[0];
                }

                //Create x1 JSON object
                JSONObject productJson = new JSONObject();
                productJson.put("name", name);
                productJson.put("currentPrice", price);

                productsArray.put(productJson);
            }

            //Write products array to JSON file
            writeToFile("products.json", productsArray.toString(2));

        } catch (IOException e) {
            System.out.println("An error occurred while connecting to the website or retrieving data.");
            e.printStackTrace();
        }
    }

    private static void writeToFile(String filename, String data) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data);
            System.out.println("Product information saved to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}
