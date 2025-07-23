package dto;

public class UpdateFoodItemResult extends ServiceResult {
    private FoodDTO item;

    public UpdateFoodItemResult(int status, String message, FoodDTO item) {
        super(status, message);
        this.item = item;
    }

    public FoodDTO getItem() {
        return item;
    }
}