package dto;

public class RestaurantResult extends ServiceResult {
    private String restaurantId;

    public RestaurantResult(int status, String message, String restaurantId) {
        super(status, message);
        this.restaurantId = restaurantId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}