package ma.ensah.soutenance.algorithm;

import java.util.*;


import ma.ensah.soutenance.model.entity.*;

public class BalancedBlockFiliereStrategy implements RepartitionStrategy {

    @Override
    public List<GroupPfe> repartir(List<Professeur> professeurs, List<Etudiant> etudiants) {

        List<GroupPfe> groupes = new ArrayList<>();

        if (professeurs == null || professeurs.isEmpty() || etudiants == null || etudiants.isEmpty()) {
            return groupes;
        }

        Map<String, List<Etudiant>> etudiantsParFiliere = classerParFiliere(etudiants);

        List<String> filieresTriees = trierFilieresParTaille(etudiantsParFiliere);

        Map<Professeur, List<Etudiant>> repartition = initialiserRepartition(professeurs);

        int nbProfs = professeurs.size();

        List<Etudiant> resteGlobal = new ArrayList<>();

        for (String filiere : filieresTriees) {

            List<Etudiant> listeFiliere = etudiantsParFiliere.get(filiere);

            if (listeFiliere.size() >= nbProfs) {

                for (int i = 0; i < nbProfs; i++) {
                    repartition.get(professeurs.get(i)).add(listeFiliere.get(i));
                }

                for (int i = nbProfs; i < listeFiliere.size(); i++) {
                    resteGlobal.add(listeFiliere.get(i));
                }

            } else {

                List<Etudiant> bloc = new ArrayList<>(listeFiliere);

                while (bloc.size() < nbProfs && !resteGlobal.isEmpty()) {
                    bloc.add(resteGlobal.remove(0));
                }

                for (int i = 0; i < bloc.size(); i++) {
                    repartition.get(professeurs.get(i)).add(bloc.get(i));
                }
            }
        }

        int indexProf = 0;
        while (!resteGlobal.isEmpty()) {
            Professeur prof = professeurs.get(indexProf);
            repartition.get(prof).add(resteGlobal.remove(0));

            indexProf++;
            if (indexProf == professeurs.size()) {
                indexProf = 0;
            }
        }

        for (Professeur prof : professeurs) {
            for (Etudiant etudiant : repartition.get(prof)) {
                GroupPfe group = new GroupPfe();
                group.setEncadrant(prof);
                group.setEtudiants(Arrays.asList(etudiant));
                groupes.add(group);
            }
        }

        return groupes;
    }

    private Map<String, List<Etudiant>> classerParFiliere(List<Etudiant> etudiants) {
        Map<String, List<Etudiant>> map = new HashMap<>();

        for (Etudiant e : etudiants) {
            String filiere = e.getFiliere();

            if (!map.containsKey(filiere)) {
                map.put(filiere, new ArrayList<Etudiant>());
            }

            map.get(filiere).add(e);
        }

        return map;
    }

    private List<String> trierFilieresParTaille(Map<String, List<Etudiant>> map) {
        List<String> filieres = new ArrayList<>(map.keySet());

        filieres.sort((f1, f2) -> map.get(f2).size() - map.get(f1).size());

        return filieres;
    }

    private Map<Professeur, List<Etudiant>> initialiserRepartition(List<Professeur> professeurs) {
        Map<Professeur, List<Etudiant>> repartition = new HashMap<>();

        for (Professeur p : professeurs) {
            repartition.put(p, new ArrayList<Etudiant>());
        }

        return repartition;
    }
}