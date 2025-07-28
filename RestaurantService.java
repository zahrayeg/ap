package service;

import DAO.*;
import dto.*;
import entity.*;
import org.hibernate.Session;
import util.HibernateUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RestaurantService {
    private final RestaurantDAO dao = new RestaurantDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();
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
            dao.save(restaurant);
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



    public ServiceResult getMenusForRestaurant(UUID restaurantId, UUID sellerId) {
        // 1) اعتبارسنجی ورودی
        if (restaurantId == null) {
            return new ServiceResult(400, "Invalid input: restaurantId is required");
        }

        // 2) بازیابی رستوران
        Optional<Restaurant> opt = dao.findById(restaurantId);
        if (!opt.isPresent()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = opt.get();

        // 3) بررسی مالکیت
        if (restaurant.getSeller() == null
                || !restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403,
                    "Forbidden: cannot view menus of another seller's restaurant");
        }

        // 4) واکشی منوها و آیتم‌ها
        MenuResponseDTO menus = dao.findMenusAndItemsByRestaurantId(restaurantId);

        // 5) بازگشت نتیجه‌ی موفق با payload
        return new GetMenusResult(200, "Menus fetched successfully", menus);
    }


    public ServiceResult updateRestaurant(UUID restaurantId, RestaurantDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getAddress() == null || dto.getPhone() == null) {
            return new ServiceResult(400, "Invalid input: name, address and phone are required");
        }


        Optional<Restaurant> opt = dao.findById(restaurantId);
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
            dao.update(restaurant);
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

    public List<RestaurantDTO> getRestaurants(String search, List<String> categories) {
        List<Restaurant> restaurants = dao.findRestaurants(search, categories);
        return restaurants.stream()
                .map(r -> {
                    RestaurantDTO dto = new RestaurantDTO();
                    dto.setId(r.getId());
                    dto.setName(r.getName());
                    dto.setAddress(r.getAddress());
                    dto.setPhone(r.getPhone());
                    dto.setEmail(r.getEmail());
                    // مدیریت مقادیر قابل null
                    dto.setRate(r.getRate() != null ? r.getRate() : 0); // پیش‌فرض 0 اگر null باشد
                    dto.setCategory(r.getCategory() != null ? r.getCategory() : "نامشخص"); // پیش‌فرض
                    dto.setLogoBase64(r.getLogoBase64()); // می‌تواند null باشد
                    dto.setTaxFee(r.getTaxFee() != null ? r.getTaxFee() : 0); // پیش‌فرض
                    dto.setAdditionalFee(r.getAdditionalFee() != null ? r.getAdditionalFee() : 0); // پیش‌فرض 0
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public List<RestaurantDTO> getMyRestaurants(UUID sellerId) {

        List<Restaurant> restaurants = dao.findBySellerId(sellerId);
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

    public MenuResponseDTO getMenusAndItems(UUID restaurantId) {
        return dao.findMenusAndItemsByRestaurantId(restaurantId);
    }
    public Restaurant getRestaurantById(UUID id) {
        return dao.findById(id).get();
    }

    public List<Restaurant> getAllRestaurants() {
        return dao.findAll();
    }

    public ServiceResult addFoodItem(UUID restaurantId, FoodDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getPrice() < 0 || dto.getSupply() == null) {
            return new ServiceResult(400, "Invalid input: name, price and supply are required");
        }

        Optional<Restaurant> opt = dao.findById(restaurantId);
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
        item.setCategories(dto.getCategories() != null ? dto.getCategories() : null);
        item.setRestaurant(restaurant);

        try {
            new FoodDAO().save(item);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to add food item");
        }

        FoodDTO result = new FoodDTO();
        result.setId(UUID.fromString(item.getId().toString()));
        result.setName(item.getName());
        result.setImageBase64(item.getImageBase64());
        result.setDescription(item.getDescription());
        result.setPrice(item.getPrice());
        result.setSupply(item.getSupply());
        result.setCategories(item.getCategories() != null ? item.getCategories() : null);
        result.setRestaurantId(UUID.fromString(restaurantId.toString()));

        return new CreateFoodItemResult(200, "Food item created and added successfully", result);
    }





    public ServiceResult updateFoodItem(UUID restaurantId, UUID itemId, FoodDTO dto, UUID sellerId) {

        if (dto.getName() == null || dto.getPrice() <0 || dto.getSupply() == null) {
            return new ServiceResult(400, "Invalid input: name, price and supply are required");
        }

        Optional<Restaurant> optRest = dao.findById(restaurantId);
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
        food.setCategories(dto.getCategories() != null ? dto.getCategories() : null);

        try {
            foodDAO.update(food);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: failed to update food item");
        }

        FoodDTO out = new FoodDTO();
        out.setId(UUID.fromString(food.getId().toString()));
        out.setName(food.getName());
        out.setImageBase64(food.getImageBase64());
        out.setDescription(food.getDescription());
        out.setPrice(food.getPrice());
        out.setSupply(food.getSupply());
        out.setCategories(food.getCategories() != null ? food.getCategories() : null);
        out.setRestaurantId(UUID.fromString(restaurantId.toString()));

        return new UpdateFoodItemResult(200, "Food item edited successfully", out);
    }



    public ServiceResult addMenu(UUID restaurantId, MenuDTO dto, UUID sellerId) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ServiceResult(400, "Invalid input: title is required");
        }

        Optional<Restaurant> or = dao.findById(restaurantId);
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
        out.setId(menu.getId());
        out.setTitle(menu.getTitle());
        return new CreateMenuResult(200, "Food menu created and added to restaurant successfully", out);
    }


    public ServiceResult deleteFoodItem(UUID restaurantId, UUID itemId, UUID sellerId) {

        Optional<Restaurant> or = dao.findById(restaurantId);
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

        Optional<Restaurant> or = dao.findById(restaurantId);
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

        Optional<Restaurant> or = dao.findById(restaurantId);
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

        Optional<Restaurant> or = dao.findById(restaurantId);
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

    public ServiceResult getOrders(UUID restaurantId, String status, String searchCustomerName, UUID userId, UUID courierId, UUID sellerId) {
        // بررسی وجود رستوران
        Optional<Restaurant> restaurantOpt = dao.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            return new ServiceResult(404, "Resource not found: restaurant does not exist");
        }
        Restaurant restaurant = restaurantOpt.get();

        // بررسی دسترسی فروشنده
        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new ServiceResult(403, "Forbidden: cannot view orders of another seller's restaurant");
        }

        try {
            // دریافت سفارش‌ها با فیلترها
            List<Order> orders = orderDAO.getOrdersByRestaurant(restaurantId, status, searchCustomerName, userId, courierId);

            // تبدیل به DTO
            List<RestaurantOrderDTO> dtos = orders.stream().map(order -> {
                List<RestaurantOrderItemDTO> items = order.getOrderItems().stream()
                        .map(oi -> new RestaurantOrderItemDTO(
                                oi.getFood().getId(),
                                oi.getQuantity()
                        ))
                        .collect(Collectors.toList());

                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : null;
                String customerName = order.getCustomer() != null ? order.getCustomer().getFullName() : "نامشخص";
                String courierName = order.getCourierId() != null ? getCourierName(order.getCourierId()) : null;

                return new RestaurantOrderDTO(
                        order.getId(),
                        order.getDeliveryAddress(),
                        order.getCustomer() != null ? order.getCustomer().getId() : null,
                        customerName,
                        order.getCourierId(),
                        courierName,
                        order.getRestaurant() != null ? order.getRestaurant().getId() : null,
                        items,
                        order.getPayPrice(),
                        order.getStatus(),
                        createdAt
                );
            }).collect(Collectors.toList());

            return new DataResult<>(200, dtos);
        } catch (Exception e) {
            return new ServiceResult(500, "Error fetching orders: " + e.getMessage());
        }
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









    public DataResult<List<MenuDTO>> getMenusByRestaurant(UUID restaurantId, UUID sellerId) {

        Optional<Restaurant> optRest = dao.findById(restaurantId);
        if (optRest.isEmpty()) {
            return new DataResult<>(404, null);
        }
        Restaurant restaurant = optRest.get();
        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new DataResult<>(403, null);
        }

        List<Menu> menus = menuDAO.findByRestaurantId(restaurantId);

        List<MenuDTO> dtoList = menus.stream()
                .map(menu -> {
                    MenuDTO dto = new MenuDTO();
                    dto.setId(menu.getId());
                    dto.setTitle(menu.getTitle());
                    return dto;
                })
                .collect(Collectors.toList());

        return new DataResult<>(200, dtoList);
    }





    public DataResult<List<FoodDTO>> getItemsByRestaurant(UUID restaurantId, UUID sellerId) {
        // 1. بررسی وجود رستوران
        Optional<Restaurant> optRest = dao.findById(restaurantId);
        if (optRest.isEmpty()) {
            return new DataResult<>(404, null);
        }
        Restaurant restaurant = optRest.get();

        // 2. بررسی مالکیت
        if (!restaurant.getSeller().getId().equals(sellerId)) {
            return new DataResult<>(403, null);
        }

        // 3. واکشی تمام آیتم‌های غذا برای آن رستوران
        List<Food> foods = foodDAO.findAllByRestaurant(restaurantId);

        // 4. نگاشت به لیست FoodDTO
        List<FoodDTO> dtoList = foods.stream()
                .map(food -> {
                    FoodDTO dto = new FoodDTO();
                    dto.setId(food.getId());
                    dto.setName(food.getName());
                    dto.setImageBase64(food.getImageBase64());
                    dto.setDescription(food.getDescription());
                    dto.setRestaurantId(restaurantId);
                    dto.setPrice(food.getPrice());
                    dto.setSupply(food.getSupply());
                    dto.setRate(food.getRate());
                    dto.setCategories(food.getCategories());
                    return dto;
                })
                .collect(Collectors.toList());

        // 5. بازگشت نتیجه موفق
        return new DataResult<>(200, dtoList);
    }
















    public DataResult<List<RestaurantDTO>> getSellerRestaurants(UUID sellerId) {
        // 1. واکشی رستوران‌ها از DAO
        List<Restaurant> restaurants = dao.findBySellerId(sellerId);

        // 2. نگاشت هر Restaurant به RestaurantDTO
        List<RestaurantDTO> dtoList = restaurants.stream()
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

        // 3. بسته‌بندی در DataResult و بازگشت
        return new DataResult<>(200, dtoList);
    }















    public List<FoodDTO> searchFoods(UUID restaurantId, String search, List<String> categories) {
        return dao.searchFoods(restaurantId, search, categories);
    }
    private String getCourierName(UUID courierId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User courier = session.get(User.class, courierId);
            return courier != null ? courier.getFullName() : "نامشخص";
        } catch (Exception e) {
            return null;
        }
    }
}