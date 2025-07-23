package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.RestaurantDTO;
import service.RestaurantService;

import java.util.Map;

public class RestaurantFormController {

    @FXML private TextField nameField, addressField, phoneField;
    @FXML private TextArea logoArea;
    @FXML private Spinner<Integer> taxFeeSpinner, additionalFeeSpinner;
    @FXML private Button submitButton;

    private final RestaurantService service = new RestaurantService();
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE";

    @FXML
    public void handleSubmit(ActionEvent event) {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String logoBase64 = logoArea.getText().trim();
        Integer taxFee = taxFeeSpinner.getValue();
        Integer additionalFee = additionalFeeSpinner.getValue();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()
                || taxFee == null || additionalFee == null) {
            showAlert("Please fill all required fields (name, address, phone, tax fee, additional fee).");
            return;
        }

        if (!phone.matches("\\d+")) {
            showAlert("Phone number must be numeric.");
            return;
        }

        RestaurantDTO dto = new RestaurantDTO();
        dto.setName(name);
        dto.setAddress(address);
        dto.setPhone(phone);
        dto.setTaxFee(taxFee);
        dto.setAdditionalFee(additionalFee);
        if (!logoBase64.isEmpty()) {
            dto.setLogoBase64(logoBase64);
        }

        service.createRestaurant(token, dto)
                .thenAccept(response -> Platform.runLater(() -> {
                    int status = ((Number) response.get("status")).intValue();
                    if (status >= 200 && status < 300) {
                        showAlert("✅ Restaurant created successfully.");
                    } else {
                        String msg = response.getOrDefault("error", response.get("message")).toString();
                        showAlert("❌ Error (" + status + "): " + msg);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            showAlert("❌ Request error: " + ex.getMessage())
                    );
                    return null;
                });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Restaurant");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}