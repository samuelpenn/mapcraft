package uk.org.glendale.mapcraft.server;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import uk.org.glendale.mapcraft.server.database.MapData;

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
		MapData		data = null;
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
			data = new MapData(cx);
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
	
	public static void main(String[] args) {
		AppManager		app = new AppManager();
		
		app.configureDatabase();
	}
}
