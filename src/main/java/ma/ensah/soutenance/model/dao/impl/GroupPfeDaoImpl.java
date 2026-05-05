package ma.ensah.soutenance.model.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ma.ensah.soutenance.model.dao.GroupPfeDao;
import ma.ensah.soutenance.model.entity.GroupPfe;
import ma.ensah.soutenance.util.HibernateUtil;

public class GroupPfeDaoImpl implements GroupPfeDao {

    @Override
    public void save(GroupPfe groupPfe) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.save(groupPfe);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @Override
    public List<GroupPfe> findAllWithDetails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "select distinct g from GroupPfe g " +
                "left join fetch g.encadrant " +
                "left join fetch g.etudiants", 
                GroupPfe.class
            ).list();
        }
    }
    
    @Override
    public List<GroupPfe> findAllWithDetailsByVersion(Long versionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "select distinct g from GroupPfe g " +
                "left join fetch g.encadrant " +
                "left join fetch g.etudiants " +
                "where g.version.id = :versionId",
                GroupPfe.class
            )
            .setParameter("versionId", versionId)
            .list();
        }
    }
    
    
    
}