package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.RestaurantService;

import java.util.List;
import java.util.Map;

public class DeleteFoodItemView extends Application {

    private final RestaurantService service = new RestaurantService();
    private final ObservableList<RestaurantEntry> restaurantList = FXCollections.observableArrayList();
    private final ObservableList<FoodItemEntry> foodList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Delete Food Item");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<RestaurantEntry> restaurantCombo = new ComboBox<>(restaurantList);
        ComboBox<FoodItemEntry> foodCombo = new ComboBox<>(foodList);
        restaurantCombo.setPrefWidth(220);
        foodCombo.setPrefWidth(220);
        restaurantCombo.setPromptText("Select Restaurant");
        foodCombo.setPromptText("Select Food Item");

        Button deleteBtn = new Button("Delete Item");
        deleteBtn.setPrefWidth(220);
        deleteBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(220);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(20, title,
                new HBox(20, restaurantCombo, foodCombo),
                deleteBtn, backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Delete Food Item");
        stage.setScene(new Scene(root, 520, 320));
        stage.show();

        // بارگذاری رستوران‌ها
        service.getSellerRestaurants("Bearer your-token").thenAccept(restaurants -> {
            Platform.runLater(() -> {
                restaurantList.setAll(restaurants.stream()
                        .map(m -> new RestaurantEntry(Integer.parseInt(m.get("id").toString()), m.get("name").toString()))
                        .toList());
            });
        });

        // بارگذاری آیتم‌ها وقتی رستوران انتخاب شد
        restaurantCombo.valueProperty().addListener((obs, oldV, selectedRestaurant) -> {
            if (selectedRestaurant == null) return;
            deleteBtn.setDisable(true);

            service.getRestaurantMenu("Bearer your-token", selectedRestaurant.id).thenAccept(items -> {
                Platform.runLater(() -> {
                    foodList.setAll(items.stream()
                            .map(m -> new FoodItemEntry(Integer.parseInt(m.get("item_id").toString()), m.get("name").toString()))
                            .toList());
                    if (!foodList.isEmpty()) {
                        foodCombo.getSelectionModel().selectFirst();
                        deleteBtn.setDisable(false);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait());
                return null;
            });
        });

        // حذف آیتم غذا
        deleteBtn.setOnAction(e -> {
            var restaurant = restaurantCombo.getValue();
            var food = foodCombo.getValue();
            service.deleteFoodItem(food.id, "Bearer your-token").thenAccept(response -> {
                Platform.runLater(() -> {
                    String msg = response.containsKey("message") ? response.get("message").toString() : "Deleted successfully";
                    new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
                    restaurantCombo.getSelectionModel().select(restaurant); // ری‌لود آیتم‌ها
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait());
                return null;
            });
        });
    }

    private record RestaurantEntry(int id, String name) {
        @Override public String toString() { return name; }
    }

    private record FoodItemEntry(int id, String name) {
        @Override public String toString() { return name; }
    }
}