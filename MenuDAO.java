package DAO;

import entity.Food;
import entity.Menu;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MenuDAO {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public void save(Menu menu) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(menu);
            session.getTransaction().commit();
        }
    }

    public List<Menu> findByRestaurantId(UUID restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);
            Root<Menu> root = cq.from(Menu.class);

            cq.select(root)
                    .where(cb.equal(root.get("restaurant").get("id"), restaurantId));

            return session.createQuery(cq).getResultList();
        }
    }



    public Optional<Menu> findByRestaurantAndTitle(UUID restId, String title) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);

            Root<Menu> root = cq.from(Menu.class);
            root.fetch("items", JoinType.LEFT);

            Predicate byRest  = cb.equal(root.get("restaurant").get("id"), restId);
            Predicate byTitle = cb.equal(root.get("title"), title);
            cq.where(cb.and(byRest, byTitle));

            Menu menu = session.createQuery(cq).uniqueResult();
            return Optional.ofNullable(menu);
        }
    }

    public void delete(Menu menu) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(menu);
            session.getTransaction().commit();
        }
    }



    public void update(Menu menu) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(menu);
            session.getTransaction().commit();
        }
    }

    public Optional<Menu> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            Menu m = session.get(Menu.class, id);
            return Optional.ofNullable(m);
        }
    }

    public List<Menu> findMenusByFoodId(UUID foodId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);

            Root<Menu> root = cq.from(Menu.class);

            Join<Menu, Food> join = root.join("items", JoinType.INNER);

            cq.where(cb.equal(join.get("id"), foodId));

            return session.createQuery(cq).getResultList();
        }
    }









}