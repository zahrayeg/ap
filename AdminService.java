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

public class AdminService {
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * GET /admin/users
     */
    public ServiceResult listUsers() {
        List<User> users = userDAO.findAll();
        List<UserAdminDTO> dtos = users.stream()
                .map(u -> {
                    UserAdminDTO dto = new UserAdminDTO();
                    dto.setId              (u.getId());
                    dto.setFullName        (u.getFullName());
                    dto.setPhone           (u.getPhone());
                    dto.setEmail           (u.getEmail());
                    dto.setRole            (u.getRole());
                    dto.setAddress         (u.getAddress());
                    dto.setProfileImageBase64(u.getProfileImageBase64());

                    if (u.getBankInfo() != null) {
                        dto.setBankInfo(new BankInfoDTO(
                                u.getBankInfo().getBankName(),
                                u.getBankInfo().getAccountNumber()
                        ));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new DataResult<>(200, dtos);
    }


    public ServiceResult deleteUser(String userIdStr) {
        // parse UUID
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
            return new ServiceResult(409, "Cannot delete a user that has been approved");
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
     * PATCH /admin/users/{id}/status
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
            return new ServiceResult(404, "Resource not found: user does not exist");
        }

        User user = opt.get();
        if (user.isApproved() == approved) {
            return new ServiceResult(409, "Conflict: status unchanged");
        }

        user.setApproved(approved);
        try {
            userDAO.update(user);
            return new ServiceResult(200, "User approval status updated successfully");
        } catch (Exception ex) {
            return new ServiceResult(500, "Internal server error: failed to update user");
        }
    }

    /**
     * GET /admin/orders
     */
    public class AdminOrderService {
        private final OrderDAO orderDAO = new OrderDAO();

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

            return orders.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        private OrderAdminDTO mapToDto(Order o) {
            OrderAdminDTO dto = new OrderAdminDTO();
            dto.setId(o.getId());
            dto.setDeliveryAddress(o.getDeliveryAddress());
            dto.setCustomerId(o.getCustomer().getId());
            dto.setVendorId(o.getRestaurant().getId());
            dto.setItemIds(o.getOrderItems()
                    .stream()
                    .map(item -> item.getFood().getId())  // یا getItemId()
                    .collect(Collectors.toList()));
            dto.setRawPrice(o.getRawPrice());
            dto.setTaxFee(o.getTaxFee());
            dto.setCourierFee(o.getCourierFee());
            dto.setPayPrice(o.getPayPrice());
            dto.setCourierId(o.getCourierId());
            dto.setStatus(o.getStatus());
            dto.setCreatedAt(o.getCreatedAt());
            dto.setUpdatedAt(o.getUpdatedAt());
            return dto;
        }
    }

    /*
     * GET /admin/transactions
     */
    public ServiceResult listTransactions(
            String search,
            String userIdStr,
            String method,
            String status
    ) {
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

        final String q = (search != null && !search.isBlank())
                ? search.trim().toLowerCase()
                : null;
        final String m = (method != null && !method.isBlank())
                ? method.trim().toLowerCase()
                : null;
        final String s = (status != null && !status.isBlank())
                ? status.trim().toLowerCase()
                : null;

        List<Transaction> txs = transactionDAO.findByType(q);

        if (filterUserId != null) {
            txs = txs.stream()
                    .filter(t -> t.getUser().getId().equals(filterUserId))
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
                    dto.setId     (t.getId());
                    dto.setOrderId(t.getOrder()  != null ? t.getOrder().getId() : null);
                    dto.setUserId (t.getUser()   != null ? t.getUser().getId() : null);
                    dto.setMethod (t.getMethod());
                    dto.setStatus (t.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());

        return new DataResult<>(200, dtos);
    }


}
