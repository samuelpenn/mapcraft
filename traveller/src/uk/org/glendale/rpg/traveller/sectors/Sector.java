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

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.PostScript;
import uk.org.glendale.rpg.traveller.systems.Description;
import uk.org.glendale.rpg.traveller.systems.Name;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.UWP;
import uk.org.glendale.rpg.traveller.worlds.WorldBuilder;
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
	ObjectFactory					factory = null;
	private String					name = null;
	private int						x = 0;
	private int						y = 0;
	private int						id = 0;
	private HashSet<SectorCode>		codes = null;
	private String					allegiance = "Na";
	
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
	
	/**
	 * Return the coordinate within the sector as a single formatted
	 * string. The format is XXYY, e.g. 0523. This is a coordinate
	 * within the sector for a given X/Y position.
	 * 
	 * @param x		X coordinate within the sector.
	 * @param y		Y coordinate within the sector.
	 * @return		Coordinate in the format XXYY.
	 */
	public static String getCoordinate(int x, int y) {
		return String.format("%02d%02d", x, y);
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
		System.out.println("Getting sector "+id);
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

	public Sector(ObjectFactory factory, String name) throws ObjectNotFoundException {
		this.factory = factory;
		if (!read("name='"+name.replaceAll("'", "''")+"'")) {
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
				allegiance = rs.getString("allegiance");
				
				String	codeList = rs.getString("codes");
				codes = new HashSet<SectorCode>();
				for (String c : codeList.split(" ")) {
					try {
						codes.add(SectorCode.valueOf(c));
					} catch (Throwable t) {
						// Unrecognised sector code.
					}
				}
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
		
		String		c = "";
		for (SectorCode code : codes.toArray(new SectorCode[0])) {
			c += code.name()+" ";
		}
		data.put("codes", c.trim());
		
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
	 * Does this sector have the specified sector code?
	 */
	public boolean hasCode(SectorCode code) {
		return codes.contains(code);
	}
	
	public void addCode(SectorCode code) {
		codes.add(code);
	}
	
	public String getAllegiance() {
		return allegiance;
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
	
	/**
	 * @param x		X coordinate of point in sector, 1-40
	 * @param y		Y coordinate of point in sector, 1-32.
	 */
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
	
	/**
	 * Get the distance in parsecs between two planets. Works out which
	 * system they are both in, then figures the distance between the
	 * two systems. If they are in the same system, the distance is 0.
	 * 
	 * @param p1		First planet.
	 * @param p2		Second planet.
	 * @return			Distance in parsecs.
	 */
	public int getDistance(Planet p1, Planet p2) {
		try {
			StarSystem		s1 = new StarSystem(factory, p1.getSystemId());
			StarSystem		s2 = new StarSystem(factory, p2.getSystemId());
			
			return getDistance(s1, s2);
		} catch (ObjectNotFoundException e) {
			// The database foreign key constraints should prevent this.
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Get the distance in parsecs between two systems.
	 * 
	 * @param s1	First system.
	 * @param s2	Second system.
	 * @return		Distance in parsecs.
	 */
	public int getDistance(StarSystem s1, StarSystem s2) {
		int		parsecs = 0;
		
		if (s1.getId() == s2.getId()) {
			// Trivial case.
			return 0;
		}
		
		int		x1 = s1.getX();
		int		y1 = s1.getY();
		int		x2 = s2.getX();
		int		y2 = s2.getY();
		
		if (s1.getSectorId() == s2.getSectorId()) {
			// Easy case, both in same sector.
			parsecs = (int)Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
		} else {
			// General case, need to figure out sector differences.
			try {
				Sector	sector = new Sector(s1.getSectorId());
				x1 += sector.getX() * 32;
				y1 += sector.getY() * 40;
				sector = new Sector(s2.getSectorId());
				x2 += sector.getX() * 32;
				y2 += sector.getY() * 40;
				
				parsecs = (int)Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
			} catch (ObjectNotFoundException e) {
				
			}
		}
		
		return parsecs;
	}
	
	/**
	 * Get the bilateral trade number for two planets. The planets may or
	 * may not be in the same sector (or even this sector). This calculation
	 * is based on that in GURPS Free Trader p.14. See there for full
	 * details of what the numbers mean.
	 * 
	 * @param p1		First planet.
	 * @param p2		Second planet.
	 * @return			Relative amount of trade between the two planets.
	 */
	public double getBTN(Planet p1, Planet p2) {
		if (p1 == null || p2 == null) {
			// If either world is null, there is no trade.
			return 0;
		} else if (p1.getPopulation() == 0 || p2.getPopulation() == 0) {
			// If either world is unpopulated, then there is no trade.
			return 0;
		}

		double	wtcm = 0.0;	// World Trade Classification Modifier, not yet used.
		double 	btn = 0.0;
		int		distance = getDistance(p1, p2);
		double	modifier = 0;
		
		if (distance >= 1000) modifier = 6.0;
		else if (distance >= 600) modifier = 5.5;
		else if (distance >= 300) modifier = 5.0;
		else if (distance >= 200) modifier = 4.5;
		else if (distance >= 100) modifier = 4.0;
		else if (distance >= 60) modifier = 3.5;
		else if (distance >= 30) modifier = 3.0;
		else if (distance >= 20) modifier = 2.5;
		else if (distance >= 10) modifier = 2.0;
		else if (distance >= 6) modifier = 1.5;
		else if (distance >= 3) modifier = 1.0;
		else if (distance == 2) modifier = 0.5;
		else modifier = 0.0;
		
		modifier *= modifier;
		
		btn = p1.getWTN() + p2.getWTN() + wtcm - modifier;

		// Limit the BTN according to the smallest of the two trade partners.
		double	smallest = Math.min(p1.getWTN(), p2.getWTN());
		if (btn > smallest + 5) btn = smallest + 5;

		return btn;
	}
	
	public String toXML2() {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<sector id=\"").append(id).append("\" name=\"");
		buffer.append(name).append("\" ");
		buffer.append("x=\"").append(x).append("\" y=\"").append(y).append("\"/>");
		
		return buffer.toString();
	}

	private String escape(String text) {
		text = text.replaceAll("&", "&amp;");
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		text = text.replaceAll("'", "&apos;");
		text = text.replaceAll("\"", "&quot;");
		return text;
	}
	
	public String toXML() {
		return toXML(true);
	}
	
	public String toSEC() {
		StringBuffer		buffer = new StringBuffer();

		Iterator<StarSystem>	i = listSystems().iterator();
		while (i.hasNext()) {
			StarSystem		ss = i.next();
			
			buffer.append(ss.getUWP().toString()+"\n");
		}

		return buffer.toString();
	}
	
	public String toXML(boolean header) {
		StringBuffer		buffer = new StringBuffer();
		
		if (header) {
			buffer.append("<?xml version=\"1.0\"?>\n");
		}
		
		buffer.append("<sector xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\" name=\"");
		buffer.append(escape(getName()));
		buffer.append("\" x=\"");
		buffer.append(getX());
		buffer.append("\" y=\"");
		buffer.append(getY());
		buffer.append("\" id=\"");
		buffer.append(getId());
		buffer.append("\">\n");
		
		for (int y=0; y < 4; y++) {
			for (int x=0; x < 4; x++) {
				buffer.append("<subsector x=\""+x+"\" y=\""+y+"\" name=\"");
				buffer.append(escape(getSubSectorName(x*8+1, y*10+1)));
				buffer.append("\"/>\n");
			}
		}
		
		Iterator<StarSystem>	i = listSystems().iterator();
		while (i.hasNext()) {
			StarSystem		ss = i.next();
			
			buffer.append("<system id=\"");
			buffer.append(ss.getId());
			buffer.append("\" name=\"");
			buffer.append(escape(ss.getName()));
			buffer.append("\" x=\"");
			buffer.append(ss.getX());
			buffer.append("\" y=\"");
			buffer.append(ss.getY());				
			buffer.append("\"/>\n");
		}
		buffer.append("</sector>\n");
		
		return buffer.toString();
	}
	
	/**
	 * Create a new random sector. The sector density is the chance of any
	 * given hex having a star system. Each star system is generated randomly.
	 * 
	 * @param density		Percentage of hexes which have a star system.
	 */
	public void create(int density) {
		Name		names = new Name("planet");

		for (int x=1; x <= WIDTH; x++) {
			for (int y=1; y <= HEIGHT; y++) {
				if (Die.d100() <= density) {
					// Create a new star system.
					String			name = names.getName();
					try {
						while (new StarSystem(factory, name) != null) {
							name = names.getName();
						}
					} catch (ObjectNotFoundException e) {
						// Come here if the chosen name is unique.
					}
					
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
		FileReader          reader = null;
        LineNumberReader    input = null;

		for (StarSystem sys: getSystems()) {
			int		sysId = sys.getId();
			try {
				factory.deleteStarSystem(sysId);
			} catch (Throwable e) {
				
			}
		}
        
        
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
		            	System.gc();
	            	}
					//int				ssx = (uwp.getX()%8 == 0)?8:uwp.getX()%8;
					//int				ssy = (uwp.getY()%10 == 0)?10:uwp.getY()%10;
					//int				number = ssx*100 + ssy;
					//String			name = baseName + Sector.getSubSector(uwp.getX(), uwp.getY())+number;
					//uwp.depopulate(name);
	            	
					// Seed the random number generator.
					new Random(getX()*100000+getY()*1000 + uwp.getX()*40 +  uwp.getY());
					if (factory.getStarSystem(id, uwp.getX(), uwp.getY()) != null) continue;
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
	
	public void populateUWPs(File file) {
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
	            	
	            	
					int		x = uwp.getX();
					int		y = uwp.getY();
					StarSystem	system = factory.getStarSystem(id, x, y);
					if (system.getUWP() != null) continue;
					//System.out.println(system.getName()+" ["+line+"] ["+line.length()+"]");
					system.setUWP(line);
					system.persist();
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
		System.out.println("Creating sector ["+name+"]");
		Sector		sector = new Sector(name, x, y);
		//String		filename = "data/core/gni/"+datafile+".GNI";
		//sector.create(new File(filename));
		sector.create(30);
	}
	
	/**
	 * Given a density map of Known Space, create a new sector from it.
	 * The density map is a greyscale map giving an indication of the density
	 * of star systems at each location. If the sector already has star systems,
	 * these are deleted first.
	 * 
	 * @param sx		X coordinate of the sector.
	 * @param sy		Y coordinate of the sector.
	 * @param file		File containing the image.
	 * @param basex		Top left X sector coordinate of the map.
	 * @param basey		Top left Y sector coordinate of the map.
	 * 
	 * @throws ObjectNotFoundException
	 */
	private static void createFromImageMap(ObjectFactory factory, int sx, int sy, File file, int basex, int basey) throws ObjectNotFoundException {
		BufferedImage	image = new SimpleImage(file).getBufferedImage();
		Sector			sector = new Sector(factory, sx, sy);
		System.out.println("Cleaning sector ["+sector.getName()+"]");
		for (StarSystem sys: sector.getSystems()) {
			int		sysId = sys.getId();
			try {
				factory.deleteStarSystem(sysId);
			} catch (Throwable e) {
				
			}
		}
		// Whether to fudge the chance of Earth-like worlds.
		// 0 = No fudging (if Sector is Barren),
		// 1 = A little fudging (default)
		// 2 = A lot of fudging (if Sector is Fertile).
		int		fudgeFactor = 1;
		if (sector.hasCode(SectorCode.Ba)) {
			fudgeFactor = 0;
		} else if (sector.hasCode(SectorCode.Fe)) {
			fudgeFactor = 2;
		}
		
		// How well this sector has been colonised.
		// 0  = Very light colonisation of most fertile worlds
		// 1  = Moderate colonisation of hospitable worlds
		// 2+ = Aggressive colonisation of all but the most hostile worlds
		int		colonisationTenacity = 1;
		if (sector.hasCode(SectorCode.Lo)) {
			colonisationTenacity = 0;
		} else if (sector.hasCode(SectorCode.Hi)) {
			colonisationTenacity = 2;
		}
		
		Allegiance		allegiance = new Allegiance(sector.getAllegiance());
		Name			names = null;
		
		if (allegiance.getLanguage() != null) {
			names = new Name(allegiance.getLanguage()+"_planet");
		}
		
		basex = (sx - basex) * 32;
		basey = (int)((sy - basey) * 40.5);
		
		for (int y=1; y <= 40; y++) {
			for (int x=1; x <= 32; x++) {
				int		px = basex + x;
				int		py = basey + y;
				String	hex = Integer.toHexString(image.getRGB(px, py)).substring(6);
				int		colour = Integer.parseInt(hex, 16);
        		if (Die.rollZero(400) < colour) {
        			String	name = null;
        			if (names != null) {
        				name = names.getName();
        			} else {
        				int		number = (((x-1)%8)+1)*100 + ((y-1)%10)+1;
        				name = Sector.getSubSector(x, y).toString()+number;
        			}
        			StarSystem	sys = new StarSystem(factory, name, sector.getId(), x, y, allegiance, fudgeFactor, colonisationTenacity);
        			sys.persist();
        		}
			}
		}
	}
	
	public static void createSol() {
		create("Sol", 0, 0, "iG");		
	}
	
	public static void createMortals() {
		// Core sectors.
		create("Aquila", -1, -1, "hF");
		create("Serpens", 0, -1, "hG");
		create("Virgo", 1, -1, "hH");
		create("Aquarius", -1, 0, "iF");
		//create("Sol", 0, 0, "iG");
		create("Hydra", 1, 0, "iH");
		create("Pisces", -1, 1, "jF");
		create("Taurus", 0, 1, "jG");
		create("Orion", 1, 1, "jH");
		
		// Other sectors.
		create("Rift", -2, -2, "gE");
		create("Passage", -1, -2, "gF");
		create("Borders", -2, -1, "hE");
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
			
			if (!sector.getName().equals("Karleaya")) continue;
			
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
	
	public static void populateUWPs() {
		ObjectFactory		factory = new ObjectFactory();
		Vector<Sector>		sectors = factory.getSectors();
		String				position = "012345ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		for (Sector sector : sectors) {
			int			x = sector.getX();
			int			y = sector.getY();
			String		filename = position.substring(y+11, y+12).toLowerCase()+position.substring(x+12, x+13).toUpperCase();
			//String		filename = Integer.toString(y+15, 36).toLowerCase()+Integer.toString(x+16, 36).toUpperCase();
			
			System.out.println("Populating "+sector.getId()+": "+sector.getName());

			try {
				File		file = new File("data/core/gni/"+filename+".GNI");
				if (file.canRead()) {
					sector.populateUWPs(file);
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
	
	public static void regenerateSystem(int id) {
		regenerateSystem(new ObjectFactory(), id);
	}
	
	public static void regenerateSystem(ObjectFactory factory, int id) {
		for (Planet p : factory.getPlanetsBySystem(id)) {
			Description.setDescription(p);
			p.persist();
			try {
				WorldBuilder	wb = WorldBuilder.getBuilder(p, 513, 257);
				wb.generate();
				SimpleImage		simple = wb.getWorldMap(2);
				factory.storePlanetMap(p.getId(), simple.save());
				factory.storePlanetGlobe(p.getId(), wb.getWorldGlobe(2).save());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}		
	}
	
	public static void regenerate(int id) throws ObjectNotFoundException, IOException {
		ObjectFactory	factory = new ObjectFactory();
		Sector			sector = new Sector(factory, id);
		
		System.out.println(sector.getName());
		
		for (StarSystem sys: sector.getSystems()) {
			System.out.println(" > "+sys.getName());
			int		sysId = sys.getId();
			if (sys.getUWP() == null) {
				String	name = sys.getName();
				int		x = sys.getX();
				int		y = sys.getY();
				factory.deleteStarSystem(sysId);
    			new StarSystem(factory, name, sector.getId(), x, y).persist();
			} else {
				factory.cleanStarSystem(sysId);
				StarSystem		system = factory.getStarSystem(sysId);
				system.regenerate();
				//regenerateSystem(factory, sys.getId());
			}
		}

	}
	
	private static void createSector(int sectorId) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		File			map = new File("/home/sam/density2.jpg");
		final int		baseX = -9;
		final int		baseY = -5;

		Sector			sector = new Sector(factory, sectorId);
		
		createFromImageMap(factory, sector.getX(), sector.getY(), map, baseX, baseY);
		
		factory.close();
	}
	
	private static void createMissingSectors() throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		File			map = new File("/home/sam/density2.jpg");
		final int		baseX = -9;
		final int		baseY = -5;
		
		for (int x=-9; x <= 6; x++) {
			//createFromImageMap(factory, x, -5, map, baseX, baseY);
			//createFromImageMap(factory, x, -4, map, baseX, baseY);
			createFromImageMap(factory, x, -3, map, baseX, baseY);
			factory.close(); factory=null;
			Thread.sleep(2000);	System.gc(); Thread.sleep(3000);
			factory = new ObjectFactory();
		}
		/*
		for (int y=-2; y <= 0; y++) {
			//createFromImageMap(factory, -9, y, map, baseX, baseY);
			//createFromImageMap(factory, -8, y, map, baseX, baseY);
			//createFromImageMap(factory, +4, y, map, baseX, baseY);
			//createFromImageMap(factory, +5, y, map, baseX, baseY);
			//createFromImageMap(factory, +6, y, map, baseX, baseY);
			factory.close(); factory=null;
			Thread.sleep(2000);	System.gc(); Thread.sleep(3000);
			factory = new ObjectFactory();
		}
		*/
		factory.close();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(GraphicsEnvironment.isHeadless());
		//createTravellerSub();
		//createMortals();
		//createSol();
		//regenerate(1);
		//populateUWPs();
		//createTraveller();
		//createMissingSectors();
		
		createSector(45);
		//regenerate(28);
		
		System.exit(0);
		
		ObjectFactory		factory = new ObjectFactory();
		Sector				sector = new Sector(factory, 0, 0);
		StarSystem			s1 = factory.getStarSystem(69);
		StarSystem			s2 = factory.getStarSystem(13);
		
		System.out.println(sector.getBTN(s1.getMainWorld(), s2.getMainWorld()));
		
		/*
		Sector		sector = new Sector(0, 0);
		for (int y=0; y < 4; y++) {
			for (int x=0; x < 4; x++) {
				System.out.println(sector.getSubSectorName(x*8+1, y*10+1));
			}
		}
		*/
	}
}
