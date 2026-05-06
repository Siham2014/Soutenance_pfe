package ma.ensah.soutenance.service;

import ma.ensah.soutenance.model.entity.Soutenance;
import java.util.List;

public interface PlanningService {
    // Génère le planning depuis l'Excel
    void genererPlanning(List<String[]> creneaux, List<String> salles);
    // Récupère tout le planning pour l'affichage
    List<Soutenance> getPlanning();
}
