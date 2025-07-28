package dto;

import java.util.UUID;

public class UserAdminDTO {
    private UUID       id;
    private String     fullName;
    private String     phone;
    private String     email;
    private String     role;
    private String     address;
    private String     profileImageBase64;
    private BankInfoDTO bankInfo;

    public UUID getId()                       { return id; }
    public void setId(UUID id)               { this.id = id; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String fullName){ this.fullName = fullName; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String phone)     { this.phone = phone; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    public String getRole()                  { return role; }
    public void   setRole(String role)       { this.role = role; }

    public String getAddress()               { return address; }
    public void   setAddress(String address) { this.address = address; }

    public String getProfileImageBase64()                 { return profileImageBase64; }
    public void   setProfileImageBase64(String base64)    { this.profileImageBase64 = base64; }

    public BankInfoDTO getBankInfo()         { return bankInfo; }
    public void       setBankInfo(BankInfoDTO bankInfo){ this.bankInfo = bankInfo; }
}