package dto;

import java.util.List;

public class UsersListResult extends ServiceResult {
    private final List<UserAdminDTO> users;

    public UsersListResult(int code, String message, List<UserAdminDTO> users) {
        super(code, message);
        this.users = users;
    }

    public List<UserAdminDTO> getUsers() {
        return users;
    }
}