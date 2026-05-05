package ma.ensah.soutenance.algorithm;

import java.util.List;

import ma.ensah.soutenance.model.entity.*;

public interface RepartitionStrategy {
	List<GroupPfe> repartir(List<Professeur>professeurs,List<Etudiant>etudiants);
	

}
