package controller;

import com.google.gson.Gson;
import service.RestaurantService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RestaurantOrdersController {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();
    private String token = "Bearer your-token";

    public void setToken(String t) {
        this.token = t.startsWith("Bearer ") ? t : "Bearer " + t;
    }

    /**
     * بارگذاری سفارش‌ها و فیلتر کردن در خودِ کنترلر
     */
    public CompletableFuture<List<OrderEntry>> loadOrders(
            int restaurantId,
            String status,
            String search,
            String user,
            String courier
    ) {
        return service.getOrders(token, restaurantId)
                .thenApply(list -> list.stream()
                        // فیلتر وضعیت
                        .filter(m -> status == null
                                || status.isBlank()
                                || status.equals(m.get("status").toString()))
                        // جستجو در آدرس تحویل
                        .filter(m -> search == null
                                || search.isBlank()
                                || m.get("delivery_address").toString()
                                .toLowerCase()
                                .contains(search.toLowerCase()))
                        // فیلتر مشتری
                        .filter(m -> user == null
                                || user.isBlank()
                                || m.get("customer_id").toString().equals(user)
                                || m.get("customer_name").toString().toLowerCase()
                                .contains(user.toLowerCase()))
                        // فیلتر پیک
                        .filter(m -> courier == null
                                || courier.isBlank()
                                || m.get("courier_id").toString().equals(courier))
                        // نگاشت به OrderEntry
                        .map(m -> {
                            @SuppressWarnings("unchecked")
                            List<?> items = (List<?>) m.getOrDefault("item_ids", List.of());
                            String itemIds = items.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", "));
                            return new OrderEntry(
                                    ((Number) m.get("id")).intValue(),
                                    m.get("delivery_address").toString(),
                                    ((Number) m.get("customer_id")).intValue(),
                                    ((Number) m.get("vendor_id")).intValue(),
                                    ((Number) m.getOrDefault("coupon_id", 0)).intValue(),
                                    itemIds,
                                    ((Number) m.getOrDefault("raw_price", 0)).doubleValue(),
                                    ((Number) m.getOrDefault("tax_fee", 0)).doubleValue(),
                                    ((Number) m.getOrDefault("additional_fee", 0)).doubleValue(),
                                    ((Number) m.getOrDefault("courier_fee", 0)).doubleValue(),
                                    ((Number) m.getOrDefault("pay_price", 0)).doubleValue(),
                                    ((Number) m.getOrDefault("courier_id", 0)).intValue(),
                                    m.get("status").toString(),
                                    m.get("created_at").toString(),
                                    m.get("updated_at").toString()
                            );
                        })
                        .collect(Collectors.toList())
                );
    }

    public record OrderEntry(
            int id,
            String deliveryAddress,
            int customerId,
            int vendorId,
            int couponId,
            String itemIds,
            double rawPrice,
            double taxFee,
            double additionalFee,
            double courierFee,
            double payPrice,
            int courierId,
            String status,
            String createdAt,
            String updatedAt
    ) {}
}