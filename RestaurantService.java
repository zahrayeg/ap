package service;

import DAO.RestaurantDAO;
import DAO.MenuDAO;
import DAO.FoodDAO;
import Entity.Restaurant;
import Entity.Menu;
import Entity.Food;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RestaurantService {
    private final RestaurantDAO dao = new RestaurantDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final FoodDAO foodDAO = new FoodDAO();

    public boolean createRestaurant(String name, String address) {
        if (name == null || address == null || name.trim().isEmpty() || address.trim().isEmpty())
            return false;

        Restaurant restaurant = new Restaurant(name.trim(), address.trim());
        dao.save(restaurant);
        return true;
    }

    public Restaurant getRestaurantById(UUID id) {
        return dao.findById(id);
    }

    public List<Restaurant> getAllRestaurants() {
        return dao.findAll();
    }

    public boolean deleteRestaurant(UUID id) {
        Restaurant rest = dao.findById(id);
        if (rest == null) return false;

        dao.delete(rest);
        return true;
    }

    public boolean updateRestaurant(UUID id, String newName, String newAddress) {
        Restaurant restaurant = dao.findById(id);
        if (restaurant == null) return false;

        if (newName != null && !newName.trim().isEmpty())
            restaurant.setName(newName.trim());

        if (newAddress != null && !newAddress.trim().isEmpty())
            restaurant.setAddress(newAddress.trim());

        dao.save(restaurant);
        return true;
    }

    public List<Menu> getMenusOfRestaurant(UUID id) {
        return menuDAO.findMenusByRestaurant(id);
    }

    public List<Food> getFoodsOfRestaurant(UUID id) {
        List<Food> all = foodDAO.findAll();
        return all.stream()
                .filter(food -> food.getRestaurant().getId().equals(id))
                .collect(Collectors.toList());
    }
}