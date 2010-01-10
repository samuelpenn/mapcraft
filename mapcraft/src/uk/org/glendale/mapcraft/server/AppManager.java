package uk.org.glendale.mapcraft.server;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import uk.org.glendale.mapcraft.graphics.MapSector;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapManager;

public class AppManager {
	private static Properties		properties = new Properties();
	
	private DataSource				dataSource = null;
	private String					databaseClass = null;
	private Connection				connection = null;
	
	static {
		ResourceBundle		bundle = ResourceBundle.getBundle("uk.org.glendale.mapcraft.config");
		
		Enumeration<String>		e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String		key = e.nextElement();
			String		value = bundle.getString(key);
			properties.setProperty(key, value);
		}
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
        String     dsName = properties.getProperty("database.resource");
        if (dsName != null && getDataSource(dsName) != null) {
            // Get data source from Application Server environment.

            System.out.println(">>> Have got database from the data source ["+dsName+"]");
            dataSource = getDataSource(dsName);
        } else {
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
		
		MapInfo			info = manager.getMap("test");
		MapData			data = new MapData(info, app.getDatabaseConnection());
		Map				map = new Map(info, data);
		
		//map.setTerrain(10, 10, 2);
		//map.setTerrain(11, 10, 2);
		//map.setTerrain(10, 11, 2);
		//System.out.println(map.getTerrain(10,10));
		//System.out.println(map.getTerrain(10,11));
		//System.out.println(map.getTerrain(10,12));
		//map.saveAll();
		MapSector		imageMap = new MapSector(map, new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/colour"));
		imageMap.drawMap(0, 0, 200, 100);
		imageMap.save(new File("/home/sam/hexmap.jpg"));
	}
}
