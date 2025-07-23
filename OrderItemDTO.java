package dto;

import java.util.UUID;

public class OrderItemDTO {
    private UUID itemId;
    private int quantity;

    // Constructors
    public OrderItemDTO() {}

    public OrderItemDTO(UUID itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}