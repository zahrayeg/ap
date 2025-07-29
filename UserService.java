package org.example.demo1.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.demo1.model.StatusResult;
import org.example.demo1.model.UserDTO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private static final String BASE_URL = "http://localhost:8080";
    private final Gson gson = new Gson();
    private String token;

    public CompletableFuture<Map<String, Object>> signUp(UserDTO user) {
        HttpClient client = HttpClient.newHttpClient();
        String json = new Gson().toJson(user);
        System.out.println("JSON sent: " + json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType()));

    }

    public CompletableFuture<Map<String, Object>> login(String phone, String password) {
        if (phone == null || phone.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Phone and password are required"
            ));
        }

        HttpClient client = HttpClient.newHttpClient();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", phone);
        requestBody.put("password", password);
        String json = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());

                    if (response.statusCode() == 200) {
                        // استخراج مقادیر از LoginResult
                        Map<String, Object> loginResult = new HashMap<>();
                        loginResult.put("status", response.statusCode());
                        loginResult.put("message", result.getOrDefault("message", "Login successful"));
                        loginResult.put("token", result.getOrDefault("token", ""));
                        loginResult.put("user", result.getOrDefault("user", new HashMap<>()));
                        token = "Bearer " + (String) loginResult.get("token"); // ذخیره توکن
                        return loginResult;
                    } else {
                        return Map.of(
                                "status", response.statusCode(),
                                "message", result.getOrDefault("message", "incorrect password or phone number")
                        );
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Login error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "message", "Internal server error: " + throwable.getMessage()
                    );
                });
    }
    public CompletableFuture<Map<String, Object>> getProfile(String token) {
        System.out.println("Sending token to /auth/profile: " + token); // لاگ توکن
        HttpClient client = HttpClient.newHttpClient();
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/profile"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /auth/profile: " + body); // لاگ پاسخ خام
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());

                    // بررسی کد وضعیت HTTP
                    if (response.statusCode() == 200) {
                        // پاسخ از نوع ProfileResult است، شامل user
                        Map<String, Object> profileResult = new HashMap<>();
                        profileResult.put("status", 200); // اضافه کردن status به صورت دستی
                        profileResult.put("user", result); // کل پاسخ به عنوان user
                        return profileResult;
                    } else {
                        // پاسخ از نوع ServiceResult است
                        return Map.of(
                                "status", response.statusCode(),
                                "message", result.getOrDefault("message", "Profile fetch failed")
                        );
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Profile fetch error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "message", "Internal server error: " + throwable.getMessage()
                    );
                });
    }
    public CompletableFuture<Map<String, Object>> updateProfile(String token, Map<String, Object> profileData) {
        HttpClient client = HttpClient.newHttpClient();
        String json = new Gson().toJson(profileData);
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/profile"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    if (response.statusCode() == 200) {
                        // پاسخ از نوع ProfileResult است
                        Map<String, Object> profileResult = new HashMap<>();
                        profileResult.put("status", 200);
                        profileResult.put("user", result); // کل پاسخ به عنوان user
                        return profileResult;
                    } else {
                        // پاسخ از نوع ServiceResult است
                        return Map.of(
                                "status", response.statusCode(),
                                "message", result.getOrDefault("message", "Profile update failed")
                        );
                    }
                })
                .exceptionally(throwable -> Map.of(
                        "status", 500,
                        "message", "Internal server error: " + throwable.getMessage()
                ));
    }
    public CompletableFuture<Map<String, Object>> logOut(String token) {
        HttpClient client = HttpClient.newHttpClient();
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/logout"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Raw response from /auth/logout: " + response.body());
                    Map<String, Object> result = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){}.getType());
                    result.put("status", response.statusCode());
                    return result;
                })
                .exceptionally(throwable -> {
                    System.err.println("Logout error: " + throwable.getMessage());
                    return Map.of("status", 500, "message", "خطا در ارتباط با سرور: " + throwable.getMessage());
                });

    }

    public CompletableFuture<StatusResult> getStatus() {
        if (token == null || token.isBlank()) {
            return CompletableFuture.completedFuture(
                    new StatusResult(401, "Unauthorized: Missing token", false)
            );
        }

        HttpClient client = HttpClient.newHttpClient();
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/status"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    StatusResult result = gson.fromJson(body, StatusResult.class);
                    result.setStatus(response.statusCode());
                    return result;
                })
                .exceptionally(ex -> new StatusResult(500, "Internal server error: " + ex.getMessage(), false));
    }


    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}