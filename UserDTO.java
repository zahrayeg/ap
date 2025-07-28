package dto;

import com.google.gson.annotations.SerializedName;

public class UserDTO {
    @SerializedName("full_name")
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

    @SerializedName("profile_image_base64")
    private String profileImageBase64;

    @SerializedName("bank_info")
    private BankInfoDTO bankInfo;

    /**
     * وضعیت تأیید توسط ادمین.
     * false = منتظر تأیید
     * true  = تأیید شده
     */
    @SerializedName("approved")
    private boolean approved = false;

    public UserDTO() {}

    // --- getters & setters ---

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }

    public BankInfoDTO getBankInfo() {
        return bankInfo;
    }
    public void setBankInfo(BankInfoDTO bankInfo) {
        this.bankInfo = bankInfo;
    }

    public boolean isApproved() {
        return approved;
    }
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}