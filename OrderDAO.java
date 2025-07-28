package DAO;

import entity.Order;
import entity.Restaurant;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    //==========================================================================
    // 1) SAVE / UPDATE
    //==========================================================================

    /** ذخیره سفارش و آیتم‌ها بصورت خودکار تراکنش باز و بسته می‌کند */
    public Order saveOrder(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(order);
            session.getTransaction().commit();
            return order;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
        }
    }

    /** ذخیره یا بروزرسانی درون تراکنش موجود */
    public Order saveOrder(Order order, Session session) {
        session.saveOrUpdate(order);
        return order;
    }

    /** بروزرسانی سفارش با تراکنش مجزا */
    public void update(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(order);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order: " + e.getMessage(), e);
        }
    }

    /** بروزرسانی درون تراکنش موجود */
    public void update(Order order, Session session) {
        session.merge(order);
    }

    //==========================================================================
    // 2) FIND BY ID
    //==========================================================================

    /** بارگذاری سفارش بر اساس شناسه با باز و بسته کردن خودکار Session */
    public Optional<Order> findById(UUID orderId) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Order.class, orderId));
        }
    }

    /** بارگذاری سفارش درون Session جاری */
    public Optional<Order> findById(UUID orderId, Session session) {
        return Optional.ofNullable(session.get(Order.class, orderId));
    }

    //==========================================================================
    // 3) BASIC LIST QUERIES
    //==========================================================================

    /** فراخوانی همه سفارش‌ها با HQL ساده */
    public List<Order> findAll() {
        try (Session session = sessionFactory.openSession()) {
            // 1. obtain CriteriaBuilder & build a CriteriaQuery for Order
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);

            // 2. define the root—this is equivalent to "FROM Order"
            Root<Order> orderRoot = cq.from(Order.class);

            // 3. select the root (all Order instances)
            cq.select(orderRoot);

            // 4. execute and return
            return session.createQuery(cq).getResultList();
        }
    }

    /**
     * فیلترهای ترکیبی بر اساس vendorId، courierId، customerId، وضعیت و رشته جستجو
     * (آدرس، نام مشتری یا رستوران)
     */
    public List<Order> findAllWithFilters(
            UUID vendorId,
            UUID courierId,
            UUID customerId,
            String status,
            String search
    ) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);

            List<Predicate> preds = new ArrayList<>();

            if (vendorId != null) {
                preds.add(cb.equal(root.get("restaurant").get("id"), vendorId));
            }
            if (courierId != null) {
                preds.add(cb.equal(root.get("courierId"), courierId));
            }
            if (customerId != null) {
                preds.add(cb.equal(root.get("user").get("id"), customerId));
            }
            if (status != null && !status.isBlank()) {
                preds.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Join<Order, User> userJoin = root.join("user", JoinType.LEFT);
                Join<Order, Restaurant> restJoin = root.join("restaurant", JoinType.LEFT);
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("deliveryAddress")), pattern),
                        cb.like(cb.lower(userJoin.get("name")), pattern),
                        cb.like(cb.lower(restJoin.get("name")), pattern)
                ));
            }

            cq.where(preds.toArray(new Predicate[0]))
                    .orderBy(cb.desc(root.get("createdAt")));

            return session.createQuery(cq).getResultList();
        }
    }

    //==========================================================================
    // 4) RESTAURANT-SPECIFIC QUERIES
    //==========================================================================

    /**
     * سفارش‌های یک رستوران با فیلتر وضعیت، نام مشتری، شناسه مشتری و پیک
     * (Criteria API)
     */
    public List<Order> getOrdersByRestaurant(
            UUID restaurantId,
            String status,
            String searchCustomerName,
            UUID userId,
            UUID courierId
    ) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);

            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("restaurant").get("id"), restaurantId));

            if (status != null && !status.isBlank()) {
                preds.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (searchCustomerName != null && !searchCustomerName.isBlank()) {
                Join<Order, User> userJoin = root.join("user", JoinType.INNER);
                preds.add(cb.like(
                        cb.lower(userJoin.get("name")),
                        "%" + searchCustomerName.toLowerCase() + "%"
                ));
            }
            if (userId != null) {
                preds.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (courierId != null) {
                preds.add(cb.equal(root.get("deliveryMan").get("id"), courierId));
            }

            cq.where(cb.and(preds.toArray(new Predicate[0])))
                    .orderBy(cb.desc(root.get("orderedDateTime")));

            return session.createQuery(cq).getResultList();
        }
    }

    //==========================================================================
    // 5) ORDER HISTORY
    //==========================================================================

    /** تاریخچه‌ی سفارش بر اساس خریدار با Criteria API */
    public List<Order> getOrderHistory(
            UUID buyerId,
            String search,
            String vendorId
    ) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);

            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("user").get("id"), buyerId));

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate p1 = cb.like(cb.lower(root.get("deliveryAddress")), pattern);
                Predicate p2 = cb.like(cb.lower(root.get("status")), pattern);
                preds.add(cb.or(p1, p2));
            }
            if (vendorId != null && !vendorId.trim().isEmpty()) {
                preds.add(cb.equal(root.get("vendorId"), UUID.fromString(vendorId)));
            }

            cq.where(cb.and(preds.toArray(new Predicate[0])))
                    .orderBy(cb.desc(root.get("orderedDateTime")));

            return session.createQuery(cq).getResultList();
        }
    }

    /** تاریخچه‌ی سفارش با HQL و JOIN FETCH (نیازی به Criteria نیست) */
    public List<Order> getOrderHistory(
            UUID buyerId,
            String search,
            String vendor,
            Session session
    ) {
        try {
            // 1. CriteriaBuilder و CriteriaQuery بگیرید
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> o = cq.from(Order.class);

            // 2. JOIN FETCHها (بدون ارجاع به کلاس OrderItem)
            o.fetch("user", JoinType.INNER);
            o.fetch("restaurant", JoinType.INNER);
            Fetch<?, ?> items = o.fetch("orderItems", JoinType.LEFT);
            items.fetch("food", JoinType.LEFT);

            // 3. ساخت لیست Predicateها
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(o.get("user").get("id"), buyerId));

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                predicates.add(cb.or(
                        cb.like(o.get("deliveryAddress"), pattern),
                        cb.like(o.get("status"), pattern)
                ));
            }

            if (vendor != null && !vendor.trim().isEmpty()) {
                predicates.add(cb.like(
                        o.get("restaurant").get("name"),
                        "%" + vendor.trim() + "%"
                ));
            }

            // 4. اعمال DISTINCT و WHERE
            cq.select(o)
                    .distinct(true)
                    .where(predicates.toArray(new Predicate[0]));

            // 5. اجرای کوئری
            return session.createQuery(cq).getResultList();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch order history (Criteria API)", ex);
        }
    }

    //==========================================================================
    // 6) USER FROM TOKEN
    //==========================================================================

    public User findUserByToken(String token) {
        try (Session session = sessionFactory.openSession()) {
            String subject = util.JwtUtil.validateToken(token);
            return session.get(User.class, UUID.fromString(subject));
        }
    }

    //==========================================================================
    // 7) FAVORITES
    //==========================================================================

    /** اضافه کردن علاقه‌مندی با تراکنش خودکار */
    public void addFavorite(UUID userId, UUID restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            Restaurant r = session.get(Restaurant.class, restaurantId);
            user.addFavorite(r);
            session.merge(user);
            tx.commit();
        }
    }

    /** اضافه کردن علاقه‌مندی درون Session موجود */
    public void addFavorite(UUID userId, UUID restaurantId, Session session) {
        // شروع تراکنش (اگر هنوز آغاز نشده)
        Transaction tx = session.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }

        try {
            // بارگذاری کاربر و رستوران
            User user = session.get(User.class, userId);
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);

            // افزودن علاقه‌مندی
            user.addFavorite(restaurant);
            // اگر رابطه CascadeType.MERGE یا CascadeType.ALL تنظیم شده،
            // Hibernate خودش تغییر را تشخیص می‌دهد، ولی می‌توانید صراحتاً هم بگویید:
            session.merge(user);

            // بدون canCommit(): فقط اگر تراکنش فعال است commit کن
            if (tx.isActive()) {
                tx.commit();
            }
        } catch (RuntimeException ex) {
            // در صورت بروز خطا، رول‌بک کن
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }


    /** حذف علاقه‌مندی با تراکنش خودکار */
    public void removeFavorite(UUID userId, UUID restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            Restaurant r = session.get(Restaurant.class, restaurantId);
            user.removeFavorite(r);
            session.merge(user);
            tx.commit();
        }
    }

    /** حذف علاقه‌مندی درون Session موجود */
    public void removeFavorite(UUID userId, UUID restaurantId, Session session) {
        Transaction tx = session.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }

        try {
            // بارگذاری User و Restaurant
            User user = session.get(User.class, userId);
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);

            // حذف علاقه‌مندی
            user.removeFavorite(restaurant);

            // با توجه به تنظیمات Cascade، می‌توانید از merge یا update استفاده کنید
            session.merge(user);

            // کامیت تراکنش
            if (tx.isActive()) {
                tx.commit();
            }
        } catch (RuntimeException ex) {
            // رول‌بک در صورت هر خطا
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    /** خواندن لیست علاقه‌مندی‌ها از موجودیت User */
    public List<Restaurant> getFavorites(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, userId);
            return user.getFavorites();
        }
    }

    /** خواندن لیست علاقه‌مندی‌ها با HQL JOIN */
    public List<Restaurant> getFavoritesByJoin(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT r FROM Restaurant r JOIN r.favoritedBy u WHERE u.id = :userId";
            return session.createQuery(hql, Restaurant.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }
}