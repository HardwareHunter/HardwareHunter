package backend.main.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

class GameSpecs {
    private String gameName; //New field for game name
    private String os;
    private String processor;
    private String memory;
    private String graphics;
    private String directX;
    private String storage;

    //Getters and Setters
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    public String getDirectX() {
        return directX;
    }

    public void setDirectX(String directX) {
        this.directX = directX;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }


    public void displaySpecs() {
        System.out.println("Game Name: " + gameName);
        System.out.println("Operating System: " + os);
        System.out.println("Processor: " + processor);
        System.out.println("Memory: " + memory);
        System.out.println("Graphics: " + graphics);
        System.out.println("DirectX: " + directX);
        System.out.println("Storage: " + storage);
    }
}

public class SteamGameSpecs {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the Steam game URL: ");
        String gameUrl = scanner.nextLine();

        try {
            String appId = extractAppId(gameUrl);
            System.out.println("Extracted App ID: " + appId);

            String apiUrl = "https://store.steampowered.com/api/appdetails?appids=" + appId;
            System.out.println("API URL: " + apiUrl);

            String jsonResponse = sendHttpRequest(apiUrl);
            JSONObject json = new JSONObject(jsonResponse);

            if (json.has(appId) && json.getJSONObject(appId).getBoolean("success")) {
                JSONObject data = json.getJSONObject(appId).getJSONObject("data");


                GameSpecs gameSpecs = new GameSpecs();
                gameSpecs.setGameName(data.getString("name"));


                if (data.has("pc_requirements")) {
                    JSONObject pcRequirements = data.getJSONObject("pc_requirements");


                    if (pcRequirements.has("minimum")) {
                        String minimumSpecsString = pcRequirements.getString("minimum");
                        GameSpecs minimumSpecs = parseSpecs(minimumSpecsString, "Minimum");
                        minimumSpecs.setGameName(gameSpecs.getGameName());


                        System.out.println("\nMinimum Specs:");
                        minimumSpecs.displaySpecs();
                    } else {
                        System.out.println("Minimum specs information not available.");
                    }


                    if (pcRequirements.has("recommended")) {
                        String recommendedSpecsString = pcRequirements.getString("recommended");
                        GameSpecs recommendedSpecs = parseSpecs(recommendedSpecsString, "Recommended");
                        recommendedSpecs.setGameName(gameSpecs.getGameName());


                        System.out.println("\nRecommended Specs:");
                        recommendedSpecs.displaySpecs();
                    } else {
                        System.out.println("Recommended specs information not available.");
                    }
                } else {
                    System.out.println("PC requirements information not available.");
                }
            } else {
                System.out.println("Failed to retrieve game information.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static String extractAppId(String gameUrl) {
        String[] parts = gameUrl.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        throw new IllegalArgumentException("Invalid Steam URL: No app ID found");
    }

    private static String sendHttpRequest(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

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

    private static GameSpecs parseSpecs(String specsString, String type) {
        GameSpecs specs = new GameSpecs();


        String[] lines = specsString.split("<br>");
        for (String line : lines) {
            line = line.replaceAll("<[^>]+>", "").trim();
            if (line.contains("OS:")) {
                specs.setOs(line.replace("OS:", "").trim());
            } else if (line.contains("Processor:")) {
                specs.setProcessor(line.replace("Processor:", "").trim());
            } else if (line.contains("Memory:")) {
                specs.setMemory(line.replace("Memory:", "").trim());
            } else if (line.contains("Graphics:")) {
                specs.setGraphics(line.replace("Graphics:", "").trim());
            } else if (line.contains("DirectX:")) {
                specs.setDirectX(line.replace("DirectX:", "").trim());
            } else if (line.contains("Storage:")) {
                specs.setStorage(line.replace("Storage:", "").trim());
            }
        }

        return specs;
    }
}