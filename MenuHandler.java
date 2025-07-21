package httpRequestHandler;

import Entity.Menu;
import dto.MenuDTO;
import service.MenuService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MenuHandler {
    private final MenuService menuService = new MenuService();

    public boolean createMenu(UUID restaurantId, MenuDTO dto) {
        return menuService.createMenu(restaurantId, dto.getTitle());
    }

    public boolean deleteMenu(UUID restaurantId, String title) {
        return menuService.deleteMenu(restaurantId, title);
    }

    public boolean addFood(UUID restaurantId, String title, UUID foodId) {
        return menuService.addFoodToMenu(restaurantId, title, foodId);
    }

    public boolean removeFood(UUID restaurantId, String title, UUID foodId) {
        return menuService.removeFoodFromMenu(restaurantId, title, foodId);
    }

    public List<MenuDTO> getMenusOfRestaurant(UUID restaurantId) {
        List<Menu> menus = menuService.getMenusByRestaurant(restaurantId);
        return menus.stream()
                .map(menu -> new MenuDTO(menu.getTitle()))
                .collect(Collectors.toList());
    }
}