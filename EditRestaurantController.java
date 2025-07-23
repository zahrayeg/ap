package controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import model.RestaurantDTO;
import service.RestaurantService;

import java.util.Map;

public class EditRestaurantController {
    private final RestaurantService service = new RestaurantService();
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE";

    public void handleUpdate(
            String idText,
            String name,
            String address,
            String phone,
            String logoBase64,
            String taxFeeText,
            String additionalFeeText,
            Label statusLabel
    ) {
        int id;
        int taxFee;
        int additionalFee;

        // parse and validate ID
        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException e) {
            statusLabel.setText(" Invalid restaurant ID.");
            return;
        }

        // validate required text fields
        if (name == null || name.isBlank() ||
                address == null || address.isBlank() ||
                phone == null || phone.isBlank() ||
                logoBase64 == null || logoBase64.isBlank() ||
                taxFeeText == null || taxFeeText.isBlank() ||
                additionalFeeText == null || additionalFeeText.isBlank()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        // validate phone numeric
        if (!phone.matches("\\d+")) {
            statusLabel.setText(" Phone must be numeric.");
            return;
        }

        // parse and validate fee fields
        try {
            taxFee = Integer.parseInt(taxFeeText.trim());
            additionalFee = Integer.parseInt(additionalFeeText.trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Tax and Additional fees must be numbers.");
            return;
        }

        // build DTO
        RestaurantDTO dto = new RestaurantDTO();
        dto.setName(name.trim());
        dto.setAddress(address.trim());
        dto.setPhone(phone.trim());
        dto.setLogoBase64(logoBase64.trim());
        dto.setTaxFee(taxFee);
        dto.setAdditionalFee(additionalFee);

        // send PUT request
        service.updateRestaurant(token, id, dto)
                .thenAccept(response -> Platform.runLater(() -> {
                    int status = ((Number) response.get("status")).intValue();
                    String msg = response.containsKey("message")
                            ? response.get("message").toString()
                            : response.getOrDefault("error", "Unknown").toString();
                    if (status >= 200 && status < 300) {
                        statusLabel.setText("✅ Updated successfully.");
                    } else {
                        statusLabel.setText(" Error (" + status + "): " + msg);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            statusLabel.setText(" Request failed: " + ex.getMessage())
                    );
                    return null;
                });
    }
}