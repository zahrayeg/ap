package dto;

import java.util.List;

public class FoodDTO {
    private String id;
    private String name;
    private String imageBase64;
    private String description;
    private int price;
    private Integer supply;
    private List<String> keywords;
    private String vendorId;  // UUID رستوران

    // getters و setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
}