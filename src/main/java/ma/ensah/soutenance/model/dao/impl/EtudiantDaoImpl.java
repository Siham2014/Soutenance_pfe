package ma.ensah.soutenance.model.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ma.ensah.soutenance.model.dao.EtudiantDao;
import ma.ensah.soutenance.model.entity.Etudiant;
import ma.ensah.soutenance.util.HibernateUtil;

public class EtudiantDaoImpl  implements EtudiantDao {
	
	@Override
	public void save(Etudiant etudiant ) {
		Transaction transaction =null;
		
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			
			transaction= session.beginTransaction();
			session.save(etudiant);
			transaction.commit();
			
					
			
	
			
		}catch(Exception e ){
			
			if (transaction != null) {
				transaction.rollback();
				
			}
			
			e.printStackTrace();
			
			
		}
		
		
		
		
	}
	@Override
	public List<Etudiant> findAll() {
	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        return session.createQuery("from Etudiant", Etudiant.class).list();
	    }
	}

}
