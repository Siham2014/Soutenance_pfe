package ma.ensah.soutenance.service.impl;

import ma.ensah.soutenance.model.dao.*;
import ma.ensah.soutenance.model.dao.impl.*;
import ma.ensah.soutenance.model.entity.*;
import ma.ensah.soutenance.service.PlanningService;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlanningServiceImpl implements PlanningService {

    private SoutenanceDao soutenanceDao;
    private SalleDao salleDao;
    private GroupPfeDao groupPfeDao;
    private ProfesseurDao professeurDao;

    public PlanningServiceImpl() {
        this.soutenanceDao = new SoutenanceDaoImpl();
        this.salleDao = new SalleDaoImpl();
        this.groupPfeDao = new GroupPfeDaoImpl();
        this.professeurDao = new ProfesseurDaoImpl();
    }

    @Override
    public void genererPlanning(List<String[]> creneaux, List<String> nomsSalles) {

        soutenanceDao.deleteAll();

        List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
        List<Professeur> professeurs = professeurDao.findAll();
        List<Salle> salles = salleDao.findAll();

        if (groupes.isEmpty() || professeurs.isEmpty() || salles.isEmpty() || creneaux.isEmpty()) {
            System.out.println("Données manquantes pour générer le planning.");
            return;
        }

        List<Professeur> profsInfo = new ArrayList<>();
        List<Professeur> profsMath = new ArrayList<>();

        for (Professeur p : professeurs) {
            if (p.getSpecialite() == null) continue;

            String sp = p.getSpecialite().trim().toLowerCase();

            if (sp.contains("info")) {
                profsInfo.add(p);
            } else if (sp.contains("math")) {
                profsMath.add(p);
            }
        }

        if (profsInfo.isEmpty() || profsMath.isEmpty()) {
            System.out.println("Impossible : il faut au moins un prof Info et un prof Math.");
            return;
        }

        Map<Long, Integer> chargeProf = new HashMap<>();

        for (Professeur p : professeurs) {
            chargeProf.put(p.getId(), 0);
        }

        for (GroupPfe groupe : groupes) {

            Professeur encadrant = groupe.getEncadrant();

            for (Etudiant etudiant : groupe.getEtudiants()) {

                boolean planifie = false;

                for (String[] creneau : creneaux) {

                    String dateStr = creneau[0];
                    String heureDeb = creneau[1];
                    String heureFin = creneau[2];
                    Date dateObj = parseDate(dateStr);

                    if (soutenanceDao.isProfOccupe(encadrant.getId(), dateObj, heureDeb)) {
                        continue;
                    }

                    if (!aReposSuffisant(encadrant.getId(), dateObj, heureDeb)) {
                        continue;
                    }

                    Salle salleLibre = chercherSalleLibre(salles, dateObj, heureDeb);

                    if (salleLibre == null) {
                        continue;
                    }

                    Professeur membreMath = chercherProfDisponible(
                            profsMath,
                            encadrant,
                            null,
                            dateObj,
                            heureDeb,
                            chargeProf
                    );

                    if (membreMath == null) {
                        continue;
                    }

                    Professeur membreInfo = chercherProfDisponible(
                            profsInfo,
                            encadrant,
                            membreMath,
                            dateObj,
                            heureDeb,
                            chargeProf
                    );

                    if (membreInfo == null) {
                        continue;
                    }

                    Soutenance s = new Soutenance();
                    s.setEtudiant(etudiant);
                    s.setEncadrant(encadrant);
                    s.setMembreMath(membreMath);
                    s.setMembreInfo(membreInfo);
                    s.setSalle(salleLibre);
                    s.setDateSoutenance(dateObj);
                    s.setHeureDebut(heureDeb);
                    s.setHeureFin(heureFin);

                    soutenanceDao.save(s);

                    chargeProf.put(encadrant.getId(), chargeProf.get(encadrant.getId()) + 1);
                    chargeProf.put(membreMath.getId(), chargeProf.get(membreMath.getId()) + 1);
                    chargeProf.put(membreInfo.getId(), chargeProf.get(membreInfo.getId()) + 1);

                    planifie = true;

                    System.out.println("Planifié : "
                            + etudiant.getNom() + " "
                            + etudiant.getPrenom()
                            + " | " + dateStr + " " + heureDeb
                            + " | Encadrant : " + encadrant.getNom()
                            + " | Math : " + membreMath.getNom()
                            + " | Info : " + membreInfo.getNom());

                    break;
                }

                if (!planifie) {
                    System.out.println("Impossible de planifier : "
                            + etudiant.getNom() + " " + etudiant.getPrenom());
                }
            }
        }

        System.out.println("Planning généré !");
    }

    private Salle chercherSalleLibre(List<Salle> salles, Date date, String heureDebut) {
        for (Salle salle : salles) {
            if (!soutenanceDao.isSalleOccupee(salle.getId(), date, heureDebut)) {
                return salle;
            }
        }
        return null;
    }

    private Professeur chercherProfDisponible(
            List<Professeur> candidats,
            Professeur encadrant,
            Professeur autreJury,
            Date date,
            String heureDebut,
            Map<Long, Integer> chargeProf) {

        Professeur meilleur = null;
        int chargeMin = Integer.MAX_VALUE;

        for (Professeur prof : candidats) {

            if (prof.getId().equals(encadrant.getId())) {
                continue;
            }

            if (autreJury != null && prof.getId().equals(autreJury.getId())) {
                continue;
            }

            if (soutenanceDao.isProfOccupe(prof.getId(), date, heureDebut)) {
                continue;
            }

            if (!aReposSuffisant(prof.getId(), date, heureDebut)) {
                continue;
            }

            int charge = chargeProf.getOrDefault(prof.getId(), 0);

            if (charge < chargeMin) {
                chargeMin = charge;
                meilleur = prof;
            }
        }

        return meilleur;
    }

    @Override
    public List<Soutenance> getPlanning() {
        return soutenanceDao.findAll();
    }

    private boolean aReposSuffisant(Long profId, Date date, String heureDebut) {
        List<Soutenance> dejaPlanifiees = soutenanceDao.findByProf(profId, date);

        int hDebut = parseHeure(heureDebut);

        for (Soutenance s : dejaPlanifiees) {
            int hExistante = parseHeure(s.getHeureDebut());

            if (Math.abs(hDebut - hExistante) < 60) {
                return false;
            }
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