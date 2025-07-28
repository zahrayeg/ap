package dto;

import java.util.UUID;

public class TransactionAdminDTO {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private String type;
    private String method;
    private String status;

    public UUID getId()              { return id; }
    public void setId(UUID id)       { this.id = id; }

    public UUID getOrderId()         { return orderId; }
    public void setOrderId(UUID oid) { this.orderId = oid; }

    public UUID getUserId()          { return userId; }
    public void setUserId(UUID uid)  { this.userId = uid; }

    public String getType()          { return type; }
    public void setType(String t)    { this.type = t; }

    public String getMethod()        { return method; }
    public void setMethod(String m)  { this.method = m; }

    public String getStatus()        { return status; }
    public void setStatus(String s)  { this.status = s; }
}