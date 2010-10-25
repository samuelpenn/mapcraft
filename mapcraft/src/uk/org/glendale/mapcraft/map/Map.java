package uk.org.glendale.mapcraft.map;

import java.sql.SQLException;
import java.util.Hashtable;

import uk.org.glendale.mapcraft.server.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapData;

/**
 * Represents a complete map. Caches map data provided by the database.
 * 
 * @author Samuel Penn
 */
public class Map {
	private MapInfo		info;
	private MapData		data;
	private Hashtable<SectorCoord,Sector>	sectorCache = new Hashtable<SectorCoord,Sector>();
	

	
	public Map(MapInfo info, MapData data) {
		this.info = info;
		this.data = data;
	}
	
	public MapInfo getInfo() {
		return info;
	}
	
	/**
	 * Retrieve a cached sector, or read it from the database if it
	 * isn't cached. Sectors are identified by the coordinate of
	 * their (0,0) tile, though any tile in the sector can be used,
	 * and it will be 'trimmed' to (0,0).
	 * 
	 * @param x		Coordinate of sector to read.
	 * @param y		Coordinate of sector to read.
	 * @return
	 */
	private Sector getSector(int x, int y) {
		SectorCoord	c = new SectorCoord(x, y);
		Sector	s = sectorCache.get(c);
		
		if (s == null) {
			try {
				s = data.readSector(c.getX(), c.getY());
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			sectorCache.put(c, s);
		}
		return s;
	}
	
	public void saveAll() {
		for (Sector sector : sectorCache.values()) {
			if (sector.isDirty()) {
				try {
					data.writeSector(sector);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		sectorCache.clear();
	}
	
	
	public int getTerrain(int x, int y) {
		Sector	s = getSector(x, y);
		return s.getTerrain(x, y);
	}
	
	public void setTerrain(int x, int y, int terrainId) {
		Sector	s = getSector(x, y);
		s.setTerrain(x, y, terrainId);
	}
	
	public int getFeature(int x, int y) {
		Sector	s = getSector(x, y);
		return s.getFeature(x, y);		
	}
	
	public void setFeature(int x, int y, int featureId) {
		Sector s = getSector(x, y);
		s.setFeature(x, y, featureId);
	}
	
}
