package ma.ensah.soutenance.model.dao.impl;

import ma.ensah.soutenance.model.dao.DashboardDao;
import ma.ensah.soutenance.util.HibernateUtil;
import org.hibernate.Session;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implémentation Hibernate des requêtes statistiques du dashboard.
 */
public class DashboardDaoImpl implements DashboardDao {

    // ─────────────────────────────────────────────────────
    //  Compteurs globaux
    // ─────────────────────────────────────────────────────

    @Override
    public long countEtudiants() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(e) from Etudiant e", Long.class)
                          .uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    @Override
    public long countProfesseurs() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(p) from Professeur p", Long.class)
                          .uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    @Override
    public long countSoutenances() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(s) from Soutenance s", Long.class)
                          .uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    @Override
    public long countSalles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(s) from Salle s", Long.class)
                          .uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ─────────────────────────────────────────────────────
    //  Par professeur
    // ─────────────────────────────────────────────────────

    @Override
    public Map<String, Long> countEtudiantsParProf() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Compte les étudiants dans les groupes_pfe par encadrant
            List<Object[]> rows = session.createQuery(
                "select g.encadrant.nom, g.encadrant.prenom, count(e) " +
                "from GroupPfe g join g.etudiants e " +
                "group by g.encadrant.id, g.encadrant.nom, g.encadrant.prenom " +
                "order by g.encadrant.nom",
                Object[].class).list();
            for (Object[] row : rows) {
                String key = row[0] + " " + row[1];
                Long val = ((Number) row[2]).longValue();
                result.merge(key, val, Long::sum);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    @Override
    public Map<String, Long> countSoutenancesParProf() {
        // Toutes les participations : encadrant + membreInfo + membreMath
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Encadrant
            addProfCounts(session, result,
                "select s.encadrant.nom, s.encadrant.prenom, count(s) " +
                "from Soutenance s group by s.encadrant.id, s.encadrant.nom, s.encadrant.prenom");
            // MembreInfo
            addProfCounts(session, result,
                "select s.membreInfo.nom, s.membreInfo.prenom, count(s) " +
                "from Soutenance s group by s.membreInfo.id, s.membreInfo.nom, s.membreInfo.prenom");
            // MembreMath
            addProfCounts(session, result,
                "select s.membreMath.nom, s.membreMath.prenom, count(s) " +
                "from Soutenance s group by s.membreMath.id, s.membreMath.nom, s.membreMath.prenom");
        } catch (Exception e) { e.printStackTrace(); }
        // Trier par valeur décroissante
        return sortByValueDesc(result);
    }

    @Override
    public Map<String, Long> countSoutenancesEncadrantParProf() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            addProfCounts(session, result,
                "select s.encadrant.nom, s.encadrant.prenom, count(s) " +
                "from Soutenance s group by s.encadrant.id, s.encadrant.nom, s.encadrant.prenom " +
                "order by s.encadrant.nom");
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    @Override
    public Map<String, Long> countChargeJuryParProf() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            addProfCounts(session, result,
                "select s.membreInfo.nom, s.membreInfo.prenom, count(s) " +
                "from Soutenance s group by s.membreInfo.id, s.membreInfo.nom, s.membreInfo.prenom");
            addProfCounts(session, result,
                "select s.membreMath.nom, s.membreMath.prenom, count(s) " +
                "from Soutenance s group by s.membreMath.id, s.membreMath.nom, s.membreMath.prenom");
        } catch (Exception e) { e.printStackTrace(); }
        return sortByValueDesc(result);
    }

    // ─────────────────────────────────────────────────────
    //  Par filière
    // ─────────────────────────────────────────────────────

    @Override
    public Map<String, Long> countSoutenancesParFiliere() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                "select s.etudiant.filiere, count(s) from Soutenance s " +
                "group by s.etudiant.filiere order by s.etudiant.filiere",
                Object[].class).list();
            for (Object[] row : rows) {
                result.put((String) row[0], ((Number) row[1]).longValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    @Override
    public Map<String, Long> countEtudiantsParFiliere() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                "select e.filiere, count(e) from Etudiant e " +
                "group by e.filiere order by e.filiere",
                Object[].class).list();
            for (Object[] row : rows) {
                result.put((String) row[0], ((Number) row[1]).longValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // ─────────────────────────────────────────────────────
    //  Par salle
    // ─────────────────────────────────────────────────────

    @Override
    public Map<String, Long> countSoutenancesParSalle() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                "select s.salle.nom, count(s) from Soutenance s " +
                "group by s.salle.id, s.salle.nom order by s.salle.nom",
                Object[].class).list();
            for (Object[] row : rows) {
                result.put((String) row[0], ((Number) row[1]).longValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // ─────────────────────────────────────────────────────
    //  Par date
    // ─────────────────────────────────────────────────────

    @Override
    public Map<String, Long> countSoutenancesParDate() {
        Map<String, Long> result = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                "select s.dateSoutenance, count(s) from Soutenance s " +
                "group by s.dateSoutenance order by s.dateSoutenance",
                Object[].class).list();
            for (Object[] row : rows) {
                String dateStr = row[0] instanceof java.util.Date
                    ? sdf.format((java.util.Date) row[0])
                    : row[0].toString();
                result.put(dateStr, ((Number) row[1]).longValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // ─────────────────────────────────────────────────────
    //  Spécialité profs
    // ─────────────────────────────────────────────────────

    @Override
    public Map<String, Long> countProfsParSpecialite() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                "select p.specialite, count(p) from Professeur p " +
                "group by p.specialite order by p.specialite",
                Object[].class).list();
            for (Object[] row : rows) {
                result.put((String) row[0], ((Number) row[1]).longValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // ─────────────────────────────────────────────────────
    //  Helpers privés
    // ─────────────────────────────────────────────────────

    private void addProfCounts(Session session, Map<String, Long> result, String hql) {
        List<Object[]> rows = session.createQuery(hql, Object[].class).list();
        for (Object[] row : rows) {
            String key = row[0] + " " + row[1];
            Long val = ((Number) row[2]).longValue();
            result.merge(key, val, Long::sum);
        }
    }

    private Map<String, Long> sortByValueDesc(Map<String, Long> map) {
        Map<String, Long> sorted = new LinkedHashMap<>();
        map.entrySet().stream()
           .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
           .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }
}
