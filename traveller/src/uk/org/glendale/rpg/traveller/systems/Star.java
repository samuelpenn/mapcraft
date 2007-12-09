/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.11 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems;

import java.sql.*;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.SpectralType;
import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.StarForm;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.utils.Die;

/**
 * Define a single star.
 * 
 * @author Samuel Penn.
 */
public class Star {
	private ObjectFactory	factory = null;
	private int		  id = 0;
    private String	  name = null;
    private int		  systemId = 0;
    private int		  parentId = 0;
    private int		  distance = 0;

	private StarForm  starForm = StarForm.Star;
	private StarClass starClass = StarClass.V;
	private SpectralType  spectralType = SpectralType.G2;

	//private double    mass = 1.0;
	
	public static final double	SOLAR_MASS = 2.2e30;
	public static final double  G = 6.6e-11;
	public static final double	AU = 150;  // Distance of Earth from Sun, in M Km.
	
	/**
	 * Populate star data from a database result set.
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public Star(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		systemId = rs.getInt("system_id");
		parentId = rs.getInt("parent_id");
		distance = rs.getInt("distance");
		
		starForm = StarForm.valueOf(rs.getString("form"));
		starClass = StarClass.valueOf(rs.getString("class"));
		spectralType = SpectralType.valueOf(rs.getString("type"));
	}

	/**
	 * Create a new star based on the information given.
	 * 
	 * @param systemId		System star belongs to.
	 * @param name			Name of the star.
	 * @param hr			Spectral type of the star (e.g. "G2")
	 * @param sc			Star class (e.g. "V")
	 */
	public Star(ObjectFactory factory, int systemId, String name, String hr, String sc) {
		this.factory = factory;
		this.systemId = systemId;
		this.name = name;

		try {
			this.spectralType = SpectralType.valueOf(hr);
		} catch (IllegalArgumentException e) {
			this.spectralType = SpectralType.M0;
		}
		
		try {
			this.starClass = StarClass.valueOf(sc);
		} catch (IllegalArgumentException e) {
			this.starClass = StarClass.VI;
		}
		
		if (starClass == StarClass.D) {
			starForm = StarForm.WhiteDwarf;
		}
		
		persist();
	}
	
