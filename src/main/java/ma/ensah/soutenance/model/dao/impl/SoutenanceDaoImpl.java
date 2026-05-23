package ma.ensah.soutenance.model.dao.impl;

import ma.ensah.soutenance.model.dao.SoutenanceDao;
import ma.ensah.soutenance.model.entity.Soutenance;
import ma.ensah.soutenance.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SoutenanceDaoImpl implements SoutenanceDao {

    @Override
    public void save(Soutenance soutenance) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(soutenance);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public List<Soutenance> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "select distinct s from Soutenance s " +
                "left join fetch s.etudiant " +
                "left join fetch s.encadrant " +
                "left join fetch s.membreInfo " +
                "left join fetch s.membreMath " +
                "left join fetch s.salle " +
                "order by s.dateSoutenance, s.heureDebut",
                Soutenance.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Vide la table soutenances avant chaque génération
    @Override
    public void deleteAll() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE soutenances").executeUpdate();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    //  Contrainte 1 : salle pas occupée au même créneau
    @Override
    public boolean isSalleOccupee(Long salleId, Date date, String heureDebut) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "select count(s) from Soutenance s " +
                "where s.salle.id = :salleId " +
                "and s.dateSoutenance = :date " +
                "and s.heureDebut = :heureDebut",
                Long.class)
                .setParameter("salleId", salleId)
                .setParameter("date", date)
                .setParameter("heureDebut", heureDebut)
                .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Contrainte 2 : prof pas occupé au même créneau
    @Override
    public boolean isProfOccupe(Long profId, Date date, String heureDebut) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "select count(s) from Soutenance s " +
                "where (s.encadrant.id = :profId " +
                "   or s.membreInfo.id = :profId " +
                "   or s.membreMath.id = :profId) " +
                "and s.dateSoutenance = :date " +
                "and s.heureDebut = :heureDebut",
                Long.class)
                .setParameter("profId", profId)
                .setParameter("date", date)
                .setParameter("heureDebut", heureDebut)
                .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //  Contrainte 3 : soutenances du prof pour vérifier 1h de repos
    @Override
    public List<Soutenance> findByProf(Long profId, Date date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "from Soutenance s " +
                "where (s.encadrant.id = :profId " +
                "   or s.membreInfo.id = :profId " +
                "   or s.membreMath.id = :profId) " +
                "and s.dateSoutenance = :date " +
                "order by s.heureDebut",
                Soutenance.class)
                .setParameter("profId", profId)
                .setParameter("date", date)
                .list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    @Override
    public Soutenance findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "select s from Soutenance s " +
                "left join fetch s.etudiant " +
                "left join fetch s.encadrant " +
                "left join fetch s.membreInfo " +
                "left join fetch s.membreMath " +
                "left join fetch s.salle " +
                "where s.id = :id",
                Soutenance.class
            )
            .setParameter("id", id)
            .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    @Override
    public List<Soutenance> findByEtudiantAndDate(
            String nomEtudiant,
            Date dateSoutenance) {

        try(Session session =
                HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(

                "select s from Soutenance s " +
                "where lower(s.etudiant.nom) = :nom " +
                "and s.dateSoutenance = :date",

                Soutenance.class
            )

            .setParameter("nom",
                    nomEtudiant.toLowerCase())

            .setParameter("date",
                    dateSoutenance)

            .list();

        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
