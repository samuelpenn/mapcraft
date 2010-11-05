package uk.org.glendale.mapcraft.map;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Represents an area of the map 32x40 tiles in size. Top left coordinate
 * is 0,0, bottom right is 31,39.
 * 
 * Because Sectors are always part of a larger map, coordinates are
 * accepted as either the map coordinate or the sector coordinate,
 * i.e. all coordinates have x%32 and y%40 applied to them before use.
 * 
 * A Sector has no concept of persistence, but methods to set/read all
 * the data in a single block are provided for the database layer to use.
 * 
 * Note that a Sector's view on things is very low level. It stores the
 * ids of terrain and features, but not the actual class types. This is
 * to keep the in-memory foot print fast and light.
 * 
 * @author Samuel Penn
 */
public class Sector {
	public static final int WIDTH = 32;
	public static final int HEIGHT = 40;
	
	private int			originX;
	private int			originY;
	private boolean		dirty = false;
	private long		lastUsed = 0;
	
	// Array of terrain data, [X][Y].
	private boolean[][]	changed;
	private int[][]		terrain;
	private int[][]		feature;
	private int[][]		area;
	
	// List of Things in this sector
	private Hashtable<Integer,NamedPlace>	places = new Hashtable<Integer,NamedPlace>();
	
	/**
	 * Create a new blank sector with the given origin.
	 * 
	 * @param originX
	 * @param originY
	 */
	public Sector(int originX, int originY) {
		this.originX = originX;
		this.originY = originY;
		
		changed = new boolean[WIDTH][HEIGHT];
		terrain = new int[WIDTH][HEIGHT];
		feature = new int[WIDTH][HEIGHT];
		area = new int[WIDTH][HEIGHT];
		// Mark terrain as unset.
		for (int x=0; x < WIDTH; x++) {
			for (int y=0; y < HEIGHT; y++) {
				terrain[x][y] = -1;
				feature[x][y] = -1;
				area[x][y] = -1;
				changed[x][y] = false;
			}
		}
	}
	
	public int getOriginX() {
		return originX;
	}
	
	public int getOriginY() {
		return originY;
	}
	
	/**
	 * Has the data been changed since it was initialised?
	 * 
	 * @return		True if any of the data has changed.
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Has a given tile been changed since it was last read from
	 * the database?
	 * 
	 * @param x		X coordinate to check.
	 * @param y		Y coordinate to check.
	 * @return		True iff some aspect of the tile has been changed. 
	 */
	public boolean isDirty(int x, int y) {
		return changed[x][y];
	}
	
	/**
	 * Gets all the terrain data as a single array. Used for purposes
	 * of reading/writing the whole data set to the database. Values of
	 * -1 represent 'unset' data that shouldn't be stored.
	 * 
	 * @return		Array of terrain data.
	 */
	public int[][] getTerrainData() {
		return terrain;
	}
	
	public int[][] getFeatureData() {
		return feature;
	}
	
	public int[][] getAreaData() {
		return area;
	}
	
	public Hashtable<Integer,NamedPlace> getPlaceData() {
		return places;
	}
	
	/**
	 * Sets the terrain data to the passed array. Used for purposes
	 * of reading/writing the whole data set to the database. Note
	 * that doing this clears the isDirty flag.
	 * 
	 * @param terrain	Array of terrain data.
	 * @param feature	Array of feature data.
	 */
	public void setMapData(int[][] terrain, int[][] feature, int[][] area, Hashtable<Integer,NamedPlace> places) {
		this.lastUsed = System.currentTimeMillis();
		this.terrain = terrain;
		this.feature = feature;
		this.area = area;
		this.places = places;
		this.dirty = false;
	}
	
