/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.14 $
 * $Date: 2007/12/09 17:45:17 $
 */

package uk.org.glendale.rpg.traveller.sectors;

import java.io.*;
import java.sql.*;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.PostScript;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.UWP;
import uk.org.glendale.rpg.utils.Die;

/**
 * Defines a sector of space. A sector in the Traveller universe consists of
 * a region 32 parsecs wide by 40 high. Location 0101 is the top left of the
 * sector, and 3240 is the bottom right.
 * 
 * Each sector is divided into 16 sub-sectors, labelled a-p, each 8x10 parsecs
 * in size. A parsec is a represented on a Sector map is a single hex, which
 * may contain zero or one stars systems.
 * 
 * @author Samuel Penn
 */
public class Sector {
	ObjectFactory		factory = null;
	private String		name = null;
	private int			x = 0;
	private int			y = 0;
	private int			id = 0;
	
	public static final int HEIGHT = 40;
	public static final int WIDTH = 32;
	
	public enum SubSector {
		A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P;
		
		/**
		 * Get the index of this SubSector, starting at 0 for A, and
		 * moving across and down to 15 for P.
		 * 
		 * @return		Index, from 0 to 15.
		 */
		public int getId() {
			return ordinal();
		}
		
		/**
		 * Get the X position of this subsector within the sector, counting
		 * from the left (spinward) edge of the sector.
		 * 
		 * @return		Position from 0 to 3.
		 */
		public int getX() {
			return (ordinal() % 4);
		}
		
		/**
		 * Get the Y position of this subsector within the sector, counting
		 * from the top (coreward) edge of the sector.
		 * 
		 * @return		Position from 0 to 3.
		 */
		public int getY() {
			return (ordinal()/4);
		}

		/**
		 * Get the X coordinate of the top left system in this sub sector.
		 * @return		X coordinate, either 1, 9, 17 or 25.
		 */
		public int getXOffset() {
			return (ordinal() % 4) * 8 + 1;
		}
		
		/**
		 * Get the Y coordinate of the top left system in this sub sector.
		 * 
		 * @return		Y coordinate, either 1, 11, 21 or 31.
		 */
		public int getYOffset() {
			return (ordinal()/4) * 10 + 1;
		}
		
		public static SubSector getByCoordinate(int x, int y) {
			try {
				return values()[(x-1)/8 + ((y-1)/10)*4];
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Cannot get coordinate for ["+x+","+y+"]");
				throw e;
			}
		}
	}
	
	public static String getCoordinate(int x, int y) {
		String		value = "";
		
		if (x < 10) {
			value += "0";
		}
		value += x;
		
		if (y < 10) {
			value += "0";
		}
		value += y;
		
		return value;
	}

	/**
	 * Instantiate a new sector object. If a Sector is found in the database
	 * which the provided x and y coordinates, the name of the Sector is read.
	 * Otherwise, a new sector is created with the given name.
	 * 
	 * The sector coordinates are independant of the coordinates within a
	 * sector.
	 * 
	 * @param name		Name of a new sector. Ignored if sector already exists.
	 * @param x		X coordinate of sector.
	 * @param y		Y coordinate of sector.
	 */
	public Sector(String name, int x, int y) {
		this.name = name;
		this.x = x;
		this.y = y;
		
		factory = new ObjectFactory();
		
		if (!read("x="+x+" and y="+y) && name != null) {
			persist();
		}
	}
	
	/**
	 * Get the sector with the given id. If the sector does not exist, then an
	 * exception will be thrown.
	 * 
	 * @param id		Id of the sector to be fetched.
	 * @throws ObjectNotFoundException
	 */
	public Sector(int id) throws ObjectNotFoundException {
		factory = new ObjectFactory();
		
		if (!read("id="+id)) {
			factory.close();
			throw new ObjectNotFoundException("Could not find a sector with id "+id);
		}
	}

	/**
	 * Get the sector with the given id. If the sector does not exist, then an
	 * exception will be thrown.
	 * 
	 * @param id		Id of the sector to be fetched.
	 * @throws ObjectNotFoundException
	 */
	public Sector(ObjectFactory factory, int id) throws ObjectNotFoundException {
		this.factory = factory;
		
		if (!read("id="+id)) {
			factory.close();
			throw new ObjectNotFoundException("Could not find a sector with id "+id);
		}
	}
	
	/**
	 * Get the sector of the given name. If the sector does not exist, then an
	 * exception will be thrown.
	 * 
	 * @param name		Name of the sector to be fetched.
	 * @throws ObjectNotFoundException
	 */
	public Sector(String name) throws ObjectNotFoundException {
		factory = new ObjectFactory();
		if (!read("name='"+name.replaceAll("'", "''")+"'")) {
			factory.close();
			throw new ObjectNotFoundException("Could not find a sector named ["+name+"]");
		}
	}

