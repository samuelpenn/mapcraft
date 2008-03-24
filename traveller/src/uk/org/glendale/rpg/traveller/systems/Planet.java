/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.16 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.civilisation.trade.Commodity;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.glossary.GlossaryEntry;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Define a single planet.
 * 
 * @author Samuel Penn
 */
public class Planet {
	ObjectFactory		factory = null;
	private int 		id = 0;
	private int			systemId = 0;
	private int			parentId = 0;
	private boolean		moon = false;
	private int			moonCount = 0;
	
	private String			name = null;
	private StarportType	starport = StarportType.X;
	private int				distance = 0;
	private int				radius = 0;
	private int				day = 0;
	private int				tilt = 0;
	
	private AtmosphereType			atmosphereType = AtmosphereType.Vacuum;
	private AtmospherePressure		atmospherePressure = AtmospherePressure.None;
	private int						hydrographics = 0;
	private long					population = 0;
	private GovernmentType			government = GovernmentType.Anarchy;;
	private int						lawLevel = 0;
	private int						techLevel = 0;
	private PlanetType				planetType = PlanetType.Undefined;
	private LifeType				lifeType = LifeType.None;
	private Temperature				temperature = Temperature.Standard;
	private String					base = "";
	private String					tradeCodes = "";
	private String					description = null;
	private EnumSet<PlanetFeature>	features = null;

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public PlanetType getType() {
		return planetType;
	}
	
	public StarportType getStarport() {
		return starport;
	}
	
