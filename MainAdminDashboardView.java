package org.example.demo1.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;

public class MainAdminDashboardView extends Application {

    @Override
    public void start(Stage stage) {
        // Dashboard buttons in a single horizontal row
        Button txDashboardBtn     = new Button("Transactions");
        Button ordersDashboardBtn = new Button("Orders");
        Button usersDashboardBtn  = new Button("Users");

        txDashboardBtn    .setOnAction(e -> {
            try {
                openView(new TransactionDashboardView());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        ordersDashboardBtn.setOnAction(e -> {
            try {
                openView(new OrderDashboardView());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        usersDashboardBtn .setOnAction(e -> {
            try {
                openView(new AdminDashboardView());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox centerBar = new HBox(20, txDashboardBtn, ordersDashboardBtn, usersDashboardBtn);
        centerBar.setAlignment(Pos.CENTER);
        centerBar.setPadding(new Insets(30));

        // Back button at the bottom
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> stage.close());
        HBox bottomBar = new HBox(backBtn);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10));

        // Root pane with orange background
        BorderPane root = new BorderPane();
        root.setCenter(centerBar);
        root.setBottom(bottomBar);
        root.setStyle("-fx-background-color: orange;");

        Scene scene = new Scene(root, 600, 200);
        stage.setTitle("Admin Main Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void openView(Application view) throws Exception {
        Stage newStage = new Stage();
        view.start(newStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}