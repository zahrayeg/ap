package org.example.demo1.controller;

import org.example.demo1.model.ServiceResult;
import org.example.demo1.model.StatusResult;
import org.example.demo1.model.UserDTO;
import org.example.demo1.service.UserService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.example.demo1.service.UserService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthController {
    private final UserService userService = new UserService();

    public CompletableFuture<Map<String, Object>> signUp(UserDTO user) {
        return userService.signUp(user);
    }

    public CompletableFuture<Map<String, Object>> login(String phone, String password) {
        return userService.login(phone, password);
    }

    /**
     * متدی که در ویو به صورت sync استفاده می‌شود:
     * ServiceResult result = authController.getStatus(token);
     */
    public ServiceResult getStatus(String token) {
        userService.setToken(token);

        try {
            StatusResult sr = userService.getStatus().join();

            return sr;
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            String msg = (cause != null) ? cause.getMessage() : ex.getMessage();
            return new ServiceResult(500, "Internal error: " + msg);
        }
    }

}
