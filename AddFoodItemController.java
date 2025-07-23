package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddFoodItemController {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080";

    public void handleAddFoodItem(
            String token,
            int restaurantId,
            String name,
            String description,
            String priceText,
            String supplyText,
            List<String> keywords,
            String imageBase64,
            Label statusLabel
    ) {
        if (name == null || name.isBlank()) {
            statusLabel.setText("❌ Name is required.");
            return;
        }
        if (description == null || description.isBlank()) {
            statusLabel.setText("❌ Description is required.");
            return;
        }
        int price;
        try {
            price = Integer.parseInt(priceText.trim());
            if (price < 0) throw new NumberFormatException();
        } catch (Exception e) {
            statusLabel.setText("❌ Price must be a non-negative integer.");
            return;
        }
        int supply;
        try {
            supply = Integer.parseInt(supplyText.trim());
            if (supply < 0) throw new NumberFormatException();
        } catch (Exception e) {
            statusLabel.setText("❌ Supply must be a non-negative integer.");
            return;
        }
        if (keywords == null || keywords.isEmpty()) {
            statusLabel.setText("❌ At least one keyword is required.");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name.trim());
        payload.put("description", description.trim());
        payload.put("price", price);
        payload.put("supply", supply);
        payload.put("keywords", keywords);
        if (imageBase64 != null && !imageBase64.isBlank()) {
            payload.put("imageBase64", imageBase64.trim());
        }

        String json = gson.toJson(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/item"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int code = response.statusCode();
                    Platform.runLater(() -> {
                        switch (code) {
                            case 201 -> statusLabel.setText("✅ Item created successfully.");
                            case 409 -> statusLabel.setText("❌ Conflict: Duplicate item name.");
                            case 400 -> statusLabel.setText("❌ Bad Request: Invalid input.");
                            case 401 -> statusLabel.setText("❌ Unauthorized.");
                            case 404 -> statusLabel.setText("❌ Not Found: Invalid restaurant ID.");
                            default -> statusLabel.setText("⚠️ Error " + code + ": " + response.body());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            statusLabel.setText("❌ Request failed: " + ex.getMessage())
                    );
                    return null;
                });
    }
}