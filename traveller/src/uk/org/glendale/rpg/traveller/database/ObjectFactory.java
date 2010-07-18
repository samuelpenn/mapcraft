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

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import uk.org.glendale.database.Database;
import uk.org.glendale.rpg.traveller.civilisation.Ship;
import uk.org.glendale.rpg.traveller.civilisation.trade.Commodity;
import uk.org.glendale.rpg.traveller.civilisation.trade.Facility;
import uk.org.glendale.rpg.traveller.civilisation.trade.Trade;
import uk.org.glendale.rpg.traveller.civilisation.trade.TradeGood;
import uk.org.glendale.rpg.traveller.database.Simulation.LogType;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.*;
import uk.org.glendale.rpg.traveller.worlds.WorldBuilder;

/**
 * Factory class which generates Traveller objects - stars, systems and planets.
 * These should only be one instance of this in an application.
 * 
 * @author Samuel Penn
 *
 */
public class ObjectFactory {
	protected Database				db = null;
	private static ObjectFactory	instance = null;
	private static final int		RECYCLE = 10;
	private static int				connectionCount = 0;
	
	public ObjectFactory() {
		//Log.info("ObjectFactory: Creating new database connection");
		db = Database.connect(Config.getDatabaseHost(), Config.getDatabaseName(),
							  Config.getDatabaseUser(), Config.getDatabasePassword());
	}
	
