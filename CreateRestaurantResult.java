package dto;

public class CreateRestaurantResult extends ServiceResult {
    private RestaurantDTO restaurant;

    public CreateRestaurantResult(int status, String message, RestaurantDTO restaurant) {
        super(status, message);
        this.restaurant = restaurant;
    }

    public RestaurantDTO getRestaurant() {
        return restaurant;
    }
}