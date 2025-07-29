package org.example.demo1.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.example.demo1.model.AdminUserDTO;
import org.example.demo1.service.AdminService;



import java.util.List;

public class AdminDashboardView extends Application {

    private final AdminService service = new AdminService();
    private final TableView<AdminUserDTO> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        configureTableColumns();
        loadUsers();

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadUsers());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(table);

        Scene scene = new Scene(root, 900, 500);
        primaryStage.setTitle("Admin Dashboard - Manage Users");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configureTableColumns() {
        TableColumn<AdminUserDTO, String> idCol    = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<AdminUserDTO, String> nameCol  = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<AdminUserDTO, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<AdminUserDTO, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<AdminUserDTO, String> roleCol  = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<AdminUserDTO, Boolean> approvedCol = new TableColumn<>("Approved");
        approvedCol.setCellValueFactory(new PropertyValueFactory<>("approved"));

        TableColumn<AdminUserDTO, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button deleteBtn  = new Button("Delete");
            private final HBox container     = new HBox(5, approveBtn, deleteBtn);

            {
                container.setPadding(new Insets(0, 10, 0, 0));
                approveBtn.setOnAction(evt -> {
                    AdminUserDTO user = getTableView().getItems().get(getIndex());
                    approveUser(user);
                });
                deleteBtn.setOnAction(evt -> {
                    AdminUserDTO user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AdminUserDTO user = getTableView().getItems().get(getIndex());
                    boolean approved = user.isApproved();

                    approveBtn.setDisable(approved);
                    deleteBtn.setDisable(approved);
                    setGraphic(container);
                }
            }
        });

        table.getColumns().addAll(
                idCol, nameCol, phoneCol, emailCol, roleCol, approvedCol, actionCol
        );

        // جلوگیری از جابه‌جایی ستون‌ها
        table.getColumns().forEach(c -> c.setReorderable(false));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadUsers() {
        service.listUsers()
                .thenAccept(this::updateTable)
                .exceptionally(ex -> {
                    showError("Failed to load users: " + ex.getMessage());
                    return null;
                });
    }

    private void updateTable(List<AdminUserDTO> users) {
        Platform.runLater(() -> table.getItems().setAll(users));
    }

    private void approveUser(AdminUserDTO user) {
        service.approveUser(user.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    user.setApproved(true);
                    table.refresh();
                }))
                .exceptionally(ex -> {
                    showError("Approve failed: " + ex.getMessage());
                    return null;
                });
    }

    private void deleteUser(AdminUserDTO user) {
        service.deleteUser(user.getId())
                .thenRun(() -> Platform.runLater(() ->
                        table.getItems().remove(user)
                ))
                .exceptionally(ex -> {
                    showError("Delete failed: " + ex.getMessage());
                    return null;
                });
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