package dto;

import java.util.List;
import java.util.UUID;

public class RestaurantOrderDTO {
    private UUID id;
    private String deliveryAddress;
    private UUID customerId;
    private String customerName;
    private UUID courierId;
    private String courierName;
    private UUID vendorId;
    private List<RestaurantOrderItemDTO> items;
    private int totalPrice;
    private String status;
    private String createdAt;

    public RestaurantOrderDTO(UUID id,
                              String deliveryAddress,
                              UUID customerId,
                              String customerName,
                              UUID courierId,
                              String courierName,
                              UUID vendorId,
                              List<RestaurantOrderItemDTO> items,
                              int totalPrice,
                              String status,
                              String createdAt) {
        this.id               = id;
        this.deliveryAddress  = deliveryAddress;
        this.customerId       = customerId;
        this.customerName     = customerName;
        this.courierId        = courierId;
        this.courierName      = courierName;
        this.vendorId         = vendorId;
        this.items            = items;
        this.totalPrice       = totalPrice;
        this.status           = status;
        this.createdAt        = createdAt;
    }

    // getters omitted for brevity
}