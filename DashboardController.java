package controller;

import javafx.application.Application;
import javafx.stage.Stage;
import view.*;

public class DashboardController {

    private final Stage dashboardStage;

    public DashboardController(Stage dashboardStage) {
        this.dashboardStage = dashboardStage;
    }

    public void openAddMenuPage() {
        new view.AddMenuView().start(new Stage());
    }

    public void openOrdersPage() {
        new RestaurantOrdersView().start(new Stage());
    }

    public void openUpdateStatusPage() {
        new UpdateOrderStatusView().start(new Stage());
    }






    public void openDeleteMenuPage() {
        new view.DeleteMenuView().start(new Stage());
    }

    public void openAddItemToMenuPage() {
        new view.AddItemToMenuView().start(new Stage());
    }

    public void openRemoveItemFromMenuPage() {
        new view.RemoveItemFromMenuView().start(new Stage());
    }

    // GET /restaurants/mine
    public void openViewRestaurants() {
        launchPage(new MyRestaurantsTableView());
    }

    // POST /restaurants
    public void openAddRestaurant() {
        launchPage(new RestaurantFormTestApp());
    }

    // PUT /restaurants/{id}
    public void openEditRestaurant() {
        launchPage(new EditRestaurantView());
    }

    // POST /restaurants/{id}/item
    public void openAddFoodItem() {
        launchPage(new AddFoodItemView());
    }

    // PUT /restaurants/{id}/item/{item_id}
    public void openEditFoodItem() {
        launchPage(new EditFoodItemView());
    }

    // DELETE /restaurants/{id}/item/{item_id}
    public void openDeleteFoodItemPage() {
        launchPage(new DeleteFoodItemView());
    }

    private void launchPage(Application page) {
        dashboardStage.hide();
        Stage pageStage = new Stage();
        try {
            page.start(pageStage);
            pageStage.setOnHidden(e -> dashboardStage.show());
        } catch (Exception ex) {
            ex.printStackTrace();
            dashboardStage.show();
        }
    }
}