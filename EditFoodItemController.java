package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Label;
import model.FoodDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EditFoodItemController {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080";

    public void handleUpdateFoodItem(
            String token,
            int restaurantId,
            int foodId,
            String name,
            String description,
            String priceText,
            Label statusLabel
    ) {
        // ۱. اعتبارسنجی ورودی‌ها
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
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Price must be a non-negative integer.");
            return;
        }

        // ۲. ساخت DTO کامل
        FoodDTO dto = new FoodDTO(name.trim(), description.trim(), price);
        String json = gson.toJson(dto);

        // ۳. آماده‌سازی و ارسال درخواست PUT
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL +
                        "/restaurants/" + restaurantId +
                        "/item/" + foodId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int code = response.statusCode();
                    String body = response.body();
                    Platform.runLater(() -> {
                        switch (code) {
                            case 200:
                                statusLabel.setText("✅ Item updated successfully.");
                                break;
                            case 400:
                                statusLabel.setText("❌ Bad Request: Invalid input.");
                                break;
                            case 401:
                                statusLabel.setText("❌ Unauthorized.");
                                break;
                            case 404:
                                statusLabel.setText("❌ Not Found: Invalid restaurant or item ID.");
                                break;
                            case 409:
                                statusLabel.setText("❌ Conflict: Duplicate item name.");
                                break;
                            default:
                                statusLabel.setText("⚠️ Error " + code + ": " + body);
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