package view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.RestaurantService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RemoveItemFromMenuView extends Application {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();

    private final ObservableList<RestaurantEntry> restaurantList = FXCollections.observableArrayList();
    private final ObservableList<MenuEntry> menuList = FXCollections.observableArrayList();
    private final ObservableList<FoodEntry> foodList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Remove Item from Menu");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<RestaurantEntry> restaurantCombo = new ComboBox<>(restaurantList);
        restaurantCombo.setPrefWidth(240);
        restaurantCombo.setPromptText("Select Restaurant");

        ComboBox<MenuEntry> menuCombo = new ComboBox<>(menuList);
        menuCombo.setPrefWidth(240);
        menuCombo.setPromptText("Select Menu");

        ComboBox<FoodEntry> foodCombo = new ComboBox<>(foodList);
        foodCombo.setPrefWidth(240);
        foodCombo.setPromptText("Select Item to Remove");

        Button deleteBtn = new Button("Delete Item");
        deleteBtn.setPrefWidth(240);
        deleteBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(240);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, title, restaurantCombo, menuCombo, foodCombo, deleteBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Remove Item from Menu");
        stage.setScene(new Scene(layout, 480, 400));
        stage.show();

        // بارگذاری رستوران‌های seller
        service.getSellerRestaurants("Bearer your-token").thenAccept(rests ->
            Platform.runLater(() -> restaurantList.setAll(
                rests.stream()
                     .map(m -> new RestaurantEntry(Integer.parseInt(m.get("id").toString()), m.get("name").toString()))
                     .toList()
            ))
        );

        // وقتی رستوران انتخاب شد → منوهاشو بارگذاری کن
        restaurantCombo.valueProperty().addListener((obs, old, selectedRestaurant) -> {
            if (selectedRestaurant == null) return;
            deleteBtn.setDisable(true);
            menuCombo.getItems().clear();
            foodCombo.getItems().clear();

            service.getRestaurantMenu("Bearer your-token", selectedRestaurant.id).thenAccept(menus ->
                Platform.runLater(() -> menuList.setAll(
                    menus.stream().map(m -> new MenuEntry(m.get("title").toString())).toList()
                ))
            );
        });

        // وقتی منو انتخاب شد → آیتم‌های داخلش رو بارگذاری کن
        menuCombo.valueProperty().addListener((obs, old, selectedMenu) -> {
            var restaurant = restaurantCombo.getValue();
            if (restaurant == null || selectedMenu == null) return;
            deleteBtn.setDisable(true);
            foodCombo.getItems().clear();

            service.getRestaurantMenu("Bearer your-token", restaurant.id).thenAccept(menus -> {
                Platform.runLater(() -> {
                    // پیداکردن منوی انتخاب‌شده و استخراج آیتم‌ها
                    menus.stream()
                         .filter(m -> selectedMenu.title.equals(m.get("title").toString()))
                         .findFirst()
                         .ifPresent(menu -> {
                             List<Map<String, Object>> items = (List<Map<String, Object>>) menu.get("items");
                             List<FoodEntry> entries = items.stream()
                                 .map(f -> new FoodEntry(Integer.parseInt(f.get("item_id").toString()), f.get("name").toString()))
                                 .toList();
                             foodList.setAll(entries);
                         });
                });
            });
        });

        // فعال‌سازی دکمه بعد از انتخاب کامل
        foodCombo.valueProperty().addListener((obs, old, selectedFood) -> {
            deleteBtn.setDisable(selectedFood == null || menuCombo.getValue() == null || restaurantCombo.getValue() == null);
        });

        // حذف آیتم از منو
        deleteBtn.setOnAction(e -> {
            var restaurant = restaurantCombo.getValue();
            var menu = menuCombo.getValue();
            var item = foodCombo.getValue();

            service.removeItemFromMenu(restaurant.id, menu.title, item.id, "Bearer your-token")
                   .thenAccept(response -> Platform.runLater(() -> {
                       String msg = response.containsKey("message")
                                ? response.get("message").toString()
                                : "Item removed from menu.";
                       new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
                       foodCombo.getSelectionModel().clearSelection();
                       deleteBtn.setDisable(true);
                   }))
                   .exceptionally(ex -> {
                       Platform.runLater(() -> {
                           String msg;
                           try {
                               Type t = new TypeToken<Map<String, Object>>(){}.getType();
                               Map<String, Object> err = gson.fromJson(ex.getMessage(), t);
                               msg = err.containsKey("error") ? err.get("error").toString() : ex.getMessage();
                           } catch (Exception parseEx) {
                               msg = ex.getMessage().contains("404") ? "Item or menu not found."
                                   : ex.getMessage().contains("401") ? "Unauthorized access."
                                   : ex.getMessage().contains("400") ? "Invalid input."
                                   : ex.getMessage().contains("409") ? "Conflict: Could not remove item."
                                   : "Unexpected error.";
                           }
                           new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
                       });
                       return null;
                   });
        });
    }

    private record RestaurantEntry(int id, String name) { @Override public String toString() { return name; } }
    private record MenuEntry(String title) { @Override public String toString() { return title; } }
    private record FoodEntry(int id, String name) { @Override public String toString() { return name; } }
}