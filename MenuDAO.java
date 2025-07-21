package DAO;

import Entity.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class MenuDAO {

    public void save(Menu menu) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(menu);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to save menu: " + e.getMessage());
        }
    }

    public Menu findByRestaurantAndTitle(UUID restaurantId, String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);
            Root<Menu> root = cq.from(Menu.class);

            cq.select(root).where(
                    cb.and(
                            cb.equal(root.get("restaurant").get("id"), restaurantId),
                            cb.equal(root.get("title"), title)
                    )
            );

            return session.createQuery(cq).uniqueResult();
        }
    }
    public List<Menu> findMenusByRestaurant(UUID restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);
            Root<Menu> root = cq.from(Menu.class);

            cq.select(root).where(cb.equal(root.get("restaurant").get("id"), restaurantId));

            return session.createQuery(cq).getResultList();
        }
    }

    public void delete(Menu menu) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(menu);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to delete menu: " + e.getMessage());
        }
    }

    public void addItemToMenu(Menu menu, Food item) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            menu.addItem(item);
            session.update(menu);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to add item: " + e.getMessage());
        }
    }

    public void removeItemFromMenu(Menu menu, Food item) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            menu.removeItem(item);
            session.update(menu);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to remove item: " + e.getMessage());
        }
    }
}