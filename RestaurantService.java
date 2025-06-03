package service;

import DAO.RestaurantDAO;
import DAO.FoodDAO;
import dto.RestaurantDTO;
import dto.FoodDTO;
import Entity.Restaurant;
import Entity.Food;

import java.util.List;
import java.util.stream.Collectors;

public class RestaurantService {

    private RestaurantDAO restaurantDAO;
    private FoodDAO foodDAO;

    public RestaurantService() {
        this.restaurantDAO = new RestaurantDAO();
        this.foodDAO = new FoodDAO();
    }

    public void createRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantDTO.getName());
        restaurant.setAddress(restaurantDTO.getAddress());
        restaurant.setPhone(restaurantDTO.getPhone());

        restaurantDAO.save(restaurant);
    }

    // دریافت لیست رستوران‌های فروشنده (مطابق GET /restaurants/mine)
    public List<RestaurantDTO> getRestaurantsBySeller(int sellerId) {
        return restaurantDAO.findBySellerId(sellerId).stream()
                .map(r -> new RestaurantDTO(r.getName(), r.getAddress(), r.getPhone()))
                .collect(Collectors.toList());
    }

    // دریافت جزئیات یک رستوران (مطابق GET /restaurants/{id})
    public RestaurantDTO getRestaurantById(int id) {
        Restaurant restaurant = restaurantDAO.findById(id);
        if (restaurant == null) return null;
        return new RestaurantDTO(restaurant.getName(), restaurant.getAddress(), restaurant.getPhone());
    }

    // ویرایش اطلاعات یک رستوران (مطابق PUT /restaurants/{id})
    public void updateRestaurant(int id, RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantDAO.findById(id);
        if (restaurant != null) {
            restaurant.setName(restaurantDTO.getName());
            restaurant.setAddress(restaurantDTO.getAddress());
            restaurant.setPhone(restaurantDTO.getPhone());

            restaurantDAO.update(restaurant);
        }
    }

    // اضافه کردن آیتم غذا به رستوران (مطابق POST /restaurants/{id}/item)
    public void addFoodToRestaurant(int restaurantId, FoodDTO foodDTO) {
        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant != null) {
            Food food = new Food();
            food.setName(foodDTO.getName());
            food.setPrice(foodDTO.getPrice());
            food.setCategory(foodDTO.getCategory());
            food.setRestaurant(restaurant);

            foodDAO.save(food);
        }
    }

    // ویرایش آیتم غذا در رستوران (مطابق PUT /restaurants/{id}/item/{item_id})
    public void updateFoodItem(int restaurantId, int foodId, FoodDTO updatedFoodDTO) {
        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        Food food = foodDAO.findById(foodId);

        if (restaurant != null && food != null && food.getRestaurant().getId() == restaurantId) {
            food.setName(updatedFoodDTO.getName());
            food.setPrice(updatedFoodDTO.getPrice());
            food.setCategory(updatedFoodDTO.getCategory());
            
            foodDAO.update(food);
        }
    }

    // حذف آیتم غذا از رستوران (مطابق DELETE /restaurants/{id}/item/{item_id})
    public void deleteFoodItem(int restaurantId, int foodId) {
        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        Food food = foodDAO.findById(foodId);

        if (restaurant != null && food != null && food.getRestaurant().getId() == restaurantId) {
            foodDAO.delete(food);
        }
    }
}