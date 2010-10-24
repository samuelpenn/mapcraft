package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import uk.org.glendale.mapcraft.map.Terrain;
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
	
	public void disconnect() {
		try {
			cx.close();
		} catch (SQLException e) {
			
		}
	}
	
	private void prepareStatements() throws SQLException {
		selectMaps = cx.prepareStatement(selectMapsSQL);
		insertMaps = cx.prepareStatement(insertMapsSQL);
	}
	
	/**
	 * Refresh list of all available maps on this server.
	 * 
	 * @throws SQLException
	 */
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
				readTerrain(info);

				maps.put(name, info);
			}
		} finally {
			rs.close();
		}
	}
	
	
	private void readTerrain(MapInfo info) throws SQLException {
		Statement	stmnt = cx.createStatement();
		
		ResultSet	rs = stmnt.executeQuery("SELECT * from "+info.getName()+"_terrain");
		while (rs.next()) {
			int		id = rs.getInt("id");
			String	name = rs.getString("name");
			String	title = rs.getString("title");
			String	image = rs.getString("image");
			
			info.addTerrain(new Terrain(id, name, title, image, 0));
		}
		rs.close();
	}
	
	/**
	 * Create a new map in the database. A new entry is created in the table
	 * holding a list of all maps, plus database tables are created to store
	 * the map data for this map.
	 * 
	 * @param name		Unique name of the map.
	 * @param title		Descriptive title for the map.
	 * @param width		Width of the map, in tiles.
	 * @param height	Height of the map, in tiles.
	 * @param world		Is this map a spherical world map?
	 * 
	 * @throws SQLException
	 */
	public void createMap(String name, String title, int width, int height, boolean world) throws SQLException {
		insertMaps.clearParameters();
		insertMaps.setString(1, name);
		insertMaps.setString(2, title);
		insertMaps.setInt(3, width);
		insertMaps.setInt(4, height);
		insertMaps.setBoolean(5, world);
		
		insertMaps.executeUpdate();
		createTables(name);
	}
	
	private void createTables(String prefix) throws SQLException {
		Statement		stmnt = cx.createStatement();
		
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_terrain (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(32) NOT NULL, title VARCHAR(32) NOT NULL, image VARCHAR(16) NOT NULL, PRIMARY KEY(id))");
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_map (x INT NOT NULL, y INT NOT NULL, terrain_id INT NOT NULL, PRIMARY KEY(x, y))");
		
		addTerrain(prefix, "water.sea", "Sea", "sea");
		addTerrain(prefix, "temperate.grassland", "Grassland", "grass");
		addTerrain(prefix, "temperate.hills", "Hills", "hills");
		addTerrain(prefix, "temperate.woodland", "Light woodland", "woods");
		addTerrain(prefix, "temperate.moors", "Moorland", "moors");
		addTerrain(prefix, "temperate.heath", "Heathland", "heath");
		addTerrain(prefix, "temperate.crops", "Farmland", "cropland");
		
		refresh();
		fillMap(getMap(prefix), 1);
	}
	
	private void addTerrain(String prefix, String name, String title, String image) throws SQLException {
		PreparedStatement	ps = cx.prepareStatement("INSERT INTO "+prefix+"_terrain (name, image, title) VALUES(?,?,?)");
		
		ps.setString(1, name);
		ps.setString(2, image);
		ps.setString(3, title);
		
		ps.execute();
	}
	
	private void fillMap(MapInfo map, int terrainId) throws SQLException {
		MapData		data = new MapData(map, cx);
		for (int y=0; y < map.getHeight(); y+=40) {
			for (int x=0; x < map.getWidth(); x+=32) {
				data.setTerrain(x, y, terrainId);
			}
		}
	}
	
	public MapInfo[] getMaps() {
		return maps.values().toArray(new MapInfo[0]);
	}
	
	public MapInfo getMap(String name) {
		return maps.get(name);
	}
	
	public void getMap(MapInfo info) {
		MapInfo	map = maps.get(info.getName());
		info.setTitle(map.getTitle());
		info.setHeight(map.getHeight());
		info.setWidth(map.getWidth());
	}
	
	public static void main(String[] args) throws Exception {
		AppManager		app = new AppManager();
		
		MapManager		manager = new MapManager(app.getDatabaseConnection());
		manager.createMap("eorthe", "World of Eorthe", 8000, 4000, true);
		System.out.println(manager.getMap("eorthe").getTitle());
	}
}
