package ma.ensah.soutenance.model.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ma.ensah.soutenance.model.entity.Professeur;
import ma.ensah.soutenance.model.dao.ProfesseurDao;

import ma.ensah.soutenance.util.HibernateUtil;


public class ProfesseurDaoImpl implements ProfesseurDao {
	
	public void save(Professeur professeur) {
		Transaction transaction= null;
		
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			
			transaction=session.beginTransaction();
			session.save(professeur);
			
			transaction.commit();
			
			
			
		}catch(Exception e){
			if(transaction !=  null) {
				transaction.rollback();
				
			}
			e.printStackTrace();
			
			
			
		}
	}
	
	@Override
	public List<Professeur> findAll() {
	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        return session.createQuery("from Professeur", Professeur.class).list();
	    }
	}
	
	
	

}
