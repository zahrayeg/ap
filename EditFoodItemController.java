package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class EditFoodItemController {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080";

    public void sendEditRequest(String token, int restaurantId, int foodId, Map<String, Object> updates, Label statusLabel) {
        String json = gson.toJson(updates);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/item/" + foodId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int code = response.statusCode();
                    String body = response.body();

                    Platform.runLater(() -> {
                        switch (code) {
                            case 200 -> statusLabel.setText("Item updated successfully.");
                            case 409 -> statusLabel.setText("Conflict: Another item with the same name already exists.");
                            case 400 -> statusLabel.setText("Bad Request: Some fields are invalid.");
                            case 401 -> statusLabel.setText("Unauthorized: Invalid token or access denied.");
                            case 404 -> statusLabel.setText("Not Found: Restaurant or item ID is invalid.");
                            default -> statusLabel.setText("Error " + code + ": " + body);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Request failed: " + ex.getMessage()));
                    return null;
                });
    }
}