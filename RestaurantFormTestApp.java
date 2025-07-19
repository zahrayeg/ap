package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class RestaurantFormTestApp extends Application {

    @Override
    public void start(Stage stage) {
        // عنوان فرم
        Label title = new Label("Create a New Restaurant");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        // فیلدهای ورودی با لیبل چپ
        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField phoneField = new TextField();
        TextField taxFeeField = new TextField();
        TextField additionalFeeField = new TextField();

        Label imageLabel = new Label("No image selected");
        Button uploadBtn = new Button("Upload Logo");
        final String[] logoBase64 = {null};

        // ساخت HBox برای هر فیلد
        HBox nameBox = createInputRow("Name:", nameField);
        HBox addressBox = createInputRow("Address:", addressField);
        HBox phoneBox = createInputRow("Phone:", phoneField);
        HBox taxBox = createInputRow("Tax Fee (Optional):", taxFeeField);
        HBox additionalBox = createInputRow("Additional Fee (Optional):", additionalFeeField);

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    logoBase64[0] = Base64.getEncoder().encodeToString(bytes);
                    imageLabel.setText("Logo: " + file.getName());
                } catch (Exception ex) {
                    imageLabel.setText("Error loading image");
                }
            }
        });

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: yellow;");

        Button submitBtn = new Button("Submit");
        submitBtn.setStyle("-fx-background-color#2c6cec; -fx-text-fill: #0c96df;");
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String addr = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            String taxFee = taxFeeField.getText().trim();
            String additionalFee = additionalFeeField.getText().trim();

            // اعتبارسنجی
            if (name.isEmpty() || addr.isEmpty() || phone.isEmpty()) {
                messageLabel.setText("Please fill all required fields!");
                return;
            }
            if (!phone.matches("\\d+")) {
                messageLabel.setText("Phone number must contain digits only!");
                return;
            }

            // نمایش اطلاعات
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Restaurant Created");
            alert.setHeaderText("Submitted Data:");
            alert.setContentText(
                    "Name: " + name + "\n" +
                            "Address: " + addr + "\n" +
                            "Phone: " + phone + "\n" +
                            "Tax Fee: " + (taxFee.isEmpty() ? "N/A" : taxFee) + "\n" +
                            "Additional Fee: " + (additionalFee.isEmpty() ? "N/A" : additionalFee) + "\n" +
                            "Logo: " + (logoBase64[0] != null ? "Included" : "None")
            );
            alert.showAndWait();
            messageLabel.setText(""); // پاک کردن پیام خطا بعد از موفقیت
        });

        VBox root = new VBox(10,
                title,
                nameBox, addressBox, phoneBox,
                taxBox, additionalBox,
                uploadBtn, imageLabel,
                submitBtn, messageLabel
        );
        VBox.setVgrow(nameBox, Priority.NEVER); // فیلدهای مهم مثل اسم
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#1e3a5f; -fx-alignment: center;");

        Scene scene = new Scene(root, 480, 450);
        stage.setScene(scene);
        stage.setTitle("Create Restaurant");
        stage.show();
    }

    // ساخت ردیف لیبل و فیلد
    private HBox createInputRow(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setMinWidth(160);

        field.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        field.setPrefWidth(250);
        HBox box = new HBox(10, label, field);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}