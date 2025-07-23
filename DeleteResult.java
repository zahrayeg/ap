package dto;

public class DeleteResult extends ServiceResult {
    private String message;

    public DeleteResult(int status, String message) {
        super(status, message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}