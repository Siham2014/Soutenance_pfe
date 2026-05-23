package ma.ensah.soutenance.model.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ma.ensah.soutenance.model.dao.VersionRepartitionDao;
import ma.ensah.soutenance.model.entity.VersionRepartition;
import ma.ensah.soutenance.util.HibernateUtil;

public class VersionRepartitionDaoImpl implements VersionRepartitionDao {

    @Override
    public void save(VersionRepartition version) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.save(version);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
    }
}