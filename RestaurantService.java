package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import model.RestaurantDTO;

import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RestaurantService {
    private static final String BASE_URL = "http://localhost:8080";
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    //  ساخت رستورا
    public CompletableFuture<Map<String, Object>> createRestaurant(RestaurantDTO restaurant, String token) {
        String json = gson.toJson(restaurant);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parseJsonResponse(response));
    }
    public CompletableFuture<Map<String, Object>> getRestaurantById(int id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + id))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    Map<String, Object> result = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){}.getType());
                    result.put("status", response.statusCode());
                    return result;
                });
    }
    //  دریافت لیست رستوران‌های فروشنده


        public CompletableFuture<List<Map<String, Object>>> getSellerRestaurants(String token) {
        /*  کد واقعی برای اتصال به سرور
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/restaurants/mine"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .GET()
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> gson.fromJson(response.body(), new TypeToken<List<Map<String, Object>>>() {}.getType()));
        */

            //  داده تستی برای اجرا در حالت آفلاین
            List<Map<String, Object>> sampleData = List.of(
                    Map.of("id", "1", "name", "رستوران تستی", "address", "تهران", "phone", "09121234567", "tax_fee", "5", "additional_fee", "3000"),
                    Map.of("id", "2", "name", "فست‌فود شبانه", "address", "اصفهان", "phone", "09361234567", "tax_fee", "4", "additional_fee", "2500")
            );

            return CompletableFuture.completedFuture(sampleData);
        }













    //  بروزرسانی اطلاعات رستوران
    public CompletableFuture<Map<String, Object>> updateRestaurant(int restaurantId, RestaurantDTO updatedData, String token) {
        String json = gson.toJson(updatedData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parseJsonResponse(response));
    }

    //  متد کمکی برای تبدیل پاسخ به Map و افزودن statusCode
    private Map<String, Object> parseJsonResponse(HttpResponse<String> response) {
        Map<String, Object> result = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){}.getType());
        result.put("status", response.statusCode());
        return result;
    }

    // 🥘 افزودن آیتم غذا به رستوران
    public CompletableFuture<Map<String, Object>> addFoodItem(int restaurantId, Map<String, Object> itemData, String token) {
        String json = gson.toJson(itemData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/item"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }
    public CompletableFuture<List<Map<String, Object>>> getRestaurantMenu(String token, int restaurantId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/menu"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> gson.fromJson(response.body(), new TypeToken<List<Map<String, Object>>>() {}.getType()));
    }
    // ️ ویرایش آیتم غذا
    public CompletableFuture<Map<String, Object>> editFoodItem(int itemId, Map<String, Object> newItemData, String token) {
        String json = gson.toJson(newItemData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + itemId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }

    // ️ حذف آیتم غذا
    public CompletableFuture<Map<String, Object>> deleteFoodItem(int itemId, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + itemId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }

    // ️ افزودن منو برای رستوران
    public CompletableFuture<Map<String, Object>> addMenu(int restaurantId, String title, String token) {
        Map<String, Object> body = Map.of("title", title);
        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/" + restaurantId + "/menu"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }

    // ️ حذف منو از رستوران
    public CompletableFuture<Map<String, Object>> deleteMenu(int restaurantId, String title, String token) {
        String uri = BASE_URL + "/restaurants/" + restaurantId + "/menu/" + title;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }

    // افزودن آیتم به منوی خاص
    public CompletableFuture<Map<String, Object>> addItemToMenu(int restaurantId, String title, int itemId, String token) {
        Map<String, Object> body = Map.of("item_id", itemId);
        String json = gson.toJson(body);
        String uri = BASE_URL + "/restaurants/" + restaurantId + "/menu/" + title;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }

    // ️ حذف آیتم از منوی خاص
    public CompletableFuture<Map<String, Object>> removeItemFromMenu(int restaurantId, String title, int itemId, String token) {
        String uri = BASE_URL + "/restaurants/" + restaurantId + "/menu/" + title + "/" + itemId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }
    //  دریافت لیست سفارش‌های یک رستوران
    public CompletableFuture<List<Map<String, Object>>> getRestaurantOrders(int restaurantId, Map<String, String> filters, String token) {
        StringBuilder query = new StringBuilder("?");
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                query.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String uri = BASE_URL + "/restaurants/" + restaurantId + "/orders" + (query.length() > 1 ? query.toString() : "");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> gson.fromJson(response.body(), new TypeToken<List<Map<String, Object>>>(){}.getType()));
    }

    //  تغییر وضعیت سفارش خاص
    public CompletableFuture<Map<String, Object>> updateOrderStatus(int orderId, String status, String token) {
        Map<String, String> body = Map.of("status", status);
        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/restaurants/orders/" + orderId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResponse);
    }
}