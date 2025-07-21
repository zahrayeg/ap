package dto;

import com.google.gson.annotations.SerializedName;

public class UserDTO {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("address")
    private String address;

    @SerializedName("profileImageBase64")
    private String profileImageBase64;

    @SerializedName("bankInfo")
    private BankInfoDTO bankInfo;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public BankInfoDTO getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfoDTO bankInfo) { this.bankInfo = bankInfo; }
    public UserDTO(String fullName, String phone, String email, String password, String role, String address, String profileImageBase64) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;

    }
    public UserDTO(){}
}