	/**
	 * Read the terrain at this location. If the tile is unset, search
	 * upwards for the nearest set terrain item.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getTerrain(int x, int y) {
		this.lastUsed = System.currentTimeMillis();
		x %= WIDTH; y %= HEIGHT;
		int		t = terrain[x][y];
		if (t > -1) {
			return t;
		}
		// Look for sub-sector value.
		x -= x%8; y-= y%10;
		if (terrain[x][y] > -1) return terrain[x][y];
		// Return sector value.
		return terrain[0][0];
	}
	
	/**
	 * Gets the feature at the given coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getFeature(int x, int y) {
		this.lastUsed = System.currentTimeMillis();
		x %= WIDTH; y %= HEIGHT;
		int		t = feature[x][y];
		if (t > -1) {
			return t;
		}
		// Look for sub-sector value.
		x -= x%8; y-= y%10;
		if (feature[x][y] > -1) return feature[x][y];
		// Return sector value.
		return feature[0][0];
	}

	public int getArea(int x, int y) {
		this.lastUsed = System.currentTimeMillis();
		x %= WIDTH; y %= HEIGHT;
		int		t = area[x][y];
		if (t > -1) {
			return t;
		}
		// Look for sub-sector value.
		x -= x%8; y-= y%10;
		if (area[x][y] > -1) return feature[x][y];
		// Return sector value.
		return area[0][0];
	}
	
	public void setTile(int x, int y, int terrainId, int featureId, int areaId) {
		this.lastUsed = System.currentTimeMillis();
		x %= WIDTH; y %= HEIGHT;
		int		oldTerrain = terrain[x][y];
		int		oldFeature = feature[x][y];
		int		oldArea = area[x][y];
		
		terrain[x][y] = terrainId;
		feature[x][y] = featureId;
		area[x][y] = areaId;
		changed[x][y] = true;
		dirty = true;
		
		if (x==0 && y==0) {
			// Need to modify any sub-sectors to inherit the old
			// value if they are still inheriting this value.
			for (int xx=0; xx < 32; xx+=8) {
				for (int yy=0; yy < 40; yy+=10) {
					if (terrain[xx][yy] == -1) {
						terrain[xx][yy] = oldTerrain;
						feature[xx][yy] = oldFeature;
						area[xx][yy] = oldArea;
						changed[xx][yy] = true;
					}
				}
			}
		} else if (x%8 == 0 && y%10 ==0) {
			for (int xx=x; xx < x+8; xx++) {
				for (int yy=y; yy < y+10; yy++) {
					if (terrain[xx][yy] == -1) {
						terrain[xx][yy] = oldTerrain;
						feature[xx][yy] = oldFeature;
						area[xx][yy] = oldArea;
						changed[xx][yy] = true;
					}
				}
			}
		}
		
	}
	
	public void setTerrain(int x, int y, int terrainId) {
		setTile(x, y, terrainId, getFeature(x, y), getArea(x, y));
	}

	public void setFeature(int x, int y, int featureId) {
		setTile(x, y, getTerrain(x, y), featureId, getArea(x, y));
	}
	
	public void setNamedArea(int x, int y, int areaId) {
		setTile(x, y, getTerrain(x, y), getFeature(x, y), areaId);
	}
	
	/**
	 * Add a Thing to the list of things in this sector. A thing is a
	 * place such as a town or castle.
	 * 
	 * @param thing		Thing to be added.
	 */
	public void addPlace(NamedPlace place) {
		places.put(place.getId(), place);
	}
	
	/**
	 * Gets the Thing identified by its unique id.
	 * 
	 * @param id		Id of Thing to retrieve.
	 * @return			Thing if found, null otherwise.
	 */
	public NamedPlace getPlace(int id) {
		return places.get(id);
	}
	
	/**
	 * Gets a list of all the Things present on the given tile.
	 * 
	 * @param x			X coordinate of tile.
	 * @param y			Y coordiante of tile.
	 * @return			List of things, may be empty.
	 */
	public List<NamedPlace> getPlaces(int x, int y) {
		List<NamedPlace>	foundPlaces = new ArrayList<NamedPlace>();
		
		for (NamedPlace place : places.values()) {
			if (place.getX()%WIDTH == x%WIDTH && place.getY()%HEIGHT == y%HEIGHT) {
				foundPlaces.add(place);
			}
		}
		
		return foundPlaces;
	}
}
