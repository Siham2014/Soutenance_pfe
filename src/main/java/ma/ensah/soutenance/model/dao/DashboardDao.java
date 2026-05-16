package ma.ensah.soutenance.model.dao;

import java.util.List;
import java.util.Map;

/**
 * DAO dédié aux requêtes statistiques pour le dashboard.
 */
public interface DashboardDao {

    // ── Compteurs globaux ─────────────────────────────────
    long countEtudiants();
    long countProfesseurs();
    long countSoutenances();
    long countSalles();

    // ── Par professeur ────────────────────────────────────
    /** Nombre d'étudiants encadrés par professeur (via groupes_pfe) */
    Map<String, Long> countEtudiantsParProf();

    /** Nombre de soutenances où le prof est encadrant + jury (toutes participations) */
    Map<String, Long> countSoutenancesParProf();

    /** Nombre de soutenances où le prof est uniquement encadrant */
    Map<String, Long> countSoutenancesEncadrantParProf();

    /** Charge jury : participations membreInfo ou membreMath par prof */
    Map<String, Long> countChargeJuryParProf();

    // ── Par filière ───────────────────────────────────────
    /** Nombre de soutenances par filière (via l'étudiant) */
    Map<String, Long> countSoutenancesParFiliere();

    /** Nombre d'étudiants par filière */
    Map<String, Long> countEtudiantsParFiliere();

    // ── Par salle ─────────────────────────────────────────
    Map<String, Long> countSoutenancesParSalle();

    // ── Par date ──────────────────────────────────────────
    Map<String, Long> countSoutenancesParDate();

    // ── Spécialité profs ──────────────────────────────────
    Map<String, Long> countProfsParSpecialite();
}
