package model;

public class RestaurantDTO {
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int tax_fee;
    private int additional_fee;

    public RestaurantDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public int getTax_fee() {
        return tax_fee;
    }

    public void setTax_fee(int tax_fee) {
        this.tax_fee = tax_fee;
    }

    public int getAdditional_fee() {
        return additional_fee;
    }

    public void setAdditional_fee(int additional_fee) {
        this.additional_fee = additional_fee;
    }
}