package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.RestaurantDTO;
import service.RestaurantService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RestaurantFormController {

    @FXML private TextField nameField, addressField, phoneField;
    @FXML private TextArea logoArea;
    @FXML private Spinner<Integer> taxFeeSpinner, additionalFeeSpinner;
    @FXML private Button submitButton;

    private final RestaurantService service = new RestaurantService();
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE"; // جایگزین با توکن واقعی

    @FXML
    public void handleSubmit(ActionEvent event) {
        RestaurantDTO r = new RestaurantDTO();
        r.setName(nameField.getText().trim());
        r.setAddress(addressField.getText().trim());
        r.setPhone(phoneField.getText().trim());
        r.setLogoBase64(logoArea.getText().trim());
        r.setTax_fee(taxFeeSpinner.getValue());
        r.setAdditional_fee(additionalFeeSpinner.getValue());

        // اعتبارسنجی ساده قبل از ارسال
        if (r.getName().isEmpty() || r.getAddress().isEmpty() || r.getPhone().isEmpty()) {
            showAlert("Please fill all required fields.");
            return;
        }
        if (!r.getPhone().matches("\\d+")) {
            showAlert("Phone number must be numeric.");
            return;
        }

        // ارسال async و نمایش نتیجه
        CompletableFuture<Map<String, Object>> future = service.createRestaurant(r, token);
        future.thenAccept(response -> Platform.runLater(() -> {
            int status = ((Number) response.get("status")).intValue();
            String message = response.getOrDefault("message", "Unknown result").toString();
            if (status == 201) {
                showAlert("✅ Success: " + message);
            } else {
                showAlert("❌ Error (" + status + "): " + message);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("❌ Request error: " + ex.getMessage()));
            return null;
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Restaurant Submission");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}