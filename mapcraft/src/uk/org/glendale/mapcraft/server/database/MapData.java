package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import uk.org.glendale.mapcraft.map.NamedPlace;
import uk.org.glendale.mapcraft.map.Sector;

/**
 * Implementation of the persisted map data. Each map consists of tiles,
 * each of which is stored as an individual row in the database. A more
 * efficient packing may be used at a later date, once it is known
 * exactly what data needs to be stored.
 * 
 * It is possible to set/get individual tiles, however the majority of
 * operations are assumed to be bulk reads and updates. To facilitate
 * this, a map is divided into 'sectors' (and 'sub-sectors'), each
 * sector being 32x40 tiles (w*h). A sub-sector is 8x10. A read will
 * read an entire sector's worth of data.
 * 
 * A sector is suspiciously similar to a Traveller sector in size. It
 * also happens to fit nicely onto an A4 page at a sensible resolution.
 * 
 * To save on storage, a sector may be 'sparse'. A sparse sector has
 * a single tile defined (0, 0), which represents the entire sector.
 * The same holds true for sub-sectors. This allows undeveloped parts
 * of the map to be stored at low resolution. 
 * 
 * @author Samuel Penn
 */
public class MapData {
	private Connection		cx;
	private String			prefix;	
	
	public MapData(String prefix, Connection cx) throws SQLException {
		this.cx = cx;
		this.prefix = prefix;
		
		prepareStatements();
	}

	private PreparedStatement   deleteTerrain;
	private PreparedStatement	insertTerrain;
	private PreparedStatement	selectSector;
	private PreparedStatement	selectPlaces;
	
	private void prepareStatements() throws SQLException {
		deleteTerrain = cx.prepareStatement("DELETE FROM "+prefix+"_map WHERE x=? AND y=?");
		insertTerrain = cx.prepareStatement("INSERT INTO "+prefix+"_map VALUES(?, ?, ?, ?, ?)");
		
		selectSector = cx.prepareStatement("SELECT x, y, terrain_id, feature_id, area_id FROM "+prefix+"_map WHERE x >= ? AND x < ? AND y >= ? AND y < ?");
		selectPlaces = cx.prepareStatement("SELECT id, thing_id, name, title, importance, x, y, sx, sy FROM "+prefix+"_things WHERE x >= ? AND x < ? AND y >= ? AND y < ?");
	}
	
	public void setTile(int x, int y, int terrainId, int featureId, int areaId) throws SQLException {
		deleteTerrain.clearParameters();
		deleteTerrain.setInt(1, x);
		deleteTerrain.setInt(2, y);
		deleteTerrain.executeUpdate();
		
		insertTerrain.clearParameters();
		insertTerrain.setInt(1, x);
		insertTerrain.setInt(2, y);
		insertTerrain.setInt(3, terrainId);
		insertTerrain.setInt(4, featureId);
		insertTerrain.setInt(5, areaId);
		insertTerrain.executeUpdate();
	}
	
	/**
	 * Read all terrain and feature data for the given sector.
	 * 
	 * @param origX
	 * @param origY
	 * @return
	 * @throws SQLException
	 */
	public Sector readSector(int origX, int origY) throws SQLException {
		Sector		sector = new Sector(origX, origY);
		
		selectSector.clearParameters();
		selectSector.setInt(1, origX);
		selectSector.setInt(2, origX+Sector.WIDTH);
		selectSector.setInt(3, origY);
		selectSector.setInt(4, origY+Sector.HEIGHT);
		
		ResultSet	rs = selectSector.executeQuery();

		int[][]		terrainData = sector.getTerrainData();
		int[][]		featureData = sector.getFeatureData();
		int[][]		areaData = sector.getAreaData();
		while (rs.next()) {
			int		x = rs.getInt(1);
			int		y = rs.getInt(2);
			terrainData[x%Sector.WIDTH][y%Sector.HEIGHT] = rs.getInt(3);
			featureData[x%Sector.WIDTH][y%Sector.HEIGHT] = rs.getInt(4);
			areaData[x%Sector.WIDTH][y%Sector.HEIGHT] = rs.getInt(5);
		}
		rs.close();
		
		Hashtable<Integer,NamedPlace> places = new Hashtable<Integer,NamedPlace>();
		selectPlaces.clearParameters();
		selectPlaces.setInt(1, origX);
		selectPlaces.setInt(2, origX+Sector.WIDTH);
		selectPlaces.setInt(3, origY);
		selectPlaces.setInt(4, origY+Sector.HEIGHT);
		rs = selectPlaces.executeQuery();
		while (rs.next()) {
			int		id = rs.getInt(1);
			int		thingId = rs.getInt(2);
			String	name = rs.getString(3);
			String	title = rs.getString(4);
			short	importance = rs.getShort(5);
			int		x = rs.getInt(6);
			int		y = rs.getInt(7);
			int		sx = rs.getInt(8);
			int		sy = rs.getInt(9);
			places.put(id, new NamedPlace(id, thingId, name, title, importance, x, y, sx, sy));
			System.out.println("Adding ["+name+"] to sector");
		}
		rs.close();
		
		sector.setMapData(terrainData, featureData, areaData, places);
		
		return sector;
	}
	
	public void writeSector(Sector sector) throws SQLException {
		if (!sector.isDirty()) {
			// Nothing changed, so don't write to database.
			return;
		}
		
		int	ox = sector.getOriginX();
		int	oy = sector.getOriginY();
		for (int x=0; x < Sector.WIDTH; x++) {
			for (int y=0; y < Sector.HEIGHT; y++) {
				if (sector.isDirty(x, y)) {
					setTile(x+ox, y+oy, sector.getTerrain(x, y), sector.getFeature(x, y), sector.getArea(x, y));
				}
			}
		}
	}
}
