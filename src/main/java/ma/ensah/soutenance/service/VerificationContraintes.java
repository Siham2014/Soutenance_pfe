package ma.ensah.soutenance.service;
 
import ma.ensah.soutenance.model.dao.impl.GroupPfeDaoImpl;
import ma.ensah.soutenance.model.entity.GroupPfe;
import ma.ensah.soutenance.model.entity.Professeur;
 
import java.util.*;
 
/**
 * Vérifie les contraintes avant la génération du planning.
 * Retourne une liste d'AlerteContrainte (BLOQUANTE ou AVERTISSEMENT).
 */
public class VerificationContraintes {
 
    // ── Types d'alertes ──────────────────────────────────────────────────────
    public enum Niveau { BLOQUANT, AVERTISSEMENT }
 
    public static class AlerteContrainte {
        private final Niveau niveau;
        private final String code;       // identifiant court ex: "PAS_PROF_INFO"
        private final String message;    // message lisible
 
        public AlerteContrainte(Niveau niveau, String code, String message) {
            this.niveau  = niveau;
            this.code    = code;
            this.message = message;
        }
 
        public Niveau  getNiveau()  { return niveau; }
        public String  getCode()    { return code; }
        public String  getMessage() { return message; }
        public boolean isBloquant() { return niveau == Niveau.BLOQUANT; }
    }
 
