package ma.ensah.soutenance.model.dto;

import java.util.Map;

/**
 * DTO regroupant toutes les statistiques du dashboard.
 */
public class DashboardStats {

    // ── Compteurs globaux ─────────────────────────────────
    private int totalEtudiants;
    private int totalProfesseurs;
    private int totalSoutenances;
    private int totalSalles;

    // ── Statistiques par professeur ───────────────────────
    /** nom complet du prof → nombre d'étudiants encadrés */
    private Map<String, Long> etudiantsParProf;
    /** nom complet du prof → nombre de soutenances (encadrant + jury) */
    private Map<String, Long> soutenancesParProf;
    /** nom complet du prof → nombre de soutenances en tant qu'encadrant uniquement */
    private Map<String, Long> soutenancesEncadrantParProf;

    // ── Statistiques par filière ──────────────────────────
    /** filière → nombre de soutenances */
    private Map<String, Long> soutenancesParFiliere;
    /** filière → nombre d'étudiants */
    private Map<String, Long> etudiantsParFiliere;

    // ── Statistiques par salle ────────────────────────────
    /** nom salle → nombre de soutenances */
    private Map<String, Long> soutenancesParSalle;

    // ── Statistiques par date ─────────────────────────────
    /** date (string) → nombre de soutenances */
    private Map<String, Long> soutenancesParDate;

    // ── Spécialité des professeurs ────────────────────────
    /** spécialité → nombre de profs */
    private Map<String, Long> profsParSpecialite;

    // ── Charge jury ──────────────────────────────────────
    /** nom prof → total participations jury (membreInfo + membreMath) */
    private Map<String, Long> chargeJuryParProf;

    // ─────────────────────────────────────────────────────
    //  Getters & Setters
    // ─────────────────────────────────────────────────────

    public int getTotalEtudiants() { return totalEtudiants; }
    public void setTotalEtudiants(int totalEtudiants) { this.totalEtudiants = totalEtudiants; }

    public int getTotalProfesseurs() { return totalProfesseurs; }
    public void setTotalProfesseurs(int totalProfesseurs) { this.totalProfesseurs = totalProfesseurs; }

    public int getTotalSoutenances() { return totalSoutenances; }
    public void setTotalSoutenances(int totalSoutenances) { this.totalSoutenances = totalSoutenances; }

    public int getTotalSalles() { return totalSalles; }
    public void setTotalSalles(int totalSalles) { this.totalSalles = totalSalles; }

    public Map<String, Long> getEtudiantsParProf() { return etudiantsParProf; }
    public void setEtudiantsParProf(Map<String, Long> etudiantsParProf) { this.etudiantsParProf = etudiantsParProf; }

    public Map<String, Long> getSoutenancesParProf() { return soutenancesParProf; }
    public void setSoutenancesParProf(Map<String, Long> soutenancesParProf) { this.soutenancesParProf = soutenancesParProf; }

    public Map<String, Long> getSoutenancesEncadrantParProf() { return soutenancesEncadrantParProf; }
    public void setSoutenancesEncadrantParProf(Map<String, Long> m) { this.soutenancesEncadrantParProf = m; }

    public Map<String, Long> getSoutenancesParFiliere() { return soutenancesParFiliere; }
    public void setSoutenancesParFiliere(Map<String, Long> soutenancesParFiliere) { this.soutenancesParFiliere = soutenancesParFiliere; }

    public Map<String, Long> getEtudiantsParFiliere() { return etudiantsParFiliere; }
    public void setEtudiantsParFiliere(Map<String, Long> etudiantsParFiliere) { this.etudiantsParFiliere = etudiantsParFiliere; }

    public Map<String, Long> getSoutenancesParSalle() { return soutenancesParSalle; }
    public void setSoutenancesParSalle(Map<String, Long> soutenancesParSalle) { this.soutenancesParSalle = soutenancesParSalle; }

    public Map<String, Long> getSoutenancesParDate() { return soutenancesParDate; }
    public void setSoutenancesParDate(Map<String, Long> soutenancesParDate) { this.soutenancesParDate = soutenancesParDate; }

    public Map<String, Long> getProfsParSpecialite() { return profsParSpecialite; }
    public void setProfsParSpecialite(Map<String, Long> profsParSpecialite) { this.profsParSpecialite = profsParSpecialite; }

    public Map<String, Long> getChargeJuryParProf() { return chargeJuryParProf; }
    public void setChargeJuryParProf(Map<String, Long> chargeJuryParProf) { this.chargeJuryParProf = chargeJuryParProf; }
}

