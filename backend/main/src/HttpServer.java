package backend.main.src;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8000), 0);
        server.createContext("/fetch-specs", new GameSpecsHandler());
        server.createContext("/search-stores", new StoreSearchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }

    static class GameSpecsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                InputStream inputStream = exchange.getRequestBody();
                String gameUrl = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                System.out.println("Received game URL: " + gameUrl);

                JSONObject responseJson = getGameSpecs(gameUrl);


                if (responseJson.isEmpty()) {
                    System.out.println("No data returned from getGameSpecs");
                }


                String response = responseJson.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private JSONObject getGameSpecs(String gameUrl) {
            JSONObject specsJson = new JSONObject();
            try {
                String appId = extractAppId(gameUrl);
                String apiUrl = "https://store.steampowered.com/api/appdetails?appids=" + appId;

                String jsonResponse = sendHttpRequest(apiUrl);
                JSONObject json = new JSONObject(jsonResponse);

                if (json.has(appId) && json.getJSONObject(appId).getBoolean("success")) {
                    JSONObject data = json.getJSONObject(appId).getJSONObject("data");


                    specsJson.put("name", data.getString("name"));
                    JSONObject pcRequirements = data.getJSONObject("pc_requirements");

                    if (pcRequirements.has("minimum")) {
                        specsJson.put("minimum", pcRequirements.getString("minimum"));
                    }
                    if (pcRequirements.has("recommended")) {
                        specsJson.put("recommended", pcRequirements.getString("recommended"));
                    }
                    if (data.has("header_image")) {
                        specsJson.put("cover", data.getString("header_image"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return specsJson;
        }

        private String extractAppId(String gameUrl) {
            String[] parts = gameUrl.split("/");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    return part;
                }
            }
            throw new IllegalArgumentException("Invalid Steam URL: No app ID found");
        }

        private String sendHttpRequest(String apiUrl) throws Exception {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();
            } else {
                throw new Exception("HTTP request failed. Response Code: " + responseCode);
            }
        }
    }
    static class StoreSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                JSONObject requestData = new JSONObject(body);

                String address = requestData.getString("zipCode");
                String searchQuery = requestData.getString("searchQuery");

                double[] userCoordinates = BestBuyStoreScraper.getCoordinatesFromZip(address);
                List<Store> nearbyStores = BestBuyStoreScraper.loadStoresFromCSV(BestBuyStoreScraper.STORE_CSV_FILE, userCoordinates);
                JSONObject allStoresData = BestBuyStoreScraper.scrapeStores(nearbyStores, searchQuery);

                String response = allStoresData.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }


}