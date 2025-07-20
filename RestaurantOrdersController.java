package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import service.RestaurantService;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RestaurantOrdersController {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();

    // ⚠️ پس از login مقدار token را ست کن
    private String token = "Bearer your-token";

    public void setToken(String t) {
        this.token = t.startsWith("Bearer ") ? t : "Bearer " + t;
    }

    /**
     * بارگذاری سفارش‌ها با امکان فیلتر:
     * @param restaurantId شناسه رستوران (اجباری)
     * @param status       وضعیت سفارش (اختیاری)
     * @param search       متن جستجو (اختیاری)
     * @param user         شناسه یا نام مشتری (اختیاری)
     * @param courier      شناسه پیک (اختیاری)
     */
    public CompletableFuture<List<OrderEntry>> loadOrders(
            int restaurantId,
            String status,
            String search,
            String user,
            String courier)
    {
        // ساخت map فیلترهای کوئری
        var filters = new LinkedHashMap<String, String>();
        if (status    != null && !status.isBlank())   filters.put("status", status);
        if (search    != null && !search.isBlank())   filters.put("search", search);
        if (user      != null && !user.isBlank())     filters.put("user", user);
        if (courier   != null && !courier.isBlank())  filters.put("courier", courier);

        // فراخوانی متد سرویس
        return service.getFilteredOrders(token, restaurantId, filters)
                .thenApply(list -> list.stream().map(m -> {
                    List<?> items = (List<?>) m.getOrDefault("item_ids", List.of());
                    String itemIds = items.stream().map(Object::toString).reduce((a,b)->a+", "+b).orElse("");
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
                }).toList());
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