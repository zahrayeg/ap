package controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import model.RestaurantDTO;
import service.RestaurantService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EditRestaurantController {
    private final RestaurantService service = new RestaurantService();
    private final String token = "Bearer YOUR_VALID_TOKEN_HERE"; // جایگزین کن با توکن واقعی

    public void handleUpdate(String idText, String name, String address, String phone,
                             String logoBase64, String taxFeeText, String additionalFeeText, Label statusLabel) {
        try {
            int id = Integer.parseInt(idText.trim());

            // مرحله ۱: گرفتن اطلاعات فعلی از سرور
            service.getRestaurantById(id, token).thenAccept(previous -> Platform.runLater(() -> {
                if (previous == null || previous.isEmpty()) {
                    statusLabel.setText("❌ Restaurant not found.");
                    return;
                }

                RestaurantDTO dto = new RestaurantDTO();

                // مرحله ۲: فقط فیلدهای پرشده رو تغییر بده
                dto.setName(name.isEmpty() ? previous.get("name").toString() : name);
                dto.setAddress(address.isEmpty() ? previous.get("address").toString() : address);
                dto.setPhone(phone.isEmpty() ? previous.get("phone").toString() : phone);

                dto.setLogoBase64(logoBase64 != null ? logoBase64 : previous.get("logoBase64").toString());

                try {
                    dto.setTax_fee(taxFeeText.isEmpty() ? ((Number) previous.get("tax_fee")).intValue()
                            : Integer.parseInt(taxFeeText.trim()));
                    dto.setAdditional_fee(additionalFeeText.isEmpty() ? ((Number) previous.get("additional_fee")).intValue()
                            : Integer.parseInt(additionalFeeText.trim()));
                } catch (NumberFormatException e) {
                    statusLabel.setText("❌ Invalid number format in fees.");
                    return;
                }

                // مرحله ۳: ارسال درخواست به‌روزرسانی
                service.updateRestaurant(id, dto, token).thenAccept(response -> Platform.runLater(() -> {
                    int status = ((Number) response.get("status")).intValue();
                    String message = response.getOrDefault("message", "No message").toString();
                    if (status == 200) {
                        statusLabel.setText("✅ Restaurant updated: " + message);
                    } else {
                        statusLabel.setText("❌ Error (" + status + "): " + message);
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("❌ Exception: " + ex.getMessage()));
                    return null;
                });

            }));
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid restaurant ID.");
        }
    }
}