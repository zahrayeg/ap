package httpRequestHandler;

import Entity.Food;
import Entity.Restaurant;
import dto.FoodDTO;
import service.FoodService;
import service.RestaurantService;

import java.util.UUID;

public class FoodHandler {
    private final FoodService foodService = new FoodService();
    private final RestaurantService restaurantService = new RestaurantService();

    public boolean registerFood(UUID restaurantId, FoodDTO dto, int quantity) {
        Restaurant rest = restaurantService.getRestaurantById(restaurantId);
        if (rest == null) return false;

        return foodService.createFood(dto.getName(), dto.getPrice(), quantity, dto.getCategory(), rest);
    }

    public boolean updateFood(UUID foodId, FoodDTO dto, int quantity) {
        return foodService.updateFood(foodId, dto.getName(), dto.getPrice(), quantity, dto.getCategory());
    }

    public boolean removeFood(UUID foodId) {
        return foodService.deleteFood(foodId);
    }

    public FoodDTO getFoodDetails(UUID foodId) {
        Food food = foodService.getFoodById(foodId);
        if (food == null) return null;

        return new FoodDTO(food.getName(), food.getPrice(), food.getCategory());
    }
}