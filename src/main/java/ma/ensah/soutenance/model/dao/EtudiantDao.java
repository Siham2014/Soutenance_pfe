package ma.ensah.soutenance.model.dao;

import java.util.List;

import ma.ensah.soutenance.model.entity.*;
public interface EtudiantDao {
	
	void save(Etudiant etudiant);
	List<Etudiant> findAll();
}
