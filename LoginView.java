package org.example.demo1.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo1.model.UserDTO;
import org.example.demo1.service.UserService;

import java.io.IOException;
import java.util.Map;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.example.demo1.controller.AuthController;
import org.example.demo1.model.ServiceResult;
import org.example.demo1.model.StatusResult;
public class LoginView {
    private final AuthController authController = new AuthController();
    private final Stage stage;
    private String token;
    private Button dashboardButton;
    private Label messageLabel;

    public LoginView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(100));
        vbox.setStyle("-fx-background-color: #4CAF50;");

        Label phoneLabel     = new Label("Phone:");
        TextField phoneField = new TextField();

        Label passwordLabel    = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white;");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white;");

        // دکمه‌ی داشبورد، تا وقتی seller لاگین نکرده غیرفعال است
        dashboardButton = new Button("Go to Dashboard");
        dashboardButton.setDisable(true);
        dashboardButton.setOnAction(e -> goToDashboard());

        Hyperlink signUpLink = new Hyperlink("Don't have an account? Sign Up");
        signUpLink.setStyle("-fx-text-fill: white;");
        signUpLink.setOnAction(e -> new SignUpView(stage));

        vbox.getChildren().addAll(
                phoneLabel, phoneField,
                passwordLabel, passField,
                loginButton, signUpLink,
                messageLabel, dashboardButton
        );

        loginButton.setOnAction(e -> {
            String phone    = phoneField.getText().trim();
            String password = passField.getText().trim();

            authController.login(phone, password)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.containsKey("error")) {
                            messageLabel.setText("Error: " + response.get("error"));
                            return;
                        }

                        int status = (int) response.getOrDefault("status", 500);
                        String msg  = (String) response.getOrDefault("message", "Unknown error");
                        if (status == 200) {
                            token = (String) response.get("token");
                            String role = ((String) ((Map<?, ?>) response.get("user")).get("role")).toLowerCase();
                            messageLabel.setText("Login success! Role: " + role);

                            if ("seller".equals(role)) {
                                dashboardButton.setDisable(false);
                            } else {
                                switch (role) {
                                    case "buyer":
                                        try {
                                            new MainView(stage, token);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                        break;
                                    case "courier":
                                        try {
                                            new CourierMainView(stage, token);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                        break;
                                }
                            }
                        } else {
                            messageLabel.setText(msg);
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() ->
                                messageLabel.setText("Exception: " + ex.getMessage())
                        );
                        return null;
                    });
        });

        Scene scene = new Scene(vbox, 900, 900);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    private void goToDashboard() {
        new Thread(() -> {
            ServiceResult result = authController.getStatus(token);
            Platform.runLater(() -> {
                if (result instanceof StatusResult sr && sr.isApproved()) {
                    new DashboardView(stage, token);
                } else {
                    messageLabel.setText("Your account is not approved yet.");
                }
            });
        }).start();
    }
}