    // ── Point d'entrée ───────────────────────────────────────────────────────
    /**
     * @param creneaux  liste de String[]{date, heure_debut, heure_fin}
     * @param salles    liste de String[]{nom, capacite}
     * @param professeurs liste de String[]{nom, prenom, specialite}
     * @param nbEtudiants nombre total d'étudiants à planifier
     */
    public List<AlerteContrainte> verifier(
            List<String[]> creneaux,
            List<String[]> salles,
            List<String[]> professeurs,
            int nbEtudiants) {
 
        List<AlerteContrainte> alertes = new ArrayList<>();
 
        // ── 1. Créneaux vides ────────────────────────────────────────────────
        if (creneaux == null || creneaux.isEmpty()) {
            alertes.add(new AlerteContrainte(
                Niveau.BLOQUANT,
                "CRENEAUX_VIDES",
                "Aucun créneau horaire n'est défini dans le fichier Excel. "
                + "Renseignez la feuille 'Config_Creneaux' ou 'Creneau'."
            ));
        }
 
        // ── 2. Salles vides ──────────────────────────────────────────────────
        if (salles == null || salles.isEmpty()) {
            alertes.add(new AlerteContrainte(
                Niveau.BLOQUANT,
                "SALLES_VIDES",
                "Aucune salle n'est définie dans la feuille 'Salle'. "
                + "Ajoutez au moins une salle avec sa capacité."
            ));
        }
 
        // ── 3. Pas d'étudiants ───────────────────────────────────────────────
        if (nbEtudiants == 0) {
            alertes.add(new AlerteContrainte(
                Niveau.BLOQUANT,
                "ETUDIANTS_VIDES",
                "Aucun étudiant trouvé en base. Importez d'abord les données "
                + "via 'Importer & Répartir'."
            ));
        }
 
        // ── 4. Pas de professeurs Info ───────────────────────────────────────
        long nbInfo = 0, nbMath = 0;
        if (professeurs != null) {
            for (String[] p : professeurs) {
                String sp = p[2] == null ? "" : p[2].trim().toLowerCase();
                if (sp.contains("info")) nbInfo++;
                else if (sp.contains("math")) nbMath++;
            }
        }
        if (nbInfo == 0) {
            alertes.add(new AlerteContrainte(
                Niveau.BLOQUANT,
                "PAS_PROF_INFO",
                "Aucun professeur de spécialité Informatique trouvé. "
                + "Le jury nécessite au moins un membre Info. "
                + "Vérifiez la colonne 'specialite' de la feuille 'Professeur'."
            ));
        }
        if (nbMath == 0) {
            alertes.add(new AlerteContrainte(
                Niveau.BLOQUANT,
                "PAS_PROF_MATH",
                "Aucun professeur de spécialité Mathématique trouvé. "
                + "Le jury nécessite au moins un membre Math. "
                + "Vérifiez la colonne 'specialite' de la feuille 'Professeur'."
            ));
        }
 
        // ── 5. Capacité créneaux vs étudiants ────────────────────────────────
        if (creneaux != null && !creneaux.isEmpty() && salles != null && !salles.isEmpty()) {
            int nbSalles   = salles.size();
            int nbCreneaux = creneaux.size();
            int capaciteTotale = nbCreneaux * nbSalles; // 1 étudiant par salle/créneau
 
            if (nbEtudiants > 0 && capaciteTotale < nbEtudiants) {
                alertes.add(new AlerteContrainte(
                    Niveau.BLOQUANT,
                    "CAPACITE_INSUFFISANTE",
                    String.format(
                        "Impossible de planifier tous les étudiants : "
                        + "%d créneaux × %d salle(s) = %d places disponibles, "
                        + "mais il y a %d étudiant(s) à planifier. "
                        + "Ajoutez des jours, élargissez les plages horaires ou ajoutez des salles.",
                        nbCreneaux, nbSalles, capaciteTotale, nbEtudiants
                    )
                ));
            } else if (nbEtudiants > 0 && capaciteTotale < nbEtudiants * 12 / 10) {
                // Moins de 20% de marge → avertissement
                alertes.add(new AlerteContrainte(
                    Niveau.AVERTISSEMENT,
                    "MARGE_FAIBLE",
                    String.format(
                        "Marge de créneaux faible : %d places disponibles pour %d étudiant(s). "
                        + "Les contraintes d'encadrant peuvent bloquer certaines affectations. "
                        + "Envisagez d'ajouter un jour supplémentaire.",
                        capaciteTotale, nbEtudiants
                    )
                ));
            }
        }
 
        // ── 6. Professeurs insuffisants pour couvrir les jurys ───────────────
        if (nbEtudiants > 0) {
            // Chaque soutenance mobilise 3 profs dont 1 Info et 1 Math.
            // Un prof peut faire au max ~4-5 soutenances/jour sans repos < 1h.
            // On vérifie simplement qu'il y a au moins 2 profs Info et 2 Math.
            if (nbInfo < 2) {
                alertes.add(new AlerteContrainte(
                    Niveau.AVERTISSEMENT,
                    "PROF_INFO_INSUFFISANT",
                    String.format(
                        "Seulement %d professeur(s) Informatique disponible(s). "
                        + "Avec %d étudiant(s), certains jurys risquent d'être impossibles à former "
                        + "si les profs sont déjà occupés au même créneau.",
                        nbInfo, nbEtudiants
                    )
                ));
            }
            if (nbMath < 2) {
                alertes.add(new AlerteContrainte(
                    Niveau.AVERTISSEMENT,
                    "PROF_MATH_INSUFFISANT",
                    String.format(
                        "Seulement %d professeur(s) Mathématique disponible(s). "
                        + "Avec %d étudiant(s), certains jurys risquent d'être impossibles à former.",
                        nbMath, nbEtudiants
                    )
                ));
            }
        }
 
        // ── 7. Créneaux dupliqués ────────────────────────────────────────────
        if (creneaux != null) {
            Set<String> vus = new HashSet<>();
            List<String> doublons = new ArrayList<>();
            for (String[] c : creneaux) {
                String cle = c[0] + "_" + c[1];
                if (!vus.add(cle)) {
                    doublons.add(c[0] + " " + c[1]);
                }
            }
            if (!doublons.isEmpty()) {
                alertes.add(new AlerteContrainte(
                    Niveau.AVERTISSEMENT,
                    "CRENEAUX_DUPLIQUES",
                    "Des créneaux en doublon ont été détectés : "
                    + String.join(", ", doublons.subList(0, Math.min(3, doublons.size())))
                    + (doublons.size() > 3 ? " …" : "")
                    + ". Ils seront ignorés lors de la génération."
                ));
            }
        }
 
        // ── 8. Répartition non effectuée ─────────────────────────────────────
        // (vérifiée en amont dans le servlet, on l'inclut ici pour être complet)
        // Cette contrainte est déjà gérée avant d'appeler verifier(), donc on
        // n'ajoute rien ici pour éviter le doublon.
 
        return alertes;
    }
 
    // ── Helpers ──────────────────────────────────────────────────────────────
    public static boolean aDesBloquants(List<AlerteContrainte> alertes) {
        for (AlerteContrainte a : alertes) {
            if (a.isBloquant()) return true;
        }
        return false;
    }
}