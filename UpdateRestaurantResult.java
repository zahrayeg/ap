package dto;

public class UpdateRestaurantResult extends ServiceResult {
    private RestaurantDTO restaurant;

    public UpdateRestaurantResult(int status, String message, RestaurantDTO restaurant) {
        super(status, message);
        this.restaurant = restaurant;
    }

    public RestaurantDTO getRestaurant() {
        return restaurant;
    }
}