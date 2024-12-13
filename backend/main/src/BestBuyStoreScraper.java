package backend.main.src;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Store {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;

    public Store(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }
}
public class BestBuyStoreScraper {


    public static final String STORE_CSV_FILE = "Updated_BBY_locations_with_coordinates.csv";
    private static final double MAX_DISTANCE = 10.0; //Maximum distance away
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter?data=";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please input your zip code: ");
        String zipCode = scanner.nextLine().trim();


        double[] userCoordinates = getCoordinatesFromZip(zipCode);

        if (userCoordinates[0] == 0.0 && userCoordinates[1] == 0.0) {
            System.out.println("Could not retrieve coordinates for the provided ZIP code.");
            return;
        }

        System.out.print("Enter the item to search for: ");
        String searchQuery = scanner.nextLine().trim();

        System.out.println("Loading store data from CSV...");
        List<Store> nearbyStores = loadStoresFromCSV(STORE_CSV_FILE, userCoordinates);

        JSONObject allStoresData = new JSONObject();
        int storeCount = 0;

        for (Store store : nearbyStores) {
            if (storeCount >= 20) {
                System.out.println("Reached maximum of 20 stores to scrape.");
                break;
            }

            System.out.println("Scraping data for Store ID: " + store.getId() + " (" + store.getName() + ")");
            JSONArray productsArray = scrapeProductsForStore(store.getId(), searchQuery);

            if (productsArray.length() > 0) {
                JSONObject storeJson = new JSONObject();
                storeJson.put("storeId", store.getId());
                storeJson.put("storeName", store.getName());
                storeJson.put("latitude", store.getLatitude());
                storeJson.put("longitude", store.getLongitude());
                storeJson.put("products", productsArray);

                allStoresData.put(store.getId(), storeJson);
                storeCount++;
                System.out.println("Successfully scraped " + productsArray.length() + " products from " + store.getName());
            } else {
                System.out.println("No products found at " + store.getName());
            }
        }

        writeToFile("products_by_store.json", allStoresData.toString(2));
        System.out.println("Scraping complete. Results saved to products_by_store.json");
    }

    public static double[] getCoordinatesFromZip(String location) {
        double[] coordinates = new double[2];
        try {
            String encodedLocation = java.net.URLEncoder.encode(location, "UTF-8");
            String url = NOMINATIM_URL + encodedLocation + "&countrycodes=US";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray results = new JSONArray(response.toString());
                if (!results.isEmpty()) {
                    String lat = results.getJSONObject(0).getString("lat");
                    String lon = results.getJSONObject(0).getString("lon");
                    coordinates[0] = Double.parseDouble(lat);
                    coordinates[1] = Double.parseDouble(lon);
                } else {
                    System.out.println("No location found for: " + location);
                }
            } else {
                System.out.println("HTTP request failed. Response Code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            System.out.println("Error in getCoordinatesFromZip: " + e.getMessage());
        }
        return coordinates;
    }


    public static List<Store> loadStoresFromCSV(String filePath, double[] userCoordinates) {
        List<Store> stores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 1) {
                    String storeId = values[0].replace("\"", "").trim();
                    String storeName = values[1].replace("\"", "").trim();

                    double latitude = parseCoordinate(values[values.length - 2].replace("\"", "").trim());
                    double longitude = parseCoordinate(values[values.length - 1].replace("\"", "").trim());

                    if (latitude != 0.0 && longitude != 0.0) {
                        double distance = calculateDistance(userCoordinates[0], userCoordinates[1], latitude, longitude);
                        if (distance <= MAX_DISTANCE) {
                            stores.add(new Store(storeId, storeName, latitude, longitude));
                        }
                    }
                }
            }
            System.out.println("Loaded " + stores.size() + " nearby stores from CSV.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stores;
    }

    private static double parseCoordinate(String coordinate) {
        try {
            return Double.parseDouble(coordinate.replace("\"", "").trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinate value: " + coordinate);
            return 0.0;
        }
    }
    //Uses Haversine formula to calculate the distance

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static JSONArray scrapeProductsForStore(String storeId, String searchQuery) {
        JSONArray productsArray = new JSONArray();
        try {
            String url = "https://www.bestbuy.com/site/searchpage.jsp?id=pcat17071&st=" +
                    searchQuery.replace(" ", "+").toLowerCase() +
                    "&qp=storepickupstores_facet%3DStore+Availability+-+In+Store+Pickup~" + storeId;

            System.out.println("Connecting to URL: " + url);
            Document document = Jsoup.connect(url).get();
            Elements products = document.select(".sku-item");

            for (Element product : products) {
                String name = product.select(".sku-title").text();
                String priceText = product.select(".priceView-customer-price span").text();
                String price = priceText.replaceAll("Your price for this item is ", "").trim();

                if (price.contains(" ")) {
                    price = price.split(" ")[0];
                }

                JSONObject productJson = new JSONObject();
                productJson.put("name", name);
                productJson.put("currentPrice", price);
                productsArray.put(productJson);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while connecting to the website or retrieving data for Store ID: " + storeId);
            e.printStackTrace();
        }
        return productsArray;
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

    public static JSONObject scrapeStores(List<Store> nearbyStores, String searchQuery) {
        JSONObject allStoresData = new JSONObject();
        int storeCount = 0;
        for (Store store : nearbyStores) {
            if (storeCount >= 20) {
                break;
            }
            JSONArray productsArray = scrapeProductsForStore(store.getId(), searchQuery);
            if (productsArray.length() > 0) {
                JSONObject storeJson = new JSONObject();
                storeJson.put("storeId", store.getId());
                storeJson.put("storeName", store.getName());
                storeJson.put("latitude", store.getLatitude());
                storeJson.put("longitude", store.getLongitude());
                storeJson.put("products", productsArray);
                allStoresData.put(store.getId(), storeJson);
                storeCount++;
            }
        }
        return allStoresData;
    }



}


