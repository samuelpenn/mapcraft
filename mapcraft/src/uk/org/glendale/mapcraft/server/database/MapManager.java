package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.MapInfo;

/**
 * Manages a connection to the database, and the list of maps available.
 * Used to create and select maps.
 * 
 * @author Samuel Penn
 *
 */
public class MapManager {
	private Hashtable<String,MapInfo>	maps = null;
	private Connection		cx = null;
	
	private static final String		selectMapsSQL = "SELECT name, title, width, height, world FROM mapcraft";
	private static final String		insertMapsSQL = "INSERT INTO mapcraft (name, title, width, height, world) VALUES(?, ?, ?, ?, ?)";
	
	private PreparedStatement	selectMaps;
	private PreparedStatement	insertMaps;
	
	public MapManager(Connection cx) throws SQLException {
		this.cx = cx;
		prepareStatements();
		refresh();
	}
	
	private void prepareStatements() throws SQLException {
		selectMaps = cx.prepareStatement(selectMapsSQL);
		insertMaps = cx.prepareStatement(insertMapsSQL);
	}
	
	public void refresh() throws SQLException {
		selectMaps.clearParameters();
		ResultSet	rs = selectMaps.executeQuery();

		maps = new Hashtable<String,MapInfo>();
		try {
			while (rs.next()) {
				String		name = rs.getString("name");
				String		title = rs.getString("title");
				int			width = rs.getInt("width");
				int			height = rs.getInt("height");
				boolean		world = rs.getBoolean("world");
				
				MapInfo		info = new MapInfo(name, title, width, height, world);

				maps.put(name, info);
			}
		} finally {
			rs.close();
		}
	}
	
	public void createMap(String name, String title, int width, int height, boolean world) throws SQLException {
		insertMaps.clearParameters();
		insertMaps.setString(1, name);
		insertMaps.setString(2, title);
		insertMaps.setInt(3, width);
		insertMaps.setInt(4, height);
		insertMaps.setBoolean(5, world);
		
		insertMaps.executeUpdate();
		refresh();
	}
	
	public MapInfo[] getMaps() {
		return maps.values().toArray(new MapInfo[0]);
	}
	
	public MapInfo getMap(String name) {
		return maps.get(name);
	}
	
	public static void main(String[] args) throws Exception {
		AppManager		app = new AppManager();
		
		MapManager		manager = new MapManager(app.getDatabaseConnection());
		//manager.createMap("test", "First Test Map", 1000, 1000, false);
		System.out.println(manager.getMap("test").getTitle());
	}
}
