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

public class AddMenuView extends Application {

    private final RestaurantService service = new RestaurantService();
    private final ObservableList<RestaurantEntry> restaurantList = FXCollections.observableArrayList();
    private final Gson gson = new Gson();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Create Restaurant Menu");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<RestaurantEntry> restaurantCombo = new ComboBox<>(restaurantList);
        restaurantCombo.setPrefWidth(240);
        restaurantCombo.setPromptText("Select Restaurant");

        TextField menuTitleField = new TextField();
        menuTitleField.setPrefWidth(240);
        menuTitleField.setPromptText("Enter Menu Title");

        Button createBtn = new Button("Create Menu");
        createBtn.setPrefWidth(240);
        createBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(240);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, title, restaurantCombo, menuTitleField, createBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Add Restaurant Menu");
        stage.setScene(new Scene(layout, 460, 360));
        stage.show();

        // بارگذاری رستوران‌های فروشنده
        service.getSellerRestaurants("Bearer your-token")
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    restaurantList.setAll(restaurants.stream()
                            .map(m -> new RestaurantEntry(
                                    Integer.parseInt(m.get("id").toString()),
                                    m.get("name").toString()))
                            .toList());
                }));

        // فعال‌سازی دکمه فقط وقتی ورودی معتبر باشه
        menuTitleField.textProperty().addListener((obs, oldV, newV) -> {
            createBtn.setDisable(newV.trim().isEmpty() || restaurantCombo.getValue() == null);
        });

        restaurantCombo.valueProperty().addListener((obs, old, selected) -> {
            createBtn.setDisable(menuTitleField.getText().trim().isEmpty() || selected == null);
        });

        // ارسال منو
        createBtn.setOnAction(e -> {
            var selectedRestaurant = restaurantCombo.getValue();
            String menuTitle = menuTitleField.getText().trim();

            service.addMenu(selectedRestaurant.id, menuTitle, "Bearer your-token")
                    .thenAccept(response -> Platform.runLater(() -> {
                        Object titleObj = response.get("title");
                        String msg = titleObj != null
                                ? "Menu \"" + titleObj.toString() + "\" created successfully."
                                : "Menu created successfully.";
                        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
                        menuTitleField.clear();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            String msg;
                            String raw = ex.getMessage();
                            try {
                                Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                                Map<String, Object> error = gson.fromJson(raw, mapType);
                                msg = error.containsKey("error") ? error.get("error").toString() : raw;
                            } catch (Exception parseEx) {
                                msg = raw.contains("409") ? "Menu title already exists."
                                        : raw.contains("400") ? "Invalid input – please check your fields."
                                        : raw.contains("401") ? "Unauthorized – please login again."
                                        : raw.contains("500") ? "Server error – please try later."
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
}