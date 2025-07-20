package view;

import controller.RestaurantOrdersController;
import controller.RestaurantOrdersController.OrderEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestaurantOrdersView extends Application {

    private final RestaurantOrdersController controller = new RestaurantOrdersController();
    private final ObservableList<OrderEntry> orders = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Restaurant Orders");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // فیلدهای فیلتر
        TextField restaurantIdField = new TextField();
        restaurantIdField.setPromptText("Restaurant ID");
        restaurantIdField.setPrefWidth(80);

        ComboBox<String> statusField = new ComboBox<>(FXCollections.observableArrayList(
                "submitted", "unpaid and cancelled", "waiting vendor",
                "cancelled", "finding courier", "on the way", "completed"
        ));
        statusField.setPromptText("Status");

        TextField searchField = new TextField();  searchField.setPromptText("Search");
        TextField userField   = new TextField();  userField.setPromptText("User");
        TextField courierField = new TextField(); courierField.setPromptText("Courier");

        Button searchBtn = new Button("Search Orders");
        searchBtn.setDisable(true);

        // دسته‌بندی فیلترها
        HBox filters = new HBox(10,
                restaurantIdField, statusField,
                searchField, userField, courierField, searchBtn
        );
        filters.setAlignment(Pos.CENTER);

        // جدول سفارشات
        TableView<OrderEntry> table = new TableView<>(orders);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("id", "ID");
        columns.put("deliveryAddress", "Address");
        columns.put("customerId", "Customer");
        columns.put("vendorId", "Vendor");
        columns.put("couponId", "Coupon");
        columns.put("itemIds", "Items");
        columns.put("rawPrice", "Raw");
        columns.put("taxFee", "Tax");
        columns.put("additionalFee", "Add Fee");
        columns.put("courierFee", "Courier Fee");
        columns.put("payPrice", "Pay");
        columns.put("courierId", "Courier ID");
        columns.put("status", "Status");
        columns.put("createdAt", "Created");
        columns.put("updatedAt", "Updated");
        for (var colEntry : columns.entrySet()) {
            String field = colEntry.getKey();
            String header = colEntry.getValue();

            TableColumn<OrderEntry, String> col = new TableColumn<>(header);
            col.setCellValueFactory(data -> {
                OrderEntry o = data.getValue();
                String val = switch (field) {
                    case "id" -> String.valueOf(o.id());
                    case "deliveryAddress" -> o.deliveryAddress();
                    case "customerId" -> String.valueOf(o.customerId());
                    case "vendorId" -> String.valueOf(o.vendorId());
                    case "couponId" -> String.valueOf(o.couponId());
                    case "itemIds" -> o.itemIds();
                    case "rawPrice" -> String.valueOf(o.rawPrice());
                    case "taxFee" -> String.valueOf(o.taxFee());
                    case "additionalFee" -> String.valueOf(o.additionalFee());
                    case "courierFee" -> String.valueOf(o.courierFee());
                    case "payPrice" -> String.valueOf(o.payPrice());
                    case "courierId" -> String.valueOf(o.courierId());
                    case "status" -> o.status();
                    case "createdAt" -> o.createdAt();
                    case "updatedAt" -> o.updatedAt();
                    default -> "";
                };
                return new ReadOnlyStringWrapper(val);
            });
            col.setReorderable(false);
            table.getColumns().add(col);
        }

        // دکمه بازگشت
        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, filters, table, backBtn);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Restaurant – Orders List");
        stage.setScene(new Scene(root, 1100, 600));
        stage.show();

        // فعال‌سازی دکمه Search
        restaurantIdField.textProperty().addListener((obs, o, n) ->
                searchBtn.setDisable(n.trim().isEmpty())
        );

        // اجرای جستجوی سفارش‌ها
        searchBtn.setOnAction(e -> {
            try {
                int restId = Integer.parseInt(restaurantIdField.getText().trim());
                String st     = statusField.getValue();
                String txt    = searchField.getText().trim();
                String usr    = userField.getText().trim();
                String cour   = courierField.getText().trim();

                controller.loadOrders(restId, st, txt, usr, cour)
                        .thenAccept(list -> Platform.runLater(() -> {
                            orders.setAll(list);
                        }));
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid restaurant ID", ButtonType.OK).showAndWait();
            }
        });
    }
}