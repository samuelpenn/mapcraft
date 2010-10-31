package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.NamedArea;
import uk.org.glendale.mapcraft.map.Feature;
import uk.org.glendale.mapcraft.map.Terrain;
import uk.org.glendale.mapcraft.server.AppManager;

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
		if (cx == null || cx.isClosed()) {
			throw new IllegalArgumentException("Connection is null or closed");
		}
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
	
	public Connection getConnection() {
		return cx;
	}
	
	private void validateName(String name) throws IllegalArgumentException {
		if (name == null || name.length() == 0 || name.length() > 16 || !name.matches("[a-zA-Z0-9]+")) {
			throw new IllegalArgumentException("Illegal map name");
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
				
				System.out.println("Adding map ["+name+"] ("+width+"x"+height+")");
				
				MapInfo		info = new MapInfo(this, name, title, width, height, world);
				readTerrain(info);

				maps.put(name, info);
			}
		} finally {
			rs.close();
		}
	}
	
	
	private void readTerrain(MapInfo info) throws SQLException {
		Statement	stmnt = cx.createStatement();

		// Read list of terrain types.
		ResultSet	rs = stmnt.executeQuery("SELECT * from "+info.getName()+"_terrain");
		while (rs.next()) {
			int		id = rs.getInt("id");
			String	name = rs.getString("name");
			String	title = rs.getString("title");
			String	image = rs.getString("image");
			
			info.addTerrain(new Terrain(id, name, title, image, 0));
		}
		rs.close();

		// Read list of feature types.
		rs = stmnt.executeQuery("SELECT * from "+info.getName()+"_feature");
		while (rs.next()) {
			int		id = rs.getInt("id");
			String	name = rs.getString("name");
			String	title = rs.getString("title");
			String	image = rs.getString("image");
			
			info.addFeature(new Feature(id, name, title, image));
		}
		rs.close();

		// Read list of named areas.
		rs = stmnt.executeQuery("SELECT * from "+info.getName()+"_area");
		while (rs.next()) {
			int		id = rs.getInt("id");
			String	name = rs.getString("name");
			String	title = rs.getString("title");
			int		parentId = rs.getInt("parent_id");
			
			info.addNamedArea(new NamedArea(id, name, title, parentId));
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
		validateName(name);
		insertMaps.clearParameters();
		insertMaps.setString(1, name);
		insertMaps.setString(2, title);
		insertMaps.setInt(3, width);
		insertMaps.setInt(4, height);
		insertMaps.setBoolean(5, world);
		
		insertMaps.executeUpdate();
		createTables(name);
	}
	
	/**
	 * Delete a map from the database. All references and data for tha map are
	 * removed.
	 * 
	 * @param name		Name of map.
	 * @throws SQLException
	 */
	public void deleteMap(String name) throws SQLException {
		validateName(name);
		Statement		stmnt = cx.createStatement();
		stmnt.executeUpdate("DELETE FROM mapcraft WHERE name='"+name+"'");
		stmnt.executeUpdate("DROP TABLE IF EXISTS "+name+"_terrain");
		stmnt.executeUpdate("DROP TABLE IF EXISTS "+name+"_feature");
		stmnt.executeUpdate("DROP TABLE IF EXISTS "+name+"_area");
		stmnt.executeUpdate("DROP TABLE IF EXISTS "+name+"_map");
		
		refresh();
	}
	
	private void createTables(String prefix) throws SQLException {
		Statement		stmnt = cx.createStatement();
		
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_terrain (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(32) NOT NULL, title VARCHAR(32) NOT NULL, image VARCHAR(16) NOT NULL, PRIMARY KEY(id))");
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_feature (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(32) NOT NULL, title VARCHAR(32) NOT NULL, image VARCHAR(16) NOT NULL, PRIMARY KEY(id))");
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_area (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(32) NOT NULL, title VARCHAR(64) NOT NULL, parent_id INT DEFAULT 0, PRIMARY KEY(id))");
		stmnt.executeUpdate("CREATE TABLE "+prefix+"_map (x INT NOT NULL, y INT NOT NULL, terrain_id INT NOT NULL, feature_id INT DEFAULT 0, area_id INT DEFAULT 0, PRIMARY KEY(x, y))");
		
		addFeature(prefix, "hills.low", "Low hills", "lowhills");
		addFeature(prefix, "hills.high", "High hills", "highhills");
		addFeature(prefix, "mountains.low", "Low mountains", "lowmnts");
		addFeature(prefix, "mountains.medium", "Medium mountains", "medmnts");
		addFeature(prefix, "mountains.high", "High mountains", "highmnts");
		addFeature(prefix, "wetlands", "Wetlands", "wetlands");
		addFeature(prefix, "ice", "Ice", "ice");
		
		addTerrain(prefix, "water.ocean", "Ocean", "ocean");
		addTerrain(prefix, "water.sea", "Sea", "sea");
		addTerrain(prefix, "temperate.grassland", "Grassland", "grass");
		addTerrain(prefix, "temperate.crops", "Cropland", "cropland");
		addTerrain(prefix, "temperate.woodland", "Light woodland", "woods");
		addTerrain(prefix, "temperate.mixed", "Mixed woodland", "mixed_forest");
		addTerrain(prefix, "temperate.coniferous", "Coniferous woodland", "coniferous");
		addTerrain(prefix, "temperate.moors", "Moorland", "moors");
		addTerrain(prefix, "temperate.heath", "Heathland", "heath");
		addTerrain(prefix, "temperate.crops", "Farmland", "cropland");
		addTerrain(prefix, "subtropical.grassland", "Dry grassland", "dry");
		addTerrain(prefix, "subtropical.desert", "Desert", "desert");
		addTerrain(prefix, "boreal.forest", "Boreal forest", "boreal_forest");
		addTerrain(prefix, "arctic.snow", "Snow", "snow");
		
		addArea(prefix, "Unnamed", "unnamed", 0);
		
		refresh();
		fillMap(getMapInfo(prefix), 1, 0);
	}

	/**
	 * Add a terrain type into the map database.
	 * 
	 * @param prefix
	 * @param name
	 * @param title
	 * @param image
	 * @throws SQLException
	 */
	private void addTerrain(String prefix, String name, String title, String image) throws SQLException {
		PreparedStatement	ps = cx.prepareStatement("INSERT INTO "+prefix+"_terrain (name, image, title) VALUES(?,?,?)");
		
		ps.setString(1, name);
		ps.setString(2, image);
		ps.setString(3, title);
		
		ps.execute();
	}
	
	private void addFeature(String prefix, String name, String title, String image) throws SQLException {
		PreparedStatement	ps = cx.prepareStatement("INSERT INTO "+prefix+"_feature (name, image, title) VALUES(?,?,?)");
		
		ps.setString(1, name);
		ps.setString(2, image);
		ps.setString(3, title);
		
		ps.execute();		
	}
	
	private void addArea(String prefix, String name, String title, int parentId) throws SQLException {
		PreparedStatement	ps = cx.prepareStatement("INSERT INTO "+prefix+"_area (name, title, parent_id) VALUES(?,?,?)");
		
		ps.setString(1, name);
		ps.setString(2, title);
		ps.setInt(3, parentId);
		
		ps.execute();				
	}
	
	private void fillMap(MapInfo info, int terrainId, int featureId) throws SQLException {
		MapData		data = new MapData(info.getName(), cx);
		for (int y=0; y < info.getHeight(); y+=40) {
			for (int x=0; x < info.getWidth(); x+=32) {
				data.setTile(x, y, terrainId, featureId, 0);
			}
		}
	}
	
	public MapInfo[] getMaps() {
		return maps.values().toArray(new MapInfo[0]);
	}
	
	public MapInfo getMapInfo(String name) {
		validateName(name);
		return maps.get(name);
	}
	
	public Map getMap(String name) throws SQLException {
		validateName(name);
		
		MapInfo		info = maps.get(name);
		MapData		data = new MapData(name, cx);
		
		return new Map(info, data);
	}
	
	public void getMap(MapInfo info) {
		validateName(info.getName());
		MapInfo	map = maps.get(info.getName());
		info.setTitle(map.getTitle());
		info.setHeight(map.getHeight());
		info.setWidth(map.getWidth());
	}
	
	public static void main(String[] args) throws Exception {
		AppManager		app = new AppManager();
		
		MapManager		manager = new MapManager(app.getDatabaseConnection());
		
		if (manager.getConnection() == null) {
			System.out.println("No database");
		}
		
		//manager.createMap("eorthe", "World of Eorthe", 8000, 4000, true);
		//System.out.println(manager.getMap("eorthe").getTitle());
		manager.deleteMap("test2");
		manager.createMap("test2", "Two layer test", 1000, 1000, true);
		System.out.println(manager.getMap("test2").getInfo().getTitle());
	}
}
