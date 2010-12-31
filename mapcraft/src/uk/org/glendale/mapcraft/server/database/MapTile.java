package uk.org.glendale.mapcraft.server.database;

/**
 * Represents a specific tile. Used by some methods to return a full
 * set of information about a tile in a single query.
 * 
 * @author Samuel Penn
 */
public class MapTile {
	private int		x, y;
	private int		terrainId;
	private int		featureId;
	private int		areaId;
	
	public MapTile(int x, int y, int terrainId, int featureId, int areaId) {
		this.x = x;
		this.y = y;
		this.terrainId = terrainId;
		this.featureId = featureId;
		this.areaId = areaId;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getTerrainId() {
		return terrainId;
	}
	
	public int getFeatureId() {
		return featureId;
	}
	
	public int getAreaId() {
		return areaId;
	}
}
