package ma.ensah.soutenance.service.impl;

import ma.ensah.soutenance.model.dao.ProfesseurDao;
import ma.ensah.soutenance.model.entity.Professeur;
import ma.ensah.soutenance.service.*;

public class ProfesseurServiceImpl implements ProfesseurService {

	
	private ProfesseurDao professeurDao;
	
	public ProfesseurServiceImpl(ProfesseurDao professeurDao) {
		this.professeurDao=professeurDao;
	}
	
	
	public void ajouterProfesseur(Professeur professeur) {
		professeurDao.save(professeur);
		
	}

}
