package dto;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

public class OrderAdminDTO {
    private UUID id;
    private String deliveryAddress;

    // اضافه‌شده برای نمایش رستوران
    private String restaurantName;

    // اضافه‌شده برای نمایش آیتم‌ها
    private List<OrderItemDTO> items;

    // اضافه‌شده برای نمایش وضعیت ارسال
    private String deliveryStatus;

    private UUID customerId;
    private UUID vendorId;
    private List<UUID> itemIds;
    private int rawPrice;
    private int taxFee;
    private int courierFee;
    private int payPrice;
    private UUID courierId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public List<UUID> getItemIds() { return itemIds; }
    public void setItemIds(List<UUID> itemIds) { this.itemIds = itemIds; }
    public int getRawPrice() { return rawPrice; }
    public void setRawPrice(int rawPrice) { this.rawPrice = rawPrice; }
    public int getTaxFee() { return taxFee; }
    public void setTaxFee(int taxFee) { this.taxFee = taxFee; }
    public int getCourierFee() { return courierFee; }


    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() { return id; }
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setCourierFee(int courierFee) {
        this.courierFee = courierFee;
    }
    public int getPayPrice() { return payPrice; }
    public void setPayPrice(int payPrice) { this.payPrice = payPrice; }
    public UUID getCourierId() { return courierId; }

    public void setCourierId(UUID courierId) {
        this.courierId = courierId;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}