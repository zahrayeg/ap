package DAO;

import java.util.ArrayList;

import entity.Order;
import entity.Restaurant;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();


    public Order saveOrder(Order order) {
        System.out.println("Saving order: " + order.getId());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(order); // ذخیره سفارش و آیتم‌های مرتبط
            session.getTransaction().commit();
            return order;
        } catch (Exception e) {
            System.err.println("Error in saveOrder: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save order: " + e.getMessage());
        }
    }

    public List<Order> getOrdersByRestaurant(UUID restaurantId, String status, String searchCustomerName, UUID userId, UUID courierId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);

            List<Predicate> preds = new ArrayList<>();


            preds.add(cb.equal(root.get("restaurant").get("id"), restaurantId));

            if (status != null && !status.isBlank()) {
                preds.add(cb.equal(
                        cb.lower(root.get("status")),
                        status.toLowerCase()
                ));
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

            cq.where(cb.and(preds.toArray(new Predicate[0])));
            cq.orderBy(cb.desc(root.get("orderedDateTime")));

            return session.createQuery(cq).getResultList();
        }
    }


    public Optional<Order> findById(UUID orderId) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Order.class, orderId));
        }
    }

    public void update(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(order);
            session.getTransaction().commit();
        }
    }




    public User findUserByToken(String token) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String subject = util.JwtUtil.validateToken(token);
            return session.get(User.class, java.util.UUID.fromString(subject));
        } catch (Exception e) {
            System.err.println("Error in findUserByToken: " + e.getMessage());
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }


    public List<Order> getOrderHistory(UUID buyerId,
                                       String search,
                                       String vendorId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    root.get("user").get("id"),
                    buyerId
            ));

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate pAddress = cb.like(
                        cb.lower(root.get("deliveryAddress")),
                        pattern
                );
                Predicate pStatus = cb.like(
                        cb.lower(root.get("status")),
                        pattern
                );
                predicates.add(cb.or(pAddress, pStatus));
            }
            if (vendorId != null && !vendorId.trim().isEmpty()) {
                predicates.add(cb.equal(
                        root.get("vendorId"),
                        UUID.fromString(vendorId)
                ));
            }

            cq.where(cb.and(predicates.toArray(new Predicate[0])));
            cq.orderBy(cb.desc(root.get("orderedDateTime")));

            return session.createQuery(cq).getResultList();
        }
    }


    public void addFavorite(UUID userId, UUID restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant not found");
            }
            user.addFavorite(restaurant);
            session.merge(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in addFavorite: " + e.getMessage());
            throw new RuntimeException("Failed to add favorite: " + e.getMessage());
        }
    }
    public void removeFavorite(UUID userId, UUID restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new IllegalArgumentException("Restaurant not found");
            }
            user.removeFavorite(restaurant);
            session.merge(user);
            session.getTransaction().commit();
        }
        catch (Exception e) {
            System.err.println("Error in removeFavorite: " + e.getMessage());
            throw new RuntimeException("Failed to remove favorite: " + e.getMessage());
        }
    }
    public List<Restaurant> getFavorites(UUID userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user.getFavorites();
        } catch (Exception e) {
            System.err.println("Error in getFavorites: " + e.getMessage());
            throw new RuntimeException("Failed to fetch favorites: " + e.getMessage());
        }
    }
}