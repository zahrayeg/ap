// src/main/java/service/UserService.java
package service;

import DAO.UserDAO;
import Entity.User;
import exception.ForbiddenException;
import exception.NotFoundException;
import exception.UnauthorizedException;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     *  احراز هویت و بررسی نقش SELLER
     *  @throws UnauthorizedException  وقتی کاربر پیدا نشد
     *  @throws ForbiddenException     وقتی نقش SELLER نبود
     */
    public User validateSeller(int sellerId) {
        User user = userDAO.findById(sellerId);
        if (user == null) {
            throw new UnauthorizedException("Unauthorized request");
        }
        if (!"SELLER".equals(user.getRole())) {
            throw new ForbiddenException("Forbidden request");
        }
        return user;
    }

    /**
     * بارگذاری کاربر بر اساس شناسه
     * @throws NotFoundException وقتی کاربر پیدا نشد
     */
    public User findById(int id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new NotFoundException("Resource not found");
        }
        return user;
    }
}