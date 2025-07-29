// File: src/main/java/org/example/demo1/dto/StatusResult.java
package org.example.demo1.model;

public class StatusResult extends ServiceResult {
    private boolean approved;

    public StatusResult() { }

    public StatusResult(int status, String message, boolean approved) {
        super(status, message);
        this.approved = approved;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}