package ma.ensah.soutenance.service.impl;

import ma.ensah.soutenance.model.dao.*;
import ma.ensah.soutenance.model.dao.impl.*;
import ma.ensah.soutenance.model.entity.*;
import ma.ensah.soutenance.service.PlanningService;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlanningServiceImpl implements PlanningService {

    private SoutenanceDao soutenanceDao;
    private SalleDao      salleDao;
    private GroupPfeDao   groupPfeDao;
    private ProfesseurDao professeurDao;

    public PlanningServiceImpl() {
        this.soutenanceDao = new SoutenanceDaoImpl();
        this.salleDao      = new SalleDaoImpl();
        this.groupPfeDao   = new GroupPfeDaoImpl();
        this.professeurDao = new ProfesseurDaoImpl();
    }

    @Override
    public void genererPlanning(List<String[]> creneaux, List<String> nomsSalles) {

        // 1. Vider les anciennes soutenances avant de régénérer
        soutenanceDao.deleteAll();

        // 2. Charger les données depuis la DB
        List<GroupPfe>   groupes     = groupPfeDao.findAllWithDetails();
        List<Professeur> professeurs = professeurDao.findAll();
        List<Salle>      salles      = salleDao.findAll();

        System.out.println("Groupes: " + groupes.size());
        System.out.println("Professeurs: " + professeurs.size());
        System.out.println("Salles: " + salles.size());
        System.out.println("Créneaux: " + creneaux.size());

        if (groupes.isEmpty() || professeurs.isEmpty() || salles.isEmpty() || creneaux.isEmpty()) {
            System.out.println("❌ Données manquantes pour générer le planning.");
            return;
        }

        // 3. Pour chaque étudiant → créneau + salle + jury
        for (GroupPfe groupe : groupes) {
            Professeur encadrant = groupe.getEncadrant();

            for (Etudiant etudiant : groupe.getEtudiants()) {

                boolean planifie = false;

                creneauLoop:
                for (String[] creneau : creneaux) {
                    String dateStr  = creneau[0];
                    String heureDeb = creneau[1];
                    String heureFin = creneau[2];
                    Date   dateObj  = parseDate(dateStr);

                    // ✅ Contrainte : encadrant pas occupé à ce créneau
                    if (soutenanceDao.isProfOccupe(encadrant.getId(), dateObj, heureDeb)) {
                        continue;
                    }

                    // ✅ Contrainte : 1h de repos pour l'encadrant
                    if (!aReposSuffisant(encadrant.getId(), dateObj, heureDeb)) {
                        continue;
                    }

                    // ── Chercher une salle libre ──────────────────────
                    Salle salleLibre = null;
                    for (Salle salle : salles) {
                        if (!soutenanceDao.isSalleOccupee(salle.getId(), dateObj, heureDeb)) {
                            salleLibre = salle;
                            break;
                        }
                    }
                    if (salleLibre == null) continue;

                    // ── Chercher membre jury Informatique ─────────────
                    Professeur membreInfo = null;
                    for (Professeur prof : professeurs) {
                        if (prof.getId().equals(encadrant.getId())) continue;
                        if (prof.getSpecialite() == null) continue;
                        if (!prof.getSpecialite().trim().toLowerCase().contains("info")) continue;
                        if (soutenanceDao.isProfOccupe(prof.getId(), dateObj, heureDeb)) continue;
                        if (!aReposSuffisant(prof.getId(), dateObj, heureDeb)) continue;
                        membreInfo = prof;
                        break;
                    }
                    if (membreInfo == null) {
                        System.out.println("⚠️ Pas de membre Info disponible pour créneau " + dateStr + " " + heureDeb);
                        continue;
                    }

                    // ── Chercher membre jury Mathématiques ────────────
                    Professeur membreMath = null;
                    for (Professeur prof : professeurs) {
                        if (prof.getId().equals(encadrant.getId())) continue;
                        if (prof.getId().equals(membreInfo.getId())) continue;
                        if (prof.getSpecialite() == null) continue;
                        if (!prof.getSpecialite().trim().toLowerCase().contains("math")) continue;
                        if (soutenanceDao.isProfOccupe(prof.getId(), dateObj, heureDeb)) continue;
                        if (!aReposSuffisant(prof.getId(), dateObj, heureDeb)) continue;
                        membreMath = prof;
                        break;
                    }
                    if (membreMath == null) {
                        System.out.println("⚠️ Pas de membre Math disponible pour créneau " + dateStr + " " + heureDeb);
                        continue;
                    }

                    // ✅ Tout OK → créer la soutenance
                    Soutenance s = new Soutenance();
                    s.setEtudiant(etudiant);
                    s.setEncadrant(encadrant);
                    s.setMembreInfo(membreInfo);
                    s.setMembreMath(membreMath);
                    s.setSalle(salleLibre);
                    s.setDateSoutenance(dateObj);
                    s.setHeureDebut(heureDeb);
                    s.setHeureFin(heureFin);

                    soutenanceDao.save(s);
                    planifie = true;
                    System.out.println("✅ Planifié: " + etudiant.getNom() + " → " + dateStr + " " + heureDeb);
                    break creneauLoop;
                }

                if (!planifie) {
                    System.out.println("❌ Impossible de planifier : "
                        + etudiant.getNom() + " " + etudiant.getPrenom());
                }
            }
        }

        System.out.println("✅ Planning généré !");
    }

    @Override
    public List<Soutenance> getPlanning() {
        return soutenanceDao.findAll();
    }

    // ✅ Contrainte : 1h de repos minimum entre 2 soutenances du même prof
    private boolean aReposSuffisant(Long profId, Date date, String heureDebut) {
        List<Soutenance> dejaPlanifiees = soutenanceDao.findByProf(profId, date);
        int hDebut = parseHeure(heureDebut);
        for (Soutenance s : dejaPlanifiees) {
            int hExistante = parseHeure(s.getHeureDebut());
            if (Math.abs(hDebut - hExistante) < 60) return false;
        }
        return true;
    }

    private int parseHeure(String heure) {
        String[] parts = heure.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
}