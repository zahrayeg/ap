package org.example.demo1.view;
import org.example.demo1.controller.MainViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class MainView {
    private final Stage stage;
    private final String token;

    public MainView(Stage stage, String token) throws IOException {
        this.stage = stage;
        this.token = token;
        setupUI();
    }

    private void setupUI() throws IOException {
        System.out.println("Loading FXML from: " + getClass().getResource("/org/example/demo1/mainView.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/mainView.fxml"));
        // تنظیم دستی کنترلر
        MainViewController controller = new MainViewController();
        loader.setController(controller);
        Parent root = loader.load();
        controller.setStage(stage);
        controller.setToken(token); // انتقال توکن
        stage.setScene(new Scene(root));
        stage.setTitle("Main View");
        stage.show();
    }
}