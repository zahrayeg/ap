package view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

public class AddItemToMenuView extends Application {

    private final RestaurantService service = new RestaurantService();
    private final Gson gson = new Gson();

    private final ObservableList<RestaurantEntry> restaurantList = FXCollections.observableArrayList();
    private final ObservableList<MenuEntry> menuList = FXCollections.observableArrayList();
    private final ObservableList<FoodEntry> foodList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Add Food Item to Menu");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<RestaurantEntry> restaurantCombo = new ComboBox<>(restaurantList);
        restaurantCombo.setPrefWidth(240);
        restaurantCombo.setPromptText("Select Restaurant");

        ComboBox<MenuEntry> menuCombo = new ComboBox<>(menuList);
        menuCombo.setPrefWidth(240);
        menuCombo.setPromptText("Select Menu");

        ComboBox<FoodEntry> foodCombo = new ComboBox<>(foodList);
        foodCombo.setPrefWidth(240);
        foodCombo.setPromptText("Select Food Item");

        Button addBtn = new Button("Add Item to Menu");
        addBtn.setPrefWidth(240);
        addBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(240);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, title, restaurantCombo, menuCombo, foodCombo, addBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Add Item to Menu");
        stage.setScene(new Scene(layout, 480, 400));
        stage.show();

        // بارگذاری رستوران‌های seller
        service.getSellerRestaurants("Bearer your-token").thenAccept(rests -> Platform.runLater(() -> {
            restaurantList.setAll(rests.stream()
                .map(m -> new RestaurantEntry(Integer.parseInt(m.get("id").toString()), m.get("name").toString()))
                .toList());
        }));

        // وقتی رستوران انتخاب شد → منوها و آیتم‌ها رو بارگذاری کن
        restaurantCombo.valueProperty().addListener((obs, oldV, selectedRestaurant) -> {
            if (selectedRestaurant == null) return;

            // بارگذاری منوهای آن رستوران
            service.getRestaurantMenu("Bearer your-token", selectedRestaurant.id).thenAccept(menus -> {
                Platform.runLater(() -> {
                    menuList.setAll(menus.stream()
                        .map(m -> new MenuEntry(m.get("title").toString()))
                        .toList());
                    menuCombo.getSelectionModel().clearSelection();
                });
            });

            // بارگذاری آیتم‌های غذایی آن رستوران
            service.getItemsByRestaurantId("Bearer your-token", selectedRestaurant.id).thenAccept(items -> {
                Platform.runLater(() -> {
                    foodList.setAll(items.stream()
                        .map(m -> new FoodEntry(Integer.parseInt(m.get("item_id").toString()), m.get("name").toString()))
                        .toList());
                    foodCombo.getSelectionModel().clearSelection();
                });
            });
        });

        // بررسی تکمیل انتخاب‌ها برای فعال‌سازی دکمه
        ChangeListener<Object> checker = (obs, oldV, newV) -> {
            boolean valid = restaurantCombo.getValue() != null &&
                            menuCombo.getValue() != null &&
                            foodCombo.getValue() != null;
            addBtn.setDisable(!valid);
        };

        restaurantCombo.valueProperty().addListener(checker);
        menuCombo.valueProperty().addListener(checker);
        foodCombo.valueProperty().addListener(checker);

        // انجام درخواست اضافه‌کردن آیتم به منو
        addBtn.setOnAction(e -> {
            var restaurant = restaurantCombo.getValue();
            var menu = menuCombo.getValue();
            var food = foodCombo.getValue();

            service.addItemToMenu(restaurant.id, menu.title, food.id, "Bearer your-token")
                   .thenAccept(response -> Platform.runLater(() -> {
                       String msg = response.containsKey("message") ?
                                    response.get("message").toString() :
                                    "Item added to menu successfully.";
                       new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
                       foodCombo.getSelectionModel().clearSelection();
                       addBtn.setDisable(true);
                   }))
                   .exceptionally(ex -> {
                       Platform.runLater(() -> {
                           String msg;
                           try {
                               Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                               Map<String, Object> err = gson.fromJson(ex.getMessage(), mapType);
                               msg = err.containsKey("error") ? err.get("error").toString() : ex.getMessage();
                           } catch (Exception parseEx) {
                               msg = ex.getMessage().contains("409") ? "Conflict – item already in menu."
                                   : ex.getMessage().contains("400") ? "Invalid input."
                                   : ex.getMessage().contains("401") ? "Unauthorized."
                                   : ex.getMessage().contains("404") ? "Not found."
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

    private record FoodEntry(int id, String name) {
        @Override public String toString() { return name; }
    }
}