	/**
	 * Get the sector of the given coordinates. If the sector does not exist,
	 * then an exception will be thrown.
	 * 
	 * @param x		X coordinate of the sector.
	 * @param y		Y coordinate of the sector.
	 * @throws ObjectNotFoundException
	 */
	public Sector(int x, int y) throws ObjectNotFoundException {
		factory = new ObjectFactory();
		if (!read("x="+x+" and y="+y)) {
			factory.close();
			throw new ObjectNotFoundException("Could not find a sector with coordinates ["+x+","+y+"]");
		}
	}

	/**
	 * Get the sector of the given coordinates. If the sector does not exist,
	 * then an exception will be thrown.
	 * 
	 * @param x		X coordinate of the sector.
	 * @param y		Y coordinate of the sector.
	 * @throws ObjectNotFoundException
	 */
	public Sector(ObjectFactory factory, int x, int y) throws ObjectNotFoundException {
		this.factory = factory;
		if (!read("x="+x+" and y="+y)) {
			throw new ObjectNotFoundException("Could not find a sector with coordinates ["+x+","+y+"]");
		}
	}
		
	/**
	 * Read sector information from the database. If a matching sector is found,
	 * then returns true and populates this object's fields.
	 * 
	 * @param query		Query to use to find a sector.
	 * 
	 * @return				True if sector exists, false otherwise.
	 */
	private boolean read(String query) {
		ResultSet			rs = null;
		
		try {
			rs = factory.read("sector", query);
			if (rs.next()) {
				id = rs.getInt("id");
				name = rs.getString("name");
				x = rs.getInt("x");
				y = rs.getInt("y");
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) rs.close();
			} catch (SQLException e) {
				// Really don't care.
			}
		}
		
