package Entity;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="foods")
public class Food {
    @Id
    private UUID id = UUID.randomUUID();
    private String name;
    private double price;
    private int quantity;
    private String category;
    @ManyToOne
    @JoinColumn(name="restaurant_id")
    private Restaurant restaurant;
    public Food(){}
    public Food(String name, double price, int quantity, Restaurant restaurant) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.restaurant = restaurant;
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
}