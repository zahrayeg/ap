package service;

import DAO.OrderDAO;
import DAO.UserDAO;
import DAO.TransactionDAO;
import dto.*;
import entity.User;
import entity.Order;
import entity.Transaction;

import java.util.*;
import java.util.stream.Collectors;

import dto.AdminUserDTO;
import dto.BankInfoDTO;
import dto.DataResult;
import dto.ServiceResult;
import java.util.*;
import java.util.UUID;

public class AdminService {
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * GET /admin/users
     * برگرداندن لیست کاربران با فیلدهای profileImageBase64، bankInfo، approved
     */
    public ServiceResult listUsers() {
        List<User> users = userDAO.findAll();
        List<AdminUserDTO> dtos = users.stream()
                .map(u -> {
                    AdminUserDTO dto = new AdminUserDTO();
                    dto.setId(u.getId().toString());
                    dto.setFullName(u.getFullName());
                    dto.setPhone(u.getPhone());
                    dto.setEmail(u.getEmail());
                    dto.setRole(u.getRole());
                    dto.setAddress(u.getAddress());
                    dto.setProfileImageBase64(u.getProfileImageBase64());

                    if (u.getBankInfo() != null) {
                        dto.setBankInfo(new BankInfoDTO(
                                u.getBankInfo().getBankName(),
                                u.getBankInfo().getAccountNumber()
                        ));
                    }

                    dto.setApproved(u.isApproved());
                    return dto;
                })
                .collect(Collectors.toList());

        return new DataResult<>(200, dtos);
    }

    /**
     * DELETE /admin/users/{id}
     */
    public ServiceResult deleteUser(String userIdStr) {
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException ex) {
            return new ServiceResult(400, "Invalid `id`");
        }

        Optional<User> opt = userDAO.findById(userId);
        if (opt.isEmpty()) {
            return new ServiceResult(404, "User not found");
        }

        User user = opt.get();
        if (user.isApproved()) {
            return new ServiceResult(409, "Cannot delete an approved user");
        }

        try {
            userDAO.delete(user);
            return new ServiceResult(200, "User deleted successfully");
        } catch (Exception ex) {
            return new ServiceResult(500,
                    "Internal server error: failed to delete user (" + ex.getMessage() + ")");
        }
    }

    /**
     * PATCH /admin/users/{id}/status?approved={true|false}
     */
    public ServiceResult updateUserStatus(String userIdStr, boolean approved) {
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException ex) {
            return new ServiceResult(400, "Invalid `id`");
        }

        Optional<User> opt = userDAO.findById(userId);
        if (opt.isEmpty()) {
            return new ServiceResult(404, "User not found");
        }

        User user = opt.get();
        if (user.isApproved() == approved) {
            return new ServiceResult(409, "Status unchanged");
        }

        user.setApproved(approved);
        try {
            userDAO.update(user);
            return new ServiceResult(200, "User approval status updated successfully");
        } catch (Exception ex) {
            return new ServiceResult(500,
                    "Internal server error: failed to update status (" + ex.getMessage() + ")");
        }
    }

    /**
     * سرویس سفارشات ادمین
     */
    public class AdminOrderService {
        public List<OrderAdminDTO> getAllOrders(
                String search,
                UUID vendorId,
                UUID courierId,
                UUID customerId,
                String status
        ) {
            List<Order> orders = orderDAO.findAllWithFilters(
                    vendorId, courierId, customerId, status, search
            );

            return orders.stream().map(o -> {
                OrderAdminDTO dto = new OrderAdminDTO();

                // فیلدهای پایه
                dto.setId(o.getId());
                dto.setDeliveryAddress(o.getDeliveryAddress());
                dto.setCreatedAt(o.getCreatedAt());
                dto.setStatus(o.getStatus());

                // نام رستوران
                dto.setRestaurantName(o.getRestaurant().getName());

                // لیست آیتم‌ها
                List<OrderItemDTO> items = o.getOrderItems().stream()
                        .map(item -> {
                            OrderItemDTO it = new OrderItemDTO();
                            it.setFoodId(item.getFood().getId());
                            it.setQuantity(item.getQuantity());
                            it.setRestaurantId(o.getRestaurant().getId());
                            it.setName(item.getFood().getName());
                            it.setPrice(item.getFood().getPrice());
                            return it;
                        })
                        .collect(Collectors.toList());
                dto.setItems(items);

                // وضعیت تحویل و مبلغ نهایی
                dto.setDeliveryStatus(o.getDeliveryStatus());
                dto.setPayPrice(o.getPayPrice());

                return dto;
            }).collect(Collectors.toList());
        }
    }



    /**
     * GET /admin/transactions
     */
    public ServiceResult listTransactions(
            String search,
            String userIdStr,
            String method,
            String status
    ) {
        // 1. تبدیل userIdStr به UUID (یکبار تعریف، متغیر نهایی برای استفاده در لامبدا)
        final UUID filterUserId;
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                filterUserId = UUID.fromString(userIdStr.trim());
            } catch (IllegalArgumentException ex) {
                return new ServiceResult(400, "Invalid `user`");
            }
        } else {
            filterUserId = null;
        }

        // 2. نرمال‌سازی سایر فیلترها
        String q = (search != null && !search.isBlank())
                ? search.trim().toLowerCase()
                : null;
        String m = (method != null && !method.isBlank())
                ? method.trim().toLowerCase()
                : null;
        String s = (status != null && !status.isBlank())
                ? status.trim().toLowerCase()
                : null;
        List<Transaction> txs = transactionDAO.findByType(q);
        if (filterUserId != null) {
            txs = txs.stream()
                    .filter(t -> t.getUser() != null
                            && filterUserId.equals(t.getUser().getId()))
                    .collect(Collectors.toList());
        }

        if (m != null) {
            txs = txs.stream()
                    .filter(t -> t.getMethod().equalsIgnoreCase(m))
                    .collect(Collectors.toList());
        }
        if (s != null) {
            txs = txs.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(s))
                    .collect(Collectors.toList());
        }
        List<TransactionAdminDTO> dtos = txs.stream()
                .map(t -> {
                    TransactionAdminDTO dto = new TransactionAdminDTO();
                    dto.setId(t.getId());
                    dto.setOrderId(t.getOrder() != null ? t.getOrder().getId() : null);
                    dto.setUserId(t.getUser()  != null ? t.getUser().getId()  : null);
                    dto.setMethod(t.getMethod());
                    dto.setStatus(t.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());

        // 8. بازگشت نتیجه
        return new DataResult<>(200, dtos);
    }
}