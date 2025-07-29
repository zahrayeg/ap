package org.example.demo1.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.demo1.model.UserDTO;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.example.demo1.controller.AuthController;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.example.demo1.model.StatusResult;
import org.example.demo1.model.ServiceResult;

import java.io.IOException;
public class SignUpView {
    private final Stage stage;
    private final AuthController authController = new AuthController();
    private String token;

    public SignUpView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #4CAF50;");

        // Logo
        ImageView logo = new ImageView(
                new Image("file:src/main/resources/images/img.png", 150, 150, true, true)
        );
        root.getChildren().add(logo);

        // Input fields
        TextField fullNameField      = new TextField();    fullNameField.setPromptText("Full Name");
        TextField phoneField         = new TextField();    phoneField.setPromptText("Phone");
        PasswordField passwordField  = new PasswordField();passwordField.setPromptText("Password");
        ChoiceBox<String> roleChoice = new ChoiceBox<>();
        roleChoice.getItems().addAll("buyer", "seller", "courier");
        roleChoice.setValue("buyer");
        TextField addressField       = new TextField();    addressField.setPromptText("Address");
        TextField emailField         = new TextField();    emailField.setPromptText("Email (Optional)");
        TextField bankNameField      = new TextField();    bankNameField.setPromptText("Bank Name (Optional)");
        TextField accountNumberField = new TextField();    accountNumberField.setPromptText("Account Number (Optional)");

        // Image upload
        Label imageLabel = new Label("No image selected");
        Button uploadBtn = new Button("Upload Image");
        final String[] imageBase64 = {null};
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    imageBase64[0] = Base64.getEncoder().encodeToString(bytes);
                    imageLabel.setText("Selected: " + file.getName());
                } catch (IOException ex) {
                    imageLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white;");

        // Go to User Dashboard button (disabled until approved)
        Button goToDashboardBtn = new Button("Go to Dashboard");
        goToDashboardBtn.setDisable(true);
        goToDashboardBtn.setOnAction(e -> handleGoToDashboard(messageLabel));

        // Sign Up button
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setOnAction(e -> handleSignUp(
                fullNameField.getText().trim(),
                phoneField.getText().trim(),
                passwordField.getText().trim(),
                roleChoice.getValue(),
                addressField.getText().trim(),
                emailField.getText().trim(),
                bankNameField.getText().trim(),
                accountNumberField.getText().trim(),
                imageBase64[0],
                messageLabel,
                goToDashboardBtn
        ));

        // Link to Login
        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setOnAction(e -> new LoginView(stage));

        // Admin Dashboard button (always enabled, no token required)
        Button adminDashboardBtn = new Button("Admin Dashboard");
        adminDashboardBtn.setOnAction(e -> {
            MainAdminDashboardView adminView = new MainAdminDashboardView();
            Stage adminStage = new Stage();
            adminView.start(adminStage);
        });

        // Assemble UI
        root.getChildren().addAll(
                fullNameField,
                phoneField,
                passwordField,
                roleChoice,
                addressField,
                emailField,
                bankNameField,
                accountNumberField,
                uploadBtn,
                imageLabel,
                signUpBtn,
                loginLink,
                goToDashboardBtn,
                adminDashboardBtn,
                messageLabel
        );

        Scene scene = new Scene(root, 400, 800);
        stage.setTitle("Sign Up");
        stage.setScene(scene);
        stage.show();
    }

    private void handleSignUp(
            String fullName,
            String phone,
            String password,
            String role,
            String address,
            String email,
            String bankName,
            String accountNumber,
            String imageBase64,
            Label messageLabel,
            Button goToDashboardBtn
    ) {
        if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty()) {
            messageLabel.setText("لطفاً همهٔ فیلدهای اجباری را پر کنید.");
            return;
        }

        UserDTO user = new UserDTO(
                fullName, phone, password, role, address,
                email.isEmpty()        ? null : email,
                bankName.isEmpty()     ? null : bankName,
                accountNumber.isEmpty()? null : accountNumber
        );
        user.setProfileImageBase64(imageBase64);

        CompletableFuture<Map<String, Object>> future = authController.signUp(user);
        future.thenAccept(resp -> Platform.runLater(() -> {
            if (resp == null) {
                messageLabel.setText("پاسخی از سرور دریافت نشد.");
                return;
            }

            int status       = ((Number) resp.getOrDefault("status", 0)).intValue();
            String msg       = (String) resp.getOrDefault("message", "");
            boolean approved = Boolean.TRUE.equals(resp.get("approved"));
            token            = (String) resp.getOrDefault("token", "");

            if (status < 200 || status >= 300) {
                messageLabel.setText("خطا: " + msg);
                return;
            }

            if (!approved) {
                messageLabel.setText("ثبت‌نام موفق بود. منتظر تأیید ادمین باشید.");
            } else {
                messageLabel.setText("ثبت‌نام و تأیید موفق! اکنون روی Go to Dashboard کلیک کنید.");
                goToDashboardBtn.setDisable(false);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() ->
                    messageLabel.setText("خطا در ارتباط: " + ex.getMessage())
            );
            return null;
        });
    }

    private void handleGoToDashboard(Label messageLabel) {
        new Thread(() -> {
            ServiceResult result = authController.getStatus(token);
            Platform.runLater(() -> {
                if (result instanceof StatusResult && ((StatusResult) result).isApproved()) {
                    new DashboardView(stage, token);
                } else {
                    messageLabel.setText("هنوز توسط ادمین تأیید نشده‌اید.");
                }
            });
        }).start();
    }
}