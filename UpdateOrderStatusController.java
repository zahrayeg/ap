package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import service.RestaurantService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UpdateOrderStatusController {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();
    private final String token = "Bearer your-token";

    public CompletableFuture<List<RestaurantEntry>> loadRestaurants() {
        return service.getSellerRestaurants(token)
                .thenApply(list -> list.stream()
                        .map(m -> new RestaurantEntry(
                                ((Number) m.get("id")).intValue(),
                                m.get("name").toString()))
                        .toList());
    }

    public CompletableFuture<List<OrderEntry>> loadOrdersForRestaurant(int restaurantId) {
        return service.getOrders(token, restaurantId)
                .thenApply(list -> list.stream()
                        .map(m -> new OrderEntry(
                                ((Number) m.get("id")).intValue(),
                                m.get("status").toString()))
                        .toList());
    }

    public CompletableFuture<String> updateStatus(int orderId, String newStatus) {
        return service.patchOrderStatus(token, String.valueOf(orderId), newStatus)
                .thenApply(map -> map.getOrDefault("message", "Status updated").toString())
                .exceptionally(ex -> {
                    try {
                        Type t = new TypeToken<Map<String, Object>>() {}.getType();
                        Map<String, Object> err = gson.fromJson(ex.getMessage(), t);
                        return err.getOrDefault("error", ex.getMessage()).toString();
                    } catch (Exception e) {
                        return "Unexpected error: " + ex.getMessage();
                    }
                });
    }

    public record RestaurantEntry(int id, String name) {
        @Override public String toString() { return name; }
    }

    public record OrderEntry(int id, String status) {
        @Override public String toString() { return "Order #" + id + " [" + status + "]"; }
    }
}