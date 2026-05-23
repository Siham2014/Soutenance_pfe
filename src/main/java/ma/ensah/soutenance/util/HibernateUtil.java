package ma.ensah.soutenance.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static SessionFactory sessionFactory;
	
	static {
		try {
			sessionFactory=new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
			
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Erreur creation SessionFactory");
			
			
		}
	}
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
