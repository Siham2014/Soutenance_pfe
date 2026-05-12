package ma.ensah.soutenance.model.dao.impl;

import ma.ensah.soutenance.model.dao.SalleDao;
import ma.ensah.soutenance.model.entity.Salle;
import ma.ensah.soutenance.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class SalleDaoImpl implements SalleDao {

    @Override
    public void save(Salle salle) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(salle);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public List<Salle> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Salle", Salle.class).list();
        }
    }

    // Vide la table salles avant chaque import
    @Override
    public void deleteAll() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE salles").executeUpdate();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }
}