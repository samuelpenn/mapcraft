package uk.org.glendale.mapcraft.map;

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
	
	public Sector(int originX, int originY) {
		this.originX = originX;
		this.originY = originY;
		
		changed = new boolean[WIDTH][HEIGHT];
		terrain = new int[WIDTH][HEIGHT];
		// Mark terrain as unset.
		for (int x=0; x < WIDTH; x++) {
			for (int y=0; y < HEIGHT; y++) {
				terrain[x][y] = -1;
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
	public int[][]	getTerrainData() {
		return terrain;
	}
	
	/**
	 * Sets the terrain data to the passed array. Used for purposes
	 * of reading/writing the whole data set to the database. Note
	 * that doing this clears the isDirty flag.
	 * 
	 * @param terrain	Array of terrain data.
	 */
	public void setTerrainData(int[][] terrain) {
		this.lastUsed = System.currentTimeMillis();
		this.terrain = terrain;
		this.dirty = false;
	}
	
	public int getTerrain(int x, int y) {
		this.lastUsed = System.currentTimeMillis();
		int		t = terrain[x%WIDTH][y%HEIGHT];
		if (t == -1) {
			return terrain[0][0];
		}
		return t;
	}
	
	public void setTerrain(int x, int y, int terrainId) {
		this.lastUsed = System.currentTimeMillis();
		terrain[x%WIDTH][y%HEIGHT] = terrainId;
		changed[x%WIDTH][y%HEIGHT] = true;
		dirty = true;
	}
}
