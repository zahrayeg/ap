package service;
import DAO.RestaurantDAO;
import dto.MenuItemDTO;
import DAO.FoodDAO;
import DAO.MenuDAO;
import DAO.OrderDAO;
import dto.RestaurantDTO;
import dto.FoodDTO;
import Entity.Restaurant;
import Entity.Food;
import Entity.Menu;
import Entity.Order;
import exception.BadRequestException;
import exception.ConflictException;
import exception.ForbiddenException;
import exception.NotFoundException;
import exception.InternalServerErrorException;
import dto.OrderDTO;
import java.util.List;

public class RestaurantService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final RestaurantDAO restaurantDAO;
    private final FoodDAO foodDAO;
    private final UserService userService;

    public RestaurantService() {
        this.restaurantDAO = new RestaurantDAO();
        this.foodDAO       = new FoodDAO();
        this.userService   = new UserService();

    }

    public RestaurantService(RestaurantDAO restaurantDAO,
                             FoodDAO foodDAO,
                             UserService userService) {
        this.restaurantDAO = restaurantDAO;
        this.foodDAO       = foodDAO;
        this.userService   = userService;
    }

    public Menu createRestaurantMenu(int restaurantId,
                                     String title,
                                     int sellerId) {
        userService.validateSeller(sellerId);

        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Resource not found");
        }
        if (restaurant.getSeller().getId() != sellerId) {
            throw new ForbiddenException("Forbidden request");
        }
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Invalid `field name`");
        }

        MenuDAO menuDAO = new MenuDAO();
        boolean exists = menuDAO
                .findByTitleAndRestaurant(title, restaurantId)
                .stream()
                .findAny()
                .isPresent();
        if (exists) {
            throw new ConflictException("Conflict occurred");
        }

        Menu menu = new Menu();
        menu.setTitle(title);
        menu.setRestaurant(restaurant);
        menuDAO.save(menu);

        return menu;
    }


    /**
     * ایجاد یک رستوران جدید توسط فروشنده
     * @param dto       داده‌های رستوران
     * @param sellerId  شناسه فروشنده
     * @return          رستوران ذخیره‌شده
     */
    public Restaurant createRestaurant(RestaurantDTO dto, int sellerId) {
        // 1. اعتبارسنجی ورودی‌ها
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Invalid `name`");
        }
        if (dto.getAddress() == null || dto.getAddress().isBlank()) {
            throw new BadRequestException("Invalid `address`");
        }
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new BadRequestException("Invalid `phone`");
        }


        userService.validateSeller(sellerId);


        List<Restaurant> conflictList =
                restaurantDAO.findByNameAndSeller(dto.getName(), sellerId);
        if (!conflictList.isEmpty()) {
            throw new ConflictException("Conflict occurred");
        }


        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setLogoBase64(dto.getLogoBase64());
        restaurant.setTaxFee(dto.getTaxFee());
        restaurant.setAdditionalFee(dto.getAdditionalFee());
        restaurant.setSeller(userService.findById(sellerId));

        try {
            restaurantDAO.save(restaurant);
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal server error");
        }

        return restaurant;
    }

    /**
     * ویرایش اطلاعات رستوران
     * @param dto            داده‌های جدید
     * @param restaurantId   شناسه رستوران
     * @param sellerId       شناسه فروشنده
     * @return               رستوران ویرایش‌شده
     */
    public Restaurant updateRestaurant(RestaurantDTO dto,
                                       int restaurantId,
                                       int sellerId) {

        userService.validateSeller(sellerId);


        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Resource not found");
        }


        if (restaurant.getSeller().getId() != sellerId) {
            throw new ForbiddenException("Forbidden request");
        }


        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Restaurant name cannot be blank.");
        }
        if (dto.getAddress() == null || dto.getAddress().isBlank()) {
            throw new BadRequestException("Address is required.");
        }
        if (dto.getTaxFee() < 0) {
            throw new BadRequestException("Tax fee is required.");
        }


        boolean nameConflict = restaurantDAO
                .findByNameAndSeller(dto.getName(), sellerId)
                .stream()
                .anyMatch(r -> r.getId() != restaurantId);
        if (nameConflict) {
            throw new ConflictException("You already have another restaurant with the same name.");
        }


        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setLogoBase64(dto.getLogoBase64());
        restaurant.setTaxFee(dto.getTaxFee());
        restaurant.setAdditionalFee(dto.getAdditionalFee());

        restaurantDAO.update(restaurant);
        return restaurant;
    }

    /**
     * ایجاد یک آیتم غذا در رستوران
     */
    public Food createFoodItem(FoodDTO dto,
                               int restaurantId,
                               int sellerId) {

        userService.validateSeller(sellerId);

          Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Resource not found");
        }
        if (restaurant.getSeller().getId() != sellerId) {
            throw new ForbiddenException("Forbidden request");
        }


        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Invalid field name");
        }
        if (dto.getPrice() < 0) {
            throw new BadRequestException("Invalid field price");
        }
        if (dto.getSupply() < 0) {
            throw new BadRequestException("Invalid field supply");
        }
        if (dto.getKeywords() == null) {
            throw new BadRequestException("Invalid field keywords");
        }


        boolean exists = foodDAO
                .findByNameAndRestaurant(dto.getName(), restaurantId)
                .stream()
                .findAny()
                .isPresent();
        if (exists) {
            throw new ConflictException("Conflict occurred");
        }


        Food food = new Food();
        food.setName(dto.getName());
        food.setImageBase64(dto.getImageBase64());
        food.setDescription(dto.getDescription());
        food.setPrice(dto.getPrice());
        food.setSupply(dto.getSupply());
        food.setKeywords(dto.getKeywords());
        food.setRestaurant(restaurant);

        foodDAO.save(food);

         return food;
    }

    public void deleteRestaurantItem(int restaurantId, int itemId, int sellerId) {
        userService.validateSeller(sellerId);
        MenuDAO menuDAO = new MenuDAO();
        Restaurant r = restaurantDAO.findById(restaurantId);
        if (r == null) {
            throw new NotFoundException("Resource not found");  // 404
        }
        if (r.getSeller().getId() != sellerId) {
            throw new ForbiddenException("Forbidden request");  // 403
        }

        Food f = foodDAO.findById(itemId);
        if (f == null || f.getRestaurant().getId() != restaurantId) {
            throw new NotFoundException("Resource not found");  // 404
        }

        try {
            List<Menu> menus = menuDAO.findMenusContainingFood(itemId, restaurantId);
            for (Menu menu : menus) {
                menu.getItems().removeIf(food -> food.getId() == itemId);
               menuDAO.update(menu);
            }

            foodDAO.delete(f);
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal server error");  // 500
        }
    }

    public List<Restaurant> getMyRestaurants(int sellerId) {
        userService.validateSeller(sellerId);
        return restaurantDAO.findBySellerId(sellerId);
    }

    public Food updateRestaurantItem(FoodDTO dto, int restaurantId, int itemId, int sellerId) {
        userService.validateSeller(sellerId);
        Restaurant r = restaurantDAO.findById(restaurantId);
        if (r == null) throw new NotFoundException("Resource not found");
        if (r.getSeller().getId() != sellerId) throw new ForbiddenException("Forbidden request");
        Food f = foodDAO.findById(itemId);
        if (f == null || f.getRestaurant().getId() != restaurantId) throw new NotFoundException("Resource not found");
        if (dto.getName() == null || dto.getName().isBlank()) throw new BadRequestException("Invalid field name");
        if (dto.getPrice() < 0) throw new BadRequestException("Invalid field price");
        if (dto.getSupply() < 0) throw new BadRequestException("Invalid field supply");
        if (dto.getKeywords() == null) throw new BadRequestException("Invalid field keywords");
        boolean conflict = foodDAO
                .findByNameAndRestaurant(dto.getName(), restaurantId)
                .stream().anyMatch(x -> x.getId() != itemId);
        if (conflict) throw new ConflictException("Conflict occurred");
        f.setName(dto.getName());
        f.setImageBase64(dto.getImageBase64());
        f.setDescription(dto.getDescription());
        f.setPrice(dto.getPrice());
        f.setSupply(dto.getSupply());
        f.setKeywords(dto.getKeywords());
        foodDAO.update(f);
        return f;
    }

    public void addMenuItem(int restaurantId, String title, int itemId, int sellerId) {
        userService.validateSeller(sellerId);

        Restaurant r = restaurantDAO.findById(restaurantId);
        if (r == null) throw new NotFoundException("Resource not found");
        if (r.getSeller().getId() != sellerId) throw new ForbiddenException("Forbidden request");

        MenuDAO menuDAO = new MenuDAO();

        Menu m = menuDAO.findByTitleAndRestaurant(title, restaurantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        if (m.getItems().stream().anyMatch(f -> f.getId() == itemId))
            throw new ConflictException("Conflict occurred");

        menuDAO.addMenuItem(m.getId(), itemId);
    }


    public void deleteMenu(int restaurantId, String title, int sellerId) {

        userService.validateSeller(sellerId);

        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null)
            throw new NotFoundException("Resource not found");

        if (restaurant.getSeller().getId() != sellerId)
            throw new ForbiddenException("Forbidden request");

        MenuDAO menuDAO = new MenuDAO();

        Menu menu = menuDAO.findByTitleAndRestaurant(title, restaurantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        try {
            menuDAO.delete(menu);
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal server error");
        }
    }






    public void removeItemFromMenu(int restaurantId, String title, int itemId, int sellerId) {

        userService.validateSeller(sellerId);

        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null)
            throw new NotFoundException("Resource not found");

        if (restaurant.getSeller().getId() != sellerId)
            throw new ForbiddenException("Forbidden request");

        MenuDAO menuDAO = new MenuDAO();

        Menu menu = menuDAO.findByTitleAndRestaurant(title, restaurantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Food food = foodDAO.findById(itemId);
        if (food == null || !menu.getItems().contains(food))
            throw new NotFoundException("Resource not found");

        try {
            menuDAO.removeMenuItem(menu.getId(), itemId);
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal server error");
        }
    }

    public void updateOrderStatus(int restaurantId, long orderId, String newStatus, int sellerId) {
        userService.validateSeller(sellerId);

        // بارگذاری رستوران و بررسی مالکیت
        Restaurant restaurant = restaurantDAO.findById(restaurantId);
        if (restaurant == null) throw new NotFoundException("Resource not found");
        if (restaurant.getSeller().getId() != sellerId) throw new ForbiddenException("Forbidden request");
        OrderDAO orderDAO = new OrderDAO();
         Order order = orderDAO.findById(orderId);
        if (order == null) throw new NotFoundException("Resource not found");

         if (newStatus == null || newStatus.isBlank()) {
            throw new BadRequestException("Invalid `field name`");
        }
        List<String> validStatuses = List.of("pending", "accepted", "rejected", "delivered", "cancelled");
        if (!validStatuses.contains(newStatus.toLowerCase())) {
            throw new ConflictException("Conflict occurred");
        }

         try {
            order.setStatus(newStatus.toLowerCase());
            orderDAO.update(order);
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal server error");
        }
    }
    public List<OrderDTO> getOrdersForRestaurant(int restaurantId,
                                                 String status,
                                                 String search,
                                                 String userQuery,
                                                 String courierQuery,
                                                 int sellerId) {
        userService.validateSeller(sellerId);

        Restaurant r = restaurantDAO.findById(restaurantId);
        if (r == null) throw new NotFoundException("Resource not found");
        if (r.getSeller().getId() != sellerId) throw new ForbiddenException("Forbidden request");

   if (status != null && !List.of("submitted", "accepted", "rejected", "delivered").contains(status.toLowerCase())) {
            throw new BadRequestException("Invalid `status`");
        }

        return orderDAO.findByFilters(restaurantId, status, search, userQuery, courierQuery);
    }

}