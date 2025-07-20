package view;

import controller.UpdateOrderStatusController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UpdateOrderStatusView extends Application {

    private final UpdateOrderStatusController controller = new UpdateOrderStatusController();

    private final ObservableList<UpdateOrderStatusController.RestaurantEntry> restaurants =
            FXCollections.observableArrayList();

    private final ObservableList<UpdateOrderStatusController.OrderEntry> orders =
            FXCollections.observableArrayList();

    private final ObservableList<String> statuses = FXCollections.observableArrayList(
            "submitted", "unpaid and cancelled", "waiting vendor",
            "cancelled", "finding courier", "on the way", "completed"
    );

    @Override
    public void start(Stage stage) {
        Label title = new Label("Change Order Status");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        ComboBox<UpdateOrderStatusController.RestaurantEntry> restaurantCombo =
                new ComboBox<>(restaurants);
        restaurantCombo.setPromptText("Select Restaurant");
        restaurantCombo.setPrefWidth(240);

        ComboBox<UpdateOrderStatusController.OrderEntry> orderCombo =
                new ComboBox<>(orders);
        orderCombo.setPromptText("Select Order");
        orderCombo.setPrefWidth(240);

        ComboBox<String> statusCombo = new ComboBox<>(statuses);
        statusCombo.setPromptText("Select New Status");
        statusCombo.setPrefWidth(240);

        Button confirmBtn = new Button("Confirm Update");
        confirmBtn.setPrefWidth(240);
        confirmBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(240);
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(20, title, restaurantCombo, orderCombo, statusCombo, confirmBtn, backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Restaurant – Update Order Status");
        stage.setScene(new Scene(root, 460, 400));
        stage.show();

        // 1. بارگذاری رستوران‌ها
        controller.loadRestaurants()
                .thenAccept(list -> Platform.runLater(() -> restaurants.setAll(list)));

        // 2. وقتی رستوران انتخاب شد سفارش‌ها رو لود کن
        restaurantCombo.valueProperty().addListener((obs, oldV, sel) -> {
            orders.clear();
            statusCombo.getSelectionModel().clearSelection();
            confirmBtn.setDisable(true);
            if (sel != null) {
                controller.loadOrdersForRestaurant(sel.id())
                        .thenAccept(l -> Platform.runLater(() -> orders.setAll(l)));
            }
        });

        // 3. فعال/غیرفعال‌سازی دکمه Confirm
        ChangeListener<Object> checker = (ObservableValue<?> obs, Object o, Object n) -> {
            boolean ready = restaurantCombo.getValue() != null
                    && orderCombo.getValue() != null
                    && statusCombo.getValue() != null;
            confirmBtn.setDisable(!ready);
        };
        restaurantCombo.valueProperty().addListener(checker);
        orderCombo.valueProperty().addListener(checker);
        statusCombo.valueProperty().addListener(checker);

        // 4. ارسال تغییر وضعیت
        confirmBtn.setOnAction(e -> {
            int orderId = orderCombo.getValue().id();
            String newStatus = statusCombo.getValue();

            controller.updateStatus(orderId, newStatus)
                    .thenAccept(msg -> Platform.runLater(() -> {
                        Alert a = msg.toLowerCase().contains("error")
                                ? new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK)
                                : new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
                        a.showAndWait();
                        statusCombo.getSelectionModel().clearSelection();
                        confirmBtn.setDisable(true);
                    }));
        });
    }
}