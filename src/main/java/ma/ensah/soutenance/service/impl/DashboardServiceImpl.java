package ma.ensah.soutenance.service.impl;

import ma.ensah.soutenance.model.dao.DashboardDao;
import ma.ensah.soutenance.model.dao.impl.DashboardDaoImpl;
import ma.ensah.soutenance.model.dto.DashboardStats;
import ma.ensah.soutenance.service.DashboardService;

/**
 * Implémentation du service dashboard : agrège les données du DAO en un DTO.
 */
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDao dao;

    public DashboardServiceImpl() {
        this.dao = new DashboardDaoImpl();
    }

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        // Compteurs globaux
        stats.setTotalEtudiants((int) dao.countEtudiants());
        stats.setTotalProfesseurs((int) dao.countProfesseurs());
        stats.setTotalSoutenances((int) dao.countSoutenances());
        stats.setTotalSalles((int) dao.countSalles());

        // Par professeur
        stats.setEtudiantsParProf(dao.countEtudiantsParProf());
        stats.setSoutenancesParProf(dao.countSoutenancesParProf());
        stats.setSoutenancesEncadrantParProf(dao.countSoutenancesEncadrantParProf());
        stats.setChargeJuryParProf(dao.countChargeJuryParProf());

        // Par filière
        stats.setSoutenancesParFiliere(dao.countSoutenancesParFiliere());
        stats.setEtudiantsParFiliere(dao.countEtudiantsParFiliere());

        // Par salle
        stats.setSoutenancesParSalle(dao.countSoutenancesParSalle());

        // Par date
        stats.setSoutenancesParDate(dao.countSoutenancesParDate());

        // Spécialités
        stats.setProfsParSpecialite(dao.countProfsParSpecialite());

        return stats;
    }
}

