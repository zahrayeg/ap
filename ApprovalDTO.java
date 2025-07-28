// src/main/java/dto/ApprovalDTO.java
package dto;

public class ApprovalDTO {
    private boolean approved;        // true = تایید / false = حذف

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}