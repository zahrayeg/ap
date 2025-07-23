package DAO;

import entity.Restaurant;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestaurantDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public RestaurantDAO() {}

    public void save(Restaurant restaurant) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(restaurant);
            session.getTransaction().commit();
        }
    }

    public void update(Restaurant restaurant) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(restaurant);
            session.getTransaction().commit();
        }
    }

    public void delete(Restaurant restaurant) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(restaurant);
            session.getTransaction().commit();
        }
    }

    public Optional<Restaurant> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        }
    }

    /**
     * فهرست همه‌ی رستوران‌ها با Criteria API (بدون createQuery(String, Class))
     */
    public List<Restaurant> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Restaurant> cq = cb.createQuery(Restaurant.class);
            Root<Restaurant> root = cq.from(Restaurant.class);
            cq.select(root);
            return session.createQuery(cq).getResultList();
        }
    }

    /**
     * فهرست رستوران‌های متعلق به یک فروشنده با Criteria API
     */
    public List<Restaurant> findBySellerId(UUID sellerId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Restaurant> cq = cb.createQuery(Restaurant.class);
            Root<Restaurant> root = cq.from(Restaurant.class);

            // شرط sellerId = :sellerId
            cq.select(root)
                    .where(cb.equal(root.get("sellerId"), sellerId));

            return session.createQuery(cq).getResultList();
        }
    }
}