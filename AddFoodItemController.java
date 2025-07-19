package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class AddFoodItemController {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080";

    public void sendFoodItem(String token, int restaurantId, String name, String imageBase64, String description,
                             double price, int supply, List<String> keywords, Label statusLabel) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("imageBase64", imageBase64);
        payload.put("description", description);
        payload.put("price", price);
        payload.put("supply", supply);
        payload.put("keywords", keywords);

        String json = gson.toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/item"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
              .thenAccept(response -> {
                  if (response.statusCode() == 409) {
                      Platform.runLater(() -> statusLabel.setText("Food item with same name already exists."));
                      return;
                  }
                  if (response.statusCode() == 200) {
                      Platform.runLater(() -> statusLabel.setText(" Item created"));
                  } else {
                      Platform.runLater(() -> statusLabel.setText(" Error: " + response.statusCode() + " - " + response.body()));
                  }
              })
              .exceptionally(ex -> {
                  Platform.runLater(() -> statusLabel.setText(" Failed: " + ex.getMessage()));
                  return null;
              });
    }
}