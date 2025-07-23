package service;

import DAO.*;
import entity.*;
import dto.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RestaurantService {
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final OrderDAO orderDAO           = new OrderDAO();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;



    public ServiceResult createRestaurant(RestaurantDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getAddress() == null || dto.getPhone() == null) {
            return new ServiceResult(400, "Invalid input: name, address and phone are required");
        }

        Optional<User> sellerOptional = userDAO.findById(sellerId);
        if (!sellerOptional.isPresent()) {
            return new ServiceResult(401, "Unauthorized: seller not found");
        }

        User seller = sellerOptional.get();
        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            return new ServiceResult(403, "Forbidden: user is not a seller");
        }

        Restaurant restaurant = new Restaurant(dto.getName(), dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setLogoBase64(dto.getLogoBase64());
        restaurant.setTaxFee(dto.getTaxFee());
        restaurant.setAdditionalFee(dto.getAdditionalFee());
        restaurant.setSeller(seller);

        try {
            restaurantDAO.save(restaurant);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to save restaurant");
        }

        RestaurantDTO resultDTO = new RestaurantDTO();
        resultDTO.setId(restaurant.getId());
        resultDTO.setName(restaurant.getName());
        resultDTO.setAddress(restaurant.getAddress());
        resultDTO.setPhone(restaurant.getPhone());
        resultDTO.setLogoBase64(restaurant.getLogoBase64());
        resultDTO.setTaxFee(restaurant.getTaxFee());
        resultDTO.setAdditionalFee(restaurant.getAdditionalFee());
        resultDTO.setSellerId(seller.getId());

        return new CreateRestaurantResult(201, "Restaurant created successfully", resultDTO);
    }






    public ServiceResult updateRestaurant(UUID restaurantId, RestaurantDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getAddress() == null || dto.getPhone() == null) {
            return new ServiceResult(400, "Invalid input: name, address and phone are required");
        }


        Optional<Restaurant> opt = restaurantDAO.findById(restaurantId);
        if (!opt.isPresent()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = opt.get();


        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot update another seller's restaurant");
        }


        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setLogoBase64(dto.getLogoBase64());
        restaurant.setTaxFee(dto.getTaxFee());
        restaurant.setAdditionalFee(dto.getAdditionalFee());


        try {
            restaurantDAO.update(restaurant);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to update restaurant");
        }


        RestaurantDTO resultDTO = new RestaurantDTO();
        resultDTO.setId(restaurant.getId());
        resultDTO.setName(restaurant.getName());
        resultDTO.setAddress(restaurant.getAddress());
        resultDTO.setPhone(restaurant.getPhone());
        resultDTO.setLogoBase64(restaurant.getLogoBase64());
        resultDTO.setTaxFee(restaurant.getTaxFee());
        resultDTO.setAdditionalFee(restaurant.getAdditionalFee());
        resultDTO.setSellerId(sellerId);

        return new UpdateRestaurantResult(200, "Restaurant updated successfully", resultDTO);
    }




    public List<RestaurantDTO> getMyRestaurants(UUID sellerId) {

        List<Restaurant> restaurants = restaurantDAO.findBySellerId(sellerId);
        return restaurants.stream()
                .map(r -> {
                    RestaurantDTO dto = new RestaurantDTO();
                    dto.setId(r.getId());
                    dto.setName(r.getName());
                    dto.setAddress(r.getAddress());
                    dto.setPhone(r.getPhone());
                    dto.setLogoBase64(r.getLogoBase64());
                    dto.setTaxFee(r.getTaxFee());
                    dto.setAdditionalFee(r.getAdditionalFee());
                    dto.setSellerId(sellerId);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public ServiceResult addFoodItem(UUID restaurantId, FoodDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getPrice() < 0 || dto.getSupply() == null) {
            return new ServiceResult(400, "Invalid input: name, price and supply are required");
        }

        Optional<Restaurant> opt = restaurantDAO.findById(restaurantId);
        if (opt.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = opt.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot add item to another seller's restaurant");
        }

        Food item = new Food();
        item.setName(dto.getName());
        item.setImageBase64(dto.getImageBase64());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setSupply(dto.getSupply());
        item.setKeywords(dto.getKeywords());
        item.setRestaurant(restaurant);

        try {
            new FoodDAO().save(item);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to add food item");
        }

        FoodDTO result = new FoodDTO();
        result.setId(item.getId().toString());
        result.setName(item.getName());
        result.setImageBase64(item.getImageBase64());
        result.setDescription(item.getDescription());
        result.setPrice(item.getPrice());
        result.setSupply(item.getSupply());
        result.setKeywords(item.getKeywords());
        result.setVendorId(restaurantId.toString());

        return new CreateFoodItemResult(200, "Food item created and added successfully", result);
    }





    public ServiceResult updateFoodItem(UUID restaurantId, UUID itemId, FoodDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getPrice() <0 || dto.getSupply() == null) {
            return new ServiceResult(400, "Invalid input: name, price and supply are required");
        }

        Optional<Restaurant> optRest = restaurantDAO.findById(restaurantId);
        if (optRest.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = optRest.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot edit item of another seller's restaurant");
        }

        Optional<Food> optFood = foodDAO.findById(itemId);
        if (optFood.isEmpty()) {
            return new ServiceResult(404, "Resource not found: food item does not exist");
        }
        Food food = optFood.get();

        if (!food.getRestaurant().getId().equals(restaurantId)) {
            return new ServiceResult(403, "Forbidden: item does not belong to this restaurant");
        }

        food.setName(dto.getName());
        food.setImageBase64(dto.getImageBase64());
        food.setDescription(dto.getDescription());
        food.setPrice(dto.getPrice());
        food.setSupply(dto.getSupply());
        food.setKeywords(dto.getKeywords());

        try {
            foodDAO.update(food);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to update food item");
        }

        FoodDTO out = new FoodDTO();
        out.setId(food.getId().toString());
        out.setName(food.getName());
        out.setImageBase64(food.getImageBase64());
        out.setDescription(food.getDescription());
        out.setPrice(food.getPrice());
        out.setSupply(food.getSupply());
        out.setKeywords(food.getKeywords());
        out.setVendorId(restaurantId.toString());

        return new UpdateFoodItemResult(200, "Food item edited successfully", out);
    }



    public ServiceResult addMenu(UUID restaurantId, MenuDTO dto, UUID sellerId) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ServiceResult(400, "Invalid input: title is required");
        }

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot add menu to another seller's restaurant");
        }


        if (menuDAO.findByRestaurantAndTitle(restaurantId, dto.getTitle()).isPresent()) {
            return new ServiceResult(409, "Conflict: menu title already exists");
        }

        Menu menu = new Menu(dto.getTitle(), restaurant);
        try {
            menuDAO.save(menu);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to add menu");
        }

        MenuDTO out = new MenuDTO();
        out.setId(menu.getId().toString());
        out.setTitle(menu.getTitle());
        return new CreateMenuResult(200, "Food menu created and added to restaurant successfully", out);
    }


    public ServiceResult deleteFoodItem(UUID restaurantId, UUID itemId, UUID sellerId) {

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot delete item of another seller's restaurant");
        }

        Optional<Food> of = foodDAO.findById(itemId);
        if (of.isEmpty()) {
            return new ServiceResult(404, "Resource not found: food item does not exist");
        }
        Food food = of.get();

        if (!food.getRestaurant().getId().equals(restaurantId)) {
            return new ServiceResult(403, "Forbidden: item does not belong to this restaurant");
        }

        List<Menu> menus = menuDAO.findMenusByFoodId(itemId);
        for (Menu m : menus) {
            m.removeItem(food);
            menuDAO.update(m);
        }

        try {
            foodDAO.delete(food);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to delete food item");
        }

        return new DeleteResult(200, "Food item removed successfully");
    }




    public ServiceResult removeMenuItem(UUID restaurantId, String menuTitle, UUID itemId, UUID sellerId) {

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot modify another seller's restaurant");
        }

        Optional<Menu> om = menuDAO.findByRestaurantAndTitle(restaurantId, menuTitle);
        if (om.isEmpty()) {
            return new ServiceResult(404, "Resource not found: menu does not exist");
        }
        Menu menu = om.get();

        Optional<Food> of = foodDAO.findById(itemId);
        if (of.isEmpty()) {
            return new ServiceResult(404, "Resource not found: food item does not exist");
        }
        Food food = of.get();

        if (!menu.getItems().contains(food)) {
            return new ServiceResult(404, "Resource not found: menu item does not exist");
        }

        menu.removeItem(food);
        try {
            menuDAO.update(menu);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to update menu");
        }
        return new DeleteResult(200, "Item removed from restaurant menu successfully");
    }

    public ServiceResult addMenuItem(UUID restaurantId, String menuTitle, UUID itemId, UUID sellerId) {

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();
        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot modify another seller's restaurant");
        }
        Optional<Menu> om = menuDAO.findByRestaurantAndTitle(restaurantId, menuTitle);
        if (om.isEmpty()) {
            return new ServiceResult(404, "Resource not found: menu does not exist");
        }
        Menu menu = om.get();
        Optional<Food> of = foodDAO.findById(itemId);
        if (of.isEmpty()) {
            return new ServiceResult(404, "Resource not found: food item does not exist");
        }
        Food food = of.get();
        if (!food.getRestaurant().getId().equals(restaurantId)) {
            return new ServiceResult(403, "Forbidden: item does not belong to this restaurant");
        }

        if (menu.getItems().contains(food)) {
            return new ServiceResult(409, "Conflict: item already exists in menu");
        }

        menu.addItem(food);
        try {
            menuDAO.update(menu);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to update menu");
        }

        return new DeleteResult(200, "Food item added to restaurant menu successfully");
    }



    public ServiceResult deleteMenu(UUID restaurantId, String menuTitle, UUID sellerId) {

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();

        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot delete menu of another seller's restaurant");
        }

        Optional<Menu> om = menuDAO.findByRestaurantAndTitle(restaurantId, menuTitle);
        if (om.isEmpty()) {
            return new ServiceResult(404, "Resource not found: menu does not exist");
        }
        Menu menu = om.get();

        try {
            menuDAO.delete(menu);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to delete menu");
        }

        return new DeleteResult(200, "Food menu removed from restaurant successfully");
    }

    public ServiceResult getOrders(UUID restaurantId,
                                   String status,
                                   String searchCustomerName,
                                   UUID userId,
                                   UUID courierId,
                                   UUID sellerId) {

        Optional<Restaurant> or = restaurantDAO.findById(restaurantId);
        if (or.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = or.get();


        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot view orders of another seller's restaurant");
        }

        List<Order> orders = orderDAO.getOrdersByRestaurant(
                restaurantId,
                status,
                searchCustomerName,
                userId,
                courierId
        );
        List<RestaurantOrderDTO> dtos = orders.stream().map(o -> {
            List<RestaurantOrderItemDTO> items = o.getOrderItems().stream()
                    .map(oi -> new RestaurantOrderItemDTO(
                            oi.getFood().getId(),
                            oi.getQuantity()
                    ))
                    .collect(Collectors.toList());

            String createdAt = o.getOrderedDateTime().format(dtf);
            String customerName = o.getCustomer().getName();
            String courierName  = o.getDeliveryMan() != null
                    ? o.getDeliveryMan().getName()
                    : null;

            return new RestaurantOrderDTO(
                    o.getId(),
                    o.getDeliveryAddress(),
                    o.getCustomer().getId(),
                    customerName,
                    o.getDeliveryMan() != null ? o.getDeliveryMan().getId() : null,
                    courierName,
                    o.getVendorId(),
                    items,
                    o.getTotalPrice(),
                    o.getStatus(),
                    createdAt
            );
        }).collect(Collectors.toList());
        return new DataResult<>(200, dtos);
    }

    private static final Set<String> VALID_STATUSES = Set.of(
            "submitted", "unpaid and cancelled", "waiting vendor",
            "cancelled", "finding courier", "on the way", "completed"
    );

    public ServiceResult changeOrderStatus(UUID orderId,
                                           String newStatus,
                                           UUID sellerId) {

        Optional<Order> oo = orderDAO.findById(orderId);
        if (oo.isEmpty()) {
            return new ServiceResult(404, "Resource not found: order does not exist");
        }
        Order order = oo.get();

        Restaurant r = order.getRestaurant();
        if (!r.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot change orders of another seller's restaurant");
        }
  if (newStatus == null || !VALID_STATUSES.contains(newStatus.toLowerCase())) {
            return new ServiceResult(400, "Invalid status value");
        }
   order.setStatus(newStatus);
        try {
            orderDAO.update(order);
        } catch (Exception ex) {
            return new ServiceResult(500, "Internal server error: failed to update order status");
        }


        return new ServiceResult(200, "Order status changed successfully");
    }


}