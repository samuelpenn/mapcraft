/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.12 $
 * $Date: 2007/12/08 16:35:31 $
 */

package uk.org.glendale.rpg.traveller.database;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import uk.org.glendale.database.Database;
import uk.org.glendale.rpg.traveller.civilisation.trade.Commodity;
import uk.org.glendale.rpg.traveller.civilisation.trade.Trade;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Factory class which generates Traveller objects - stars, systems and planets.
 * These should only be one instance of this in an application.
 * 
 * @author Samuel Penn
 *
 */
public class ObjectFactory {
	private Database				db = null;
	private static ObjectFactory	instance = null;
	private static final int		RECYCLE = 10;
	private static int				connectionCount = 0;
	
	public ObjectFactory() {
		//Log.info("ObjectFactory: Creating new database connection");
		db = Database.connect(Config.getDatabaseHost(), Config.getDatabaseName(),
							  Config.getDatabaseUser(), Config.getDatabasePassword());
	}
	
	private static ObjectFactory getInstance() {
		if (instance == null || connectionCount++ > RECYCLE) {
			instance = new ObjectFactory();
			connectionCount = 0;
		}
		
		return instance;
	}
	
	public void close() {
		db.disconnect();
	}
	
	public int persist(String table, Hashtable data) {
		try {
			if (data.get("id") != null) {
				int		id = (Integer)data.get("id");
				if (id == 0) {
					return db.insert(table, data);
				} else {
					db.replace(table, data, "id = "+id);
					return id;
				}
			} else {
				return db.insert(table, data);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.error(e);
		}
		
		return 0;
	}
	
	public void clean(String table, String query, String[] tables) {
		ResultSet		rs = null;
		
		try {
			rs = db.query("select id from "+table+" where "+query);
			if (rs.next()) {
				int		id = rs.getInt("id");
				
				db.delete(table, query);
				if (tables != null) {
					for (int i=0; i < tables.length; i++) {
						System.out.println("Deleting ["+tables[i]+"] where ["+table+"_id="+id+"]");
						db.delete(tables[i], table+"_id="+id);
					}
				}
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
	}
	
	public ResultSet read(String table, String where) throws SQLException {
		String		sql = "select * from "+table;
		
		if (where != null && where.length() > 0) {
			sql += " where "+where;
		}
		ResultSet	rs = null;
		
		rs = db.query(sql);
		
		return rs;
	}
	
	/**
	 * Get a list of all the sectors in the universe.
	 * @return		Alphabetical list of sectors.
	 */
	public Vector<Sector> getSectors() {
		Vector<Sector>		list = new Vector<Sector>();		
		ResultSet			rs = null;
		
		try {
			rs = db.query("select * from sector order by name");
			while (rs.next()) {
				int			id = rs.getInt("id");
				Sector		sector = new Sector(this, id);
				list.add(sector);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		} finally {
			db.tidy(rs);
		}
		
		return list;
	}
	
	/**
	 * Get a list of all StarSystems in the given sector. Each system will be
	 * populated with star and planet data.
	 * 
	 * @param sectorId		Id of sector to return data on.
	 * 
	 * @return		List of systems in a Vector, never null but may be empty.
	 */
	public Vector<StarSystem> getStarSystemsBySector(int sectorId) {
		return getStarSystemsBySector(sectorId, true);
	}

	/**
	 * Get a list of all StarSystems in the given sector. Each system will be
	 * populated with star and planet data if the populateSystems parameter
	 * is set to be true, otherwise just basic system data will be returned.
	 * 
	 * @param sectorId			Id of sector to return data on.
	 * @param populateSystems	If true, also get star and planet information.
	 * 
	 * @return		List of systems in a Vector, never null but may be empty.
	 */
	public Vector<StarSystem> getStarSystemsBySector(int sectorId, boolean populateSystems) {
		Vector<StarSystem>		list = new Vector<StarSystem>();
		ResultSet				rs = null;
	
		try {
			rs = read("system", "sector_id="+sectorId);
			while (rs.next()) {
				StarSystem		ss = new StarSystem(rs);
				if (populateSystems) {
					ss.setStars(getStarsBySystem(ss.getId()));
					ss.setPlanets(getPlanetsBySystem(ss.getId()));
				}
				list.add(ss);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return list;
	}

	/**
	 * Get a count of the number of star systems in the given sector.
	 * If no sector is found with the given id, then a count of 0 is
	 * returned.
	 * 
	 * @param id		Id of the sector to count.
	 * @return			Number of star systems.
	 */
	public int getSystemCount(int id) {
		ResultSet		rs = null;
		int				count = 0;
		try {
			rs = db.query("select count(*) as number from system where sector_id="+id);
			if (rs.next()) {
				count = rs.getInt("number");
			}
		} catch (SQLException e) {
			
		} finally {
			rs = db.tidy(rs);
		}
		
		return count;
	}
	
	/**
	 * Get the specified planet according to its id. If no planet is
	 * found, then an exception is thrown.
	 * 
	 * @param id		Unique id of the planet to fetch.
	 * @return			The planet.
	 */
	public Planet getPlanet(int id) throws ObjectNotFoundException  {
		ResultSet	rs = null;
		Planet		planet = null;
		
		try {
			rs = db.query("select * from planet where id=?", id);
			if (rs.next()) {
				planet = new Planet(rs);
			} else {
				throw new ObjectNotFoundException("Could not find a planet with id ["+id+"]");
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return planet;
	}
	
	/**
	 * Get the star system as the designated location in the sector.
	 * Sector coordinates are 1-32 for X, and 1-40 for Y. If the
	 * coordinates are outside of this, then try and find the star
	 * system in a neighbouring sector instead.
	 * 
	 * @param sectorId		Id of default sector to look in.
	 * @param x				X coordinate relative to sector.
	 * @param y				Y coordinate relative to sector.
	 * 
	 * @return				Found system, or null if none there.				
	 */
	public StarSystem getStarSystem(int sectorId, int x, int y) {
		ResultSet				rs = null;
		StarSystem				ss = null;
		
		if (x < 1 || x > 32 || y < 1 || y > 40) {
			try {
				Sector		sector = new Sector(this, sectorId);
				int			sx = sector.getX();
				int			sy = sector.getY();
				
				while (x < 1) { sx--; x += 32; }
				while (y < 1) { sy--; y += 40; }
				while (x > 32) { sx++; x -= 32; }
				while (y > 40) { sy++; y -= 40; }
				sector = new Sector(this, sx, sy);
				sectorId = sector.getId();
			} catch (ObjectNotFoundException e) {
				return null;
			}
		}
		
		try {
			rs = read("system", "sector_id="+sectorId+" and x="+x+" and y="+y);
			if (rs.next()) {
				ss = new StarSystem(this, rs);
				ss.setStars(getStarsBySystem(ss.getId()));
				ss.setPlanets(getPlanetsBySystem(ss.getId()));
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return ss;	
	}

	public StarSystem getStarSystem(int id) throws ObjectNotFoundException {
		ResultSet				rs = null;
		StarSystem				ss = null;
		
		try {
			rs = db.query("select * from system where id=?", id);
			if (rs.next()) {
				ss = new StarSystem(this, rs);
				ss.setStars(getStarsBySystem(ss.getId()));
				ss.setPlanets(getPlanetsBySystem(ss.getId()));
			} else {
				throw new ObjectNotFoundException("Cannot find star system with id ["+id+"]");
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return ss;		
	}
	
	public Vector<Star> getStarsBySystem(int systemId) {
		Vector<Star>		list = new Vector<Star>();
		ResultSet			rs = null;
	
		try {
			rs = read("star", "system_id="+systemId+" order by distance");
			while (rs.next()) {
				Star		star = new Star(rs);
				list.add(star);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return list;		
	}
	
	/**
	 * Get the star specified by its unique id.
	 * 
	 * @param starId		Id of the star to fetch.
	 * @return				Star, or null if not found.
	 */
	public Star getStar(int starId) {
		ResultSet		rs = null;
		Star			star = null;
		try {
			rs = read("star", "id="+starId);
			if (rs.next()) {
				star = new Star(rs);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return star;
	}

	public Vector<Planet> getPlanetsBySystem(int systemId) {
		Vector<Planet>		list = new Vector<Planet>();
		ResultSet			rs = null;
	
		try {
			rs = read("planet", "system_id="+systemId+" order by distance");
			while (rs.next()) {
				Planet		planet = new Planet(this, rs);
				list.add(planet);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return list;		
	}
	
	/**
	 * Get the number of major planets in the system, not including moons.
	 * 
	 * @param systemId		System to count.
	 * @return				Number of planets.
	 */
	public int getPlanetCount(int systemId) {
		ResultSet		rs = null;
		int				count = 0;
		try {
			rs = db.query("select count(*) as number from planet where moon = 0 and system_id="+systemId);
			if (rs.next()) {
				count = rs.getInt("number");
			}
		} catch (SQLException e) {
			
		} finally {
			rs = db.tidy(rs);
		}
		
		return count;		
	}
	
	/**
	 * Get the maximum of a value from a table.
	 */
	public long getMaximum(String table, String column, String where) {
		ResultSet		rs = null;
		String			sql = "select max("+column+") as number from "+table;
		if (where != null) {
			sql += " where "+where;
		}
		long			max = 0;
		try {
			rs = db.query(sql);
			if (rs.next()) {
				max = rs.getLong("number");
			}
		} catch (SQLException e) {
			
		} finally {
			rs = db.tidy(rs);
		}
		
		return max;
	}
	
	/**
	 * Get all the moons of the selected planet, if they exist.
	 * 
	 * @param planetId		Id of the planet to get the moons for.
	 * @return				List of moons, or empty list if none.
	 */
	public Vector<Planet> getMoons(int planetId) {
		Vector<Planet>		list = new Vector<Planet>();
		ResultSet			rs = null;
		
		try {
			rs = read("planet", "parent_id="+planetId+" and moon=1 order by distance");
			while (rs.next()) {
				Planet		planet = new Planet(this, rs);
				list.add(planet);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			db.tidy(rs);
		}
		
		return list;		
	}
	
	private int getStatistic(String sql) {
		ResultSet	rs = null;
		int			value = 0;
		try {
			rs = db.query(sql);
			if (rs.next()) {
				value = rs.getInt(1);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			rs = db.tidy(rs);
		}
		
		return value;
	}
	
	/**
	 * Returns statistics on the universe as it is currently known.
	 * 
	 * @return		Collection of interesting facts.
	 */
	public Hashtable<String,Integer> getStatistics() {
		Hashtable<String,Integer>		table = new Hashtable<String,Integer>();
		
		table.put("sectors", getStatistic("select count(*) from sector"));
		table.put("systems", getStatistic("select count(*) from system"));
		table.put("planets", getStatistic("select count(*) from planet"));
		table.put("moons", getStatistic("select count(*) from planet where moon=1"));
		table.put("life", getStatistic("select count(*) from planet where life='Extensive'"));
		table.put("minx", getStatistic("select min(x) from sector"));
		table.put("maxx", getStatistic("select max(x) from sector"));
		table.put("miny", getStatistic("select min(y) from sector"));
		table.put("maxy", getStatistic("select max(y) from sector"));
		
		return table;
	}
	
	public void storePlanetMap(int id, ByteArrayOutputStream stream) {
		String		table = "map";
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		byte[]		bytes = stream.toByteArray();
		
		data.put("planet_id", id);
		data.put("image", bytes);
		
		try {
			db.delete(table, "planet_id="+id);
			db.insert2(table, data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void storePlanetGlobe(int id, ByteArrayOutputStream stream) {
		String		table = "globe";
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		byte[]		bytes = stream.toByteArray();
		
		data.put("planet_id", id);
		data.put("image", bytes);
		
		try {
			db.delete(table, "planet_id="+id);
			db.insert2(table, data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ByteArrayOutputStream getPlanetMap(int id) {
		String		sql = "select image from map where planet_id=?";
		ResultSet	rs = null;
		ByteArrayOutputStream		stream = null;
		
		try {
			rs = db.query(sql, id);
			if (rs.next()) {
				byte[] data = rs.getBytes("image");
				stream = new ByteArrayOutputStream();
				stream.write(data);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return stream;
	}
	
	public boolean hasPlanetMap(int id) {
		return db.exists("map", "planet_id="+id);
	}

	public boolean hasPlanetGlobe(int id) {
		return db.exists("globe", "planet_id="+id);
	}

	public ByteArrayOutputStream getPlanetGlobe(int id) {
		String		sql = "select image from globe where planet_id=?";
		ResultSet	rs = null;
		ByteArrayOutputStream		stream = null;
		
		try {
			rs = db.query(sql, id);
			if (rs.next()) {
				byte[] data = rs.getBytes("image");
				stream = new ByteArrayOutputStream();
				stream.write(data);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return stream;
	}
	
	public void addNote(int planetId, String message) {
		addNote(planetId, null, message);
	}
	
	public void addNote(int planetId, String property, String message) {
		String						table = "note";
		Hashtable<String,Object>	data = new Hashtable<String,Object>();

		System.out.println("addNote: "+planetId+" - "+property);
		
		data.put("planet_id", planetId);
		if (property != null) {
			data.put("property", property);
		}
		data.put("message", message);
				
		try {
			db.insert2(table, data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String[] getNotes(int planetId) {
		return getNotes(planetId, null);
	}

	public String[] getNotes(int planetId, String property) {
		String				sql = "select message from note where planet_id=?";
		ResultSet			rs = null;
		ArrayList<String>   list = new ArrayList<String>();
		
		try {
			if (property == null) {
				sql += " and property is null";
				rs = db.query(sql, planetId);
			} else {
				sql += " and property=?";
				rs = db.query(sql, planetId, property);
			}
			while (rs.next()) {
				list.add(rs.getString("message"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list.toArray(new String[0]);
	}
	
	/**
	 * Clears a star system of all planets and stars. Used when about to
	 * recreate the system.
	 * 
	 * @param id		Id of the system to clear.
	 */
	public void cleanStarSystem(int id) throws ObjectNotFoundException {
		ResultSet		rs = null;
		StarSystem		system = getStarSystem(id);
		
		for (Planet planet : system.getPlanets()) {
			db.delete("note", "planet_id="+planet.getId());
			db.delete("globe", "planet_id="+planet.getId());
			db.delete("map", "planet_id="+planet.getId());
		}
			
		db.delete("planet", "system_id="+id);
		db.delete("star", "system_id="+id);
	}
	
	public Hashtable<Integer,Commodity> getAllCommodities() {
		String					sql = "select * from commodity";
		ResultSet				rs = null;
		Hashtable<Integer,Commodity>	list = new Hashtable<Integer,Commodity>();
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				Commodity	c = new Commodity(rs);
				list.put(c.getId(), c);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * Get all the commodities for a given planet.
	 * 
	 * @param planet_id		Id of the planet to get commodities list for.
	 */
	public Hashtable<Integer,Long> getCommoditiesByPlanet(int planet_id) {
		String					sql = "select commodity_id, amount from trade where planet_id="+planet_id;
		ResultSet				rs = null;
		Hashtable<Integer,Long>	list = new Hashtable<Integer,Long>();
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				list.put(rs.getInt("commodity_id"), rs.getLong("amount"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	public void setCommodity(int planet_id, int commodity_id, long amount) {
		String			where = "planet_id="+planet_id+" and commodity_id="+commodity_id;
		Hashtable<String,Object>		data = new Hashtable<String,Object>();
		
		data.put("planet_id", planet_id);
		data.put("commodity_id", commodity_id);
		data.put("amount", amount);
		try {
			db.replace("trade", data, where);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Hashtable<Integer,Integer> getResources(int planet_id) {
		String					sql = "select commodity_id, density from resources where planet_id="+planet_id;
		ResultSet				rs = null;
		Hashtable<Integer,Integer>	list = new Hashtable<Integer,Integer>();
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				list.put(rs.getInt("commodity_id"), rs.getInt("density"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;		
	}
	
	public Commodity getCommodity(String name) {
		String		sql = "select * from commodity where name=?";
		ResultSet	rs = null;
		Commodity	c = null;
		
		try {
			rs = db.query(sql, name);
			if (rs.next()) {
				c = new Commodity(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return c;
	}

	public Commodity getCommodity(int id) {
		String		sql = "select * from commodity where id=?";
		ResultSet	rs = null;
		Commodity	c = null;
		
		try {
			rs = db.query(sql, id);
			if (rs.next()) {
				c = new Commodity(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return c;
	}
	
	public void storeResources(int planet_id, Hashtable<String,Integer> resources) {
		// Delete any existing resources first.
		db.delete("resources", "planet_id="+planet_id);
		
		for (String s : resources.keySet()) {
			Commodity	c = getCommodity(s);
			
			if (c != null) {
				Hashtable<String,Object> data = new Hashtable<String,Object>();
				data.put("planet_id", planet_id);
				data.put("commodity_id", c.getId());
				data.put("density", resources.get(s));
				
				try {
					db.insert("resources", data);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}
		
	
	public static void main(String[] args) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		/*
		ByteArrayOutputStream stream = factory.getPlanetMap(68637);
		
		FileOutputStream		writer = new FileOutputStream(new File("/home/sam/data.jpg"));
		writer.write(stream.toByteArray());
		writer.close();
		*/
		
		int		id = 14580;
		factory.cleanStarSystem(id);
		StarSystem		system = factory.getStarSystem(id);
		system.regenerate();
		factory.close();
		
		/*
		factory.addNote(666, "Hello world");
		for (String n : factory.getNotes(666)) {
			System.out.println(n);
		}
		factory.addNote(666, "government", "Complete chaos");
		for (String n : factory.getNotes(666, "government")) {
			System.out.println(n);
		}		
		for (String n : factory.getNotes(666, "%")) {
			System.out.println(n);
		}
		*/		
	}

}
