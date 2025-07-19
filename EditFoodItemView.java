package view;

import controller.EditFoodItemController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.RestaurantService;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class EditFoodItemView extends Application {

    private String imageBase64 = null;
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE";

    @Override
    public void start(Stage stage) {
        Label title = new Label("Edit Food Item");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Spinner<Integer> restaurantSpinner = new Spinner<>();
        Spinner<Integer> foodSpinner = new Spinner<>();
        TextField nameField = new TextField();
        TextArea descArea = new TextArea();
        TextField priceField = new TextField();
        TextField supplyField = new TextField();
        TextField kwField1 = new TextField();
        TextField kwField2 = new TextField();
        TextField kwField3 = new TextField();
        TextField kwField4 = new TextField();
        TextField kwField5 = new TextField();
        Label imageStatus = new Label("No image selected");
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: yellow;");

        Button uploadBtn = new Button("Upload New Image");
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    imageBase64 = Base64.getEncoder().encodeToString(bytes);
                    imageStatus.setText("Selected: " + file.getName());
                } catch (Exception ex) {
                    imageStatus.setText("Error reading image");
                }
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1e3a5f;");
        backBtn.setOnAction(e -> stage.close());

        Button submitBtn = new Button("Submit Changes");
        submitBtn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold;");
        submitBtn.setOnAction(e -> {
            Integer restaurantId = restaurantSpinner.getValue();
            Integer foodId = foodSpinner.getValue();
            if (restaurantId == null || foodId == null) {
                statusLabel.setText("Restaurant and Food IDs must be selected.");
                return;
            }

            Map<String, Object> updates = new LinkedHashMap<>();

            String name = nameField.getText().trim();
            if (!name.isEmpty()) updates.put("name", name);

            String desc = descArea.getText().trim();
            if (!desc.isEmpty()) updates.put("description", desc);

            if (!priceField.getText().trim().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    if (price > 0) updates.put("price", price);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Price must be numeric and > 0.");
                    return;
                }
            }

            if (!supplyField.getText().trim().isEmpty()) {
                try {
                    int supply = Integer.parseInt(supplyField.getText().trim());
                    if (supply >= 0) updates.put("supply", supply);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Supply must be numeric and ≥ 0.");
                    return;
                }
            }

            if (imageBase64 != null) updates.put("imageBase64", imageBase64);

            List<String> keywords = Arrays.asList(
                    kwField1.getText().trim(),
                    kwField2.getText().trim(),
                    kwField3.getText().trim(),
                    kwField4.getText().trim(),
                    kwField5.getText().trim()
            ).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (!keywords.isEmpty()) updates.put("keywords", keywords);

            if (updates.isEmpty()) {
                statusLabel.setText("At least one field must be modified.");
                return;
            }

            EditFoodItemController controller = new EditFoodItemController();
            controller.sendEditRequest(token, restaurantId, foodId, updates, statusLabel);
        });

        VBox root = new VBox(12,
                new HBox(10, backBtn, title),
                createRow("Restaurant ID:", restaurantSpinner),
                createRow("Food Item ID:", foodSpinner),
                createRow("New Name:", nameField),
                createRow("New Description:", descArea),
                createRow("New Price:", priceField),
                createRow("New Supply:", supplyField),
                createRow("Keyword 1:", kwField1),
                createRow("Keyword 2:", kwField2),
                createRow("Keyword 3:", kwField3),
                createRow("Keyword 4:", kwField4),
                createRow("Keyword 5:", kwField5),
                createRow("New Image:", uploadBtn),
                imageStatus,
                submitBtn,
                statusLabel
        );

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setScene(new Scene(root, 600, 750));
        stage.setTitle("Edit Food Item");
        stage.show();

        loadRestaurantIds(restaurantSpinner, token, foodSpinner);
    }

    private void loadRestaurantIds(Spinner<Integer> restaurantSpinner, String token, Spinner<Integer> foodSpinner) {
        RestaurantService service = new RestaurantService();
        service.getSellerRestaurants(token).thenAccept(restaurants -> {
            List<Integer> restaurantIds = restaurants.stream()
                    .map(entry -> ((Number) entry.get("id")).intValue())
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                restaurantSpinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(
                        FXCollections.observableArrayList(restaurantIds)
                ));
                if (!restaurantIds.isEmpty()) {
                    restaurantSpinner.getValueFactory().setValue(restaurantIds.get(0));
                    loadFoodIds(foodSpinner, token, restaurantIds.get(0));
                }

                restaurantSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) loadFoodIds(foodSpinner, token, newVal);
                });
            });
        });
    }

    private void loadFoodIds(Spinner<Integer> foodSpinner, String token, int restaurantId) {
        RestaurantService service = new RestaurantService();
        service.getRestaurantMenu(token, restaurantId).thenAccept(items -> {
            List<Integer> foodIds = items.stream()
                    .map(entry -> ((Number) entry.get("id")).intValue())
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                foodSpinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(
                        FXCollections.observableArrayList(foodIds)
                ));
                if (!foodIds.isEmpty()) foodSpinner.getValueFactory().setValue(foodIds.get(0));
            });
        });
    }

    private HBox createRow(String labelText, Control input) {
        Label label = new Label(labelText);
        label.setMinWidth(160);
        label.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        HBox box = new HBox(10, label, input);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}