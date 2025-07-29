package org.example.demo1.model;


import java.util.List;
import java.util.Map;

public class MenuResponseDTO {
    private List<String> menu_titles;
    private Map<String, List<FoodDTO>> menus;

    public MenuResponseDTO() {}

    public MenuResponseDTO(List<String> menuTitles, Map<String, List<FoodDTO>> menus) {
        this.menu_titles = menuTitles;
        this.menus = menus;
    }

    public List<String> getMenu_titles() {
        return menu_titles;
    }

    public void setMenu_titles(List<String> menu_titles) {
        this.menu_titles = menu_titles;
    }

    public Map<String, List<FoodDTO>> getMenus() {
        return menus;
    }

    public void setMenus(Map<String, List<FoodDTO>> menus) {
        this.menus = menus;
    }
}