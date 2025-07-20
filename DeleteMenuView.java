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

public class DeleteMenuView extends Application {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();

    private final ObservableList<RestaurantEntry> restaurantList = FXCollections.observableArrayList();
    private final ObservableList<MenuEntry> menuList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Delete Restaurant Menu");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<RestaurantEntry> restaurantCombo = new ComboBox<>(restaurantList);
        restaurantCombo.setPrefWidth(240);
        restaurantCombo.setPromptText("Select Restaurant");

        ComboBox<MenuEntry> menuCombo = new ComboBox<>(menuList);
        menuCombo.setPrefWidth(240);
        menuCombo.setPromptText("Select Menu Title");

        Button deleteBtn = new Button("Delete Menu");
        deleteBtn.setPrefWidth(240);
        deleteBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(240);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, title, restaurantCombo, menuCombo, deleteBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Delete Restaurant Menu");
        stage.setScene(new Scene(layout, 460, 360));
        stage.show();

        // بارگذاری رستوران‌های فروشنده
        service.getSellerRestaurants("Bearer your-token").thenAccept(restaurants -> Platform.runLater(() -> {
            restaurantList.setAll(restaurants.stream()
                .map(m -> new RestaurantEntry(Integer.parseInt(m.get("id").toString()), m.get("name").toString()))
                .toList());
        }));

        // وقتی رستوران انتخاب شد، لیست منوهاشو بارگذاری کن
        restaurantCombo.valueProperty().addListener((obs, old, selectedRestaurant) -> {
            if (selectedRestaurant == null) return;
            deleteBtn.setDisable(true);
            service.getRestaurantMenu("Bearer your-token", selectedRestaurant.id)
                   .thenAccept(menus -> Platform.runLater(() -> {
                       menuList.setAll(menus.stream()
                           .map(m -> new MenuEntry(m.get("title").toString()))
                           .toList());
                       deleteBtn.setDisable(menuList.isEmpty());
                   }));
        });

        // دکمه فعال فقط وقتی عنوان منو انتخاب شده
        menuCombo.valueProperty().addListener((obs, old, selectedMenu) -> {
            deleteBtn.setDisable(selectedMenu == null);
        });

        // حذف منو
        deleteBtn.setOnAction(e -> {
            var restaurant = restaurantCombo.getValue();
            var menu = menuCombo.getValue();
            service.deleteMenu(restaurant.id, menu.title, "Bearer your-token")
                   .thenAccept(response -> Platform.runLater(() -> {
                       String msg = response.containsKey("message") ?
                                    response.get("message").toString() :
                                    "Menu deleted successfully.";
                       new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
                       restaurantCombo.getSelectionModel().select(restaurant); // refresh menu list
                   }))
                   .exceptionally(ex -> {
                       Platform.runLater(() -> {
                           String msg;
                           try {
                               Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                               Map<String, Object> error = gson.fromJson(ex.getMessage(), mapType);
                               msg = error.containsKey("error") ? error.get("error").toString() : ex.getMessage();
                           } catch (Exception parseEx) {
                               msg = ex.getMessage().contains("409") ? "Conflict: Cannot delete menu."
                                   : ex.getMessage().contains("400") ? "Invalid input."
                                   : ex.getMessage().contains("404") ? "Menu not found."
                                   : ex.getMessage().contains("401") ? "Unauthorized access."
                                   : "Unexpected error.";
                           }
                           new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
                       });
                       return null;
                   });
        });
    }

    private record RestaurantEntry(int id, String name) {
        @Override public String toString() { return name; }
    }

    private record MenuEntry(String title) {
        @Override public String toString() { return title; }
    }
}