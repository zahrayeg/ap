package DAO;

import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();

    public UserDAO() { }

    public void save(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();


            Query<User> query = session.createQuery(
                    "FROM entity.User WHERE phone = :phone", User.class);
            query.setParameter("phone", user.getPhone());
            User existing = query.uniqueResult();
            if (existing != null) {
                session.getTransaction().rollback();
                throw new RuntimeException("Phone number already exists");
            }

            session.save(user);
            session.getTransaction().commit();
        }
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from entity.User", User.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
    }

    public Optional<User> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        }
    }

    public Optional<User> findByPhone(String phone) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM entity.User WHERE phone = :phone", User.class);
            query.setParameter("phone", phone);
            return Optional.ofNullable(query.uniqueResult());
        }
    }


    public boolean isPhoneTaken(String phone) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);

            cq.select(root)
                    .where(cb.equal(root.get("phone"), phone));

            List<User> result = session.createQuery(cq)
                    .setMaxResults(1)
                    .getResultList();

            return !result.isEmpty();
        }
    }



















    public void deleteById(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            session.remove(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user by id: " + e.getMessage(), e);
        }
    }

    public void update(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        }
    }

    public void delete(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(user);
            session.getTransaction().commit();
        }
    }

    public User findUserByToken(String token) {
        try (Session session = sessionFactory.openSession()) {
            String subject = util.JwtUtil.validateToken(token);
            return session.get(User.class, UUID.fromString(subject));
        }
    }
}