	public void setStarport(StarportType starport) {
		this.starport = starport;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	public int getParentId() {
		return parentId;
	}
	
	void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
	public int getSystemId() {
		return systemId;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public void setDistance(int distance) {
		if (distance > 0) {
			this.distance = distance;
		}
	}
	
	public void setMoon(boolean moon) {
		this.moon = moon;
	}
	
	public boolean isMoon() {
		return moon;
	}
	
	/**
	 * Get a count of the number of moons. This is only used when creating a new
	 * planet, and is used as a marker to signify whether the world has moons.
	 * 
	 * @return		Number of moons.
	 */
	public int getMoonCount() {
		return moonCount;
	}
	
	void setMoonCount(int moons) {
		this.moonCount = moons;
	}
	
	/**
	 * Get the axial tilt of this world from the vertical. A tilt of zero
	 * means that the world's north pole is pointing 'up', 180 points it
	 * 'down' and 90 points it along the plane of the solar system. Tilts
	 * higher than 90 mean that the world is spinning 'backwards' to
	 * normal.
	 * 
	 * @return		Axial tilt in degrees.
	 */
	public int getTilt() {
		return tilt;
	}
	
	/**
	 * Set the axial tilt of the world.
	 * 
	 * @param tilt	Axial tilt in degrees.
	 */
	public void setTilt(int tilt) {
		this.tilt = tilt;
	}
	
	public void setAtmosphereType(AtmosphereType type) {
		this.atmosphereType = type;
	}
	
	public AtmosphereType getAtmosphereType() {
		return atmosphereType;
	}
	
	public void setAtmospherePressure(AtmospherePressure pressure) {
		this.atmospherePressure = pressure;
	}
	
	public AtmospherePressure getAtmospherePressure() {
		return atmospherePressure;
	}
	
	/**
	 * Set the percentage of the world's surface which is covered by liquid.
	 * Normally water is assumed, but this is not always the case.
	 * 
	 * @param hydro			Hydrographic percentage of the planet's surface.
	 */
	public void setHydrographics(int hydro) {
		if (hydro < 0) hydro = 0;
		if (hydro > 100) hydro = 100;
		
		this.hydrographics = hydro;
	}
	
	public int getHydrographics() {
		return hydrographics;
	}
	
	public void setLifeLevel(LifeType life) {
		this.lifeType = life;
	}
	
	public LifeType getLifeLevel() {
		return lifeType;
	}
	
	public int getLawLevel() {
		return lawLevel;
	}
	
	public void setLawLevel(int lawLevel) {
		this.lawLevel = lawLevel;
	}
	
	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}
	
	public int getEffectiveDistance() {
		int		ed = distance;
		
		ed *= atmospherePressure.getEffectiveDistance();
		ed *= atmosphereType.getGreenhouse();
		
		return ed;
	}
	
	public long getPopulation() {
		return population;
	}
	
	/**
	 * Get the log of the planet's population.
	 * 
	 * @return	0 if there is no population, or the log base 10 of
	 *          the planet's population otherwise.
	 */
	public int getPopulationLog() {
		if (population > 0) {
			return (int)Math.log10(population);
		}
		return 0;
	}
	
	public String getShortPopulation() {
		if (population < 1000) {
			return ""+population;
		} else if (population < 1000000) {
			return ""+(population/1000)+"k";
		} else if (population < 1000000000) {
			return ""+(population/1000000)+"m";
		}
		
		return ""+(population/1000000000)+"b";
	}
	
	public void setPopulation(long population) {
		this.population = population;
	}
	
	/**
	 * Length of day in seconds.
	 */
	public int getDay() {
		return day;
	}
	
	/**
	 * Set the length of the day, in seconds.
	 */
	public void setDay(int day) {
		this.day = day;
	}
	
	/**
	 * Append a numerical value plus its units to a string. Handles plurals and
	 * singulars correctly. For example, will add "1 day" or "3 days", depending
	 * on the value. If value is zero or less, nothing is added.
	 * 
	 * @param buffer		Buffer to be appended.
	 * @param value		Numeric value to add.
	 * @param unit			Singular form of unit to use.
	 */
	private void appendUnitValue(StringBuffer buffer, int value, String unit) {
		if (value > 0) {
			if (buffer.length() > 0) {
				buffer.append(" ");
			}
			if (unit.length() > 2) {
				buffer.append(value).append(" ").append(unit);
				if (value > 1) {
					buffer.append("s");
				}
			} else {
				// If using abbreviations for units, use simpler form.
				buffer.append(value).append(unit);
			}
		}
	}
	
	/**
	 * Get the length of day, in the format "23 hours 5 minutes 2 seconds".
	 */
	private String getTimeAsString(int seconds, boolean verbose) {
		StringBuffer	buffer = new StringBuffer();
		int				minutes = (seconds/60)%60;
		int				hours = (seconds/3600)%24;
		int				days = seconds/86400;
		seconds = seconds%60;
		
		if (days < 3) {
			// Keep it in hours unless we really need to.
			hours += days*24;
			days = 0;

			if (hours < 2) {
				// Likewise, minutes.
				minutes += hours * 60;
				hours = 0;
			}
		}

		if (verbose) {
			appendUnitValue(buffer, days, "day");
			appendUnitValue(buffer, hours, "hour");
			appendUnitValue(buffer, minutes, "minute");
			appendUnitValue(buffer, seconds, "second");
		} else {
			appendUnitValue(buffer, days, "d");
			appendUnitValue(buffer, hours, "h");
			appendUnitValue(buffer, minutes, "m");
			appendUnitValue(buffer, seconds, "s");			
		}
		
		return buffer.toString();
	}
	
	public String getDayAsString(boolean verbose) {
		return getTimeAsString(day, verbose);
	}
	
	public String getYearAsString(boolean verbose) {
		Star	star = getStar();
		if (star == null) return "Undetermined";
		int		period = (int)star.getOrbitPeriod(distance);
		
		// If the period is longer than 10 days, then strip out
		// excess hours and seconds, so we get something in days.
		if (period > 864000) {
			period -= period % 86400;
		}
		
		return getTimeAsString(period, verbose);
	}
	
	/**
	 * Get the star which is the parent of this planet. Currently
	 * moons are not supported, but should return the star of the
	 * parent planet.
	 * 
	 * @return		Star which is this world's primary.
	 */
	public Star getStar() {
		Star		star = null;
		if (!moon) {
			if (factory == null) {
				factory = new ObjectFactory();
			}
			star = factory.getStar(parentId);
		}
		return star;
	}

	/**
	 * The level of technology for this world. This is the most commonly
	 * available level of technology, higher technology may be available
	 * but access will be limited and/or expensive. The TL given is the
	 * GURPS Traveller TL, not the Classic Traveller TL.
	 * 
	 * Overall Imperium Tech is 12.
	 */ 
	public int getTechLevel() {
		return techLevel;
	}
	
	public void setTechLevel(int techLevel) {
		if (techLevel < 0) {
			techLevel = 0;
		}
		this.techLevel = techLevel;
	}
	
	public GovernmentType getGovernment() {
		return government;
	}
	
	public void setGovernment(GovernmentType government) {
		this.government = government;
	}
	
	public Temperature getTemperature() {
		return temperature;
	}
	
	private int getDigit(String sec, int index) {
		int			digit = 0;
		String		value = sec.substring(index, index+1);
		
		try {
			digit = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			digit = 0;
		}
		
		return digit;
	}
	
	public Planet(String name, PlanetType type, int radius) {
		this.name = name;
		this.planetType = type;
		this.radius = radius;
	}
	
	public Planet(ObjectFactory factory, int id) throws ObjectNotFoundException {
		this.factory = factory;
		
		try {
			ResultSet		rs = factory.read("planet", "id="+id);
			rs.next();
			read(rs);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ObjectNotFoundException("Cannot find planet with id "+id);
		}
	}
	
	/**
	 * Populate star data from a database result set.
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public Planet(ResultSet rs) throws SQLException {
		read(rs);
	}
	
	public Planet(ObjectFactory factory, ResultSet rs) throws SQLException {
		this.factory = factory;
		read(rs);
	}
	
	private void read(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		systemId = rs.getInt("system_id");
		parentId = rs.getInt("parent_id");
		distance = rs.getInt("distance");
		
		radius = rs.getInt("radius");
		planetType = PlanetType.valueOf(rs.getString("type"));
		starport = StarportType.valueOf(rs.getString("starport"));
		atmosphereType = AtmosphereType.valueOf(rs.getString("atmosphere"));
		atmospherePressure = AtmospherePressure.valueOf(rs.getString("pressure"));
		hydrographics = rs.getInt("hydrographics");
		population = rs.getLong("population");
		government = GovernmentType.valueOf(rs.getString("government"));
		lawLevel = rs.getInt("law");
		techLevel = rs.getInt("tech");		
		temperature = Temperature.valueOf(rs.getString("temperature"));
		lifeType = LifeType.valueOf(rs.getString("life"));
		day = rs.getInt("day");
		base = rs.getString("base");
		tradeCodes = rs.getString("trade");
		description = rs.getString("description");
		moon = rs.getInt("moon")>0;
		
		String	f = rs.getString("features");
		if (f!=null && f.length() > 0) {
			String[]	list = f.split(" ");
			for (String item : list) {
				addFeature(PlanetFeature.getByCode(item));
			}
		}
	}
	
	/**
	 * Set up the planet using the UWP code. In this case, the planet is a moon,
	 * so we need to base the temperature on some wacky stuff based on the
	 * temperature of the Jovian world that is its parent.
	 * 
	 * @param factory
	 * @param systemId
	 * @param uwp
	 * @param star
	 * @param name
	 * @param distance
	 * @param temperature
	 */
	public Planet(ObjectFactory factory, int systemId, UWP uwp, Star star, String name, int distance, Temperature temperature) {
		this.factory = factory;
		this.systemId = systemId;
		this.name = name;
		this.starport = StarportType.valueOf(uwp.getStarPort());
		this.planetType = PlanetType.Gaian;
		this.distance = distance;
		this.parentId = star.getId();
		this.moon = true;

		parseSize(uwp);
		parseAtmosphere(uwp);
		parseHydrographics(uwp);
		parsePopulation(uwp);
		parseGovernment(uwp);
		parseLaw(uwp);
		parseTechLevel(uwp);
		parseBase(uwp);
		parseTradeCodes(uwp);

		if (population > 0) {
			lifeType = LifeType.Extensive;
		}

		if (temperature.isHotterThan(Temperature.Warm)) {
			this.temperature = Temperature.Warm;
		} else if (temperature.isHotterThan(Temperature.Cold)) {
			this.temperature = temperature.getColder();
		} else {
			this.temperature = temperature;
		}

		// Now perform some basic sanity checking.
		sanityCheck(star);
	}
	
	/**
	 * Define the planet using the sec string.
	 * 
	 * @param sec		Data string describing a star system.
	 */
	public Planet(ObjectFactory factory, int systemId, UWP uwp, Star star, String name, int distance) {
		this.factory = factory;
		this.systemId = systemId;
		this.name = name;
		this.starport = StarportType.valueOf(uwp.getStarPort());
		this.planetType = PlanetType.Gaian;
		this.distance = distance;
		this.parentId = star.getId();

		parseSize(uwp);
		parseAtmosphere(uwp);
		parseHydrographics(uwp);
		parsePopulation(uwp);
		parseGovernment(uwp);
		parseLaw(uwp);
		parseTechLevel(uwp);
		parseBase(uwp);
		parseTradeCodes(uwp);
		
		System.out.println("Creating main world with population "+population);

		if (population > 0) {
			lifeType = LifeType.Extensive;
		}

		// Now perform some basic sanity checking.
		temperature = star.getOrbitTemperature((int)(distance * atmospherePressure.getEffectiveDistance()));
		sanityCheck(star);
	}
	
	/**
	 * Perform some basic checks to ensure a sensible world. Used to
	 * determine the actual type of the planet, based on its size,
	 * atmosphere and distance from primary.
	 * 
	 * @param star		Star this planet orbits around.
	 */
	void sanityCheck(Star star) {
		
		if (population < starport.getMinimumPopulation()) {
			population = starport.getMinimumPopulation() * Die.d6();
		}
		
		switch (techLevel) {
		case 0: case 1: case 2: case 3:
		case 4: case 5: case 6:
			// No population required.
			break;
		case 7: case 8:
			// Need thousands of people.
			if (population < 1000) {
				population = Die.d6() * 1000;
			}
			break;
		case 9: case 10:
			// Need hundreds of thousands.
			if (population < 100000) {
				population = Die.d6() * 100000;
			}
			break;
		case 11: case 12:
			// Need tens of millions.
			if (population < 10000000) {
				population = Die.d6() * 10000000;
			}
			break;
		}
		
		if (radius == 0) {
			planetType = PlanetType.AsteroidBelt;
			lifeType = LifeType.None;
			return;
		} else if (population > 0) {
			planetType = PlanetType.Gaian;
			lifeType = LifeType.Extensive;
		}
		
		// Seen a few of these. Fix them.
		if (hydrographics >= 100) {
			hydrographics = 100;
			addTradeCode(TradeCode.Wa);
		}

		if (isMoon()) {
			// Is a moon.
			// TODO: Implement.
			
			switch (atmospherePressure) {
			case None:
				lifeType = LifeType.None;
				if (temperature.isColderThan(Temperature.Cool)) {
					planetType = PlanetType.Europan;
					lifeType = LifeType.ComplexOcean;
				} else if (temperature.isColderThan(Temperature.Warm)) {
					planetType = PlanetType.Cerean;
				} else {
					planetType = PlanetType.Vestian;
				}
				break;
			case Trace:
			case VeryThin:
				if (temperature.isColderThan(Temperature.Cool)) {
					planetType = PlanetType.Europan;
					lifeType = LifeType.ComplexOcean;
				} else if (temperature.isColderThan(Temperature.Warm)) {
					if (atmosphereType.getSuitability() > 0.2) {
						planetType = PlanetType.EoArean;
						lifeType = LifeType.SimpleLand;
					} else {
						planetType = PlanetType.Arean;
						lifeType = LifeType.None;
					}
				} else {
					if (atmosphereType.getSuitability() > 0.2) {
						planetType = PlanetType.PostGaian;
						lifeType = LifeType.ComplexLand;
					} else {
						planetType = PlanetType.Arean;
						lifeType = LifeType.None;
					}
				}
				break;
			case Thin:
			case Standard:
			case Dense:
				if (temperature.isColderThan(Temperature.Cold)) {
					planetType = PlanetType.GaianTundral;
					lifeType = LifeType.ComplexOcean;
					temperature = Temperature.VeryCold;
				} else {
					planetType = PlanetType.PostGaian;
				}
				break;
			default:
				if (temperature.isColderThan(Temperature.Cool)) {
					planetType = PlanetType.MesoTitanian;
				} else {
					planetType = PlanetType.Cytherean;
				}
				break;
			}
			
		} else if (atmospherePressure == AtmospherePressure.None) {
			// Any vacuum world.
			if (radius < 2000) {
				// Asteroids.
				if (temperature.isHotterThan(Temperature.Hot)) {
					planetType = PlanetType.Vulcanian;
				} else if (temperature.isHotterThan(Temperature.Cool)) {
					planetType = PlanetType.Basaltic;
				} else if (temperature.isColderThan(Temperature.VeryCold)) {
					planetType = PlanetType.Mimean;
				} else {
					planetType = PlanetType.Cerean;
				}
				lifeType = LifeType.None;
			} else if (radius < 4000) {
				if (temperature.isHotterThan(Temperature.Hot)) {
					planetType = PlanetType.Hermian;
				} else if (temperature.isHotterThan(Temperature.Cool)) {
					planetType = PlanetType.EoArean;
				} else if (temperature.isColderThan(Temperature.Cold)) {
					planetType = PlanetType.AreanLacustric;
				} else {
					planetType = PlanetType.Arean;
				}
				lifeType = LifeType.None;				
			} else if (radius < 7000) {
				if (temperature.isHotterThan(Temperature.Hot)) {
					planetType = PlanetType.JaniLithic;
					atmospherePressure = AtmospherePressure.Trace;
					atmosphereType = AtmosphereType.InertGases;
				} else if (temperature.isColderThan(Temperature.VeryCold)) {
					planetType = PlanetType.LithicGelidian;
				} else {
					planetType = PlanetType.Arean;
				}
				lifeType = LifeType.None;				
			} else {
				if (temperature.isHotterThan(Temperature.Hot)) {
					planetType = PlanetType.JaniLithic;
					atmospherePressure = AtmospherePressure.VeryThin;
					atmosphereType = AtmosphereType.InertGases;
					lifeType = LifeType.None;				
				} else if (temperature.isColderThan(Temperature.VeryCold)) {
					planetType = PlanetType.LithicGelidian;
					lifeType = LifeType.None;				
				} else {
					// Way too silly. Fix it.
					atmospherePressure = AtmospherePressure.Thin;
					atmosphereType = AtmosphereType.Standard;
					sanityCheck(star);
					return;
				}
			}
		} else if (atmospherePressure.isThinnerThan(AtmospherePressure.Thin)) {
			if (atmosphereType.isGaian()) {
				lifeType = LifeType.SimpleLand;
				if (temperature.isHotterThan(Temperature.Hot)) {
					planetType = PlanetType.JaniLithic;
					if (hydrographics > 10) {
						hydrographics /= 5;
					}
					lifeType = LifeType.Metazoa;
				} else if (temperature.isColderThan(Temperature.Cold)) {
					planetType = PlanetType.GaianTundral;
					lifeType = LifeType.ComplexOcean;
				} else if (radius < 5000) {
					planetType = PlanetType.EoArean;
				} else {
					planetType = PlanetType.PostGaian;					
				}
			} else {
				lifeType = LifeType.None;
				if (radius < 3000) {
					if (temperature.isHotterThan(Temperature.Hot)) {
						planetType = PlanetType.Hermian;
					} else if (temperature.isColderThan(Temperature.Cold)) {
						planetType = PlanetType.Oortean;
					} else {
						planetType = PlanetType.Cerean;
					}
				} else if (radius < 5000) {
					if (temperature.isHotterThan(Temperature.Hot)) {
						planetType = PlanetType.Hermian;
					} else if (temperature.isColderThan(Temperature.Cold)) {
						planetType = PlanetType.LithicGelidian;
					} else {
						planetType = PlanetType.Arean;
					}
				} else {
					if (temperature.isHotterThan(Temperature.Hot)) {
						planetType = PlanetType.JaniLithic;
					} else if (temperature.isColderThan(Temperature.Cold)) {
						planetType = PlanetType.GaianTundral;
						if (hydrographics > 0) {
							lifeType = lifeType.ComplexOcean;
						} else {
							lifeType = lifeType.Protozoa;
						}
					} else {
						planetType = PlanetType.PostGaian;
						if (hydrographics < 10) {
							lifeType = LifeType.Protozoa;
						} else if (hydrographics < 40) {
							lifeType = LifeType.ComplexOcean;
						} else {
							lifeType = LifeType.SimpleLand;
						}
					}
				}
			}
		} else if (atmospherePressure.isDenserThan(AtmospherePressure.Dense)) {
			if (radius < 3000) {
				
			} else if (radius < 5000) {
				
			} else if (radius < 8000) {
				
			}
			planetType = PlanetType.EoArean;
			lifeType = LifeType.SimpleLand;
			if (temperature.isColderThan(Temperature.Cold)) {
				planetType = PlanetType.Europan;		
				lifeType = LifeType.ComplexOcean;
			} else if (temperature.isColderThan(Temperature.Cool)) {
				planetType = PlanetType.AreanLacustric;
				lifeType = LifeType.SimpleLand;
			}
		} else {
			// Normal(ish) atmosphere pressure.
			if (temperature.isColderThan(Temperature.Cold)) {
				planetType = PlanetType.Europan;		
				lifeType = LifeType.ComplexOcean;
			} else if (temperature.isColderThan(Temperature.Cool)) {
				planetType = PlanetType.GaianTundral;
				lifeType = LifeType.SimpleLand;
			}
		}
		
		if (atmosphereType == AtmosphereType.SulphurCompounds) {
			// Venus like world.
			planetType = PlanetType.Cytherean;
			lifeType = LifeType.None;
			if (hydrographics == 100) {
				// Very hot, dense atmosphere, water world.
				planetType = PlanetType.Pelagic;
			}
		} else if (atmosphereType == AtmosphereType.Chlorine) {
			planetType = PlanetType.EoGaian;
			lifeType = LifeType.None;
		} else if (atmosphereType == AtmosphereType.NitrogenCompounds) {
			planetType = PlanetType.MesoGaian;
			lifeType = LifeType.Metazoa;
		}
		
		/*
		 * Finally, fix the trade codes.
		 */
		
		// Physical codes.
		removeTradeCode(TradeCode.Va);
		removeTradeCode(TradeCode.Ba);
		removeTradeCode(TradeCode.De);
		removeTradeCode(TradeCode.Fl);
		removeTradeCode(TradeCode.Ic);
		if (atmospherePressure == AtmospherePressure.None) {
			addTradeCode(TradeCode.Va);
		} else if (hydrographics < 10) {
			addTradeCode(TradeCode.De);
		} else if (lifeType.isSimplerThan(LifeType.SimpleLand)) {
			addTradeCode(TradeCode.Ba);
		}
		
		// Hydrographical codes
		if (hydrographics > 0) {
			if (atmosphereType.isNonWater()) {
				addTradeCode(TradeCode.Fl);
			}
			if (atmospherePressure.isThinnerThan(AtmospherePressure.VeryThin)) {
				addTradeCode(TradeCode.Ic);
			}
			if (temperature.isColderThan(Temperature.Cold)) {
				addTradeCode(TradeCode.Ic);
			}
		}
		
		// Civilisation codes.
		removeTradeCode(TradeCode.Ag);
		removeTradeCode(TradeCode.In);
		removeTradeCode(TradeCode.Na);
		removeTradeCode(TradeCode.Ni);
		removeTradeCode(TradeCode.Hi);
		removeTradeCode(TradeCode.Lo);
		if (population > 0) {
			// Population
			if (population >= 1000000000) {
				addTradeCode(TradeCode.Hi);
			} else if (population < 100000) {
				addTradeCode(TradeCode.Lo);
			}
			// Agriculture
			if (lifeType.isMoreComplexThan(LifeType.SimpleLand)) {
				if (!hasTradeCode(TradeCode.Lo) && !hasTradeCode(TradeCode.Hi)) {
					if (hydrographics >= 40 && hydrographics <= 80) {
						addTradeCode(TradeCode.Ag);
					}
				}
			} else if (lifeType == LifeType.SimpleLand && population >= 100000000) {
				addTradeCode(TradeCode.Na);
			} else if (population >= 1000000) {
				addTradeCode(TradeCode.Na);
			}
			// Industry
			if (population >= 1000000000 && techLevel > 5) {
				addTradeCode(TradeCode.In);
			} else if (population < 1000000 || techLevel < 6) {
				addTradeCode(TradeCode.Ni);
			}
			// Economy
			int		economy = 0;
			if (starport == StarportType.A) economy+=3;
			if (starport == StarportType.B) economy+=2;
			if (starport == StarportType.C) economy+=1;
			if (techLevel < 7) economy--;
			if (techLevel > 9) economy++;
			if (techLevel > 11) economy++;
			if (population >= 100000000L) economy++;
			if (population >= 1000000000L) economy++;
			if (atmosphereType.getSuitability() < 0.7) economy--;
			if (atmospherePressure.getSuitability() < 0.5) economy--;
			if (temperature.getSuitability() < 0.7) economy--;
			if (hydrographics <= 30) economy--;
			economy += government.getEconomyModifier();
			
			if (economy > 2) {
				addTradeCode(TradeCode.Ri);
			} else if (economy < -2) {
				addTradeCode(TradeCode.Po);
			}
		}
	}
	
	/**
	 * Get the real value of the given UWP code digit. A UWP code ranges from
	 * 0 to 9, and A to Z, missing out I and O. A is 10, B 11 etc.
	 * 
	 * @param code		One character code to be checked.
	 * 
	 * @return			Numeric value of code, or -1 if invalid.
	 */
	private int getValue(String code) {
		String		codes = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
		
		return codes.indexOf(code);
	}
	
	/**
	 * Find the radius of the planet from the UWP code. The UWP gives
	 * the diameter in thousands of miles. Covert this to km radius.
	 * Also sets a random day length, based on radius.
	 * 
	 * @param sec		Full planetary UWP code.
	 */
	private void parseSize(UWP uwp) {
		int			size = uwp.getDiameter();
		
		if (size == 0) {
			planetType = PlanetType.Cerean;
		}
		
		radius = size * 800;

		// Following sets day length to be some whole number of minutes.
		day = 60 * (int)(Die.d6(7) * 50  * Math.pow(radius/6400.0, 0.33));
		
		if (distance < 50) {
			day *= 100;
		}
	}

	/**
	 * Parse the atmosphere code from the world's UWP string.
	 * @param sec
	 */
	private void parseAtmosphere(UWP uwp) {
		int			atmosphere = uwp.getAtmosphere();

		// Defaults.
		atmospherePressure = AtmospherePressure.Standard;
		atmosphereType = AtmosphereType.Standard;

		switch (atmosphere) {
		case 0:
			atmosphereType = AtmosphereType.Vacuum;
			atmospherePressure = AtmospherePressure.None;
			break;
		case 1:
			atmospherePressure = AtmospherePressure.Trace;
			break;
		case 2:
			atmospherePressure = AtmospherePressure.VeryThin;
			atmosphereType = AtmosphereType.Pollutants;
			break;
		case 3:
			atmospherePressure = AtmospherePressure.VeryThin;
			break;
		case 4:
			atmospherePressure = AtmospherePressure.Thin;
			atmosphereType = AtmosphereType.Pollutants;
			break;
		case 5:
			atmospherePressure = AtmospherePressure.Thin;
			break;
		case 6: case 7:
			// Standard (set above).
			break;
		case 8:
			atmospherePressure = AtmospherePressure.Dense;
			break;
		case 9:
			atmospherePressure = AtmospherePressure.Dense;
			atmosphereType = AtmosphereType.Pollutants;
			break;
		case 10:
			atmosphereType = AtmosphereType.NitrogenCompounds;
			break;
		case 11:
			atmosphereType = AtmosphereType.Chlorine;
			break;
		case 12:
			atmospherePressure = AtmospherePressure.VeryDense;
			atmosphereType = AtmosphereType.SulphurCompounds;
			break;
		default:
			atmosphereType = AtmosphereType.Exotic;
			break;
		}
	}
	
	/**
	 * Parse the hydrographic percentage. This is the percentage of the world's
	 * surface which is covered by liquid.
	 * 
	 * @param sec		Full UWP code.
	 */
	private void parseHydrographics(UWP uwp) {
		hydrographics = uwp.getHydrographic() * 10;
	}
	
	private void parsePopulation(UWP uwp) {
		population = (int)(Math.pow(10, uwp.getPopulation()));
		population *= uwp.getPopulationDigit();
	}
	
	private void parseGovernment(UWP uwp) {
		int			value = uwp.getGovernment();
		
		try {
			government = GovernmentType.values()[value];
		} catch (ArrayIndexOutOfBoundsException e) {
			government = GovernmentType.ImpersonalBureaucracy;
		}
	}
	
	private void parseLaw(UWP uwp) {
		lawLevel = uwp.getLawLevel();
		
		// Convert from Traveller Law level to GURPS
		switch (uwp.getLawLevel()) {
		case 0:
			lawLevel = 0;
			break;
		case 1: case 2:
			lawLevel =1;
			break;
		case 3: case 4:
			lawLevel = 2;
			break;
		case 5:
			lawLevel = 3;
			break;
		case 6: case 7:
			lawLevel = 4;
			break;
		case 8:
			lawLevel = 5;
			break;
		default:
			lawLevel = 6;
		}
	}
	
	/**
	 * @param uwp
	 */
	private void parseTechLevel(UWP uwp) {
		techLevel = uwp.getTechLevel();
		
		// Convert from Traveller TL to GURPS TL.
		switch (uwp.getTechLevel()) {
		case 0: 
			techLevel = 3; 
			break;
		case 1: 
			techLevel = 4; 
			break;
		case 2: case 3: case 4: 
			techLevel = 5; 
			break;
		case 5: case 6:
			techLevel = 6;
			break;
		case 7:
			techLevel = 7;
			break;
		case 8:
			techLevel = 8;
			break;
		case 9: case 10: case 11:
			techLevel = 9;
			break;
		case 12: case 13:
			techLevel =10;
			break;
		case 14:
			techLevel = 11;
			break;
		case 15:
			techLevel = 12;
			break;
		case 16:
			techLevel = 13;
			break;
		default:
			// Anything higher.
			techLevel = 14;
		}
	}
	
	private void parseBase(UWP uwp) {
		base = uwp.getBase();
		if (base == null) {
			base = "";
		}
	}
	
	private void parseTradeCodes(UWP uwp) {
		tradeCodes = uwp.getTradeCodes();
		if (tradeCodes == null) {
			tradeCodes = "";
		}
	}
	
	private String getTrade(String code) {
		if (code.equals("Ag")) {
			return "Agricultural";
		} else if (code.equals("Na")) {
			return "Non-agricultural";
		} else if (code.equals("In")) {
			return "Industrial";
		} else if (code.equals("Ni")) {
			return "Non-industrial";
		} else if (code.equals("Ri")) {
			return "Rich";
		} else if (code.equals("Po")) {
			return "Poor";
		} else if (code.equals("Wa")) {
			return "Water";
		} else if (code.equals("De")) {
			return "Desert";
		} else if (code.equals("Va")) {
			return "Vacuum";
		} else if (code.equals("As")) {
			return "Asteroid";
		} else if (code.equals("Ic")) {
			return "Ice-capped";
		} else if (code.equals("Cp")) {
			return "Subsector capital";
		} else if (code.equals("Cx")) {
			return "Sector capital";
		} else if (code.equals("Ba")) {
			return "Barren";
		} else if (code.equals("Fl")) {
			return "Non-water oceans";
		} else if (code.equals("Hi")) {
			return "High population";
		} else if (code.equals("Lo")) {
			return "Low population";
		}
		return code;
	}
	
	public String[] getTradeCodes() {
		if (tradeCodes == null || tradeCodes.length() == 0) {
			return new String[0];
		}
		ArrayList			list = new ArrayList();
		StringTokenizer		tokens = new StringTokenizer(tradeCodes, " ");
		
		while (tokens.hasMoreTokens()) {
			String		token = tokens.nextToken();
			list.add(token);
		}
		
		return (String[])list.toArray(new String[0]);
	}
	
	/**
	 * Does this planet have the requested trade code?
	 * 
	 * @return		True if it does, false otherwise.
	 */
	public boolean hasTradeCode(TradeCode code) {
		if (tradeCodes == null || tradeCodes.length() == 0) {
			return false;
		}
		String[]		codes = tradeCodes.split(" +");
		for (int i=0; i < codes.length; i++) {
			if (codes[i].equals(code.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean addFeature(PlanetFeature feature) {
		if (features == null) {
			features = EnumSet.noneOf(PlanetFeature.class);
		}
		if (feature.excludedBy(features)) {
			//return false;
		}
		this.features.add(feature);
		return true;
	}
	
	public boolean hasFeature(PlanetFeature feature) {
		if (features == null) {
			return false;
		}
		return features.contains(feature);
	}
	
	public Iterator<PlanetFeature> getFeatures() {
		if (features == null) {
			return (new ArrayList<PlanetFeature>()).iterator();
		}
		return features.iterator();
	}
	
	public String getBase() {
		if (base != null && base.length() > 0) {
			return base;
		} else {
			return null;
		}
	}
	
	/**
	 * Generate a new random planet of the given distance from the specified
	 * star, and of the provided type. Current supported types are:
	 *     Cerean, Hermian, Gaian, EuJovian
	 * @param star
	 * @param distance
	 * @param planetType
	 */
	public Planet(ObjectFactory factory, String name, Star star, int distance, PlanetType planetType) {
		this.factory = factory;
		this.name = name;
		this.systemId = star.getSystemId();
		this.parentId = star.getId();
		this.distance = distance;
		this.planetType = planetType;
		this.starport = StarportType.X;
		this.radius = Die.d6(2) * 500;
		
		switch (planetType) {
		case Cerean:
			radius = Die.d6(2) * 250;
			break;
		case Hermian:
			radius = 1000 + Die.d6(3) * 250;
			if (radius > 3000) {
				atmospherePressure = AtmospherePressure.Trace;
				atmosphereType = AtmosphereType.InertGases;
			}
			break;
		case Gaian:
			radius = 4000 + Die.d6(3) * 250;
			atmospherePressure = AtmospherePressure.Standard;
			atmosphereType = AtmosphereType.Standard;
			hydrographics = Die.d100();
			break;
		case EuJovian:
			radius = 20000 + Die.d6(5) * 1000;
			atmospherePressure = AtmospherePressure.VeryDense;
			atmosphereType = AtmosphereType.Hydrogen;
			break;
		default:
			// Use the default settings above.
		}
		day = (int)(Die.d10(4) * 3000  * Math.sqrt(radius/6400.0));
	}
	
	/**
	 * Get the surface gravity in metres per second per second.
	 * 
	 * @return		Surface gravity.
	 */
	public double getSurfaceGravity() {
		double		g = 9.8016; // Earth surface gravity.
		double		radius = getRadius() / 6400.0; // Relative Earth radius.
		double		density = planetType.getDensity() / 5.5; // Relative Earth density. 
		
		return g * density * radius;
	}
	
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
		data.put("radius", radius);
		data.put("type", ""+planetType);
		data.put("starport", ""+starport);
		data.put("atmosphere", ""+atmosphereType);
		data.put("pressure", ""+atmospherePressure);
		data.put("hydrographics", hydrographics);
		data.put("population", population);
		data.put("government", ""+government);
		data.put("law", lawLevel);
		data.put("tech", techLevel);
		data.put("temperature", ""+temperature);
		data.put("life", ""+lifeType);
		data.put("base", base);
		data.put("trade", tradeCodes);
		data.put("day", day);
		data.put("moon", moon?1:0);
		if (description != null) {
			data.put("description", description);
		}
		if (features != null && features.size() > 0) {
			String		v = "";
			for (PlanetFeature f : features) {
				v += f.getCode() + " ";
			}
			data.put("features", v.trim());
		}
		
		int auto = factory.persist("planet", data);
		if (id == 0) id = auto;
		
		// If the planet has resources set, then write them out.
		if (resources != null) {
			factory.storeResources(id, resources);
		}
	}

	/**
	 * Get a string describing this planet as XML.
	 */
	public String toXML() {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<planet xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\" id=\"").append(id).append("\" name=\"").append(name).append("\">\n");
		if (parentId > 0) {
			buffer.append("    <parent>").append(parentId).append("</parent>\n");
			buffer.append("    <distance>").append(distance).append("</distance>\n");
		}
		buffer.append("    <type>").append(planetType).append("</type>\n");
		buffer.append("    <radius>").append(radius).append("</radius>\n");
		buffer.append("    <gravity>").append(getSurfaceGravity()).append("</gravity>\n");
		buffer.append("    <day seconds=\"").append(getDay()).append("\">").append(getDayAsString(false)).append("</day>\n");
		buffer.append("    <starport>").append(starport).append("</starport>\n");
		buffer.append("    <atmosphere>").append(atmosphereType).append("</atmosphere>\n");
		buffer.append("    <pressure>").append(atmospherePressure).append("</pressure>\n");
		buffer.append("    <hydrographics>").append(hydrographics).append("</hydrographics>\n");
		buffer.append("    <temperature>").append(temperature).append("</temperature>\n");
		buffer.append("    <life>").append(lifeType).append("</life>\n");
		if (population > 0) {
			buffer.append("    <population>").append(population).append("</population>\n");
			buffer.append("    <government>").append(government).append("</government>\n");
			buffer.append("    <law>").append(lawLevel).append("</law>\n");
			buffer.append("    <tech>").append(techLevel).append("</tech>\n");
			buffer.append("    <starport>").append(starport).append("</starport>\n");
		}
		if (base != null && base.length() > 0) {
			buffer.append("    <base>").append(base).append("</base>\n");
		}
		if (tradeCodes != null && tradeCodes.length() > 0) {
			String[] codes = getTradeCodes();
			for (int c=0; c < codes.length; c++) {
				buffer.append("    <trade code=\""+codes[c]+"\">").append(getTrade(codes[c])).append("</trade>\n");
			}
		}
		buffer.append("</planet>\n");
		
		return buffer.toString();
	}
		
	public String toHTML() {
		return toHTML(true);
	}
	
	/**
	 * Generate an output HTML string which describes the planet. If a header is
	 * requested, then an HTML header is included, otherwise an HTML fragment is
	 * returned for inserting into an existing document.
	 * 
	 * @return		String describing the planet.
	 */
	public String toHTML(boolean header) {
		StringBuffer		buffer = new StringBuffer();

		// Heading
		if (isMoon()) {
			buffer.append("<h4>"+getName()+" ("+planetType+")"+((population>0)?" (Populated)":"")+"</h4>\n");			
		} else {
			buffer.append("<h3>"+getName()+" ("+planetType+")"+((population>0)?" (Populated)":"")+"</h3>\n");
		}

		// Trade data
		if (tradeCodes != null && tradeCodes.length() > 0) {
			buffer.append("<p>");
			String[] codes = getTradeCodes();
			for (int c=0; c < codes.length; c++) {
				buffer.append("<img width=\"16\" height=\"16\" src=\""+Config.getBaseUrl()+"images/symbols/trade_"+codes[c].toLowerCase()+".png\"/>");
				buffer.append(getTrade(codes[c])+"; ");
			}
			buffer.append(" ("+id+")\n");
			buffer.append("</p>\n");
		}
		

		// Physical data
		buffer.append("<p>\n");
		buffer.append("<b>Distance:</b> "+distance+" "+(moon?"km":"MKm")+"; <b>Type:</b> "+planetType+"; ");
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);
		if (!planetType.isBelt()) {
			buffer.append("<b>Radius:</b> "+radius+"km; ");
			buffer.append("<b>Gravity:</b> "+format.format(getSurfaceGravity())+"ms<sup>-2</sup>; <b>Day:</b> "+getDayAsString(false));
		}
		buffer.append(" <b>Year:</b> "+getYearAsString(false));
		buffer.append("</p>\n");

		// Biosphere/Atmosphere
		if (!planetType.isBelt()) {
			buffer.append("<p>");
			buffer.append("<b>Atmosphere:</b> "+atmosphereType+"; <b>Pressure:</b> "+atmospherePressure+"; <b>Hydrographics:</b> "+hydrographics+"%; ");
			buffer.append("<b>Temperature:</b> "+temperature+"; <b>Life:</b> "+lifeType);
			buffer.append("</p>\n");
		}
		
		if (population > 0) {
			// Civilisation
			GlossaryFactory		glossary = new GlossaryFactory();
			String				text = null;
			String[]			notes = null;
			format.setGroupingUsed(true);
			buffer.append("<p><b>Population:</b> "+format.format(population)+"</p>\n");
			// Government
			buffer.append("<p><b>Government:</b> "+government+"</p>\n");
			notes = factory.getNotes(id, "government");
			if (notes == null || notes.length == 0) {
				text = getGlossaryText(glossary, "government-"+government.toString());
				if (text != null) buffer.append("<p><i>"+text+"</i></p>");
			} else {
				for (String p : notes) {
					buffer.append("<p><i>"+p+"</i></p>");
				}
			}
			// Law level
			buffer.append("<p><b>Law level:</b> "+lawLevel+"</p>\n");
			notes = factory.getNotes(id, "law");
			if (notes == null || notes.length == 0) {
				text = getGlossaryText(glossary, "law-"+lawLevel);
				if (text != null) buffer.append("<p><i>"+text+"</i></p>");				
			} else {
				for (String p : notes) {
					buffer.append("<p><i>"+p+"</i></p>");
				}
			}
			// Tech level
			buffer.append("<p><b>Tech level:</b> "+techLevel+"</p>\n");
			notes = factory.getNotes(id, "tech");
			if (notes == null || notes.length == 0) {
				text = getGlossaryText(glossary, "tech-"+techLevel);
				if (text != null) buffer.append("<p><i>"+text+"</i></p>");				
			} else {
				for (String p : notes) {
					buffer.append("<p><i>"+p+"</i></p>");
				}
			}
			// Starport
			buffer.append("<p><b>Starport:</b> "+starport+"</p>\n");
			notes = factory.getNotes(id, "starport");
			if (notes == null || notes.length == 0) {
				text = getGlossaryText(glossary, "starport-"+starport);
				if (text != null) buffer.append("<p><i>"+text+"</i></p>");
			} else {
				for (String p : notes) {
					buffer.append("<p><i>"+p+"</i></p>");
				}
			}
			glossary.close();
		}

		buffer.append("<table width=\"100%\" border=\"0\"><tr><td style=\"background-color:black\">");
		if (factory == null) factory = new ObjectFactory();
		if (factory.hasPlanetGlobe(id)) {
			int		imageSize = (int)(Math.pow(getRadius(), 0.3)*8);
			buffer.append("<a href=\""+Config.getBaseUrl()+"planet/"+id+".jpg\">");
			buffer.append("<img border=\"0\" src=\""+Config.getBaseUrl()+"planet/"+id+".jpg?globe\" width=\""+imageSize+"\" height=\""+imageSize+"\"/>");
			buffer.append("</a>");
		}
		buffer.append("</td><td valign=\"top\">"+getDescription()+"</td></tr></table>");
		
		// Moons?
		if (factory == null) factory = new ObjectFactory();
		Vector<Planet> moons = factory.getMoons(id);
		for (Planet moon : moons) {
			buffer.append("<div class=\"moon\">\n");
			buffer.append(moon.toHTML());
			buffer.append("</div>");
		}

		return buffer.toString();
	}
		
	private String getGlossaryText(GlossaryFactory glossary, String uri) {
		String		text = null;
		try {
			GlossaryEntry entry = glossary.getEntry(uri.toLowerCase());
			text = entry.getText();
		} catch (GlossaryException e) {
			text = null;
		}
		return text;
	}
	
	/**
	 * Gets a textual description about this planet. The description
	 * consists of an HTML string, and should be less than 64K.
	 * 
	 * @return		Description as HTML string.
	 */
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String text) {
		this.description = text;
	}
	
	public void setTradeCodes(String codes) {
		this.tradeCodes = codes;
	}
	
	/**
	 * Add the specified trade code to this planet's list of codes, if it
	 * doesn't already exist. If a code already exists, no change is made.
	 */
	public void addTradeCode(TradeCode code) {
		if (!hasTradeCode(code)) {
			tradeCodes += " "+code.toString();
			tradeCodes = tradeCodes.trim();
		}
	}
	
	/**
	 * Remove the specified trade code if this planet has it. If the
	 * planet does not already have the code, nothing is done.
	 */
	public void removeTradeCode(TradeCode code) {
		if (hasTradeCode(code)) {
			tradeCodes = tradeCodes.replaceAll(code.toString(), "");
			tradeCodes = tradeCodes.replaceAll("  ", "");
		}
	}
	
	public long getIdealPopulation() {
		long			ideal = getRadius() * getRadius();
		Habitability	habitable = planetType.getHabitability();
		
		if (planetType.isJovian()) {
			ideal = 0;
		} else if (planetType.isBelt()) {
			ideal = 0;
		}
		
		ideal *= habitable.getModifier() * temperature.getSuitability() * atmosphereType.getSuitability() * atmospherePressure.getSuitability();
		
		switch (techLevel) {
		case 0:
			ideal *= 1;
			break;
		case 1: case 2:
			ideal *= 10;
			break;
		case 3: case 4:
			ideal *= 25;
			break;
		case 5: case 6:
			ideal *= 75;
			break;
		case 7: case 8:
			ideal *= 100;
			break;
		case 9: case 10:
			ideal *= 200;
			break;
		case 11: case 12:
			ideal *= 300;
			break;
		default:
			ideal *= 400;
		}
		
		return ideal;
	}
	
	/**
	 * The World Trade Number rates both the size of a world' economy
	 * and its tendency to engage in interstellar trade.
	 * 
	 * @return		World Trade Number.
	 */
	public double getWTN() {
		// WTN is based on the population of the planet.
		double		wtn = Math.log10(population)/2.0;
		
		// Next, modify according to tech level.
		switch (techLevel) {
		case 0: case 1: case 2:
			wtn -= 0.5;
			break;
		case 3: case 4: case 5:
			break;
		case 6: case 7: case 8:
			wtn += 0.5;
			break;
		case 9: case 10: case 11:
			wtn += 1.0;
			break;
		default:
			wtn += 1.5;
		}
		
		wtn = starport.getModifiedWTN(wtn);

		return wtn;
	}
	
	public long getGWP() {
		long	bpci = 0;
		long[]	table = new long[] { 55, 85, 135, 229, 350, 560, 895,
				                     1430, 2290, 3660, 5860, 9375,
				                     15000, 24400 };
		
		if (techLevel < 0) {
			bpci = table[0];
		} else if (techLevel >= table.length) {
			bpci = table[table.length-1];
		} else {
			bpci = table[techLevel];
		}
		
		if (hasTradeCode(TradeCode.Ri)) bpci *= 1.6;
		if (hasTradeCode(TradeCode.In)) bpci *= 1.4;
		if (hasTradeCode(TradeCode.Ag)) bpci *= 1.2;
		if (hasTradeCode(TradeCode.Po)) bpci *= 0.8;
		if (hasTradeCode(TradeCode.Ni)) bpci *= 0.8;
		if (hasTradeCode(TradeCode.Re)) bpci *= 0.8;
		
		return bpci * population;
	}
	
	/**
	 * Terraform the planet.
	 *
	 */
	public void terraform() {
		
	}
	
	private Hashtable<String,Integer>		resources = null;
	
	/**
	 * Add to the list of resources for this world. Currently, this is a
	 * write-only list. If any resources are set, they are written to the
	 * database when the world is persisted, overwriting any resources
	 * already defined. It is expected that the data is processed elsewhere.
	 * The resource name must match one of the defined commodities, though
	 * checking is only done when the list is persisted.
	 * 
	 * @param resource		Name of resource.
	 * @param value			Quantity of resource, from 1 to 10.
	 */
	public void addResource(String resource, int value) {
		if (resources == null) {
			resources = new Hashtable<String,Integer>();
		}
		resources.put(resource, value);
	}
}
