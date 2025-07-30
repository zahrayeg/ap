package dto;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class OrderItemDTO {
    @SerializedName("foodId")
    private UUID foodId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("restaurantId")
    private UUID restaurantId;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private Integer price;

    //============= Constructors =============

    public OrderItemDTO() {}

    public OrderItemDTO(UUID foodId, Integer quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public OrderItemDTO(UUID foodId, Integer quantity, UUID restaurantId) {
        this(foodId, quantity);
        this.restaurantId = restaurantId;
    }

    public OrderItemDTO(UUID foodId,
                        Integer quantity,
                        UUID restaurantId,
                        String name,
                        Integer price) {
        this(foodId, quantity, restaurantId);
        this.name  = name;
        this.price = price;
    }

    //============= Getters & Setters =============

    public UUID getFoodId() {
        return foodId;
    }
    public void setFoodId(UUID foodId) {
        this.foodId = foodId;
    }

    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }
    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }

    //============= toString =============

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "foodId="       + foodId +
                ", quantity="    + quantity +
                ", restaurantId=" + restaurantId +
                ", name='"       + name + '\'' +
                ", price="       + price +
                '}';
    }

    public void setFoodName(String name) {
        this.name = name;
    }
}