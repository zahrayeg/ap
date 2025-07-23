package entity;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="order_items")
public class OrderItem {
    @Id
    private UUID id = UUID.randomUUID();
    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;
    @ManyToOne
    @JoinColumn(name="food_id")
    private Food food;
    private int orderedItemQuantity;
    private int unitPrice;
    private int quantity;
    @ManyToOne
    @JoinColumn(name="restaurant_id")
    private Restaurant restaurant;
    public OrderItem() {}
    public OrderItem(Order order, Food food, int orderedItemQuantity, int unitPrice, Restaurant restaurant,int quantity) {
        this.order = order;
        this.food = food;
        this.orderedItemQuantity = orderedItemQuantity;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public Order getOrder() {
        return order;
    }
    public void setOrder(Order order) {
        this.order = order;
    }
    public Food getFood() {
        return food;
    }
    public void setFood(Food food) {
        this.food = food;
    }
    public int getOrderedItemQuantity() {
        return orderedItemQuantity;
    }
    public void setOrderedItemQuantity(int orderedItemQuantity) {
        this.orderedItemQuantity = orderedItemQuantity;
    }
    public int getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }
    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}