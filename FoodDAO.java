package DAO;

import Entity.Food;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class FoodDAO {

    public void save(Food food) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(food);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public List<Food> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Food> cq = cb.createQuery(Food.class);
            Root<Food> root = cq.from(Food.class);
            cq.select(root);
            return session.createQuery(cq).getResultList();
        }
    }

    public void delete(Food food) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(food);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void update(Food food) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(food);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Food findById(UUID foodId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Food.class, foodId);
        }
    }

    public List<Food> findAllByRestaurant(UUID restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Food> cq = cb.createQuery(Food.class);
            Root<Food> root = cq.from(Food.class);

            cq.select(root).where(cb.equal(root.get("restaurant").get("id"), restaurantId));

            return session.createQuery(cq).getResultList();
        }
    }
}