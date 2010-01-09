package uk.org.glendale.mapcraft.server;

/**
 * Keeps track of metadata about a map.
 * 
 * @author Samuel Penn
 *
 */
public class MapInfo {
	private String	name;
	private String	title;
	
	private int		width;
	private int		height;
	private boolean	world;
	
	public MapInfo(String name, String title, int width, int height, boolean world) {
		this.name = name;
		this.title = title;
		this.width = width;
		this.height = height;
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
	
}
