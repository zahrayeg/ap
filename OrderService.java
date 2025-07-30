package org.example.demo1.service;

import org.example.demo1.controller.LocalDateTimeAdapter;
import org.example.demo1.model.OrderAdminDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OrderService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final String baseUrl = "http://localhost:8080/admin/orders";

    public CompletableFuture<List<OrderAdminDTO>> listOrders(Map<String, String> filters) {
        StringBuilder url = new StringBuilder(baseUrl);
        if (filters != null && !filters.isEmpty()) {
            url.append("?");
            filters.forEach((k, v) -> {
                url.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                        .append("&");
            });
            url.deleteCharAt(url.length() - 1);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseDataArray);
    }

    private List<OrderAdminDTO> parseDataArray(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = root.has("data")
                ? root.getAsJsonArray("data")
                : root.has("content")
                ? root.getAsJsonArray("content")
                : root.getAsJsonArray();
        Type type = new TypeToken<List<OrderAdminDTO>>() {}.getType();
        return gson.fromJson(arr, type);
    }
}