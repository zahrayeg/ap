package DAO;

import entity.Transaction;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {
    private final SessionFactory sessionFactory;

    public TransactionDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * ذخیره یا به‌روزرسانی تراکنش
     */
    public void saveTransaction(Transaction transaction) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(transaction);
            session.getTransaction().commit();
        }
    }

    /**
     * واکشی تراکنش‌ها بر اساس شناسه کاربر
     */
    public List<Transaction> findByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM Transaction t WHERE t.user.id = :userId", Transaction.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    /**
     * جستجو تراکنش‌ها بر اساس متنِ فیلد type
     */
    public List<Transaction> findByType(String search) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
            Root<Transaction> root = cq.from(Transaction.class);

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate byType = cb.like(cb.lower(root.get("type")), pattern);
                cq.where(byType);
            }

            return session.createQuery(cq).getResultList();
        }
    }
}