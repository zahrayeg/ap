package view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.RestaurantService;

import java.util.Map;

public class MyRestaurantsTableView extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("My Restaurants");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button backBtn = new Button("⬅️ Back");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1e3a5f; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close()); // بعدها به صفحه قبل هدایت می‌کنی

        HBox header = new HBox(10, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        TableView<Map<String, Object>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No restaurants found"));

        table.getColumns().addAll(
                createColumn("ID", "id", 60),
                createColumn("Name", "name", 160),
                createColumn("Address", "address", 200),
                createColumn("Phone", "phone", 120),
                createColumn("Tax Fee", "tax_fee", 100),
                createColumn("Additional Fee", "additional_fee", 120)
        );

        for (TableColumn<?, ?> col : table.getColumns()) {
            col.setReorderable(false);
        }

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: yellow;");

        VBox root = new VBox(15, header, table, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color:#1e3a5f;");

        stage.setScene(new Scene(root, 820, 500));
        stage.setTitle("My Restaurants");
        stage.show();

        RestaurantService service = new RestaurantService();
        service.getSellerRestaurants("no-token").thenAccept(list -> {
            ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList(list);
            table.setItems(rows);
            statusLabel.setText("Total: " + rows.size());
        });
    }

    private TableColumn<Map<String, Object>, String> createColumn(String title, String key, int width) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(title);
        col.setMinWidth(width);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getOrDefault(key, "").toString()
                )
        );
        return col;
    }

    public static void main(String[] args) {
        launch();
    }
}