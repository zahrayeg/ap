package dto;

public class CreateFoodItemResult extends ServiceResult {
    private FoodDTO item;

    public CreateFoodItemResult(int status, String message, FoodDTO item) {
        super(status, message);
        this.item = item;
    }

    public FoodDTO getItem() {
        return item;
    }
}