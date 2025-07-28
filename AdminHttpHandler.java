package httpRequestHandler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import service.AdminService;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class AdminHttpHandler implements HttpHandler {

    private final AdminService adminService
            = new AdminService();
    private final AdminService.AdminOrderService orderService
            = adminService.new AdminOrderService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path   = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/admin/users".equals(path)
                && "GET".equalsIgnoreCase(method)) {
            handleListUsers(exchange);
            return;
        }
        if (path.matches("^/admin/users/[^/]+/status$")
                && "PATCH".equalsIgnoreCase(method)) {
            handleChangeUserStatus(exchange, path);
            return;
        }
        if (path.matches("^/admin/users/[^/]+$")
                && "DELETE".equalsIgnoreCase(method)) {
            handleDeleteUser(exchange, path);
            return;
        }

        if ("/admin/orders".equals(path)
                && "GET".equalsIgnoreCase(method)) {
            handleListOrders(exchange);
            return;
        }

        if ("/admin/transactions".equals(path)
                && "GET".equalsIgnoreCase(method)) {
            handleListTransactions(exchange);
            return;
        }
        sendError(exchange, 404, "Not Found");
    }

    // GET /admin/users
    private void handleListUsers(HttpExchange exchange) throws IOException {
        ServiceResult result = adminService.listUsers();
        String json = gson.toJson(result);
        sendJson(exchange, result.getStatus(), json);
    }

    // PATCH /admin/users/{id}/status
    private void handleChangeUserStatus(HttpExchange exchange,
                                        String path)
            throws IOException {
        String idStr = path.replaceFirst(
                "^/admin/users/([^/]+)/status$", "$1");

        String body = new BufferedReader(
                new InputStreamReader(
                        exchange.getRequestBody(),
                        StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining());

        StatusDTO dto;
        try {
            dto = gson.fromJson(body, StatusDTO.class);
        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Invalid JSON");
            return;
        }
        if (dto == null) {
            sendError(exchange, 400, "Missing request body");
            return;
        }

        ServiceResult result =
                adminService.updateUserStatus(idStr, dto.isApproved());
        String json = gson.toJson(result);
        sendJson(exchange, result.getStatus(), json);
    }

    // DELETE /admin/users/{id}
    private void handleDeleteUser(HttpExchange exchange,
                                  String path)
            throws IOException {
        String idStr = path.replaceFirst(
                "^/admin/users/([^/]+)$", "$1");

        ServiceResult result =
                adminService.deleteUser(idStr);
        String json = gson.toJson(result);
        sendJson(exchange, result.getStatus(), json);
    }

    // GET /admin/orders?...
    private void handleListOrders(HttpExchange exchange)
            throws IOException {
        Map<String, String> qs = queryToMap(
                exchange.getRequestURI().getRawQuery());
        List<String> allowed = List.of(
                "search", "vendor", "courier", "customer", "status");
        for (String k : qs.keySet()) {
            if (!allowed.contains(k)) {
                sendError(exchange, 400,
                        "Invalid query parameter: " + k);
                return;
            }
        }

        UUID vendorId   = parseUuidParam(
                qs.get("vendor"), "vendor", exchange);
        UUID courierId  = parseUuidParam(
                qs.get("courier"), "courier", exchange);
        UUID customerId = parseUuidParam(
                qs.get("customer"), "customer", exchange);
        if (exchange.getResponseBody() == null) {
            return;
        }

        List<OrderAdminDTO> dtos =
                orderService.getAllOrders(
                        qs.get("search"),
                        vendorId,
                        courierId,
                        customerId,
                        qs.get("status")
                );
        String json = gson.toJson(
                new DataResult<>(200, dtos));
        sendJson(exchange, 200, json);
    }

    // GET /admin/transactions?...
    private void handleListTransactions(
            HttpExchange exchange) throws IOException {
        Map<String, String> qs = queryToMap(
                exchange.getRequestURI().getRawQuery());
        List<String> allowed = List.of(
                "search", "user", "method", "status");
        for (String k : qs.keySet()) {
            if (!allowed.contains(k)) {
                sendError(exchange, 400,
                        "Invalid query parameter: " + k);
                return;
            }
        }

        ServiceResult result =
                adminService.listTransactions(
                        qs.get("search"),
                        qs.get("user"),
                        qs.get("method"),
                        qs.get("status")
                );
        String json = gson.toJson(result);
        sendJson(exchange, result.getStatus(), json);
    }

    private UUID parseUuidParam(String raw,
                                String name,
                                HttpExchange exchange)
            throws IOException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            sendError(exchange, 400,
                    "Invalid UUID for `" + name + "`");
            return null;
        }
    }

    private Map<String, String> queryToMap(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> urlDecode(arr[0]),
                        arr -> urlDecode(arr[1])
                ));
    }

    private String urlDecode(String s) {
        try {
            return URLDecoder.decode(
                    s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private void sendJson(HttpExchange exchange,
                          int statusCode,
                          String json)
            throws IOException {
        exchange.getResponseHeaders()
                .set("Content-Type",
                        "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(
                StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(
                statusCode, bytes.length);
        try (OutputStream os =
                     exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange,
                           int statusCode,
                           String message)
            throws IOException {
        String json = gson.toJson(
                Collections.singletonMap("error", message));
        sendJson(exchange, statusCode, json);
        exchange.getResponseBody().close();
    }
}