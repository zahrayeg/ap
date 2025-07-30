package httpRequestHandler;

import DAO.OrderDAO;
import DAO.ShoppingCartDAO;
import DAO.UserDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import io.jsonwebtoken.JwtException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import service.OrderService;
import util.HibernateUtil;
import util.JwtUtil;
import service.RatingService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OrderHttpHandler implements HttpHandler {

    private final OrderService orderService;
    private final Gson gson = new Gson();
    private final RatingService ratingService;
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    public OrderHttpHandler() {
        // Initialize SessionFactory
        // Initialize DAOs and UserService
        OrderDAO orderDAO = new OrderDAO();
        ShoppingCartDAO cartDAO = new ShoppingCartDAO(sessionFactory);
        UserDAO userdao = new UserDAO();

        // Initialize Services
        this.orderService = new OrderService(orderDAO, cartDAO, userdao, sessionFactory);
        this.ratingService = new RatingService(); // Adjust if RatingService needs dependencies
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/orders".equals(path) && "POST".equals(method)) {
            handleSubmitOrder(exchange);
        }
        else if ("/orders/history".equals(path) && "GET".equals(method)) {
            handleOrderHistory(exchange);
        }
        else if (path.startsWith("/favorites/") && "PUT".equals(method)) {
            handleAddFavorite(exchange);
        } else if (path.startsWith("/favorites/") && "DELETE".equals(method)) {
            handleRemoveFavorite(exchange);
        }
        else if(path.startsWith("/favorites/") && "GET".equals(method)) {
            handleGetFavorites(exchange);
        }
        else if(path.equals("/ratings")&& "POST".equals(method)) {
            handleRating(exchange);
        }
        else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        }
    }

    private void handleOrderHistory(HttpExchange exchange) throws IOException {
        // Extract Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        // Extract query parameters
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();
        String search = null;
        String vendor = null;
        if (query != null) {
            Map<String, String> queryParams = parseQuery(query);
            search = queryParams.get("search");
            vendor = queryParams.get("vendor");
        }

        // Extract buyerId from token
        String buyerIdStr;
        try {
            buyerIdStr = JwtUtil.validateToken(authHeader.substring(7));
        } catch (JwtException e) {
            sendResponse(exchange, 401, "{\"error\": \"Invalid or expired token\"}");
            return;
        }
        UUID buyerId = UUID.fromString(buyerIdStr);

        // Fetch order history
        try {
            // از آنجایی که getOrderHistory اکنون مستقیماً JSON برمی‌گرداند، نیازی به تبدیل مجدد نیست
            String orderHistoryJson = orderService.getOrderHistory(buyerId, search, vendor);
            sendResponse(exchange, 200, orderHistoryJson);
        } catch (RuntimeException e) {
            // مدیریت خطا با ساختار JSON یکسان
            String errorJson = gson.toJson(Collections.singletonMap("error", e.getMessage()));
            sendResponse(exchange, 500, errorJson);
        }

    }
    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\",\"status\":401}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("Received raw body: " + body);

        // پارس JSON
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\",\"status\":400}");
            return;
        }

        // اعتبارسنجی و استخراج فیلدهای اصلی
        if (!json.has("delivery_address") || !json.has("restaurant_id") || !json.has("items")) {
            sendResponse(exchange, 400, "{\"error\": \"Missing required fields (delivery_address, restaurant_id, or items)\",\"status\":400}");
            return;
        }

        String deliveryAddress = json.get("delivery_address").getAsString();
        String restaurantIdStr = json.get("restaurant_id").getAsString();
        JsonArray itemsArray = json.getAsJsonArray("items");

        // تبدیل restaurantId به UUID برای اعتبارسنجی
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(restaurantIdStr);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid `restaurant_id` format\",\"status\":400}");
            return;
        }

        List<OrderItemDTO> items = new ArrayList<>();
        for (JsonElement itemElement : itemsArray) {
            JsonObject item = itemElement.getAsJsonObject();
            if (!item.has("item_id") || !item.has("quantity") || !item.has("restaurant_id")) {
                sendResponse(exchange, 400, "{\"error\": \"Missing required fields in item (item_id, quantity, or restaurant_id)\",\"status\":400}");
                return;
            }

            String itemIdStr = item.get("item_id").getAsString();
            int quantity;
            try {
                // تبدیل ایمن quantity
                JsonElement quantityElement = item.get("quantity");
                if (quantityElement.isJsonPrimitive() && quantityElement.getAsJsonPrimitive().isNumber()) {
                    quantity = quantityElement.getAsInt();
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid quantity format, must be a number\",\"status\":400}");
                    return;
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid quantity value\",\"status\":400}");
                return;
            }

            if (quantity <= 0) {
                sendResponse(exchange, 400, "{\"error\": \"Quantity must be positive\",\"status\":400}");
                return;
            }

            String itemRestaurantIdStr = item.get("restaurant_id").getAsString();

            // اعتبارسنجی item_id و تطابق restaurant_id
            try {
                UUID itemId = UUID.fromString(itemIdStr);
                UUID itemRestaurantId = UUID.fromString(itemRestaurantIdStr);
                if (!itemRestaurantId.equals(restaurantId)) {
                    sendResponse(exchange, 400, "{\"error\": \"Item restaurant_id does not match order restaurant_id\",\"status\":400}");
                    return;
                }
                items.add(new OrderItemDTO(itemId, quantity, itemRestaurantId));
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid `item_id` or `restaurant_id` format in item\",\"status\":400}");
                return;
            }
        }

        // پردازش سفارش
        try {
            Map<String, Object> response = orderService.submitOrder(authHeader.substring(7), deliveryAddress, restaurantId, items);
            int statusCode = parseStatus(response.get("status"), 200);
            sendResponse(exchange, statusCode, gson.toJson(response));
            //sendResponse(exchange, (int) response.getOrDefault("status", 200), gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\",\"status\":500}");
        }
    }
    private void handleAddFavorite(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange,401,"{\"error\": \"Unauthorized\"}");
            return;
        }
        // Extract restaurantId from path
        String path = exchange.getRequestURI().getPath();
        String restaurantIdStr = path.substring(path.lastIndexOf('/') + 1);
        UUID restaurantId;
        System.out.println("Restaurant ID: " + restaurantIdStr);
        try {
            restaurantId = UUID.fromString(restaurantIdStr);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"Restaurant not found\"}");
            return;
        }
        // Call service to add favorite
        try {
            String message = orderService.addFavorite(authHeader.substring(7), restaurantId);
            sendResponse(exchange, 200, "{\"message\": \"" + message + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 404, "{\"error\": \"Restaurant not found\"}");
        }
    }
    private void handleRemoveFavorite(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401,"{\"error\": \"Unauthorized\"}");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String restaurantIdStr = path.substring(path.lastIndexOf('/') + 1);
        System.out.println("Restaurant ID: " + restaurantIdStr);
        UUID restaurantId;
        try{
            restaurantId = UUID.fromString(restaurantIdStr);
        }
        catch(IllegalArgumentException e){
            sendResponse(exchange, 400, "{\"error\": \"Restaurant not found\"}");
            return;
        }
        try{
            String message=orderService.removeFavorite(authHeader.substring(7),restaurantId);
            sendResponse(exchange, 200, "{\"message\": \"" + message + "\"}");
        }
        catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }   catch (RuntimeException e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private void handleGetFavorites(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            List<RestaurantDTO> favorites = orderService.getFavorites(authHeader.substring(7));
            String response = gson.toJson(favorites);
            sendResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private void handleRating(HttpExchange exchange) throws IOException {
        System.out.println("Handling rating request for path: " + exchange.getRequestURI().getPath());
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            System.out.println("Unauthorized access, sending 401");
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        final int MAX_BODY_SIZE = 10 * 1024 * 1024; // حداکثر 10 مگابایت
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            System.out.println("Reading request body...");
            String line;
            while ((line = reader.readLine()) != null) {
                if (requestBody.length() + line.length() > MAX_BODY_SIZE) {
                    System.out.println("Request body too large, sending 413");
                    sendResponse(exchange, 413, "{\"error\": \"Payload Too Large\"}");
                    return;
                }
                requestBody.append(line);
            }
            System.out.println("Request body read: " + requestBody.toString());
        } catch (Exception e) {
            System.err.println("Error reading request body: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Invalid request body\"}");
            e.printStackTrace();
            return;
        }

        JsonObject json;
        try {
            System.out.println("Parsing JSON: " + requestBody.toString());
            json = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // دیباگ دقیق‌تر
        try {
            System.out.println("Attempting to extract JSON data...");
            String orderId = null;
            int rating = 0;
            String comment = null;
            String imageBase64 = null;

            // استخراج با مدیریت استثناها و null
            if (json.has("orderId") && !json.get("orderId").isJsonNull()) {
                orderId = json.get("orderId").getAsString();
                System.out.println("Extracted orderId: " + orderId);
            } else {
                System.out.println("orderId is null or missing in JSON");
                sendResponse(exchange, 400, "{\"error\": \"orderId is required\"}");
                return;
            }
            if (json.has("rating") && !json.get("rating").isJsonNull()) {
                rating = json.get("rating").getAsInt();
                System.out.println("Extracted rating: " + rating);
            } else {
                System.out.println("rating is null or missing in JSON");
                sendResponse(exchange, 400, "{\"error\": \"rating is required\"}");
                return;
            }
            if (json.has("comment") && !json.get("comment").isJsonNull()) {
                comment = json.get("comment").getAsString();
                System.out.println("Extracted comment: " + comment);
            } else {
                System.out.println("comment is null or missing in JSON");
                sendResponse(exchange, 400, "{\"error\": \"comment is required\"}");
                return;
            }
            if (json.has("imageBase64")) {
                imageBase64 = json.get("imageBase64").isJsonNull() ? null : json.get("imageBase64").getAsString();
                System.out.println("Extracted imageBase64: " + (imageBase64 != null ? "present" : "null"));
            }

            // اعتبارسنجی اولیه
            if (orderId == null || orderId.trim().isEmpty() || rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
                System.out.println("Validation failed for input data: orderId=" + orderId + ", rating=" + rating + ", comment=" + comment);
                sendResponse(exchange, 400, "Invalid input");
                return;
            }

            System.out.println("Submitting rating to service...");
            ratingService.submitRating(authHeader.substring(7), orderId, rating, comment, imageBase64);
            System.out.println("Rating submitted, sending 200");
            sendResponse(exchange, 200, "Rating submitted");
        } catch (Exception e) {
            System.err.println("Error extracting or processing JSON data: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "{\"error\": \"Error processing request\"}");
            return;
        }
    }
    private boolean isAuthenticated(String authHeader) {
        System.out.println("is authenticated orderhttphandler");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        try {
            JwtUtil.validateToken(authHeader.substring(7));
            System.out.println("JWT token validated successfully in orderhttphandler");
            return true;
        } catch (JwtException e) {
            System.err.println("Authentication failed in orderhttphandler: " + e.getMessage());
            return false;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        exchange.close();
    }
    private int parseStatus(Object statusObj, int defaultStatus) {
        if (statusObj == null) {
            return defaultStatus;
        }
        if (statusObj instanceof Number) {
            return ((Number) statusObj).intValue();
        }
        if (statusObj instanceof String) {
            try {
                return Integer.parseInt((String) statusObj);
            } catch (NumberFormatException e) {
                return defaultStatus;
            }
        }
        return defaultStatus;
    }
    private Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }
}