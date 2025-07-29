package org.example.demo1.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.demo1.model.OrderAdminDTO;
import org.example.demo1.service.OrderService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class OrderDashboardView extends Application {
    private final OrderService service = new OrderService();
    private final TableView<OrderAdminDTO> table = new TableView<>();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void start(Stage primaryStage) {
        configureTableColumns();
        loadOrders();

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadOrders());

        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(table);

        primaryStage.setTitle("Admin Dashboard – Orders");
        primaryStage.setScene(new Scene(root, 1100, 500));
        primaryStage.show();
    }

    private void configureTableColumns() {
        TableColumn<OrderAdminDTO, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<OrderAdminDTO, String> addrCol = new TableColumn<>("Delivery Address");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));

        TableColumn<OrderAdminDTO, String> restCol = new TableColumn<>("Restaurant");
        restCol.setCellValueFactory(new PropertyValueFactory<>("restaurantName"));

        TableColumn<OrderAdminDTO, Integer> itemsCol = new TableColumn<>("Item Count");
        itemsCol.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getItems().size())
        );

        TableColumn<OrderAdminDTO, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getCreatedAt().format(DF))
        );

        TableColumn<OrderAdminDTO, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<OrderAdminDTO, String> delivCol = new TableColumn<>("Delivery Status");
        delivCol.setCellValueFactory(new PropertyValueFactory<>("deliveryStatus"));

        TableColumn<OrderAdminDTO, Integer> priceCol = new TableColumn<>("Pay Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("payPrice"));

        table.getColumns().addAll(
                idCol,
                addrCol,
                restCol,
                itemsCol,
                dateCol,
                statusCol,
                delivCol,
                priceCol
        );

        // Prevent columns from being reordered by the user
        table.getColumns().forEach(col -> col.setReorderable(false));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadOrders() {
        service.listOrders(Collections.emptyMap())
                .thenAccept(this::updateTable)
                .exceptionally(ex -> {
                    showError("Failed to load orders: " + ex.getMessage());
                    return null;
                });
    }

    private void updateTable(List<OrderAdminDTO> orders) {
        Platform.runLater(() -> table.getItems().setAll(orders));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setHeaderText("Error");
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}