		return true;
	}
	
	/**
	 * Store the sector in the database.
	 */
	public void persist() {
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		data.put("id", id);
		data.put("name", name);
		data.put("x", x);
		data.put("y", y);
		
		int auto = factory.persist("sector", data);
		if (id == 0) id = auto;
	}
	
	public int getId() {
		return id;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Get a list of all the star systems in this sector. The list does not
	 * contain the contents of the star systems - no star or planet information
	 * is returned, simply the position, name and id of each system.
	 * 
	 * @return		List of all the star systems.
	 */
	public Vector<StarSystem>	listSystems() {
		Vector<StarSystem>	list = factory.getStarSystemsBySector(id, false);
		
		return list;
	}
	
	public int getSystemCount() {
		return factory.getSystemCount(id);
	}
	
	/**
	 * Get details on all of the star systems in this sector. This list
	 * contains the contents of the star systems, so is slower and returns
	 * more data than listSystems();
	 * 
	 * @return		List of all the star systems.
	 */
	public Vector<StarSystem> getSystems() {
		Vector<StarSystem>	list = factory.getStarSystemsBySector(id, true);
		
		return list;
	}
	
	public StarSystem getSystem(int x, int y) {
		StarSystem			ss = factory.getStarSystem(id, x, y);
		
		return ss;
	}
	
	public void plotSystems(PostScript ps) {
		Vector<StarSystem>	list = factory.getStarSystemsBySector(id);
		
		output(list, ps, 1, 1, 32, 40);		
	}

	/**
	 * Display all the star systems within the provided coordinates.
	 * 
	 * @param ps			Map to write the system data to.
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public void plotSystems(PostScript ps, int left, int top, int right, int bottom) {
		Vector<StarSystem>	list = factory.getStarSystemsBySector(id);
		
		output(list, ps, left, top, right, bottom);
	}
	
	private void output(Vector<StarSystem> list, PostScript ps, int left, int top, int right, int bottom) {
		Iterator<StarSystem>	i = list.iterator();
		
		while (i.hasNext()) {
			StarSystem		ss = i.next();
			
			if (ss.getX() >= left && ss.getX() <= right && ss.getY() >= top && ss.getY() <= bottom) {
				ss.plotSymbols(ps, left-1, top-1);
			}
		}	
	}
	
	/**
	 * Get the default name of a subsector. Each sector is divided into 16
	 * subsectors, labelled A to P, running left to right, top to bottom,
	 * e.g. the top row is A-D, the 2nd row is E-H.
	 * 
	 * @param x		X coordinate of point in sector, 1-40
	 * @param y		Y coordinate of point in sector, 1-32.
	 * 
	 * @return			Name of the subsector the given point is in.
	 */
	public static SubSector getSubSector(int x, int y) {
		return SubSector.getByCoordinate(x, y);
	}
	
	public String getSubSectorName(int x, int y) {
		SubSector		sub = getSubSector(x, y);
		String			name = sub.toString();
		ResultSet		rs = null;
		
		try {
			rs = factory.read("subsector", "sector_id="+getId()+" and idx="+sub.getId());
			if (rs.next()) {
				name = rs.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return name;
	}
	
	public String toXML() {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<sector id=\"").append(id).append("\" name=\"");
		buffer.append(name).append("\" ");
		buffer.append("x=\"").append(x).append("\" y=\"").append(y).append("\"/>");
		
		return buffer.toString();
	}
	
	/**
	 * Create a new random sector. The sector density is the chance of any
	 * given hex having a star system. Each star system is generated randomly.
	 * 
	 * @param density		Percentage of hexes which have a star system.
	 */
	public void create(int density) {
		String		alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String		baseName = alpha.substring(x+12, x+13)+alpha.substring(y+12, y+13);

		for (int x=1; x <= WIDTH; x++) {
			for (int y=1; y <= HEIGHT; y++) {
				if (Die.d100() <= density) {
					// Create a new star system.
					int				ssx = (x%8 == 0)?8:x%8;
					int				ssy = (y%10 == 0)?10:y%10;
					int				number = ssx*100 + ssy;
					String			name = baseName + Sector.getSubSector(x, y)+number;
					StarSystem		ss = new StarSystem(factory, name, getId(), x, y);
					ss.persist();
				}
			}
		}
	}
	
	/**
	 * Populate this sector from data read in the given file.
	 * 
	 * @param file
	 */
	public void create(File file) {
		String				alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		//String				baseName = alpha.substring(x+12, x+13)+alpha.substring(y+12, y+13);

		FileReader          reader = null;
        LineNumberReader    input = null;
        try {
            String    line = null;
            
            reader = new FileReader(file);
            input = new LineNumberReader(reader);
            
            // Look for the start of the actual data.
            boolean		start = false;
            while (!start) {
            	line = input.readLine();
            	if (line == null) {
            		break;
            	} else if (line.startsWith(" 64-80: ")) {
            		line = input.readLine();
            		start = true;
            	}
            }
            
            int		count = 0;
            for (line = input.readLine(); line != null; line = input.readLine()) {
            	//System.out.println(input.getLineNumber()+": "+line);
            	try {
	            	UWP				uwp = new UWP("XX "+line, name, x, y);
	            	System.out.print(".");
	            	if ((++count)%100 == 0) {
	            		System.out.println("");
	            	}
					//int				ssx = (uwp.getX()%8 == 0)?8:uwp.getX()%8;
					//int				ssy = (uwp.getY()%10 == 0)?10:uwp.getY()%10;
					//int				number = ssx*100 + ssy;
					//String			name = baseName + Sector.getSubSector(uwp.getX(), uwp.getY())+number;
					//uwp.depopulate(name);
	            	
					// Seed the random number generator.
					new Random(getX()*100000+getY()*1000 + uwp.getX()*40 +  uwp.getY());
	            	new StarSystem(factory, id, uwp);
            	} catch (Throwable e) {
            		System.out.println("\nERROR: "+line);
            		e.printStackTrace();
            	}
            }
            System.out.println("");
                
        } catch (IOException e) {
        	e.printStackTrace();
        }		
	}
	
	/**
	 * Import a data file containing a list of all the sectors.
	 */
	public static void readSectorNames() {
		File		file = new File("data/sectors.txt");
		String		position = "012345ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
        FileReader          reader = null;
        LineNumberReader    input = null;
        try {
            String    line = null;
            
            reader = new FileReader(file);
            input = new LineNumberReader(reader);
            
            for (line = input.readLine(); line != null; line = input.readLine()) {
            	System.out.println(input.getLineNumber()+": "+line);
            	
                String 	name = line.substring(3);
                int		x = 0, y = 0;
                
                try {
                	//y = Integer.parseInt(line.substring(0, 1).toUpperCase(), 36) - 15;
                	//x = Integer.parseInt(line.substring(1, 2).toUpperCase(), 36) - 16;
                	y = position.indexOf(line.substring(0, 1).toUpperCase()) - 11;
                	x = position.indexOf(line.substring(1, 2).toUpperCase()) - 12;
                } catch (NumberFormatException e) {
                	e.printStackTrace();
                	x = y = -1;
                }
                //System.out.println("["+name+"] "+x+","+y);
            	
            	Sector	sector = new Sector(name, x, y);
            }
        } catch (IOException e) {
        }
	}
	
	public static void readSubSectorNames() {
		ObjectFactory		factory = new ObjectFactory();
		Vector<Sector>		sectors = factory.getSectors();
		String				position = "012345ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		for (Sector sector : sectors) {
			int			x = sector.getX();
			int			y = sector.getY();
			String		filename = position.substring(y+11, y+12).toLowerCase()+position.substring(x+12, x+13).toUpperCase();
			
			//System.out.println("Reading sub sectors for "+sector.getId()+": "+sector.getName());
						
			try {
				File		file = new File("data/core/gni/"+filename+".GNI");
				if (file.canRead()) {
			        LineNumberReader    input = null;
			        String				line = null;
			        
			        input = new LineNumberReader(new FileReader(file));
		            for (line = input.readLine(); line != null; line = input.readLine()) {
		            	if (line.matches("^[A-Z]: .*")) {
		            		//System.out.println(sector.getName()+": "+line);
		            		SubSector		sub = SubSector.valueOf(line.substring(0, 1));
		            		String			name = line.substring(3);
		            		System.out.println(sub+":" + name);
		            		
		            		Hashtable<String,Object>		table = new Hashtable<String, Object>();
		            		table.put("id", 0);
		            		table.put("sector_id", sector.getId());
		            		table.put("idx", sub.getId());
		            		table.put("name", name);
		            		
		            		factory.persist("subsector", table);
		            	}
			        }
				}
			} catch (Throwable e) {
				System.out.println("Error in data for ["+filename+"]");
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void readAllegiances() {
		File		file = new File("data/allegiances.txt");
		
        FileReader          reader = null;
        LineNumberReader    input = null;
        try {
            String    line = null;
            
            reader = new FileReader(file);
            input = new LineNumberReader(reader);
            
            for (line = input.readLine(); line != null; line = input.readLine()) {
            	System.out.println(input.getLineNumber()+": "+line);
            	
            	String	code = line.substring(0, 2);
            	String  name = line.substring(3);

            	Allegiance 		allegiance = new Allegiance(code, name);
            }
        } catch (IOException e) {
        }		
	}
	
	/**
	 * Create a completely random star sector.
	 * 
	 * @param name			Name of the sector.
	 * @param x			X coordinate of the sector.
	 * @param y			Y coordinate of the sector.
	 * @param density		Star density (%).
	 */
	private static void create(String name, int x, int y, int density) {
		Sector		sector = new Sector(name, x, y);		
		sector.create(density);
	}

	/**
	 * Create a star sector based on a Traveller data file. Only the name of
	 * the file is needed, e.g. "gH". The path and ".GNI" extension is added.
	 * 
	 * @param name			Name of the sector.
	 * @param x			X coordinate of the sector.
	 * @param y			Y coordinate of the sector.
	 * @param datafile		Name of the Traveller file.
	 */
	private static void create(String name, int x, int y, String datafile) {
		try {
			new Sector(name);
			return;
		} catch (ObjectNotFoundException e) {
			
		}
		Sector		sector = new Sector(name, x, y);
		//String		filename = "data/core/gni/"+datafile+".GNI";
		//sector.create(new File(filename));
		sector.create(5);
	}
	
	public static void createMortals() {
		// Core sectors.
		create("Aquila", -1, 1, "hF");
		create("Serpens", 0, 1, "hG");
		create("Virgo", 1, 1, "hH");
		create("Aquarius", -1, 0, "iF");
		//create("Sol", 0, 0, "iG");
		create("Hydra", 1, 0, "iH");
		create("Pisces", -1, -1, "jF");
		create("Taurus", 0, -1, "jG");
		create("Orion", 1, -1, "jH");
		
		// Other sectors.
		create("Rift", -2, 2, "gE");
		create("Passage", -1, 2, "gF");
		create("Borders", -2, 1, "hE");
		create("Dominion", -2, 0, "iE");
	}
	
	public static void createTraveller() {
		//readSectorNames();
		
		ObjectFactory		factory = new ObjectFactory();
		Vector<Sector>		sectors = factory.getSectors();
		String				position = "012345ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		for (Sector sector : sectors) {
			int			x = sector.getX();
			int			y = sector.getY();
			String		filename = position.substring(y+11, y+12).toLowerCase()+position.substring(x+12, x+13).toUpperCase();
			//String		filename = Integer.toString(y+15, 36).toLowerCase()+Integer.toString(x+16, 36).toUpperCase();
			
			System.out.println("Creating "+sector.getId()+": "+sector.getName());
			
			
			try {
				File		file = new File("data/core/gni/"+filename+".GNI");
				if (file.canRead()) {
					sector.create(file);
				}
			} catch (Throwable e) {
				System.out.println("Error in data for ["+filename+"]");
				e.printStackTrace();
				return;
			}
		}
	}
		
	public static void createTravellerSub() {
		readSubSectorNames();		
	}
	
	public static void main(String[] args) throws Exception {
		//createTravellerSub();
		createMortals();
		
		Sector		sector = new Sector(0, 0);
		for (int y=0; y < 4; y++) {
			for (int x=0; x < 4; x++) {
				System.out.println(sector.getSubSectorName(x*8+1, y*10+1));
			}
		}
	}
}
