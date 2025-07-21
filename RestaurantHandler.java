package httpRequestHandler;

import Entity.Restaurant;
import dto.RestaurantDTO;
import service.RestaurantService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RestaurantHandler {
    private final RestaurantService restaurantService = new RestaurantService();

    public boolean registerRestaurant(RestaurantDTO dto) {
        return restaurantService.createRestaurant(dto.getName(), dto.getAddress());
    }

    public RestaurantDTO getRestaurantDetails(UUID restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null) return null;

        return new RestaurantDTO(restaurant.getName(), restaurant.getAddress());
    }

    public boolean updateRestaurant(UUID restaurantId, RestaurantDTO dto) {
        return restaurantService.updateRestaurant(restaurantId, dto.getName(), dto.getAddress());
    }

    public boolean deleteRestaurant(UUID restaurantId) {
        return restaurantService.deleteRestaurant(restaurantId);
    }

    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantService.getAllRestaurants()
                .stream()
                .map(r -> new RestaurantDTO(r.getName(), r.getAddress()))
                .collect(Collectors.toList());
    }
}