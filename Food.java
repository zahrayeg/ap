package entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @Lob
    private String imageBase64;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int supply;

    @ElementCollection
    @CollectionTable(name = "food_keywords", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "keyword", nullable = false)
    private List<String> keywords = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public Food() {}

    public Food(String name, String imageBase64, String description,
                int price, int supply, List<String> keywords, Restaurant restaurant) {
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
        this.restaurant = restaurant;
    }

    // --- Getter & Setter methods ---
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

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}