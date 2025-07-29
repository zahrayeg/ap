package org.example.demo1.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.demo1.model.AdminUserDTO;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.google.gson.GsonBuilder;
import org.example.demo1.controller.LocalDateTimeAdapter;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import com.google.gson.*;


public class AdminService {
    private static final String BASE_URL = "http://localhost:8080/admin/users";

    private final Gson gson;
    private final HttpClient client;

    public AdminService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.client = HttpClient.newHttpClient();
    }

    /**
     * GET /admin/users
     * Expects JSON: { "data": [ … ], "success": 200 }
     */
    public CompletableFuture<List<AdminUserDTO>> listUsers() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseListUsersResponse);
    }

    private List<AdminUserDTO> parseListUsersResponse(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();

        // بررسی کد وضعیت برگشتی در فیلد success
        int successCode = root.has("success")
                ? root.get("success").getAsInt()
                : 0;
        if (successCode < 200 || successCode >= 300) {
            throw new RuntimeException("Failed to list users: success=" + successCode);
        }

        JsonElement dataElem = root.get("data");
        if (dataElem == null || !dataElem.isJsonArray()) {
            throw new RuntimeException("Unexpected response format: missing 'data' array");
        }

        Type listType = new TypeToken<List<AdminUserDTO>>() {}.getType();
        return gson.fromJson(dataElem, listType);
    }

    /**
     * PATCH /admin/users/{id}/status?approved=true
     */
    public class StatusDTO {
        private boolean approved;
        public StatusDTO(boolean approved) { this.approved = approved; }
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
    }

    public CompletableFuture<Void> approveUser(String userId) {
        String url = BASE_URL + "/" + userId + "/status?approved=true";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                .thenAccept(resp -> {
                    if (resp.statusCode() / 100 != 2) {
                        throw new RuntimeException("Approve failed: " + resp.statusCode());
                    }
                });
    }

    /**
     * DELETE /admin/users/{id}
     */
    public CompletableFuture<Void> deleteUser(String userId) {
        String url = BASE_URL + "/" + userId;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                .thenAccept(resp -> {
                    if (resp.statusCode() / 100 != 2) {
                        throw new RuntimeException("Delete failed: " + resp.statusCode());
                    }
                });
    }
}