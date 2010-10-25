package uk.org.glendale.mapcraft.server;

import java.sql.SQLException;
import java.util.Hashtable;

import javax.faces.bean.*;

import uk.org.glendale.mapcraft.map.Feature;
import uk.org.glendale.mapcraft.map.Terrain;
import uk.org.glendale.mapcraft.server.database.MapManager;

/**
 * Keeps track of metadata about a map.
 * 
 * @author Samuel Penn
 *
 */
@ManagedBean @RequestScoped
public class MapInfo {
	private String	name;
	private String	title;
	
	private int		width;
	private int		height;
	private boolean	world;
	
	private Hashtable<Integer,Terrain>	terrain = new Hashtable<Integer,Terrain>();
	private Hashtable<Integer,Feature>	feature = new Hashtable<Integer,Feature>();
	
	private MapManager	manager = null;
	
	
	public MapInfo() throws SQLException {
		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
	}
	
	public MapInfo(String name, String title, int width, int height, boolean world) {
		this.name = name;
		this.title = title;
		this.width = width;
		this.height = height;
	}
	
	public void setName(String name) {
		if (name == null) {
			this.name = name;
		} else {
			this.name = name;
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
		return name;
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
}
