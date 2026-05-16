package ma.ensah.soutenance.service;

import ma.ensah.soutenance.model.dto.DashboardStats;

/**
 * Service exposant les statistiques du dashboard.
 */
public interface DashboardService {
    DashboardStats getStats();
}


