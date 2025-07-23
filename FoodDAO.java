package DAO;
import org.hibernate.SessionFactory;
import util.HibernateUtil;
import entity.Food;
import org.hibernate.Session;
import java.util.Optional;
import java.util.UUID;

public class FoodDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public void save(Food item) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(item);
            session.getTransaction().commit();
        }
    }
    public void update(Food item) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(item);
            session.getTransaction().commit();
        }
    }

    public void delete(Food item) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(item);
            session.getTransaction().commit();
        }
    }

    public Optional<Food> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            Food item = session.get(Food.class, id);
            return Optional.ofNullable(item);
        }
    }
}