	public ObjectFactory(Connection cx) {
		db = new Database(cx);
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
	 * Get a list of star systems by a given selection code. Systems can be
	 * grouped by a selection id, which is meant for development use. By
	 * adding a non-zero selection id to systems, operations can be
	 * performed on them as a group.
	 * 
	 * @param selection				Selection id, 1+
	 * @param populateSystems		Get stars and planets as well.
	 */
	private Vector<StarSystem> getStarSystemsBySelection(int selection, boolean populateSystems) {
		Vector<StarSystem>		list = new Vector<StarSystem>();
		ResultSet				rs = null;
	
		try {
			rs = read("system", "selection="+selection);
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
				planet = new Planet(this, rs);
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
	 * Get a list of all planets that need to be updated. A simulation time
	 * is given, and any planet with a nextevent stamp less than or equal to
	 * this time is returned. Planets with a nextevent of 0 are always ignored.
	 * 
	 * @param untilSimTime		Latest event time to fetch.
	 * 
	 * @return	List of planets whose events occur before specified time.
	 */
	public ArrayList<Planet> getNextPlanets(long untilSimTime) {
		ArrayList<Planet>		list = new ArrayList<Planet>();
		ResultSet				rs = null;
		
		try {
			rs = db.query("select * from planet where nextevent > 0 and nextevent <= "+untilSimTime);
			while (rs.next()) {
				list.add(new Planet(rs));
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;
	}
	
	public ArrayList<Ship> getNextShips(long untilSimTime) {
		ArrayList<Ship>		list = new ArrayList<Ship>();
		
		String		sql = "select id from ship where nextevent <= "+untilSimTime;
		ResultSet	rs = null;
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				int		id = rs.getInt("id");
				
				Ship		ship = getShip(id);
				list.add(ship);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;
	}

	public void setPlanetEventTime(int planetId, long nextEvent) {
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		data.put("nextevent", nextEvent);
		
		try {
			db.update("planet", data, "id="+planetId);
		} catch (SQLException e) {
			Log.error(e);
		}
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
	
	public Vector<StarSystem> findStarSystems(String name) {
		Vector<StarSystem>	list = new Vector<StarSystem>();
		ResultSet			rs = null;
		
		if (name == null || name.length() == 0) {
			return list;
		}
		name = name.replaceAll("[^a-zA-Z0-9 ]", "");
		try {
			rs = db.query("select * from system where name like '%"+name+"%' order by name");
			while (rs.next()) {
				list.add(new StarSystem(this, rs));
			}
		} catch (SQLException e) {
			
		} finally {
			db.tidy(rs);
		}
		
		return list;
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
	
	private long getStatistic(String sql) {
		ResultSet	rs = null;
		long			value = 0;
		try {
			rs = db.query(sql);
			if (rs.next()) {
				value = rs.getLong(1);
			}
		} catch (SQLException e) {
			Log.error(e);
		} finally {
			rs = db.tidy(rs);
		}
		
		return value;
	}
	
	private static Hashtable<String,Long> 	statisticsTable = null;
	
	/**
	 * Returns statistics on the universe as it is currently known.
	 * 
	 * @return		Collection of interesting facts.
	 */
	public Hashtable<String,Long> getStatistics() {
		if (statisticsTable != null) return statisticsTable;
		
		statisticsTable = new Hashtable<String,Long>();
		statisticsTable.put("sectors", getStatistic("select count(*) from sector"));
		statisticsTable.put("systems", getStatistic("select count(*) from system"));
		statisticsTable.put("planets", getStatistic("select count(*) from planet"));
		statisticsTable.put("moons", getStatistic("select count(*) from planet where moon=1"));
		statisticsTable.put("life", getStatistic("select count(*) from planet where life='Extensive'"));
		statisticsTable.put("minx", getStatistic("select min(x) from sector"));
		statisticsTable.put("maxx", getStatistic("select max(x) from sector"));
		statisticsTable.put("miny", getStatistic("select min(y) from sector"));
		statisticsTable.put("maxy", getStatistic("select max(y) from sector"));
		statisticsTable.put("population", getStatistic("select sum(population) from planet"));
		statisticsTable.put("ships", getStatistic("select count(*) from ship"));
		
		return statisticsTable;
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
		StarSystem		system = getStarSystem(id);
		
		for (Planet planet : system.getPlanets()) {
			db.delete("note", "planet_id="+planet.getId());
			db.delete("globe", "planet_id="+planet.getId());
			db.delete("map", "planet_id="+planet.getId());
			db.delete("resources", "planet_id="+planet.getId());
			db.delete("trade", "planet_id="+planet.getId());
		}
			
		db.delete("planet", "system_id="+id);
		db.delete("star", "system_id="+id);
	}
	
	public void deleteStarSystem(int id) throws ObjectNotFoundException {
		cleanStarSystem(id);
		db.delete("system", "id="+id);
	}
	
	/**
	 * Counts the number of starships currently in the specified star system.
	 * Does not include ships currently in jump transit to or from the system.
	 */
	public int countShipsInSystem(int systemId) {
		int		count = 0;
		
		ResultSet rs = null;
		
		try {
			rs = db.query("select count(*) from ship where system_id="+systemId);
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return count;
	}
	
	public Hashtable<Integer,Commodity> getAllCommodities() {
		String					sql = "select * from commodity order by id";
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
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;
	}
	
	/**
	 * Get all the commodities available for a given planet, including
	 * the amount available and the base price. This is what the planet
	 * currently has in stock, available ready to be sold.
	 * The hashtable key is the commodity id, not the unique id of the
	 * tradegood instance.
	 * 
	 * @param planet_id		Id of the planet to get commodities list for.
	 */
	public Hashtable<Integer,TradeGood> getCommoditiesByPlanet(int planet_id) {
		String					sql = "select id, commodity_id, amount, consumed, produced, bought, sold, weeklyin, weeklyout, price from trade where planet_id="+planet_id+" order by id";
		ResultSet				rs = null;
		Hashtable<Integer,TradeGood>	list = new Hashtable<Integer,TradeGood>();
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				int		id = rs.getInt("id");
				int		commodityId = rs.getInt("commodity_id");
				long	amount = rs.getLong("amount");
				long	consumed = rs.getLong("consumed");
				long	produced = rs.getLong("produced");
				long	sold = rs.getLong("sold");
				long	bought = rs.getLong("bought");
				long	in = rs.getLong("weeklyin");
				long	out = rs.getLong("weeklyout");
				int		price = rs.getInt("price");
				
				TradeGood good = new TradeGood(id, commodityId, amount, consumed, price, planet_id);
				good.setProduced(produced);
				good.setSold(sold);
				good.setBought(bought);
				good.setWeeklyIn(in);
				good.setWeeklyOut(out);
				list.put(rs.getInt("commodity_id"), good);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;
	}
	
	public void setCommodity(int planet_id, int commodity_id, long amount, long consumed, int price) {
		String			where = "planet_id="+planet_id+" and commodity_id="+commodity_id;
		
		if (amount < 1 && consumed == 0) {
			db.delete("trade", where);
		} else {
			Hashtable<String,Object>		data = new Hashtable<String,Object>();
			
			data.put("planet_id", planet_id);
			data.put("commodity_id", commodity_id);
			data.put("amount", amount);
			data.put("consumed", (consumed>=0)?consumed:0);
			data.put("price", price);
			try {
				db.replace("trade", data, where);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setCommodity(int planetId, TradeGood good) {
		String			where = "planet_id="+planetId+" and commodity_id="+good.getCommodityId();
		
		if (good.isUnused()) {
			// Unused and can be deleted.
			if (good.getId() > 0) db.delete("trade", "id="+good.getId());
		} else {
			Hashtable<String,Object>		data = new Hashtable<String,Object>();
			
			data.put("planet_id", planetId);
			data.put("commodity_id", good.getCommodityId());
			data.put("amount", good.getAmount());
			data.put("consumed", good.getConsumed());
			data.put("produced", good.getProduced());
			data.put("bought", good.getBought());
			data.put("sold", good.getSold());
			data.put("weeklyin", good.getWeeklyIn());
			data.put("weeklyout", good.getWeeklyOut());
			data.put("price", good.getPrice());
			try {
				db.replace("trade", data, where);
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		
	}

	/**
	 * Add a commodity to a planet's reserves. If the amount added is negative, then
	 * the commodity is taken away.
	 * 
	 * @param planet_id
	 * @param commodity_id
	 * @param amount
	 * @param price
	 * 
	 * @return		Amount of goods actually sold.
	 */
	public long addCommodity(int planetId, int commodityId, long amount, long consumed, int price) {
		String					sql = "select amount,consumed,price from trade where planet_id="+planetId+" and commodity_id="+commodityId;
		ResultSet				rs = null;
		TradeGood				good = null;
		
		try {
			rs = db.query(sql);
			if (rs.next()) {
				long	a = rs.getLong("amount");
				long	c = rs.getLong("consumed");
				int		p = rs.getInt("price");
				
				good = new TradeGood(commodityId, a, c, p);
			} else {
				good = new TradeGood(commodityId, 0, 0, price);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		good.setAmount(good.getAmount()+amount);
		if (good.getAmount() < 0) {
			amount = amount + good.getAmount();
			good.setAmount(0);
		}
		
		setCommodity(planetId, commodityId, good.getAmount(), good.getConsumed(), good.getPrice());

		return amount;
	}
	
	/**
	 * Get resources being produced on the specified planet.
	 * List is returned as a hashtable, with the commodity id as the
	 * key, and the value stored the density of the resources (1-100).
	 * 
	 * @param planet_id		Planet that is to be searched.
	 * @return				List of resources and their densities.
	 */
	public Hashtable<Integer,Integer> getResources(int planet_id) {
		String					sql = "select commodity_id, density from resources where planet_id="+planet_id;
		ResultSet				rs = null;
		Hashtable<Integer,Integer>	list = new Hashtable<Integer,Integer>();
		
		try {
			rs = db.query(sql);
			while (rs.next()) {
				int		id = rs.getInt("commodity_id");
				int		density = rs.getInt("density");
				
				list.put(rs.getInt("commodity_id"), rs.getInt("density"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
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
		} finally {
			rs = db.tidy(rs);
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
		} finally {
			db.tidy(rs);
		}
		
		return c;
	}
	
	public void storeResources(int planet_id, Hashtable<String,Integer> resources) {
		// Delete any existing resources first.
		db.delete("resources", "planet_id="+planet_id);
		
		for (String s : resources.keySet()) {
			try {
				Commodity	c = Constants.getCommodity(s);
				
				Hashtable<String,Object> data = new Hashtable<String,Object>();
				data.put("planet_id", planet_id);
				data.put("commodity_id", c.getId());
				data.put("density", resources.get(s));
				
				try {
					db.insert("resources", data);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (ObjectNotFoundException e) {
				System.out.println("Error persisting planet ["+planet_id+"]: "+e.getMessage());
			}
		}

	}
	
	public Ship getShip(int id) {
		String		sql = "select * from ship where id=?";
		ResultSet	rs = null;
		Ship		ship = null;
		
		try {
			rs = db.query(sql, id);
			if (rs.next()) {
				ship = new Ship(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		if (ship != null) {
			ship.setCargo(getShipCargo(id));
		}
		
		return ship;
	}
	
	public Hashtable<Integer,TradeGood> getShipCargo(int shipId) {
		String		sql = "select id, commodity_id,amount,price,planet_id from cargo where ship_id=?";
		ResultSet	rs = null;
		Hashtable<Integer,TradeGood> list = new Hashtable<Integer,TradeGood>();
		
		try {
			rs = db.query(sql, shipId);
			if (rs.next()) {
				int		id = rs.getInt("id");
				int		commodityId = rs.getInt("commodity_id");
				int		amount = rs.getInt("amount");
				int		price = rs.getInt("price");
				int		planetId = rs.getInt("planet_id");
				
				TradeGood	good = new TradeGood(id, commodityId, amount, price, planetId);
				list.put(id, good);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;		
	}
	
	public void setShipCargo(int shipId, Hashtable<Integer, TradeGood> cargo) {
		db.delete("cargo", "ship_id="+shipId);
		
		for (int id : cargo.keySet()) {
			TradeGood	good = cargo.get(id);
			
			if (good != null) {
				Hashtable<String,Object> data = new Hashtable<String,Object>();
				data.put("id", 0);
				data.put("ship_id", shipId);
				data.put("commodity_id", good.getCommodityId());
				data.put("amount", good.getAmount());
				data.put("price", good.getPrice());
				data.put("planet_id", good.getPlanetId());
				
				try {
					db.insert("cargo", data);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}	
	}

	/**
	 * Get the value of the specified property.
	 */
	public long getNumberData(String property) {
		String		sql = "select value from numbers where property='"+property+"'";
		ResultSet	rs = null;
		long		value = 0;
		
		try {
			rs = db.query(sql);
			if (rs.next()) {
				value = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			value = 0;
		}
		return value;
	}
	
	public Hashtable<Integer,Facility> getFacilities() {
		Hashtable<Integer,Facility>		table = new Hashtable<Integer, Facility>();
		
		String		sql = "select * from facility";
		ResultSet	rs = null;

		try {
			rs = db.query("select * from facility");
			while (rs.next()) {
				Facility facility = new Facility(rs);
				
				table.put(facility.getId(), facility);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return table;
	}
	
	public Hashtable<Integer,Long> getFacilitiesByPlanet(int planetId) {
		Hashtable<Integer,Long>		list = new Hashtable<Integer,Long>();
		
		String		sql = "select facility_id, size from facilities where planet_id = "+planetId;
		ResultSet	rs = null;

		try {
			rs = db.query(sql);
			while (rs.next()) {
				int		facilityId = rs.getInt("facility_id");
				long	size = rs.getInt("size");
				list.put(facilityId, size);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
		
		return list;
	}
	
	public void setFacilitiesForPlanet(int planetId, Hashtable<Integer,Long> list) {
		String		sql = "delete from facilities where planet_id="+planetId;
		ResultSet	rs = null;

		try {
			db.delete("facilities", "planet_id="+planetId);
			
			for (Iterator<Integer> i = list.keySet().iterator(); i.hasNext();) {
				int		id = i.next();
				long	size = list.get(id);
				
				Hashtable<String,Object>	data = new Hashtable<String,Object>();
				data.put("planet_id", planetId);
				data.put("facility_id", id);
				data.put("size", (int)size);
				db.insert2("facilities", data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs = db.tidy(rs);
		}
	}

	/**
	 * Set the value of the specified property.
	 */
	public void setNumberData(String property, long value) {
		String		sql = "update numbers set value=?";
		
		Hashtable<String,Object>		data = new Hashtable<String,Object>();		
		data.put("value", value);
		try {
			db.replace("numbers", data, "property='"+property+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	public void log(int ship_id, int system_id, int planet_id, long simTime, LogType type, String text) {
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		data.put("ship_id", ship_id);
		data.put("system_id", system_id);
		data.put("planet_id", planet_id);
		data.put("stamp", simTime);
		data.put("type", type.toString());
		data.put("text", text);
		
		persist("log", data);
	}

	private static void regenSector(int sectorId) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		Vector<StarSystem> list = factory.getStarSystemsBySector(sectorId);
		
		for (int s=0; s < list.size(); s++) {
			StarSystem	system = list.get(s);
			int			id = system.getId();
			factory.cleanStarSystem(id);
			system = factory.getStarSystem(id);
			system.regenerate();
		}
		factory.close();		
	}
	
	private static void regenSystem(int systemId) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		factory.cleanStarSystem(systemId);
		
		StarSystem system = factory.getStarSystem(systemId);
		system.regenerate();

		factory.close();				
	}
	
	private static void regenSelection(int selection) {
		ObjectFactory		factory = new ObjectFactory();
		Vector<StarSystem>	list = factory.getStarSystemsBySelection(selection, false);
		
		try {
			System.out.println(GraphicsEnvironment.isHeadless());

			for (StarSystem sys : list) {
				int		id = sys.getId();
				System.out.println("Regenerating "+id);
				factory.cleanStarSystem(id);
				
				StarSystem system = factory.getStarSystem(id);
				system.regenerate();
				
				system = factory.getStarSystem(id);
				//Planet		mainWorld = system.getMainWorld();
				//WorldBuilder.imagePlanet(factory, mainWorld);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			factory.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		//regenSystem(10311);
		//regenSelection(2);
		
		ObjectFactory	f = new ObjectFactory();
		//StarSystem s = f.getStarSystem(10309);
		//System.out.println(s.getMainWorld().getName());
		
		Vector<StarSystem> list = f.findStarSystems("new");
		for (StarSystem s : list) {
			System.out.println(s.getName());
		}
	}

}
