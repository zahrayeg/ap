package model;

public class RestaurantDTO {
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int taxFee;
    private int additionalFee;

    public RestaurantDTO() {}

    public RestaurantDTO(String name,
                         String address,
                         String phone,
                         String logoBase64,
                         int taxFee,
                         int additionalFee) {
        this.name           = name;
        this.address        = address;
        this.phone          = phone;
        this.logoBase64     = logoBase64;
        this.taxFee         = taxFee;
        this.additionalFee  = additionalFee;
    }

    // getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }

    public int getTaxFee() { return taxFee; }
    public void setTaxFee(int taxFee) { this.taxFee = taxFee; }

    public int getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(int additionalFee) { this.additionalFee = additionalFee; }
}