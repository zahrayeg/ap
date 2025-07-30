package org.example.demo1.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderAdminDTO {
    private UUID id;
    private String deliveryAddress;
    private String restaurantName;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private String status;
    private String deliveryStatus;
    private Integer payPrice;

    public OrderAdminDTO() {}

    public OrderAdminDTO(UUID id,
                         String deliveryAddress,
                         String restaurantName,
                         List<OrderItemDTO> items,
                         LocalDateTime createdAt,
                         String status,
                         String deliveryStatus,
                         Integer payPrice) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.restaurantName = restaurantName;
        this.items = items;
        this.createdAt = createdAt;
        this.status = status;
        this.deliveryStatus = deliveryStatus;
        this.payPrice = payPrice;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Integer getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(Integer payPrice) {
        this.payPrice = payPrice;
    }
}