	/**
	 * Create a new random star as part of an existing star system.
	 * 
	 * @param systemId			System star belongs to.
	 * @param name				Name of the star.
	 */
	public Star(ObjectFactory factory, int systemId, String name) {
		this.factory = factory;
		this.systemId = systemId;
		this.name = name;
		
		// Randomise the star class.
		switch (Die.d6(3)) {
		case 3: case 4: case 5: case 6:
			starClass = StarClass.D;
			break;
		case 7:
			starClass = StarClass.VI;
			break;
		case 8: case 9: case 10: case 11: case 12: case 13:
			starClass = StarClass.V;
			break;
		case 14: case 15:
			starClass = StarClass.IV;
			break;
		case 16: case 17:
			starClass = StarClass.III;
			break;
		case 18:
			starClass = StarClass.II;
			// Very small chance of super giant stars.
			if (Die.d10() == 1) {
				starClass = StarClass.Ib;
				if (Die.d10() == 1) {
					starClass = StarClass.Ia;
				}
			}
			break;
		}
		
		spectralType = starClass.getSpectralType();
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Get the general form of the class. This is used to determine whether it is
	 * a 'normal' star, or a remanent such as a Black Hole or White Dwarf.
	 */
	public StarForm getStarForm() {
		return starForm;
	}

	public StarClass getStarClass() {
		return starClass;
	}
	
	public SpectralType getSpectralType() {
		return spectralType;
	}
	
	/**
	 * Get the visible colour of this star. Used when plotting the star system
	 * on the map. Colour is returned in a format suitable for PostScript.
	 * 
	 * @return		RGB value for the star colour, in the form "r g b".
	 */
	public String getColour() {
		return spectralType.getRGBColour();
	}

	/**
	 * Get the relative size of this star, for plotting on the map.
	 * Sizes range from 0.2 for black holes out to 1.5 for super giants.
	 * 
	 * @return		Size, where 1.0 is average.
	 */
	public double getSize() {
		return starClass.getSize();
	}
	
	public int getSystemId() {
		return systemId;
	}
	
	/**
	 * Get id of parent if in orbit around another star, or 0 if this star
	 * has no parent. If no parent, distance should be zero.
	 * 
	 * @return		Parent, or zero.
	 */
	public int getParentId() {
		return parentId;
	}
	
	/**
	 * Get distance of this star from its parent.
	 * @return		Distance, in MKm.
	 */
	public int getDistance() {
		return distance;
	}
	
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	/**
	 * Get the heat output of this star relative to Sol. This is based on
	 * the surface temperate and surface area of the star. Used to calculate
	 * the habitable zones of the system.
	 * 
	 * @return		Relative heat output of star.
	 */
	private double getSolarConstant() {
		double		constant = 1.0;
		
		constant = (1.0 * spectralType.getSurfaceTemperature() / SpectralType.G2.getSurfaceTemperature());
		constant *= Math.pow(starClass.getRadius(), 2.0);
		
		return constant;
	}

	/**
	 * Get the innermost distance at which planets are likely to be found.
	 * Any orbits closer than this are likely to be too hot to allow planetary
	 * formation.
	 * 
	 * @return		Distance in millions of km.
	 */
	public int getInnerLimit() {
		return (int)(30 * getSolarConstant());
	}
	
	/**
	 * Get the optimal distance for an Earth-like world. Much closer than this,
	 * and the temperature is too warm, much further out and the temperature will
	 * be too cold. Note that the world's atmosphere will also affect the
	 * temperature of the world.
	 * 
	 * @return		Distance in millions of km.
	 */
	public int getEarthDistance() {
		return (int)(150 * getSolarConstant());
	}

	/**
	 * Worlds beyond this distance are likely to be cold ice worlds.
	 * 
	 * @return		Distance in millions of km.
	 */
	public int getColdPoint() {
		return (int)(1000 * getSolarConstant());
	}
	
	
	/**
	 * Get the typical temperature for a planet at the given orbital distance.
	 * The temperature is returned as relative to Earth-standard. This assumes
	 * a planet without an atmosphere. Planets with atmospheres will be somewhat
	 * warmer.
	 * 
	 * @param distance		Distance from the star, in Mkm.
	 * @return				Temperature.
	 */
	public Temperature getOrbitTemperature(int distance) {
		Temperature		temperature = Temperature.Standard;
		double			constant = getSolarConstant();
		
		if (distance < 25 * constant) {
			temperature = Temperature.ExtremelyHot;
		} else if (distance < 50 * constant) {
			temperature = Temperature.VeryHot;
		} else if (distance < 75 * constant) {
			temperature = Temperature.Hot;
		} else if (distance < 100 * constant) {
			temperature = Temperature.Warm;
		} else if (distance < 120 * constant) {
			temperature = Temperature.Standard;
		} else if (distance < 200 * constant) {
			temperature = Temperature.Cool;
		} else if (distance < 500 * constant) {
			temperature = Temperature.Cold;
		} else if (distance < 2000 * constant) {
			temperature = Temperature.VeryCold;
		} else {
			temperature = Temperature.ExtremelyCold;
		}
		
		return temperature;
	}
	
	/**
	 * Get the orbital period of a given orbit.
	 * 
	 * @param distance		Distance in millions of km.
	 * @return				Period, in seconds.
	 */
	public long getOrbitPeriod(int distance) {
		long		seconds = 0;
		double		mass = spectralType.getMass();		// Relative to the mass of our sun.
		double		a = Math.pow(distance/AU, 3);
		double		years = 0;
		
		years = Math.sqrt(a / mass);
		seconds = (long)(years * 365.25 * 86400);
		
		return seconds;
	}

/*
	public double
	getHeatOutput() {
		double		power = 0;

		// So = E(sun) x (R(sun)/r)^2
		power = (surfaceTemperature/SOLAR_TEMPERATURE) * Math.pow(radius/SOLAR_RADIUS, 2);

		return power;
	}
*/
	public void persist() {
		if (factory == null) {
			factory = new ObjectFactory();
		}

		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		data.put("id", id);
		data.put("name", name);
		data.put("system_id", systemId);
		data.put("parent_id", parentId);
		data.put("distance", distance);
		data.put("form", ""+starForm);
		data.put("class", ""+starClass);
		data.put("type", spectralType);
		
		int auto = factory.persist("star", data);
		if (id == 0) id = auto;
	}
	
	private Star(int id, String name, String type, String c) {
		this.id = id;
		this.name = name;
		this.spectralType = SpectralType.valueOf(type);
		this.starClass = StarClass.valueOf(c);
	}
	

	public static void
	main(String[] args) {
		Star			star = null;
		
		star = new Star(0, "Alpha", "G1", "VI");
		System.out.printf("[%s] Period [%d]\n", star.getName(), star.getOrbitPeriod(422)/(86400));
		/*
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "G2", "V");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "F5", "IV");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "A5", "III");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "B5", "II");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "B0", "Ib");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "O5", "Ia");
		System.out.println(star.getSolarConstant());
		star = new Star(0, "Alpha", "F9", "D");
		System.out.println(star.getSolarConstant());
		*/
	}

	/**
	 * Ensure that this star is smaller than the primary star.
	 *  
	 * @param primaryStar
	 */
	public void makeSmaller(Star primaryStar) {
		// TODO Auto-generated method stub
		if (primaryStar == null || getStarClass().isSmallerThan(primaryStar.getStarClass())) {
			// If already smaller, then do nothing.
			return;
		}
		
		starClass = primaryStar.getStarClass().getCompanionStar();
		spectralType = starClass.getSpectralType();		
	}
	
	public String toXML() {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<star id=\"").append(id).append("\" name=\"").append(name).append("\">\n");
		buffer.append("    <form>").append(starForm.toString()).append("</form>\n");
		buffer.append("    <class>").append(starClass.toString()).append("</class>\n");
		buffer.append("    <type>").append(spectralType.toString()).append("</type>\n");
		if (parentId > 0) {
			buffer.append("    <parent>").append(parentId).append("</parent>\n");
			buffer.append("    <distance>").append(distance).append("</distance>\n");
		}
		buffer.append("</star>\n");
		
		return buffer.toString();
	}

}
