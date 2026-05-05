package ma.ensah.soutenance.service.impl;

import ma.ensah.soutenance.model.dao.EtudiantDao;
import ma.ensah.soutenance.model.entity.Etudiant;
import ma.ensah.soutenance.service.EtudiantService;

public class EtudiantServiceImpl implements EtudiantService{
	
	
	private EtudiantDao etudiantDao;
	
	
	public EtudiantServiceImpl(EtudiantDao etudiantDao) {
		this.etudiantDao=etudiantDao;
	}

	public void ajouterEtudiant(Etudiant etudiant) {
		// TODO Auto-generated method stub
		etudiantDao.save(etudiant);
		
	}
	
	
	

}
