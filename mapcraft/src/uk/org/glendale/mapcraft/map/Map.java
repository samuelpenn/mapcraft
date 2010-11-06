package uk.org.glendale.mapcraft.map;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapInfo;

/**
 * Represents a complete map. Caches map data provided by the database.
 * 
 * @author Samuel Penn
 */
public class Map {
	private MapInfo		info;
	private MapData		data;
	private Hashtable<SectorCoord,Sector>	sectorCache = new Hashtable<SectorCoord,Sector>();
	
	private static Logger		log = Logger.getLogger("uk.org.glendale.mapcraft.map.Map");
		
	public Map(MapInfo info, MapData data) {
		if (info == null || data == null) {
			throw new IllegalArgumentException("Both map info and data must be non-null");
		}
		this.info = info;
		this.data = data;
	}
	
	public MapInfo getInfo() {
		return info;
	}
	
	public MapData getData() {
		return data;
	}
	
	private void shrinkCache() {
		Sector		oldestDirty = null;
		Sector		oldestClean = null;
		
		long		autoCleanTime = System.currentTimeMillis() - 60000;
		
		if (sectorCache.size() < 128) {
			// Cache is small, don't bother cleaning it.
			return;
		}
		for (Sector s : sectorCache.values()) {
			if (s.isDirty()) {
				if (oldestDirty == null) {
					oldestDirty = s;
				} else if (oldestDirty.getLastUsedTime() > s.getLastUsedTime()) {
					oldestDirty = s;
				}
			} else {
				if (s.getLastUsedTime() < autoCleanTime) {
					sectorCache.remove(s.getCoord());
				} else if (oldestClean == null) {
					oldestClean = s;
				} else if (oldestClean.getLastUsedTime() > s.getLastUsedTime()){
					oldestClean = s;
				}
			}
		}
		if (sectorCache.size() > 128) {
			if (oldestClean != null) {
				sectorCache.remove(oldestClean.getCoord());
			} else if (oldestDirty != null) {
				try {
					data.writeSector(oldestDirty);
					sectorCache.remove(oldestDirty.getCoord());
				} catch (SQLException e) {
					log.log(Level.SEVERE, "Unable to write sector", e);
				}
			}
		}
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
				shrinkCache();
				//log.info("Reading sector ["+c.getX()+","+c.getY()+"] cache size ["+sectorCache.size()+"]");
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
	
	/**
	 * Gets the id of the terrain for the given tile. The coordinate is the
	 * coordinate for the entire map, with (0,0) being the top left corner.
	 * The whole sector will be read and cached to improve reading times
	 * for subsequent calls.
	 * 
	 * @param x		X coordinate.
	 * @param y		Y coordinate.
	 * @return
	 */
	public int getTerrain(int x, int y) {
		Sector	s = getSector(x, y);
		return s.getTerrain(x, y);
	}
	
	/**
	 * Sets the terrain for the given tile. The coordinate is the
	 * coordinate for the entire map, with (0,0) being the top left corner.
	 * The relevant sector will be cached, and changes won't be written to
	 * the database until later.
	 * 
	 * @param x				X coordinate.
	 * @param y				Y coordinate.
	 * @param terrainId		Terrain type to set tile to.
	 */
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
	
	public int getArea(int x, int y) {
		Sector	s = getSector(x, y);
		return s.getArea(x, y);
	}
	
	public void setNamedArea(int x, int y, int areaId) {
		Sector s = getSector(x, y);
		s.setNamedArea(x, y, areaId);
	}
	
	public void addNamedPlace(NamedPlace place) {
		Sector	s = getSector(place.getX(), place.getY());
		s.addPlace(place);
	}
	
}
