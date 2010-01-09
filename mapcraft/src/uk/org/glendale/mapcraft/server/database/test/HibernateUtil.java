package uk.org.glendale.mapcraft.server.database.test;

import org.hibernate.*;
import org.hibernate.cfg.*;

public class HibernateUtil {
	private static SessionFactory		sessionFactory = null;
	
	static {
		try {
			sessionFactory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static void shutdown() {
		getSessionFactory().close();
	}
}
