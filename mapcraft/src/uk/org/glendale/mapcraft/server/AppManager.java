package uk.org.glendale.mapcraft.server;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;


import uk.org.glendale.mapcraft.graphics.MapSector;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.Sector;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;


/**
 * Manages the application.
 * 
 * @author Samuel Penn
 */
public class AppManager implements ServletContextListener {
	private static Properties		properties = new Properties();
	private static Logger			log = Logger.getLogger("uk.org.glendale.mapcraft.server.AppManager");
	
	private DataSource				dataSource = null;
	private String					databaseClass = null;
	private Connection				connection = null;
	private String					root = null;
	
	
	static {
		ResourceBundle		bundle = ResourceBundle.getBundle("uk.org.glendale.mapcraft.config");
		
		Enumeration<String>		e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String		key = e.nextElement();
			String		value = bundle.getString(key);
			properties.setProperty(key, value);
		}
		log.info("Configured resources");
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
				log.info("Getting new connection from datasource");
				cx = dataSource.getConnection();
			} else if (connection != null) {
				cx = connection;
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to get connection", e);
		}
		return cx;
	}
	
	private void configureDatabase() {
        String     dsName = properties.getProperty("database.resource");
        
        log.info("Checking database resource ["+dsName+"]");
        
        if (dsName != null && getDataSource(dsName) != null) {
            // Get data source from Application Server environment.

            log.warning(">>> Have got database from the data source ["+dsName+"]");
            dataSource = getDataSource(dsName);
        } else {
        	log.warning(">>> Getting direct connection");
        	// Probably running stand alone, get non-pooled connection.
            String      driverName = properties.getProperty("database.driver");

            String		hostname = properties.getProperty("database.hostname");
            String		username = properties.getProperty("database.username");
            String		password = properties.getProperty("database.password");
            String		database = properties.getProperty("database.database");
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
	
	public static void main(String[] args) throws Exception {
		AppManager		app = new AppManager();
		
		app.configureDatabase();
		
		MapManager		manager = new MapManager(app.getDatabaseConnection());
		
		Map			map = manager.getMap("eorthe");
		
		//map.setTerrain(10, 10, 2);
		//map.setTerrain(11, 10, 2);
		//map.setTerrain(10, 11, 2);
		//System.out.println(map.getTerrain(10,10));
		//System.out.println(map.getTerrain(10,11));
		//System.out.println(map.getTerrain(10,12));
		//map.saveAll();
		MapSector		imageMap = new MapSector(map, new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/colour"));
		imageMap.setBleeding(true);
		imageMap.setZoom(1.0);
		imageMap.setScale(MapSector.Scale.COMPACT);
		imageMap.drawMap(3424, 640, 128, 160);
		//imageMap.drawOverviewMap(3424, 640, 960, 600, 1);
		imageMap.save(new File("/home/sam/hexmap.jpg"));
	}
	
	private void setRootPath(String root) {
		this.root = root;
	}
	
	public String getRootPath() {
		return root;
	}
	
	private static AppManager	appManager = null;
	
	private DataSource	ds = null;

	@Override
	public void contextDestroyed(ServletContextEvent context) {
		ds = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		log.info("Mapcraft: Context initialised");
		
		log.info(context.getServletContext().getContextPath());
		
		try {
			InitialContext ic = new InitialContext();
			//ds = (DataSource) ic.lookup("java:com/env/jdbc/Mapcraft");
			//ds = (DataSource) ic.lookup("java:"+properties.getProperty("database.resource"));
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");

			// Look up our data source
			ds = (DataSource) envCtx.lookup(properties.getProperty("database.resource"));
		} catch (NamingException e) {
			log.log(Level.SEVERE, "Cannot get datasource", e);
		}
		AppManager.appManager = new AppManager();
		AppManager.appManager.setDataSource(ds);
		AppManager.appManager.configureDatabase();
		AppManager.appManager.setRootPath(context.getServletContext().getRealPath("/"));
		if (ds == null) {
			log.warning("No datasource");
		} else {
			log.info("Successfully obtained data source");
		}
		
	}
	
	public static AppManager getInstance() {
		return appManager;
	}
	
	
}
