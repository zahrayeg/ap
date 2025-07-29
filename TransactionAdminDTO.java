package org.example.demo1.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionAdminDTO {
    private UUID id;
    private String userId;
    private String method;
    private String status;
    private Integer amount;
    private LocalDateTime createdAt;

    public TransactionAdminDTO() {}

    public TransactionAdminDTO(UUID id,
                               String userId,
                               String method,
                               String status,
                               Integer amount,
                               LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.method = method;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}