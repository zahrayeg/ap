package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import controller.AddFoodItemController;
import service.RestaurantService;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class AddFoodItemView extends Application {

    private String imageBase64 = null;
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE"; // جایگزین توکن واقعی

    @Override
    public void start(Stage stage) {
        Label title = new Label("Add Food Item");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Spinner<Integer> restaurantIdSpinner = new Spinner<>();
        restaurantIdSpinner.setEditable(false);
        loadRestaurantIds(restaurantIdSpinner);

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
        Button uploadBtn = new Button("Upload Image");
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
                    imageStatus.setText(" Error reading image");
                }
            }
        });

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: yellow;");

        Button submitBtn = new Button("Add Item");
        submitBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String description = descArea.getText().trim();

            double price;
            int supply;
           String priceText = priceField.getText().trim();
            String supplyText = supplyField.getText().trim();

   // بررسی اینکه هیچ فیلدی (بجز keywordها) خالی نباشه
            if (name.isEmpty() || description.isEmpty() || imageBase64 == null ||
                    priceText.isEmpty() || supplyText.isEmpty()) {
                statusLabel.setText("All required fields must be filled.");
                return;
            }
        try {
                price = Double.parseDouble(priceText);
                supply = Integer.parseInt(supplyText);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Price and supply must be numeric.");
                return;
            }

// بررسی اینکه مقدارهای عددی منطقی باشن
            if (price <= 0 || supply < 0) {
                statusLabel.setText("Price must be greater than 0 and supply can't be negative.");
                return;
            }

            List<String> keywords = Arrays.asList(
                            kwField1.getText().trim(),
                            kwField2.getText().trim(),
                            kwField3.getText().trim(),
                            kwField4.getText().trim(),
                            kwField5.getText().trim()
                    ).stream()
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            int restaurantId = restaurantIdSpinner.getValue();

            AddFoodItemController controller = new AddFoodItemController();
            controller.sendFoodItem(token, restaurantId, name, imageBase64, description, price, supply, keywords, statusLabel);
        });

        Button backBtn = new Button("⬅️ Back");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2;");
        backBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(12,
                new HBox(10, backBtn, title),
                createRow("Restaurant ID:", restaurantIdSpinner),
                createRow("Name:", nameField),
                createRow("Description:", descArea),
                createRow("Price:", priceField),
                createRow("Supply:", supplyField),
                createRow("Keyword 1:", kwField1),
                createRow("Keyword 2:", kwField2),
                createRow("Keyword 3:", kwField3),
                createRow("Keyword 4:", kwField4),
                createRow("Keyword 5:", kwField5),
                createRow("Image:", uploadBtn),
                imageStatus,
                submitBtn,
                statusLabel
        );

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setScene(new Scene(root, 600, 720));
        stage.setTitle("Add Food Item");
        stage.show();
    }

    private void loadRestaurantIds(Spinner<Integer> spinner) {
        RestaurantService service = new RestaurantService();
        service.getSellerRestaurants(token).thenAccept(list -> {
            List<Integer> ids = list.stream()
                    .map(entry -> ((Number) entry.get("id")).intValue())
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                spinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(
                        FXCollections.observableArrayList(ids)
                ));
                if (!ids.isEmpty()) spinner.getValueFactory().setValue(ids.get(0));
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                spinner.setDisable(true);
                System.out.println("Failed to load restaurant IDs: " + ex.getMessage());
            });
            return null;
        });
    }

    private HBox createRow(String labelText, Control input) {
        Label label = new Label(labelText);
        label.setMinWidth(160);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        HBox box = new HBox(10, label, input);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}