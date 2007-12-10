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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import uk.org.glendale.database.Database;
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
	
	public StarSystem getStarSystem(int sectorId, int x, int y) {
		ResultSet				rs = null;
		StarSystem				ss = null;
		
		try {
			rs = read("system", "sector_id="+sectorId+" and x="+x+" and y="+y);
			if (rs.next()) {
				ss = new StarSystem(rs);
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
				ss = new StarSystem(rs);
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
			rs = read("star", "system_id="+systemId);
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
			rs = read("planet", "system_id="+systemId);
			while (rs.next()) {
				Planet		planet = new Planet(rs);
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
				Planet		planet = new Planet(rs);
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
		Hashtable	data = new Hashtable();
		byte[]		bytes = stream.toByteArray();
		
		data.put("planet_id", id);
		data.put("image", bytes);
		
		try {
			db.insert2(table, data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void storePlanetGlobe(int id, ByteArrayOutputStream stream) {
		String		table = "globe";
		Hashtable	data = new Hashtable();
		byte[]		bytes = stream.toByteArray();
		
		data.put("planet_id", id);
		data.put("image", bytes);
		
		try {
			db.insert2(table, data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
	
	public static void main(String[] args) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		/*
		ByteArrayOutputStream stream = factory.getPlanetMap(68637);
		
		FileOutputStream		writer = new FileOutputStream(new File("/home/sam/data.jpg"));
		writer.write(stream.toByteArray());
		writer.close();
		*/
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
	}

}
