package view;

import controller.EditRestaurantController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.RestaurantService;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class EditRestaurantView extends Application {

    private String logoBase64 = null;
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE"; // جایگزین توکن معتبرت

    @Override
    public void start(Stage stage) {
        Label title = new Label("Edit Restaurant");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        Spinner<Integer> idSpinner = new Spinner<>();
        idSpinner.setEditable(false);
        loadRestaurantIdsIntoSpinner(idSpinner); // اینجا لیست واقعاً از سرور گرفته می‌شه

        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField phoneField = new TextField();
        TextField taxFeeField = new TextField();
        TextField additionalFeeField = new TextField();

        Label logoLabel = new Label("No image selected");
        Button uploadBtn = new Button("Upload Logo");
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    logoBase64 = Base64.getEncoder().encodeToString(bytes);
                    logoLabel.setText("Selected: " + file.getName());
                } catch (Exception ex) {
                    logoLabel.setText("Error loading image");
                }
            }
        });

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: yellow;");

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #242def; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            EditRestaurantController controller = new EditRestaurantController();
            controller.handleUpdate(idSpinner.getValue() + "", nameField.getText(), addressField.getText(),
                    phoneField.getText(), logoBase64, taxFeeField.getText(), additionalFeeField.getText(), statusLabel);
        });

        VBox root = new VBox(12,
                title,
                createRow("Restaurant ID:", idSpinner),
                createRow("Name:", nameField),
                createRow("Address:", addressField),
                createRow("Phone:", phoneField),
                createRow("Tax Fee:", taxFeeField),
                createRow("Additional Fee:", additionalFeeField),
                createRow("Logo:", uploadBtn),
                logoLabel,
                updateBtn,
                statusLabel);

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setScene(new Scene(root, 480, 560));
        stage.setTitle("Edit Restaurant");
        stage.show();
    }

    private void loadRestaurantIdsIntoSpinner(Spinner<Integer> spinner) {
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
        label.setMinWidth(130);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        HBox box = new HBox(10, label, input);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}