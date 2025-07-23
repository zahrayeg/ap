package model;

public class MenuDTO {
    private String title;

    public MenuDTO() { }

    public MenuDTO(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}