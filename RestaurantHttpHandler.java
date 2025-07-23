package httpRequestHttpHandler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import service.RestaurantService;
import util.JwtUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RestaurantHttpHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/restaurants".equals(path) && "POST".equals(method)) {
            handleCreateRestaurant(exchange);
        } else if (path.startsWith("/restaurants/") && "PUT".equals(method)) {
            handleUpdateRestaurant(exchange);
        } else if ("/restaurants/mine".equals(path) && "GET".equals(method)) {
            handleGetMyRestaurants(exchange);
        } else if (path.matches("/restaurants/[^/]+/item") && "POST".equals(method)) {
            handleAddFoodItem(exchange);
        } else if (path.matches("/restaurants/[^/]+/item/[^/]+") && "PUT".equals(method)) {
            handleUpdateFoodItem(exchange);
        } else if (path.matches("/restaurants/[^/]+/menu") && "POST".equals(method)) {
            handleAddMenu(exchange);
        } else if (path.matches("/restaurants/[^/]+/item/[^/]+") && "DELETE".equals(method)) {
            handleDeleteFoodItem(exchange);
        } else if (path.matches("/restaurants/[^/]+/menu/[^/]+/[^/]+") && "DELETE".equals(method)) {
            handleRemoveMenuItem(exchange);
        }
        else if (path.matches("/restaurants/[^/]+/orders") && "GET".equals(method)) {
            handleGetOrders(exchange);
        }


        else if (path.matches("/restaurants/[^/]+/menu/[^/]+")
                && "PUT".equals(method)) {
            handleAddMenuItem(exchange);
        }
        else if (path.matches("/restaurants/[^/]+/menu/[^/]+")
                && "DELETE".equals(method)) {
            handleDeleteMenu(exchange);
        }
        else if (path.matches("/restaurants/orders/[^/]+") && "PATCH".equals(method)) {
            handlePatchOrderStatus(exchange);
        }


        else {
            sendErrorResponse(exchange, 404, "Not Found");
        }
    }


    private void handleAddFoodItem(HttpExchange exchange) throws IOException {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            String token = auth.substring("Bearer ".length()).trim();
            sellerId = JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String idStr = exchange.getRequestURI().getPath()
                .replaceAll("^/restaurants/([^/]+)/item$", "$1");
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(idStr);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }

        FoodDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, FoodDTO.class);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }

        ServiceResult result = restaurantService.addFoodItem(restaurantId, dto, sellerId);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String resp = gson.toJson(result);
        sendResponse(exchange, result.getStatus(), resp);
    }


    private void handleGetMyRestaurants(HttpExchange exchange) throws IOException {

        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            String token = header.substring("Bearer ".length()).trim();
            sellerId = JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        List<RestaurantDTO> list;
        try {
            list = restaurantService.getMyRestaurants(sellerId);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error");
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String json = gson.toJson(list);
        sendResponse(exchange, 200, json);
    }


    private void handleUpdateRestaurant(HttpExchange exchange) throws IOException {

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            String token = authHeader.substring("Bearer ".length()).trim();
            sellerId = JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String idStr = exchange.getRequestURI().getPath().substring("/restaurants/".length());
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(idStr);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }

        RestaurantDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, RestaurantDTO.class);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }

        ServiceResult result = restaurantService.updateRestaurant(restaurantId, dto, sellerId);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String response = gson.toJson(result);
        sendResponse(exchange, result.getStatus(), response);
    }


    private void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }

        UUID sellerId;
        try {
            String token = authHeader.substring("Bearer ".length()).trim();
            sellerId = JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        RestaurantDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, RestaurantDTO.class);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }

        ServiceResult result = restaurantService.createRestaurant(dto, sellerId);
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        String response = gson.toJson(result);
        sendResponse(exchange, result.getStatus(), response);
    }

    private void handleUpdateFoodItem(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }

        UUID sellerId;
        try {
            String token = auth.substring("Bearer ".length()).trim();
            sellerId = JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");

        if (parts.length != 5) {
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }

        UUID restaurantId, itemId;
        try {
            restaurantId = UUID.fromString(parts[2]);
            itemId = UUID.fromString(parts[4]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid UUID format for restaurantId or itemId");
            return;
        }

        FoodDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, FoodDTO.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }

        if (dto == null) {
            sendErrorResponse(exchange, 400, "Invalid input: Request body is empty");
            return;
        }

        ServiceResult result = restaurantService.updateFoodItem(restaurantId, itemId, dto, sellerId);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result instanceof UpdateFoodItemResult) {
            UpdateFoodItemResult ok = (UpdateFoodItemResult) result;
            String json = gson.toJson(ok.getItem());
            sendResponse(exchange, ok.getStatus(), json);
        } else {
            String json = gson.toJson(new ErrorResponse(result.getMessage()));
            sendResponse(exchange, result.getStatus(), json);
        }
    }


    private void handleAddMenu(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring("Bearer ".length()).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String restId = exchange.getRequestURI().getPath().split("/")[2];
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(restId);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }

        MenuDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, MenuDTO.class);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }
        if (dto == null) {
            sendErrorResponse(exchange, 400, "Invalid input: Request body is empty");
            return;
        }

        ServiceResult result = restaurantService.addMenu(restaurantId, dto, sellerId);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result instanceof CreateMenuResult) {
            CreateMenuResult ok = (CreateMenuResult) result;
            sendResponse(exchange, ok.getStatus(), gson.toJson(ok.getMenu()));
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }


    }


    private void handleDeleteFoodItem(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring("Bearer ".length()).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length != 5) {
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }
        UUID restaurantId, itemId;
        try {
            restaurantId = UUID.fromString(parts[2]);
            itemId = UUID.fromString(parts[4]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid UUID format");
            return;
        }

        ServiceResult result = restaurantService.deleteFoodItem(restaurantId, itemId, sellerId);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result instanceof DeleteResult) {
            String body = gson.toJson(result);
            sendResponse(exchange, result.getStatus(), body);
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }

    }

    private void handleRemoveMenuItem(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring("Bearer ".length()).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        // 2. استخراج path parameters
        // parts = ["", "restaurants", "{id}", "menu", "{title}", "{item_id}"]
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length != 6) {
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }

        UUID restaurantId, itemId;
        String menuTitle = parts[4];
        try {
            restaurantId = UUID.fromString(parts[2]);
            itemId = UUID.fromString(parts[5]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid UUID format");
            return;
        }
        ServiceResult result = restaurantService.removeMenuItem(
                restaurantId, menuTitle, itemId, sellerId
        );

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result.getStatus() == 200) {
            String json = gson.toJson(
                    Collections.singletonMap("message", result.getMessage())
            );
            sendResponse(exchange, 200, json);
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }

    }











    private void handleAddMenuItem(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring("Bearer ".length()).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");

        if (parts.length != 5) {
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }
        UUID restaurantId;
        String menuTitle = parts[4];
        try {
            restaurantId = UUID.fromString(parts[2]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }

        AddMenuItemDTO dto;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            dto = gson.fromJson(reader, AddMenuItemDTO.class);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
            return;
        }
        if (dto == null || dto.getItem_id() == null) {
            sendErrorResponse(exchange, 400, "Invalid input: item_id is required");
            return;
        }

        UUID itemId;
        try {
            itemId = UUID.fromString(dto.getItem_id());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid item_id format");
            return;
        }

        ServiceResult result = restaurantService.addMenuItem(
                restaurantId, menuTitle, itemId, sellerId
        );

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result.getStatus() == 200) {
            String json = gson.toJson(
                    Collections.singletonMap("message", result.getMessage())
            );
            sendResponse(exchange, 200, json);
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }
    }







    private void handleDeleteMenu(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring("Bearer ".length()).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        // 2. استخراج path parameters
        // ["", "restaurants", "{id}", "menu", "{title}"]
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length != 5) {
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }
        UUID restaurantId;
        String menuTitle = parts[4];
        try {
            restaurantId = UUID.fromString(parts[2]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }
        ServiceResult result = restaurantService.deleteMenu(
                restaurantId, menuTitle, sellerId
        );
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result.getStatus() == 200) {
            String json = gson.toJson(
                    Collections.singletonMap("message", result.getMessage())
            );
            sendResponse(exchange, 200, json);
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }
    }










    private void handleGetOrders(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring(7).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length != 3 + 1) {  // ["", "restaurants", "{id}", "orders"]
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(parts[2]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant id");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String status    = params.get("status");
        String search    = params.get("search");
        UUID userId      = params.get("user")    != null ? UUID.fromString(params.get("user"))    : null;
        UUID courierId   = params.get("courier") != null ? UUID.fromString(params.get("courier")) : null;

        ServiceResult result = restaurantService.getOrders(
                restaurantId, status, search, userId, courierId, sellerId
        );


        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result.getStatus() == 200) {
            if (result instanceof DataResult<?>) {
                Object data = ((DataResult<?>) result).getData();
                String body = gson.toJson(data);
                sendResponse(exchange, 200, body);
            }

            else {
                String body = gson.toJson(
                        Collections.singletonMap("message", result.getMessage())
                );
                sendResponse(exchange, 200, body);
            }
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }
    }


    private Map<String,String> parseQuery(String query) {
        Map<String,String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String kv : query.split("&")) {
            String[] pair = kv.split("=", 2);
            if (pair.length == 2) {
                map.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }


    private void handlePatchOrderStatus(HttpExchange exchange) throws IOException {

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
            return;
        }
        UUID sellerId;
        try {
            sellerId = JwtUtil.getUserIdFromToken(auth.substring(7).trim());
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid token");
            return;
        }


        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length != 4) {  // ["", "restaurants", "orders", "{order_id}"]
            sendErrorResponse(exchange, 400, "Invalid path parameters");
            return;
        }
        UUID orderId;
        try {
            orderId = UUID.fromString(parts[3]);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid order id");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String newStatus;
        try {
            var node = new Gson().fromJson(body, com.google.gson.JsonObject.class);
            newStatus = node.has("status") ? node.get("status").getAsString() : null;
        } catch (Exception ex) {
            sendErrorResponse(exchange, 400, "Invalid JSON body");
            return;
        }

        ServiceResult result = restaurantService.changeOrderStatus(orderId, newStatus, sellerId);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (result.getStatus() == 200) {
            String json = new Gson().toJson(Collections.singletonMap("message", result.getMessage()));
            sendResponse(exchange, 200, json);
        } else {
            sendErrorResponse(exchange, result.getStatus(), result.getMessage());
        }

    }








    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int status, String message) throws IOException {
        String response = gson.toJson(new ErrorResponse(message));
        sendResponse(exchange, status, response);
    }

    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }








}