package dto;

public class CreateMenuResult extends ServiceResult {
    private MenuDTO menu;

    public CreateMenuResult(int status, String message, MenuDTO menu) {
        super(status, message);
        this.menu = menu;
    }

    public MenuDTO getMenu() {
        return menu;
    }
}