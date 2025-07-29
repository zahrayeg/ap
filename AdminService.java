// File: src/main/java/org/example/demo1/service/AdminService.java
package org.example.demo1.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.demo1.model.AdminUserDTO;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminService {
    private static final String BASE_URL = "http://localhost:8080/admin/users";
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * GET /admin/users
     */
    public CompletableFuture<List<AdminUserDTO>> listUsers() {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL))
            .header("Content-Type", "application/json")
            .GET()
            .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(body -> {
                Type listType = new TypeToken<List<AdminUserDTO>>(){}.getType();
                return gson.fromJson(body, listType);
            });
    }

    /**
     * PATCH /admin/users/{id}/status?approved=true
     */
    public CompletableFuture<Void> approveUser(String userId) {
        String url = BASE_URL + "/" + userId + "/status?approved=true";
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
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