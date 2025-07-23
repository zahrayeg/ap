package DAO;
import org.hibernate.SessionFactory;
import util.HibernateUtil;
import entity.Food;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
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


    public List<Food> findByRestaurantId(UUID restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Food> cq = cb.createQuery(Food.class);
            Root<Food> root = cq.from(Food.class);

            cq.select(root)
                    .where(cb.equal(root.get("restaurant").get("id"), restaurantId));

            return session.createQuery(cq).getResultList();
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