package org.example.demo1.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.demo1.model.TransactionAdminDTO;
import org.example.demo1.service.TransactionService;

import java.util.List;

public class TransactionDashboardView extends Application {

    private final TransactionService service = new TransactionService();
    private final TableView<TransactionAdminDTO> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        configureTableColumns();
        loadTransactions();

        Button backBtn    = new Button("Back");
        Button refreshBtn = new Button("Refresh");

        backBtn   .setOnAction(e -> stage.close());
        refreshBtn.setOnAction(e -> loadTransactions());

        HBox topBar = new HBox(10, backBtn, refreshBtn);
        topBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(table);

        stage.setTitle("Admin Dashboard – Transactions");
        stage.setScene(new Scene(root, 800, 400));
        stage.show();
    }

    private void configureTableColumns() {
        TableColumn<TransactionAdminDTO, String> idCol     = new TableColumn<>("ID");
        idCol   .setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<TransactionAdminDTO, String> orderCol  = new TableColumn<>("Order ID");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<TransactionAdminDTO, String> userCol   = new TableColumn<>("User ID");
        userCol .setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<TransactionAdminDTO, String> typeCol   = new TableColumn<>("Type");
        typeCol .setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<TransactionAdminDTO, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(new PropertyValueFactory<>("method"));

        TableColumn<TransactionAdminDTO, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(
                idCol,
                orderCol,
                userCol,
                typeCol,
                methodCol,
                statusCol
        );

        // Prevent column reordering
        table.getColumns().forEach(c -> c.setReorderable(false));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadTransactions() {
        service.listTransactions()
                .thenAccept(this::updateTable)
                .exceptionally(ex -> {
                    showError("Failed to load transactions: " + ex.getMessage());
                    return null;
                });
    }

    private void updateTable(List<TransactionAdminDTO> txs) {
        Platform.runLater(() -> table.getItems().setAll(txs));
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setHeaderText("Error");
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}