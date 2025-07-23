package DAO;

import entity.Restaurant;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestaurantDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();

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

    public List<Restaurant> findBySellerId(UUID sellerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM entity.Restaurant r WHERE r.seller.id = :sellerId", Restaurant.class)
                    .setParameter("sellerId", sellerId)
                    .list();
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

    public List<Restaurant> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<Restaurant> query = session.createQuery("FROM entity.Restaurant", Restaurant.class);
            return query.list();
        }
    }




}