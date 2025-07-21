package service;

import DAO.FoodDAO;
import Entity.Food;
import Entity.Restaurant;

import java.util.List;
import java.util.UUID;

public class FoodService {
    private final FoodDAO foodDAO = new FoodDAO();

    public boolean createFood(String name, double price, int quantity, String category, Restaurant restaurant) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0 || restaurant == null)
            return false;

        Food food = new Food(name.trim(), price, quantity, restaurant);
        if (category != null && !category.trim().isEmpty()) {
            food.setCategory(category.trim());
        }

        foodDAO.save(food);
        return true;
    }

    public Food getFoodById(UUID id) {
        return foodDAO.findById(id);
    }

    public List<Food> getAllFoods() {
        return foodDAO.findAll();
    }

    public List<Food> getFoodsByRestaurant(UUID restaurantId) {
        return foodDAO.findAllByRestaurant(restaurantId);
    }

    public boolean updateFood(UUID id, String newName, Double newPrice, Integer newQuantity, String newCategory) {
        Food food = foodDAO.findById(id);
        if (food == null) return false;

        if (newName != null && !newName.trim().isEmpty()) food.setName(newName.trim());
        if (newPrice != null && newPrice >= 0) food.setPrice(newPrice);
        if (newQuantity != null && newQuantity >= 0) food.setQuantity(newQuantity);
        if (newCategory != null && !newCategory.trim().isEmpty()) food.setCategory(newCategory.trim());

        foodDAO.save(food);
        return true;
    }

    public boolean deleteFood(UUID id) {
        Food food = foodDAO.findById(id);
        if (food == null) return false;

        foodDAO.delete(food);
        return true;
    }
}