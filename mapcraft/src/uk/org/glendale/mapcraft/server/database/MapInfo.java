package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.faces.bean.*;

import uk.org.glendale.mapcraft.MapEntityException;
import uk.org.glendale.mapcraft.map.NamedArea;
import uk.org.glendale.mapcraft.map.Feature;
import uk.org.glendale.mapcraft.map.NamedPlace;
import uk.org.glendale.mapcraft.map.Terrain;
import uk.org.glendale.mapcraft.map.Thing;
import uk.org.glendale.mapcraft.server.AppManager;

/**
 * Keeps track of metadata about a map.
 * 
 * @author Samuel Penn
 *
 */
@ManagedBean @RequestScoped
public class MapInfo {
	private String	prefix;
	private String	title;
	
	private int		width;
	private int		height;
	private boolean	world;
	
	private Hashtable<Integer,Terrain>		terrain = new Hashtable<Integer,Terrain>();
	private Hashtable<Integer,Feature>		feature = new Hashtable<Integer,Feature>();
	private Hashtable<Integer,Thing>		things = new Hashtable<Integer,Thing>();

	private Hashtable<Integer,NamedArea>	areas = new Hashtable<Integer,NamedArea>();
	
	private MapManager	manager = null;
	
	
	public MapInfo() throws SQLException {
		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
	}
	
	public MapInfo(MapManager manager, String name, String title, int width, int height, boolean world) throws SQLException {
		this.manager = manager;
		this.prefix = name;
		this.title = title;
		this.width = width;
		this.height = height;
	}
	
	public void setName(String name) {
		if (name == null) {
			this.prefix = name;
		} else {
			this.prefix = name;
			this.title = "Map of "+name;			
		}
		manager.getMap(this);
	}
	
	/**
	 * The unique name for this map. Also the prefix used in the
	 * database tables that store the map data.
	 * 
	 * @return		Name for the map.
	 */
	public String getName() {
		return prefix;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	/**
	 * Does this map represent a spherical world? World maps need to
	 * account for how the sphere is projected onto a flat map, so not
	 * all of the map is available to draw on.
	 *  
	 * @return		True if map is a world.
	 */
	public boolean isWorld() {
		return world;
	}
	
	/**
	 * The width of the map in tiles.
	 * @param width		Number of tiles.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * The height of the map in tiles. This is normally
	 * half the width.
	 * 
	 * @param height	Number of tiles.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void addTerrain(Terrain t) {
		terrain.put(t.getId(), t);
	}
	
	public Terrain getTerrain(int id) {
		return terrain.get(id);
	}
	
	public Terrain getTerrain(String name) {
		for (Terrain t : terrain.values()) {
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}
	
	public void addFeature(Feature f) {
		feature.put(f.getId(), f);
	}
	
	public Feature getFeature(int id) {
		return feature.get(id);
	}

	public Feature getFeature(String name) {
		for (Feature f : feature.values()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	public void addNamedArea(NamedArea a) {
		areas.put(a.getId(), a);
	}
	
	public NamedArea getNamedArea(int id) {
		return areas.get(id);
	}
	
	public NamedArea getNamedArea(String name) {
		for (NamedArea a : areas.values()) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		System.out.println("Unable to find area ["+name+"]");
		return null;
	}
	
	/**
	 * Add a new named area to the map and store it in the database.
	 * New NamedAreas can be created at any time, so the database needs to be
	 * updated automatically and a new unique id generated.
	 * 
	 * @param name
	 * @param title
	 * @param parentId
	 * @throws SQLException 
	 * @throws MapEntityException 
	 */
	public void addNamedArea(String name, String title, int parentId) throws SQLException, MapEntityException {
		Connection			cx = manager.getConnection();
		ResultSet			rs = null;
		PreparedStatement	select = null;
		PreparedStatement	insert = null;
		
		try {
			select = cx.prepareStatement("SELECT id FROM "+prefix+"_area WHERE name=?");
			select.clearParameters();
			select.setString(1, name);
			
			rs = select.executeQuery();
			if (rs.next()) {
				throw new MapEntityException("NamedArea", name, "NamedArea already exists");
			}
			rs.close();
			select.close();

			insert = cx.prepareStatement("INSERT INTO "+prefix+"_area (name, title, parent_id) VALUES(?,?,?)", 
										 PreparedStatement.RETURN_GENERATED_KEYS);
			insert.clearParameters();
			insert.setString(1, name);
			insert.setString(2, title);
			insert.setInt(3, parentId);
			insert.executeUpdate();
			rs = insert.getGeneratedKeys();
			if (rs.next()) {
				int	 id = rs.getInt(1);
				addNamedArea(new NamedArea(id, name, title, parentId));
			}
			rs.close();
			System.out.println(areas.size());
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
			if (select != null && !select.isClosed()) {
				select.close();
			}
			if (insert != null && !insert.isClosed()) {
				insert.close();
			}
		}
	}
	
	public void addThing(Thing thing) {
		things.put(thing.getId(), thing);
	}
	
	public Thing getThing(int id) {
		return things.get(id);
	}
	
	public Thing getThing(String name) {
		for (Thing t : things.values()) {
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;		
	}

	/**
	 * Factory method to create a NamedPlace.
	 * 
	 * @param thing
	 * @param name
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 * @return
	 * @throws SQLException
	 * @throws MapEntityException
	 */
	public NamedPlace createNamedPlace(Thing thing, String name, int x, int y, int sx, int sy) throws SQLException, MapEntityException {
		Connection			cx = manager.getConnection();
		ResultSet			rs = null;
		PreparedStatement	insert = null;
		NamedPlace			place = null;
		
		try {
			insert = cx.prepareStatement("INSERT INTO "+prefix+"_things (thing_id, name, x, y, sx, sy) VALUES(?,?,?,?,?,?)", 
										 PreparedStatement.RETURN_GENERATED_KEYS);
			insert.clearParameters();
			insert.setInt(1, thing.getId());
			insert.setString(2, name);
			insert.setInt(3, x);
			insert.setInt(4, y);
			insert.setInt(5, sx);
			insert.setInt(6, sy);
			insert.executeUpdate();
			rs = insert.getGeneratedKeys();
			if (rs.next()) {
				int	 id = rs.getInt(1);
				place = new NamedPlace(id, thing.getId(), name, x, y, sx, sy);
			}
			rs.close();
			System.out.println(areas.size());
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
			if (insert != null && !insert.isClosed()) {
				insert.close();
			}
		}
		return place;
	}

	public List<NamedPlace> getNamedPlaces(int west, int north, int east, int south) throws SQLException {
		Connection			cx = manager.getConnection();
		ResultSet			rs = null;
		PreparedStatement	select = null;
		List<NamedPlace>	places = new ArrayList<NamedPlace>();
		
		try {
			select = cx.prepareStatement("SELECT id, thing_id, name, x, y, sx, sy FROM "+prefix+"_things WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?");
			select.clearParameters();
			select.setInt(1, west);
			select.setInt(2, east);
			select.setInt(3, north);
			select.setInt(4, south);
			
			rs = select.executeQuery();
			while (rs.next()) {
				NamedPlace	place = new NamedPlace(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7));
				places.add(place);
			}
			rs.close();
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		}
		
		return places;
	}
}
