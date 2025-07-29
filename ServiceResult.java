// File: src/main/java/org/example/demo1/dto/ServiceResult.java
package org.example.demo1.model;

public class ServiceResult {
    private int status;
    private String message;

    public ServiceResult() { }

    public ServiceResult(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}