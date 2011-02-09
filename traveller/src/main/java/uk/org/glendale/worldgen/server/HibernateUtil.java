package uk.org.glendale.worldgen.server;

import org.hibernate.*;
import org.hibernate.cfg.*;

public class HibernateUtil {
	private static SessionFactory		sessionFactory = null;
	
	static {
		try {
			AnnotationConfiguration		config = new AnnotationConfiguration();
			
			config.setProperty("hibernate.connection.url", "jdbc:mysql://appleseed/knownspace");
			config.setProperty("hibernate.connection.username", "knownspace");
			config.setProperty("hibernate.connection.password", "freeTraderBeowulf");
			config.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
			config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
			

			config.addAnnotatedClass(uk.org.glendale.worldgen.astro.sector.Sector.class);
			
			sessionFactory = config.buildSessionFactory();
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
