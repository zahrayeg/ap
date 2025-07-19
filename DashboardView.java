package view;

import controller.DashboardController;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardView extends Application {

    @Override
    public void start(Stage stage) {
        DashboardController controller = new DashboardController(stage);

        Label title = new Label("Seller Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Restaurant Section
        VBox restaurantBox = createSection("Restaurant",
                createButton("My Restaurants",   true,  controller::openViewRestaurants),
                createButton("Create Restaurant",true,  controller::openAddRestaurant),
                createButton("Update Restaurant",true,  controller::openEditRestaurant)
        );

        // Food Items Section
        VBox foodBox = createSection("Food Items",
                createButton("Add Food Item",     true,  controller::openAddFoodItem),
                createButton("Edit Food Item",    true,  controller::openEditFoodItem),
                createButton("Delete Food Item",  true,  controller::openDeleteFoodItemPage)
        );

        // Menus Section (placeholders)
        VBox menuBox = createSection("Menus",
                createButton("Add Restaurant Menu",        false),
                createButton("Delete Restaurant Menu",     false),
                createButton("Add Item to Restaurant Menu",false),
                createButton("Delete Item from Rest.Menu", false)
        );

        // Orders Section (placeholders)
        VBox orderBox = createSection("Orders",
                createButton("View Restaurant Orders",  false),
                createButton("Change Order Status",     false)
        );

        HBox sections = new HBox(25, restaurantBox, foodBox, menuBox, orderBox);
        sections.setAlignment(Pos.TOP_CENTER);

        VBox root = new VBox(20, title, sections);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #1e3a5f;");

        stage.setTitle("Dashboard");
        stage.setScene(new Scene(root, 1000, 420));
        stage.show();
    }

    private VBox createSection(String header, Button... buttons) {
        Label lbl = new Label(header);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold;");
        VBox box = new VBox(8, lbl);
        box.getChildren().addAll(buttons);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    private Button createButton(String text, boolean enabled) {
        Button btn = new Button(text);
        btn.setDisable(!enabled);
        btn.setPrefWidth(150);
        btn.setStyle("-fx-font-size: 11px;");
        return btn;
    }

    private Button createButton(String text, boolean enabled, Runnable action) {
        Button btn = createButton(text, enabled);
        if (enabled) btn.setOnAction(e -> action.run());
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}