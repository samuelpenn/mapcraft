package uk.org.glendale.worldgen.server;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;

/**
 * WorldGen application manager.
 * 
 * @author Samuel Penn
 */
public class AppManager implements ServletContextListener {
	private static Properties		properties = new Properties();
	
	private DataSource				dataSource = null;
	private String					databaseClass = null;
	private Connection				connection = null;
	
	private static String			universe = null;
	private static String			rootPath = null;
	
	private static boolean			drawPlanetGlobe = false;
	private static boolean			stretchPlanetMap = false;
	
	static {
		ResourceBundle		bundle = ResourceBundle.getBundle("uk.org.glendale.rpg.traveller.config");
		
		Enumeration<String>		e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String		key = e.nextElement();
			String		value = bundle.getString(key);
			properties.setProperty(key, value);
		}
		
		universe = properties.getProperty("universe"); 
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private DataSource getDataSource(String name) {
		return dataSource;
	}
	
	public Connection getDatabaseConnection() {
		Connection	cx = null;
		
		if (dataSource == null && connection == null) {
			configureDatabase();
		}

		try {
			if (dataSource != null) {
				cx = dataSource.getConnection();
			} else if (connection != null) {
				cx = connection;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cx;
	}
	
	private void configureDatabase() {
        String     dsName = properties.getProperty(universe+".database.resource");
        if (dsName != null && getDataSource(dsName) != null) {
            // Get data source from Application Server environment.

            System.out.println(">>> Have got database from the data source ["+dsName+"]");
            dataSource = getDataSource(dsName);
        } else {
        	// Probably running stand alone, get non-pooled connection.
            String      driverName = properties.getProperty(universe+".database.driver");

            String		hostname = properties.getProperty(universe+".database.hostname");
            String		username = properties.getProperty(universe+".database.user");
            String		password = properties.getProperty(universe+".database.password");
            String		database = properties.getProperty(universe+".database.name");
            String      url = "jdbc:mysql://"+hostname+"/"+database;

            try {
                Class.forName(driverName);

                Driver      driver = DriverManager.getDriver(url);
                
                Properties  properties = new Properties();
                properties.setProperty("user", username);
                properties.setProperty("password", password);
                 
                connection = driver.connect(url, properties);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
	}
	
	// Hibernate session factory.
	private SessionFactory	sessionFactory;
	private EntityManagerFactory	emf;
	
	/**
	 * Configure hibernate from the application's properties.
	 */
	private void configureHibernate() {
		try {
			AnnotationConfiguration		hibernateConfig = new AnnotationConfiguration();
    		Map<String,Object>			config = new HashMap<String,Object>();
			
	        String     dsName = properties.getProperty(universe+".database.resource");
	        if (dsName != null) {
	            System.out.println(">>> Have got hibernate database from the data source ["+dsName+"]");
	            hibernateConfig.setProperty("hibernate.connection.datasource", dsName);
	            
	            config.put("hibernate.connection.datasource", dsName);
	        } else {
	        	// Probably running stand alone, get non-pooled connection.
	            String      driverName = properties.getProperty(universe+".database.driver");

	            String		hostname = properties.getProperty(universe+".database.hostname");
	            String		username = properties.getProperty(universe+".database.user");
	            String		password = properties.getProperty(universe+".database.password");
	            String		database = properties.getProperty(universe+".database.name");


	    		config.put("hibernate.archive.autodetection", "class, hbm");
	    		config.put("hibernate.show_sql", "true");
	    		config.put("hibernate.connection.url", "jdbc:mysql://"+hostname+"/"+database);
				config.put("hibernate.connection.username", username);
				config.put("hibernate.connection.password", password);
				config.put("hibernate.connection.driver_class", driverName);
				config.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
	    		

	    		hibernateConfig.setProperty("hibernate.connection.url", "jdbc:mysql://"+hostname+"/"+database);
				hibernateConfig.setProperty("hibernate.connection.username", username);
				hibernateConfig.setProperty("hibernate.connection.password", password);
				hibernateConfig.setProperty("hibernate.connection.driver_class", driverName);
				hibernateConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
	        }
    		emf = Persistence.createEntityManagerFactory("worldgen", config);
			
			
			sessionFactory = hibernateConfig.buildSessionFactory();
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
		
	}
	
	/**
	 * Gets a reference to the application's session factory. There is a
	 * single Hibernate session factory, created when first requested.
	 * 
	 * @return	Hibernate session factory.
	 */
	public SessionFactory getHibernate() {
		if (sessionFactory == null) {
			configureHibernate();
		}
		return sessionFactory;
	}
	
	public Session getSession() {
		return getHibernate().openSession();
	}
	
	public void closeHibernate() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}
	
	public EntityManager getEntityManager() {
		if (emf == null) {
			configureHibernate();
		}
		return emf.createEntityManager();
	}

	
	public static void main(String[] args) throws Exception {
		AppManager		app = new AppManager();
		
		app.configureDatabase();
		
		ObjectFactory f = new ObjectFactory(app.getDatabaseConnection());
		System.out.println(f.getCommodity(1).getName());
		
	}
	
	
	private static AppManager	appManager = null;
	
	private DataSource	ds = null;

	public void contextDestroyed(ServletContextEvent arg0) {
		ds = null;
	}

	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Mapcraft: Context initialised");
		
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource) ic.lookup("java:com/env/jdbc/Traveller");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		AppManager.appManager = new AppManager();
		AppManager.appManager.setDataSource(ds);
		AppManager.appManager.configureDatabase();
		if (ds == null) {
			System.out.println("No datasource");
		} else {
			System.out.println("Successfully obtained data source");
		}
		
		rootPath = arg0.getServletContext().getRealPath("");

	}
	
	public static String getRootPath() {
		return rootPath;
	}
	
	public static boolean getDrawGlobe() {
		return properties.getProperty(universe+".planet.drawGlobe", "false").equals("true");
	}

	public static boolean getStretchMap() {
		return properties.getProperty(universe+".planet.stretchMap", "false").equals("true");
	}
	
	public static boolean getDrawMap() {
		return properties.getProperty(universe+".planet.drawMap", "true").equals("true");
	}
	
	public static AppManager getInstance() {
		if (appManager == null) {
			appManager = new AppManager();
		}
		return appManager;
	}
	
	
}
