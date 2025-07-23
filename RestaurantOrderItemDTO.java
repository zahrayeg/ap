package dto;

import java.util.UUID;

public class RestaurantOrderItemDTO {
    private UUID itemId;
    private int quantity;

    public RestaurantOrderItemDTO(UUID itemId, int quantity) {
        this.itemId  = itemId;
        this.quantity = quantity;
    }

    public UUID getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
}