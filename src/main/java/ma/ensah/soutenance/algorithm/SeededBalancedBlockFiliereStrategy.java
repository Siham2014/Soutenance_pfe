package ma.ensah.soutenance.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ma.ensah.soutenance.model.entity.Etudiant;
import ma.ensah.soutenance.model.entity.GroupPfe;
import ma.ensah.soutenance.model.entity.Professeur;


public class SeededBalancedBlockFiliereStrategy extends BalancedBlockFiliereStrategy {

    private long seed;

    public SeededBalancedBlockFiliereStrategy(long seed) {
        this.seed = seed;
    }

    @Override
    public List<GroupPfe> repartir(List<Professeur> professeurs, List<Etudiant> etudiants) {

        List<Professeur> professeursMelanges = new ArrayList<>(professeurs);
        Collections.shuffle(professeursMelanges, new Random(seed));

        List<Etudiant> etudiantsMelanges = new ArrayList<>(etudiants);
        Collections.shuffle(etudiantsMelanges, new Random(seed));

        return super.repartir(professeursMelanges, etudiantsMelanges);
    }
}