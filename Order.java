package entity;
import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name="orders")
public class Order {
    @Id
    private UUID id = UUID.randomUUID();
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name="deliveryMan_id")
    private Courier deliveryMan;
    @Column(name = "vendor_id")
    private UUID vendorId;
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    private LocalDateTime orderedDateTime;
    private String deliveryAddress;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems=new ArrayList<OrderItem>();
    private int totalPrice;
    @Column(nullable = false)
    private String status;
    public Order() {
        status="Pending";
    }
    public Order(User customer, Restaurant restaurant, Courier deliveryMan,String deliveryAddress) {
        this.user = customer;
        this.deliveryMan = deliveryMan;
        this.orderedDateTime = LocalDateTime.now();
        this.deliveryAddress = deliveryAddress;
        this.restaurant = restaurant;
        status="Pending";
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public User getCustomer() {
        return user;
    }
    public void setCustomer(User customer) {
        this.user = customer;
    }
    public Courier getDeliveryMan() {
        return deliveryMan;
    }
    public void setDeliveryMan(Courier deliveryMan) {
        this.deliveryMan = deliveryMan;
    }
    public LocalDateTime getOrderedDateTime() {
        return orderedDateTime;
    }
    public void setOrderedDateTime(LocalDateTime orderedDateTime) {
        this.orderedDateTime = orderedDateTime;
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    public int getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress.trim();
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public UUID getVendorId() {
        return vendorId;
    }
    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }
}