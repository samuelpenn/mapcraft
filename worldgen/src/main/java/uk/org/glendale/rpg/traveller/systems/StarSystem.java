/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.18 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.util.*;

import uk.org.glendale.utils.Options;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.StarportType;
import uk.org.glendale.worldgen.astro.star.StarClass;
import uk.org.glendale.worldgen.astro.star.StarForm;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.rpg.utils.*;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.*;
import uk.org.glendale.rpg.traveller.sectors.Allegiance;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;

/**
 * Defines a single star system in the Traveller universe. The system may be
 * defined using the .sec data file. There will be multiple planets within
 * a star system (and possibly multiple stars as well). The principal world
 * is the one which has the largest starport. If both starports are the
 * same, it is the one with the greatest GWP.
 * 
 * @author Samuel Penn
 */
public class StarSystem implements Comparable {
	ObjectFactory			factory = null;
	private String			name = null;
	private int				id = 0;
	private int				sectorId = 0;
	private int				x = 0;
	private int				y = 0;
	private Vector<Star>	stars = new Vector<Star>();
	private Vector<Planet>	planets = new Vector<Planet>();
	private String			allegiance = null;
	private Zone			zone = Zone.Green;
	private String			base = null;
	private String			uwpLine = null;
	
	public int getId() {
		return id;
	}
	
	public int getSectorId() {
		return sectorId;
	}
	
	/**
	 * Get the name of this star system.
	 * 
	 * @return		Name, as a string.
	 */
	public String getName() {
		if (name != null) {
			return name;
		} else {
			return "";
		}
	}
	
	public int getX() {
		return x;
	}
	
	public String getXAsString() {
		if (x < 10) {
			return "0"+x;
		}
		return ""+x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getYAsString() {
		if (y < 10) {
			return "0"+y;
		}
		return ""+y;
	}
	
	public Vector<Star> getStars() {
		return stars;
	}
	
	public Vector<Planet> getPlanets() {
		return planets;
	}
	
	public String getAllegiance() {
		return allegiance;
	}
	
	/**
	 * Get the full details of this system's allegiance.
	 * 
	 * @return		Full allegiance information.
	 */
	public Allegiance getAllegianceData() {
		try {
			return new Allegiance(allegiance);
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}
	
	public void setAllegiance(String allegiance) {
		this.allegiance = allegiance;
	}
	
	/**
	 * Get the UWP for this system. This may be from original data files,
	 * otherwise one is generated based on existing data.
	 * 
	 * @return		Traveller style UWP.
	 */
	public String getUWP() {
		if (uwpLine != null && uwpLine.length() > 0) {
			return uwpLine;
		} else {
			return null;
		}
	}
	
	/**
	 * Set the original UWP for this system. UWPs are limited to
	 * a length of 80 characters, so anything longer is truncated.
	 * This is now remembered so that we can regenerate the system
	 * easily at a later date.
	 * 
	 * @param uwp		The original Traveller style UWP.
	 */
	public void setUWP(String uwp) {
		if (uwp.length() > 80) uwp = uwp.substring(0, 80);
		this.uwpLine = uwp;
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public void setZone(Zone zone) {
		this.zone = zone;;
	}
	
	/**
	 * Instantiate a StarSystem object from the database. Information about
	 * stars and planets is not created by this constructor.
	 * 
	 * @param rs		ResultSet containing star system data.
	 * @throws SQLException
	 */
	public StarSystem(ResultSet rs) throws SQLException {
		read(rs);
	}
	
	public StarSystem(ObjectFactory factory, ResultSet rs) throws SQLException {
		this.factory = factory;
		read(rs);
	}
	
	private void read(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		sectorId = rs.getInt("sector_id");
		x = rs.getInt("x");
		y = rs.getInt("y");
		allegiance = rs.getString("allegiance");
		zone = Zone.valueOf(rs.getString("zone"));
		base = rs.getString("base");
		uwpLine = rs.getString("uwp");
	}
	
	public void regenerate() {
		System.out.println(uwpLine);
		UWP		uwp = new UWP(uwpLine);
		
		parseStarData(uwp);
		parseAllegiance(uwp);
		parseBase(uwp);
		parseZone(uwp);
		
		generatePlanets(uwp);
		// Now see if any of the other stars in this system have planets.
		for (int i=1; i < stars.size(); i++) {
			if (Die.d6() > i*2) {
				generatePlanets(i, 1);
			}
		}
		populateSystem();
		persist();		
	}
	
	public void setStars(Vector<Star> stars) {
		this.stars = stars;
	}
	
	public void setPlanets(Vector<Planet> planets) {
		this.planets = planets;
	}
	
	/**
	 * Does this system contain at least one planet with land life?
	 */
	public boolean hasLife() {
		return hasLife(LifeType.SimpleLand);
	}
	
	/**
	 * Does this system contain at least one planet with the level of
	 * life specified.
	 * 
	 * @param level	Minimum level of life required.
	 * @return			True if life is present to the required level.
	 */
	public boolean hasLife(LifeType level) {
		// First, make sure we have enough information about this star system.
		if (stars.size() == 0) {
			ObjectFactory		fac = new ObjectFactory();
			stars = fac.getStarsBySystem(this.id);
			planets = fac.getPlanetsBySystem(this.id);
			fac.close();
		}
		
		Iterator<Planet>	i = planets.iterator();
		while (i.hasNext()) {
			Planet		planet = i.next();
			if (planet.getLifeLevel().compareTo(level) >= 0) {
				return true;
			}
		}
			
		return false;
	}
	
	public boolean hasWater(int hydrographics) {
		if (stars.size() == 0) {
			ObjectFactory		fac = new ObjectFactory();
			stars = fac.getStarsBySystem(this.id);
			planets = fac.getPlanetsBySystem(this.id);
			fac.close();
		}
		
		Iterator<Planet>	i = planets.iterator();
		while (i.hasNext()) {
			Planet		planet = i.next();
			if (planet.getHydrographics() >= hydrographics) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the best starport in this system.
	 * 
	 * @return		Best starport, or X if there are none.
	 */
	public StarportType getBestStarport() {
		StarportType		starport = StarportType.X;

		if (stars.size() == 0) {
			ObjectFactory		fac = new ObjectFactory();
			stars = fac.getStarsBySystem(this.id);
			planets = fac.getPlanetsBySystem(this.id);
			fac.close();
		}
		
		for (Planet planet : planets) {
			if (planet.getStarport() != null && planet.getStarport().compareTo(starport) < 0) {
				starport = planet.getStarport();
			}
		}

		return starport;
	}
	
	/**
	 * Get the main world of this star system. The main world is the
	 * one with the largest star port. Failing that, it is the world
	 * with the greatest GWP. If no worlds are populated, then it is
	 * the 'nicest' world.
	 * 
	 * @return		Main world of this star system.
	 */
	public Planet getMainWorld() {
		StarportType		starport = StarportType.X;
		Planet				mainWorld = null;

		if (stars.size() == 0) {
			ObjectFactory		fac = new ObjectFactory();
			stars = fac.getStarsBySystem(this.id);
			planets = fac.getPlanetsBySystem(this.id);
			fac.close();
		}
		
		for (Planet planet : planets) {
			if (planet.getStarport() == null || planet.getPopulation() == 0) {
				continue;
			}
			if (mainWorld == null) {
				mainWorld = planet;
				starport = planet.getStarport();
			} else 	if (planet.getStarport().compareTo(starport) < 0) {
				starport = planet.getStarport();
				mainWorld = planet;
			} else if (planet.getStarport() == starport) {
				if (planet.getGWP() > mainWorld.getGWP()) {
					mainWorld = planet;
				}
			}
		}
		if (mainWorld == null) {
			// Possible that there are no inhabited planets in this system,
			// in which case we need to choose a main world based on other
			// criteria.
			for (Planet planet : planets) {
				if (mainWorld == null) {
					mainWorld = planet;
				} else if (planet.getHabitability().isBetterThan(mainWorld.getHabitability())) {
					mainWorld = planet;
				}
			} 
		}

		return mainWorld;
	}
	
	/**
	 * Do any of the planets in this system have the requested trade code?
	 * If any of the planets do, then true is returned, otherwise false.
	 * Does not provide any indication of the number of planets that have this
	 * code, so it's possible to get strange results (e.g., both Desert and Water World
	 * being returned) due to multiple planets in the system.
	 * 
	 * @param 	code		Trade code to check for.
	 * @return				True if a planet has the code.
	 */
	public boolean hasTradeCode(TradeCode code) {
		if (stars.size() == 0) {
			ObjectFactory		fac = new ObjectFactory();
			stars = fac.getStarsBySystem(this.id);
			planets = fac.getPlanetsBySystem(this.id);
			fac.close();
		}
		Iterator<Planet>	i = planets.iterator();
		while (i.hasNext()) {
			Planet		planet = i.next();
			if (planet.hasTradeCode(code)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasNavalBase() {
		if (base != null && base.equalsIgnoreCase("N")) {
			return true;
		}
		return false;
	}
	
	public boolean hasScoutBase() {
		if (base != null && base.equalsIgnoreCase("S")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Create a new star system according to a sec profile.
	 * e.g. "Saregon       2936 A584976-E    Hi                 114 Im M3 V M6 D"
	 * @param sec		String defining the star system.
	 */
	public StarSystem(ObjectFactory factory, int sectorId, UWP uwp) {
		this.factory = factory;
		this.sectorId = sectorId;
		this.name = uwp.getName();
		this.uwpLine = uwp.toString();
		
		if (name == null || name.trim().length() == 0) {
			int				ssx = (uwp.getX()%8 == 0)?8:uwp.getX()%8;
			int				ssy = (uwp.getY()%10 == 0)?10:uwp.getY()%10;
			int				number = ssx*100 + ssy;
			name = Sector.getSubSector(uwp.getX(), uwp.getY()).toString()+number;
		}
		
		parseCoords(uwp);
		
		clean();
		persist();
		
		parseStarData(uwp);
		parseAllegiance(uwp);
		parseBase(uwp);
		parseZone(uwp);
		
		generatePlanets(uwp);
		// Now see if any of the other stars in this system have planets.
		for (int i=1; i < stars.size(); i++) {
			if (Die.d6() > i*2) {
				generatePlanets(i, 1);
			}
		}
		populateSystem();
		persist();
	}
	
	private void parseCoords(UWP uwp) {
		x = uwp.getX();
		y = uwp.getY();
	}
	
	private void parseBase(UWP uwp) {
		
	}
	
	private void parseZone(UWP uwp) {
		String		zone = uwp.getZone();
		
		if (zone == null || zone.length() == 0 || zone.equals(" ") || zone.equals("G")) {
			setZone(Zone.Green);
		} else if (zone.equals("A")) {
			setZone(Zone.Amber);
		} else if (zone.equals("R")) {
			setZone(Zone.Red);
		}
	}
	
	/**
	 * Read all star data out of the data string.
	 * 
	 * @param sec
	 */
	private void parseStarData(UWP uwp) {
		String				starData = uwp.getStarData();
		StringTokenizer		tokens = new StringTokenizer(starData, " ");
		String[]			names = { "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa" };
		int					i = 0;
		int					distance = 0;
		int					parentId = 0;
		int					primaryId = 0;
		
		while (tokens.hasMoreTokens()) {
			String		hr = tokens.nextToken();
			if (!tokens.hasMoreTokens()) {
				break;
			}
			String		sc = tokens.nextToken();
			
			String		starName = name;
			if (i > 0 || tokens.hasMoreTokens()) {
				starName += " " + names[i++];
			}
			Star		star = new Star(factory, id, starName, hr, sc);
			if (i%4 == 0) {
				parentId = primaryId;
				distance = 200000 * i + Die.die(500000, i);
			}
			if (parentId > 0) {
				star.setParentId(parentId);
				star.setDistance(distance);
				distance /= 5;
			}
			stars.add(star);
			
			parentId = star.getId();
			if (primaryId == 0) {
				primaryId = parentId;
				distance = 5000 + star.getColdPoint()*5 + Die.die(star.getColdPoint(), 2);
			}
		}
		
		if (stars.size() == 0) {
			// No stars defined, or bad data. Create a random one.
			Star	star = new Star(factory, id, name);
			stars.add(star);
		}
	}
	
	private void parseAllegiance(UWP uwp) {
		String		code = uwp.getAllegiance();
		
		allegiance = code;
	}
	
	/**
	 * Read a star system from the database according to its id. Also reads
	 * all the star and planet information for this system.
	 * 
	 * @param id		Unique id of the star system to be fetched.
	 */
	public StarSystem(ObjectFactory factory, int id) throws ObjectNotFoundException {
		this.factory = factory;
		
		if (!read("id="+id)) {
			throw new ObjectNotFoundException("Could not find star system with id ["+id+"]");
		}
		stars = factory.getStarsBySystem(this.id);
		planets = factory.getPlanetsBySystem(this.id);
	}
	
	/**
	 * Read a star system from the database according to its name.
	 * 
	 * @param name		Name of the star system to be fetched.
	 * 
	 * @throws ObjectNotFoundException
	 */
	public StarSystem(ObjectFactory factory, String name) throws ObjectNotFoundException {
		this.factory = factory;
		if (!read("name='"+name+"'")) {
			throw new ObjectNotFoundException("Could not find star system with name ["+name+"]");
		}
		stars = factory.getStarsBySystem(this.id);
		planets = factory.getPlanetsBySystem(this.id);
	}

	private boolean read(String query) {
		ResultSet			rs = null;
		
		try {
			rs = factory.read("system", query);
			if (rs.next()) {
				read(rs);
			} else {
				return false;
			}
		} catch (SQLException e) {
			return false;
		} finally {
			try {
				if (rs != null) rs.close();
			} catch (SQLException e) {
				// Really don't care.
			}
		}
		
		return true;
	}
	
	public StarSystem(ObjectFactory factory, String name, int sectorId, int x, int y) {
		this.factory = factory;
		this.name = name;
		this.sectorId = sectorId;
		this.x = x;
		this.y = y;
		persist();

		createFromScratch(null, 1, 1);
	}
	
	/**
	 * Create a new star system, with random stars and stuff.
	 * 
	 * @param sectorId		Sector star system is found in.
	 * @param x			X position of star system (1..32)
	 * @param y			Y position of star system (1..40)
	 */
	public StarSystem(ObjectFactory factory, String name, int sectorId, int x, int y, Allegiance allegiance, int fudgeFactor, int tenacity) {
		this.factory = factory;
		this.name = name;
		this.sectorId = sectorId;
		this.x = x;
		this.y = y;
		this.allegiance = allegiance.getCode();
		persist();
		
		createFromScratch(allegiance, fudgeFactor, tenacity);
	}
	
	private void createFromScratch(Allegiance allegiance, int fudgeFactor, int tenacity) {
		int		numberOfStars = 0;
		
		// Generate the number of stars. Biased to single star systems, even
		// though this is unrealistic. Keeps it simple.
		switch (Die.d6(3)) {
		case 3: case 4: case 5:
			numberOfStars = 3;
			break;
		case 6: case 7: case 8:
			numberOfStars = 2;
			break;
		default:
			numberOfStars = 1;
			break;
		}
		
		Star		lastStar = null;
		String[]	suffix = { "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa" };
		for (int s = 0; s < numberOfStars; s++) {
			String		starName = name;
			if (numberOfStars > 1) {
				starName += " "+suffix[s];
			}
			Star		star = new Star(factory, id, starName);
			star.setDistance(0);
			star.setParentId(0);

			if (lastStar != null) {
				star.makeSmaller(lastStar);
				// This star orbits the previous star. Use a very large distance for the 2nd
				// star in the system (s==1), and a much smaller distance for the 3rd star (s==2)
				star.setParentId(lastStar.getId());
				star.setDistance((1000 * Die.d10(5))/(s*s*s*s));
			}
			stars.add(star);
			star.persist();
			
			lastStar = star;
		}
		
		for (int i=0; i < stars.size(); i++) {
			generatePlanets(i, fudgeFactor);
			for (Planet p : getPlanets()) {
				if (p.getParentId() == stars.elementAt(i).getId()) {
					p.sanityCheck(stars.elementAt(i));
				}
			}
		}
		colonise(tenacity, allegiance);
		
		this.uwpLine = new UWP(factory, this).toString();
		persist();
	}
	
	/**
	 * Try and colonise the main world of this system. The tenacity determines
	 * how hard the colonists try, and affects the population size.
	 * tenacity = 0, only the best worlds are colonised.
	 * tenacity = 1, hostile worlds are rejected.
	 * tenacity = 2+, most worlds are colonised.
	 * 
	 * @param tenacity
	 */
	private void colonise(int tenacity, Allegiance allegiance) {
		Planet		mainWorld = getMainWorld();
		
		if (mainWorld == null) return;
		
		if (tenacity < 1 && mainWorld.getType() != PlanetType.Gaian) {
			return;
		} else if (tenacity > 4) {
			tenacity = 4;
		}
		long		population = Die.d6(3+tenacity) * 100000;
		while (Die.d10() <= tenacity) population *= 10;
		if (tenacity == 0) population /= Die.d10(2);
		
		if (allegiance != null) {
			if (allegiance.getPopulationModifier() < 0) {
				population /= Die.d10(Math.abs(allegiance.getPopulationModifier()));
			} else if (allegiance.getPopulationModifier() > 0) {
				population *= Die.d10(allegiance.getPopulationModifier());
			}
		}

		int			techLevel = Die.d4() + 6;
		switch (mainWorld.getTemperature()) {
		case UltraCold: case ExtremelyCold:
		case UltraHot: case ExtremelyHot:
			if (tenacity < 2) return;
			population /= 100;
			break;
		case VeryCold: case VeryHot:
			if (tenacity < 1) return;
			population /= 10;
			break;
		case Cold: case Hot:
			population /= Die.d4();
			break;
		}
		switch (mainWorld.getAtmospherePressure()) {
		case None: case Trace:
			if (tenacity < 2) return;
			population /= 100;
			break;
		case VeryThin: case VeryDense: case SuperDense:
			if (tenacity < 1) return;
			population /= 10;
			break;
		case Thin:
			population /= Die.d4();
			break;
		}
		if (mainWorld.getAtmosphereType().getSuitability() < 0.5) {
			if (tenacity < 2) return;
			population /= 100;
		} else if (mainWorld.getAtmosphereType().getSuitability() < 0.8) {
			if (tenacity < 1) return;
			population /= Die.d8(2);
		}
		if (mainWorld.getHydrographics() < 10) {
			if (tenacity < 2) return;
			population /= 100;
		} else if (mainWorld.getHydrographics() < 25) {
			if (tenacity < 1) return;
			population /= 10;
		} else if (mainWorld.getHydrographics() < 50) {
			population /= Die.d4();
		} else if (mainWorld.getHydrographics() > 90) {
			if (tenacity < 1) return;
			population /= 100;
		}
		
		if (population > 1000) {
			mainWorld.setPopulation(100*((population+50)/100));
			if (population < 10000) techLevel = Math.min(7, techLevel);
			if (population < 250000) techLevel = Math.min(8, techLevel);
			if (population < 10000000) techLevel = Math.min(9, techLevel);
			mainWorld.setTechLevel(techLevel);
			
			if (allegiance != null) {
				
			}
			
			GovernmentType government = null;
			switch (Die.d6(3)) {
			case 3: case 4: case 5:
				government = GovernmentType.Balkanization;
				mainWorld.setLawLevel(Die.d3());
				break;
			case 6: case 7:
				government = GovernmentType.ParticipatingDemocracy;
				mainWorld.setLawLevel(Die.d3()+1);
				break;
			case 8: case 9: case 10: 
				government = GovernmentType.RepresentativeDemocracy;
				mainWorld.setLawLevel(Die.d3()+1);
				break;
			case 11: case 12:
				government = GovernmentType.Corporation;
				mainWorld.setLawLevel(Die.d6());
				break;
			case 13: case 14:
				government = GovernmentType.CivilService;
				mainWorld.setLawLevel(Die.d3()+3);
				break;
			case 15: case 16:
				government = GovernmentType.NonCharismaticLeader;
				mainWorld.setLawLevel(Die.d3()+2);
				break;
			case 17:
				government = GovernmentType.SelfPerpetuatingOligarchy;
				mainWorld.setLawLevel(Die.d3()+1);
				break;
			case 18:
				government = GovernmentType.Anarchy;
				mainWorld.setLawLevel(0);
				break;
			}
			if (allegiance != null && allegiance.getLawModifier() != 0) {
				mainWorld.setLawLevel(mainWorld.getLawLevel()+allegiance.getLawModifier());
				if (government != GovernmentType.Anarchy && mainWorld.getLawLevel() == 0) {
					// Only Anarchies have a law level of 1.
					mainWorld.setLawLevel(1);
				}
			}
			if (allegiance != null && allegiance.getTechModifier() != 0) {
				mainWorld.setTechLevel(mainWorld.getTechLevel()+allegiance.getTechModifier());
			}
			mainWorld.setGovernment(government);
			mainWorld.setStarport(StarportType.C);
			if (population < 10000) {
				mainWorld.setStarport(StarportType.D);
			} else if (population > 10000 && techLevel > 9) {
				mainWorld.setStarport(StarportType.B);
			}
			mainWorld.persist();
		}
	}
	
	private String getRoman(int number) {
		switch (number) {
		case 1: return "I";
		case 2: return "II";
		case 3: return "III";
		case 4: return "IV";
		case 5: return "V";
		case 6: return "VI";
		case 7: return "VII";
		case 8: return "VIII";
		case 9: return "IX";
		case 10: return "X";
		case 11: return "XI";
		case 12: return "XII";
		case 13: return "XIII";
		case 14: return "XIV";
		case 15: return "XV";
		}
		
		return "XXX";
	}
	
	/**
	 * Generate a list of planets in the system, based on the sec string.
	 * We assume that all systems have terrestrial worlds followed by Jovian
	 * worlds. This is simple, and isn't too different from Sol system.
	 * 
	 * @param sec		Full UWP line.
	 */
	private void generatePlanets(UWP uwp) {
		Star			star = stars.elementAt(0);
		int				terrestrial = uwp.getPlanetoidBelts() + 1;
		int				jovian = uwp.getGasGiants();
		int				kuiperian = Die.d4()/stars.size();
		int				planetNum = 1;
		int				increase = star.getInnerLimit();
		int				distance = star.getInnerLimit()+Die.die(increase, 2);
		PlanetFactory	planetFactory = new PlanetFactory(factory, star);
		boolean			mainWorldIsMoon = false;
		
		if (terrestrial <= 0) {
			terrestrial = 1;
		}
		
		if (distance < 10) {
			// There should be a limit to how close to the star to be.
			if (star.getStarForm() == StarForm.Star) {
				distance = 10 + Die.d10();
				increase = 10 + Die.d10(2);
			} else {
				// Stellar remanent, so assume it was a large star to star with,
				// or closer worlds have been destroyed.
				distance = 50 + Die.d10(5) * 5;
				increase = distance + Die.d10(2) - Die.d10(2);
			}
		}
		int		maxDistance = 0;
		if (stars.size() > 1) {
			// If there are other stars in this system, no planet can be more than 10% of
			// the distance to that star.
			maxDistance = stars.elementAt(1).getDistance() / 10;
		}
		
		int			bestDistance = star.getEarthDistance();
		int			closest = 0, nearestDistance = 100000000;
		
		if (bestDistance < 5) {
			// The star is too cold to support a suitable world, so assume that
			// the main world is actually around a gas giant.
			mainWorldIsMoon = true;
		}

		for (int i=0; i < terrestrial+(mainWorldIsMoon?0:1); i++) {
			Planet		planet = null;
			String		planetName = name+" "+getRoman(planetNum++);

			if (maxDistance > 0 && distance > maxDistance) {
				break;
			}
			
			// Generate a random planet.
			if (distance <= star.getEarthDistance()) {
				planet = planetFactory.getHotWorld(planetName, distance);
			} else {
				planet = planetFactory.getCoolWorld(planetName, distance);
			}
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);

			if (Math.abs(distance - bestDistance) < nearestDistance) {
				closest = i;
				nearestDistance = Math.abs(distance - bestDistance); 
			}
			
			distance += Die.die(increase, 2);
			increase *= 1.5;
		}
		
		if (!mainWorldIsMoon) {
			int			mainDistance = (planets.elementAt(closest).getDistance() + bestDistance) / 2;
			Planet		mainPlanet = new Planet(factory, id, uwp, star, name+" "+getRoman(closest+1), mainDistance);
			mainPlanet.setId(planets.elementAt(closest).getId());
			Description.setDescription(mainPlanet);
			mainPlanet.persist();
			planets.set(closest, mainPlanet);
			
			// Add descriptive notes for this world.
			Description d = new Description(mainPlanet);
			factory.addNote(mainPlanet.getId(), "tech", d.getDescription("techlevel.TL"+mainPlanet.getTechLevel()));
			factory.addNote(mainPlanet.getId(), "law", d.getDescription("lawlevel.LL"+mainPlanet.getLawLevel()));
			factory.addNote(mainPlanet.getId(), "government", d.getDescription("government."+mainPlanet.getGovernment()));
			factory.addNote(mainPlanet.getId(), "starport", d.getDescription("starport."+mainPlanet.getStarport()));
			
			if (mainDistance > distance) {
				distance = mainDistance;
			}
		}

		distance+=100;
		
		int		mainJovian = 0;
		boolean	largeJovian = false;
		if (mainWorldIsMoon) {
			Planet		planet = null;
			String		planetName = name+" "+getRoman(planetNum++);
			
			largeJovian = true;
			distance *= 1.5;
			
			planet = planetFactory.getLargeJovian(planetName, distance);
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			distance += increase + Die.die(increase, 2);
			increase *= 2;
			jovian--;

			mainJovian = planet.getId();
		}
		
		for (int i=0; i < jovian; i++) {
			Planet		planet = null;
			String		planetName = name+" "+getRoman(planetNum++);

			if (maxDistance > 0 && distance > maxDistance) {
				break;
			}
			
			if (largeJovian) {
				// There should really only be one large jovian in the system.
				planet = planetFactory.getSmallJovian(planetName, distance);
			} else {
				planet = planetFactory.getColdJovian(planetName, distance);
				switch (planet.getType()) {
				case EuJovian:
				case SuperJovian:
				case MacroJovian:
					largeJovian = true;
					break;
				}
			}
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			distance += increase + Die.die(increase, 2);
			increase *= 2;
		}
		
		if (distance >= star.getColdPoint()) {
			// Cold icy worlds in the Kuiper belt.
			for (int i=0; i < kuiperian; i++) {
				Planet		planet = null;
				String		planetName = name+" "+getRoman(planetNum++);
	
				if (maxDistance > 0 && distance > maxDistance) {
					break;
				}
				
				planet = planetFactory.getIceWorld(planetName, distance);
				Description.setDescription(planet);
				planet.persist();
				planets.add(planet);
				distance += increase + Die.die(increase, 2);
				increase *= 3;			
			}
		}

		// Having finished all the major worlds, now create any required moons.
		for (Planet planet : planets) {
			if (planet.getMoonCount() > 0) {
				Planet[]	moons = planetFactory.getMoons(planet, planet.getMoonCount());
				
				if (planet.getId() == mainJovian && moons.length > 0) {
					// Need to make one of these moons the main one.
					int		moonId = moons[0].getId();
					Planet		mainMoon = new Planet(factory, id, uwp, star, moons[0].getName(), moons[0].getDistance(), planet.getTemperature());
					mainMoon.setParentId(mainJovian);
					mainMoon.setId(moonId);
					Description.setDescription(mainMoon);
					mainMoon.persist();
					moons[0] = mainMoon;
					
					// Add descriptive notes for this world.
					Description d = new Description(mainMoon);
					factory.addNote(mainMoon.getId(), "tech", d.getDescription("techlevel.TL"+mainMoon.getTechLevel()));
					factory.addNote(mainMoon.getId(), "law", d.getDescription("lawlevel.LL"+mainMoon.getLawLevel()));
					factory.addNote(mainMoon.getId(), "government", d.getDescription("government."+mainMoon.getGovernment()));
					factory.addNote(mainMoon.getId(), "starport", d.getDescription("starport."+mainMoon.getStarport()));
				}
				
				for (Planet moon : moons) {					
					Description.setDescription(moon);
					moon.persist();
				}
				// The Moon count is simply a marker to tell us to create moons for this
				// world. Since this code is called once for each star, we can end up
				// coming through here multiple times. Set the moon count to zero, so each
				// planet only has moons generated for it once.
				planet.setMoonCount(0);
			}
		}
	
	}
	
	/**
	 * Generate completely random planets for this star system. Does not use an
	 * existing UWP, so not guaranteed to have habitable planets.
	 */
	private void generatePlanets(int starIndex, int fudgeFactor) {
		Star			star = stars.elementAt(starIndex);
		int				terrestrial = Die.d6()/stars.size() - starIndex;
		int				jovian = Die.d4()/stars.size() - starIndex;
		int				kuiperian = Die.d4()/stars.size();
		int				planetNum = 1;
		int				increase = star.getInnerLimit();
		int				distance = star.getInnerLimit()+Die.die(increase, 2);
		PlanetFactory	planetFactory = new PlanetFactory(factory, star);
		
		planetFactory.setFudgeFactor(fudgeFactor);
		
		if (distance < 10) {
			// There should be a limit to how close to the star a planet can be.
			if (star.getStarForm() == StarForm.Star) {
				distance = 10 + Die.d10();
				increase = 10 + Die.d10(2);
			} else {
				// Stellar remanent, so assume it was a large star to start with,
				// or closer worlds have been destroyed.
				distance = 50 + Die.d10(5) * 5;
				increase = distance + Die.d10(2) - Die.d10(2);
			}
		}
		int		maxDistance = 0;
		if (stars.size() > starIndex+1) {
			// If there are other stars in this system, no planet can be more than 10% of
			// the distance to that star.
			maxDistance = stars.elementAt(starIndex+1).getDistance() / 10;
		}
		
		// Epi-stellar jovian world?
		if (Die.d100() <= 10 && star.getStarClass().isBiggerThan(StarClass.VI)) {
			String	planetName = star.getName() + " "+getRoman(planetNum++);
			Planet	planet = planetFactory.getHotJovian(planetName, distance / 3);
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			distance += Die.die(increase, 4);
		}

		// Terrestrial worlds.
		int			bestDistance = star.getEarthDistance();
		boolean		alreadyFudged = false;
		for (int i=0; i < terrestrial+1; i++) {
			Planet		planet = null;
			String		planetName = null;

			if (maxDistance > 0 && distance > maxDistance) {
				break;
			}
			
			if (starIndex > 0) {
				planetName = star.getName() + " "+getRoman(planetNum++);
			} else {
				planetName = name+" "+getRoman(planetNum++);
			}
			
			// If fudging, try to create a world close to the optimum distance.
			if (!alreadyFudged && fudgeFactor > 0) {
				if (distance > bestDistance * 0.7 && distance < bestDistance * 2.0) {
					distance = (bestDistance * fudgeFactor + distance)/ (fudgeFactor + 1);
					alreadyFudged = true;
				}
			}
			
			// Generate a random planet.
			if (distance <= star.getEarthDistance()) {
				planet = planetFactory.getHotWorld(planetName, distance);
			} else {
				planet = planetFactory.getCoolWorld(planetName, distance);
			}
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			
			distance += Die.die(increase, 2);
			increase *= 1.5;
		}

		// Gas giants.
		distance+=100;
		for (int i=0; i < jovian; i++) {
			Planet		planet = null;
			String		planetName = null;

			if (maxDistance > 0 && distance > maxDistance) {
				break;
			}
			if (starIndex > 0) {
				planetName = star.getName() + " "+getRoman(planetNum++);
			} else {
				planetName = name+" "+getRoman(planetNum++);
			}

			planet = planetFactory.getColdJovian(planetName, distance);
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			distance += increase + Die.die(increase, 2);
			increase *= 2;
		}

		// Cold icy worlds in the Kuiper belt.
		for (int i=0; i < kuiperian; i++) {
			Planet		planet = null;
			String		planetName = null;

			if (maxDistance > 0 && distance > maxDistance) {
				break;
			}

			if (starIndex > 0) {
				planetName = star.getName() + " "+getRoman(planetNum++);
			} else {
				planetName = name+" "+getRoman(planetNum++);
			}

			planet = planetFactory.getIceWorld(planetName, distance);
			Description.setDescription(planet);
			planet.persist();
			planets.add(planet);
			distance += increase + Die.die(increase, 2);
			increase *= 3;			
		}
		
		// Having finished all the major worlds, now create any required moons.
		for (Planet planet : planets) {
			if (planet.getMoonCount() > 0) {
				Planet[]	moons = planetFactory.getMoons(planet, planet.getMoonCount());
				for (Planet moon : moons) {
					Description.setDescription(moon);
					moon.persist();
					// Do we need to add it to the list of planets?
					// planets.add(moon);
				}
				// The Moon count is simply a marker to tell us to create moons for this
				// world. Since this code is called once for each star, we can end up
				// coming through here multiple times. Set the moon count to zero, so each
				// planet only has moons generated for it once.
				planet.setMoonCount(0);
			}
		}
	}
	
	/**
	 * Once a star system has been generated, we probably have a single main
	 * world. Now need to think how the rest of the system is populated. There
	 * are probably mining colonies, small research outposts and general
	 * business and communities all over the place.
	 * 
	 * Types of communities:
	 *   Scientific research
	 *   Industrial research
	 *   Mining
	 *   Colony
	 *   Waystation
	 *   Military base
	 */
	private void populateSystem() {
		Planet		mainWorld = getMainWorld();
		if (mainWorld == null) {
			// No populated world, so don't bother.
			return;
		}
		int				techLevel = mainWorld.getTechLevel();
		StarportType	port = mainWorld.getStarport();
		long			population = mainWorld.getPopulation();
		boolean			localColonies = true;
		
		if (techLevel < 9) {
			localColonies = false;
		}
		switch (port) {
		case X: case E:
			population /= 100000;
			localColonies = false;
			break;
		case D:
			population /= 1000;
			localColonies = false;
			break;
		case C:
			population /= 10;
			break;
		}
		
		for (Planet p : planets) {
			if (p.getPopulation() > 0) {
				// Already populated, so ignore.
				continue;
			}
			if (p.getType().isJovian()) {
				// Gas giants aren't ever populated.
				continue;
			}
			
			if (p.getType().isBelt()) {
				// This is an asteroid belt. There will probably be mining.
				p.addTradeCode(TradeCode.Mi);
				if (localColonies) {
					p.setPopulation(population / Die.d6(2));
					p.setGovernment(mainWorld.getGovernment());
					p.setStarport(port.getWorse());
					p.setLawLevel(mainWorld.getLawLevel()-1);
					p.setTechLevel(techLevel);
					p.persist();
				} else {
					
				}
				
				continue;
			}
			if (p.getLifeLevel().isMoreComplexThan(LifeType.ComplexOcean)) {
				if (!p.hasTradeCode(TradeCode.Fl)) {
					if (localColonies) {
						p.setPopulation(population / Die.d6(4));
						p.setGovernment(mainWorld.getGovernment());
						p.setStarport(port.getWorse());
						p.setLawLevel(mainWorld.getLawLevel());
						p.setTechLevel(techLevel-1);
						p.persist();
					} else {
						
					}
					continue;
				}
			}
			
			// Base first decision on the temperature.
			switch (p.getTemperature()) {
			case UltraHot:
			case ExtremelyHot:
				break;
			case VeryHot:
			case Hot:
				break;
			case Cold:
			case VeryCold:
				break;
			case ExtremelyCold:
			case UltraCold:
				break;
			default:
				// Standard thing.
			}
		}
	}
	
	/**
	 * Store all data for this star system in the database. This includes any data on stars
	 * and planets associated with the system.
	 */
	public void persist() {
		Hashtable<String,Object>	data = new Hashtable<String,Object>();

		data.put("id", id);
		data.put("sector_id", sectorId);
		data.put("x", x);
		data.put("y", y);
		if (allegiance != null) {
			data.put("allegiance", allegiance);
		}
		data.put("name", name);
		data.put("zone", ""+zone);
		if (uwpLine != null) {
			data.put("uwp", uwpLine);
		}
		if (base != null) {
			data.put("base", base);
		}
		int		auto = factory.persist("system", data);
		// If stored for the first time, set our unique id.
		if (id == 0) id = auto;
		
		if (stars != null) {
			for (Iterator<Star> i = stars.iterator(); i.hasNext(); ) {
				Star		star = i.next();
				star.persist();
			}
		}
	}
	
	/**
	 * Look for a matching instance of this star system in the database, and if it
	 * exists, delete it. The match is performed on sector id and the x,y coordinate.
	 * All related star and planet data is also removed.
	 */
	public void clean() {
		factory.clean("system", "sector_id="+sectorId+" and x="+x+" and y="+y, 
				  new String[] { "star", "planet" });
	}
	
	private String plotLink(int x, int y, String style, String text, String link) {
		StringBuffer		buffer = new StringBuffer("<a style=\"");
		
		buffer.append("position: absolute; left: "+x+"px; top: "+y+"px; ");
		if (style != null) {
			buffer.append(style);
		}
		buffer.append("\" href=\"");
		buffer.append(link);
		buffer.append("\">");
		buffer.append(text);
		buffer.append("</a>");
		
		return buffer.toString();		
	}
	
	private String plotText(int x, int y, String style, String text) {
		StringBuffer		buffer = new StringBuffer("<span style=\"");
		
		buffer.append("position: absolute; left: "+x+"px; top: "+y+"px; ");
		if (style != null) {
			buffer.append(style);
		}
		buffer.append("\">");
		buffer.append(text);
		buffer.append("</span>");
		
		return buffer.toString();
	}

	private String plotImage(int x, int y, String style, String image, int width, int height) {
		StringBuffer		buffer = new StringBuffer("<img style=\"");
		
		buffer.append("position: absolute; left: "+x+"px; top: "+y+"px; ");
		if (style != null) {
			buffer.append(style);
		}
		buffer.append("\" src=\""+image+"\" width=\""+width+"\" height=\""+height+"\"/>");
		
		return buffer.toString();
	}
	
	/**
	 * Get the name of the image to use for this star, based on its
	 * spectral type (and hence colour).
	 * 
	 * @param star		Star to get image for.
	 * 
	 * @return			Relative path to the image file.
	 */
	private String getStarImage(Star star) {
		String		image = "images/star_";
		String		type = star.getSpectralType().toString().substring(0, 1).toLowerCase();
		
		return image+type+".png";
	}

	/**
	 * Display this star system as HTML. The HTML code is returned as a
	 * string that can be embedded in the page. Assumes absolute coordinates.
	 * 
	 * @return			Markup for this system.
	 */
	public String plotSymbols() {
		StringBuffer	buffer = new StringBuffer();
		Star			star = null;
		int				size = 16, baseSize = 12;
		int				xp = x * 55 - 28;
		int				yp = y * 64 - 16;
		
		if (x%2 == 0) {
			yp += 32;
		}
		
		String		link = "get?type=system&amp;format=xml&amp;id="+id;
		int			len = name.length()*2 - 8;
		buffer.append(plotLink(xp - len, yp, "font-size: x-small;", name, link));
		
		switch (stars.size()) {
		case 1:
			star = stars.elementAt(0);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2, yp-24, null, getStarImage(star), size, size));
			break;
		case 2:
			star = stars.elementAt(0);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2-8, yp-24, null, getStarImage(star), size, size));
			star = stars.elementAt(1);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2+8, yp-24, null, getStarImage(star), size, size));
			break;
		case 3:
			star = stars.elementAt(0);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2-8, yp-24-8, null, getStarImage(star), size, size));
			star = stars.elementAt(1);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2+8, yp-24-8, null, getStarImage(star), size, size));
			star = stars.elementAt(2);
			size = (int)(star.getSize() * baseSize);
			buffer.append(plotImage(xp+2, yp-24+8, null, getStarImage(star), size, size));
			break;
		}
		
		if (hasLife(LifeType.ComplexOcean)) {
			buffer.append(plotText(xp+24, yp-40, "font-size: small; color: green; font-weight: bold", "L"));
		} else if (hasLife(LifeType.Aerobic)) {
			buffer.append(plotText(xp+24, yp-40, "font-size: x-small; color: green;", "L"));
		}
		
		int		tl = getMaxTechLevel();
		int		pop = getMaxPopulation();
		if (tl > 0) {
			buffer.append(plotText(xp+24, yp-30, "font-size: x-small;", ""+tl));
		}
		if (pop > 0) {
			buffer.append(plotText(xp+24, yp-20, "font-size: x-small;", ""+pop));
		}
		
		// Now display info on each of the planets.
		int		plx = xp-8, ply = yp - 8;
		int		primaryId = stars.elementAt(0).getId();
		for (Planet p : planets) {
			String		image = "images/planet_rock.png";
			
			if (p.getParentId() != primaryId) {
				continue;
			}
			
			if (p.getType().isJovian()) {
				image = "images/planet_jovian.png";
			} else if (p.getLifeLevel().compareTo(LifeType.ComplexOcean) >= 0) {
				image = "images/planet_gaian.png";
			}
			
			buffer.append(plotImage(plx, ply, "border: none", image, 4, 4));
			plx += 4;
		}
		
		return buffer.toString();
	}

	/**
	 * Display this star system on a PostScript map.
	 * 
	 * @param map		Map to write output to.
	 */
	public void	plotSymbols(PostScript map, int xoff, int yoff) {
		double		sx, sy;
		double 		size = (int)(map.getScale()*0.15);
		Star		star = null;

		// Plot basic star information. How we do this depends
		// on the number of stars.
		switch (stars.size()) {
		case 1:
			star = (Star)(stars.elementAt(0));
			map.plotCircle(map.getX(x-1-xoff,y-1-yoff)+map.getScale()/2,
					(map.getY(x-1-xoff,y-1-yoff)-map.getScale()/1.2),
					(star.getSize()*size), star.getColour());
            break;
        case 2:
			sx = (map.getX(x-1-xoff,y-1-yoff)+map.getScale()*0.3);
			sy = (map.getY(x-1-xoff,y-1-yoff)-map.getScale()/1.2);
			star = (Star)(stars.elementAt(0));
			map.plotCircle(sx, sy, (star.getSize()*size), star.getColour());

            sx = (map.getX(x-1-xoff,y-1-yoff)+map.getScale()*0.7);
			star = (Star)(stars.elementAt(1));
			map.plotCircle(sx, sy, (star.getSize()*size), star.getColour());
            break;
        case 3:
			sx = (map.getX(x-1-xoff,y-1-yoff)+map.getScale()*0.3);
			sy = (map.getY(x-1-xoff,y-1-yoff)-map.getScale()/1.4);
			star = (Star)(stars.elementAt(0));
			map.plotCircle(sx, sy, (star.getSize()*size), star.getColour());

			sx = (map.getX(x-1-xoff,y-1-yoff)+map.getScale()*0.7);
			star = (Star)(stars.elementAt(0));
			map.plotCircle(sx, sy, (star.getSize()*size), star.getColour());

			sy = (map.getY(x-1-xoff,y-1-yoff)-map.getScale()/0.95);
			sx = (map.getX(x-1-xoff,y-1-yoff)+map.getScale()*0.5);
			star = (Star)(stars.elementAt(0));
			map.plotCircle(sx, sy, (star.getSize()*size), star.getColour());
			break;
        }
		// Plot textual star information.
		Enumeration		e = stars.elements();
		sx = (map.getX(x-1-xoff,y-1-yoff)-map.getScale()*0.3);
		sy = (map.getY(x-1-xoff,y-1-yoff)-map.getScale()*0.7);
		map.plotFont("Helvetica", map.getScale()/12.0);
		while (e.hasMoreElements()) {
			star = (Star) e.nextElement();
			map.plotText(sx, sy, star.getSpectralType()+" "+star.getStarClass());
			//map.plotText(sx, sy, "Hello");
			sy -= map.getScale()*0.2;
        }
				
		// Plot information on each planet in the system.
		sx = (map.getX(x-1-xoff, y-1-yoff)+map.getScale()*0.1);
		sy =  (map.getY(x-1-xoff, y-1-yoff)-map.getScale()*1.2);
		star = stars.elementAt(0);
		Iterator<Planet>	i = planets.iterator();

		String		base = null;
		int			maxTech = 0;
		long		maxPop = 0;
		boolean		cx = false, cp = false;
		String		rgb="0 0 0";
		while (i.hasNext()) {
			Planet		planet = i.next();
			String		type = planet.getType().getPlanetClass();
			
			if (planet.getParentId() != star.getId()) {
				// Only display the planets for the primary star.
				continue;
			}
			
			if (base == null && planet.getBase() != null) {
				base = planet.getBase();
			}
			if (planet.getPopulation() > maxPop) {
				maxPop = planet.getPopulation();
			}
			if(planet.getTechLevel() > maxTech) {
				maxTech = planet.getTechLevel();
			}
			switch (planet.getLifeLevel()) {
			case None: case Organic:
				rgb = "0 0 0";
				break;
			case Archaean: case Aerobic:
				rgb = "0 0.2 0";
				break;
			case ComplexOcean:
				rgb = "0 0.2 0";
				break;
			case SimpleLand:
				rgb = "0 0.5 0";
				break;
			case ComplexLand:
				rgb = "0 0.75 0";
				break;
			case Extensive:
				rgb = "0 1 0";
				break;
			}
			
			if (type.equals("Asteroid")) {
				map.plotRectangle(sx, sy, size*0.1, size*0.1, "0 0 0");
			} else if (type.equals("Terrestrial")) {
				map.plotRectangle(sx, sy, size*0.2, size*0.5, rgb);
			} else if (type.equals("Jovian")) {
				map.plotRectangle(sx, sy, size*0.3, size*1, "0.2 0 0");				
			} else {
				map.plotRectangle(sx, sy, size*0.1, size*0.2, "0 0 0");
			}
			sx += size * 0.75;
			
			for (String code : planet.getTradeCodes()) {
				if (code.equals("Cp")) {
					cp = true;
				} else if (code.equals("Cx")) {
					cx = true;
				}
			}
		}
		if (base != null) {
			map.plotFont("Helvetica", map.getScale()/9);
			sx = (map.getX(x-1-xoff, y-1-yoff)+map.getScale()*0.9);
			sy = (map.getY(x-1-xoff, y-1-yoff)-map.getScale()*0.5);
			map.plotText(sx, sy, base, "0 0 0");
		}
		if (maxPop > 0) {
			map.plotFont("Helvetica", map.getScale()/9);
			sx = (map.getX(x-1-xoff, y-1-yoff)+map.getScale()*0.9);
			sy = (map.getY(x-1-xoff, y-1-yoff)-map.getScale()*0.7);
			maxPop = (int)(Math.log10(maxPop));
			map.plotText(sx, sy, "P"+maxPop, "0 0 0");			
		}
		if (maxTech > 0) {
			map.plotFont("Helvetica", map.getScale()/9);
			sx = (map.getX(x-1-xoff, y-1-yoff)+map.getScale()*0.9);
			sy = (map.getY(x-1-xoff, y-1-yoff)-map.getScale()*0.9);
			maxPop = (int)(Math.log10(maxPop))+1;
			map.plotText(sx, sy, "T"+maxTech, "0 0 0");			
		}

		// Plot the name of the star system.
		map.plotFont("Helvetica", map.getScale()/9);
		sx = (map.getX(x-1-xoff, y-1-yoff)-map.getScale()*0);
		sy = (map.getY(x-1-xoff, y-1-yoff)-map.getScale()*1.6);
		switch (getZone()) {
		case Amber:
			rgb = "1.0 0.4 0";
			break;
		case Red:
			rgb = "1.0 0 0";
			break;
		default:
			rgb = "0 0 0";
		}
		if (cx) {
			map.plotFont("Times", size);
			map.plotText(sx, sy, getName().toUpperCase(), rgb);
		} else if (cp) {
			map.plotFont("Helvetica-Bold", size);
			map.plotText(sx, sy, getName(), rgb);			
		} else {
			map.plotText(sx, sy, getName(), rgb);
		}
	}

	private static int
	stepSize(double d) {
		int		s = 1;

		if (d < 5) {
			s = 1;
        } else if (d < 10) {
			s = 2;
        } else if (d < 25) {
			s = 5;
        } else if (d < 50) {
			s = 10;
        } else if (d < 100) {
			s = 20;
        } else if (d < 200) {
			s = 50;
        } else if (d < 500) {
			s = 100;
        } else if (d < 1000) {
			s = 200;
        } else if (d < 2000) {
			s = 500;
        } else if (d < 5000) {
			s = 1000;
        } else if (d < 10000) {
			s = 2000;
        } else {
			s = 5000;
        }

		return s;
    }
	
	public String getDescription() {
		StringBuffer		buffer = new StringBuffer();
		
		// Brief physical description.
		buffer.append(getName()+" is a");
		Allegiance	data = getAllegianceData();
		if (data != null) {
			if (data.getName().matches("[AEIOUaeiou].*")) {
				buffer.append("n");
			}
			buffer.append(" "+data.getName());
		}
		buffer.append(" system of "+stars.size()+" star");
		if (stars.size() > 1) buffer.append("s");
		buffer.append(" and "+planets.size()+" planet");
		if (planets.size() > 1) buffer.append("s");
		buffer.append(". ");
		
		// Say something about the travel zone.
		switch (zone) {
		case Amber:
			buffer.append("This system is designated as amber, and should be treated with caution. ");
			break;
		case Red:
			buffer.append("This system is a Red Zone, and should be avoided if at all possible. ");
			break;
		}
		
		return buffer.toString();
	}
	
	public String toString() {
		return name + "("+x+","+y+") "+allegiance;
	}
	
	/**
	 * Get the XML representation of this star system as a string. XML does not
	 * contain the header line (<?xml...) and is pretty printed.
	 * 
	 * @return		String containing XML fragment.
	 */
	public String toXML() {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<system xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\" ");
		buffer.append("id=\"").append(id).append("\" name=\"").append(name).append("\" ");
		buffer.append("x=\"").append(x).append("\" y=\"").append(y).append("\" ");
		buffer.append("sector=\"").append(sectorId).append("\">\n");
		if (getMainWorld() != null) {
			buffer.append("  <mainWorld>"+getMainWorld().getId()+"</mainWorld>\n");
		}

		buffer.append("  <allegiance>");
		Allegiance		a = getAllegianceData();
		if (a != null) {
			buffer.append(a.getName());
		} else {
			buffer.append(allegiance);
		}
		buffer.append("</allegiance>");
		
		int		shipCount = factory.countShipsInSystem(id);
		if (shipCount > 0) {
			buffer.append("  <ships count=\""+shipCount+"\">\n");
			buffer.append("  </ships>\n");
		}
		
		if (stars == null && planets == null) {
			// If no stars and planets, just output basic system info.
		} else {
			if (stars != null) {
				Iterator<Star>		i = stars.iterator();
				
				while (i.hasNext()) {
					Star		star = i.next();
					buffer.append(star.toXML());
				}
			}
			if (planets != null) {
				Iterator<Planet>		i = planets.iterator();
				
				while (i.hasNext()) {
					Planet		planet = i.next();
					buffer.append(planet.toXML());
				}
			}
		}
		buffer.append("</system>\n");

		return buffer.toString();
	}
	
	/**
	 * Output the system information as a page of HTML.
	 * 
	 * @return		String containing HTML body content.
	 */
	public String toHTML() {
		return toHTML(true);
	}
	
	/**
	 * Comparable class so we can sort bilateral trade numbers for lots
	 * of systems. Returns things sorted in greatest BTN first.
	 */
	private class BTN implements Comparable {
		StarSystem		system = null;
		double			btn = 0.0;
		
		BTN(StarSystem system, double btn) {
			this.system = system;
			this.btn = btn;
		}
		
		double getBTN() {
			return btn;
		}
		
		StarSystem getStarSystem() {
			return system;
		}

		public int compareTo(Object o) {
			if (o instanceof BTN) {
				BTN		b = (BTN)o;
				if (getBTN() < b.getBTN()) {
					return +1;
				} else if (getBTN() > b.getBTN()) {
					return -1;
				} else {
					return getStarSystem().compareTo(b.getStarSystem());
				}
			}
			return 0;
		}
	}
	
	public String toHTML(String property) {
		StringBuffer		buffer = new StringBuffer();
		Sector				sector = null;
		
		try {
			sector = new Sector(factory, getSectorId());
		} catch (ObjectNotFoundException e) {
			sector = null;
		}
		/*
		 * Display trade information if this system has a populated world.
		 */
		Planet		mainWorld = getMainWorld();
		if (mainWorld != null && sector != null) {
			buffer.append("<div id=\"trade\">");
			buffer.append("<h2>Trade Details</h2>");
			
			buffer.append("<table>");
			buffer.append("<tr><th>System</th><th>Distance</th><th>BTN</th><th>Cr/Year</th><th>Dt/Y</th><th>Dt/Wk</th><th>Dt/Day</th></tr>");
			
			TreeSet<BTN>	list = new TreeSet<BTN>();

			int		area = 6;
			for (int sy = getY() - area; sy < getY()+area; sy++) {
				for (int sx = getX() - area; sx < getX()+area; sx++) {
					StarSystem	s2 = sector.getSystem(sx, sy);
					if (s2 == null) continue;
					if (s2.getId() == getId()) continue;
					
					double		btn = sector.getBTN(getMainWorld(), s2.getMainWorld());
					
					list.add(new BTN(s2, btn));
				}
			}

			int		max = 5;
			for (BTN b : list) {
				StarSystem  s2 = b.getStarSystem();
				double 		btn = b.getBTN();
				long		credits = (long)Math.pow(10, btn);
				int			year = 0;
				int			week = 0;
				int			day = 0;
				
				if (btn >= 4.0) {
					year = (int)Math.pow(10, btn-4.0);
				}
				if (btn >= 5.5) {
					week = (int)Math.pow(10, btn-5.5);
				}
				if (btn >= 6.5) {
					day = (int)Math.pow(10, btn-6.5);
				}
				String		cr = null;
				if (credits > 1000000000000L) {
					credits /= 1000000000;
					cr = ""+(credits/1000.0)+" TCr";
				} else if (credits > 1000000000) {
					credits /= 1000000;
					cr = ""+(credits/1000.0)+" GCr";
				} else if (credits > 1000000) {
					credits /= 1000;
					cr = ""+(credits/1000.0)+" MCr";
				} else if (credits > 1000) {
					cr = ""+(credits/1000.0)+" KCr";
				} else {
					cr = ""+credits + " Cr";
				}
				buffer.append("<tr>");
				buffer.append("<td><a href=\""+Config.getBaseUrl()+"system/"+s2.getId()+".html\">"+s2.getName()+"</a></td>");
				buffer.append("<td>"+(int)(sector.getDistance(getMainWorld(), s2.getMainWorld()))+"pc</td>");
				buffer.append("<td>"+((int)(btn*10)/10.0)+"</td>");
				buffer.append("<td>"+cr+"</td>");
				buffer.append("<td>"+year+"</td>");
				buffer.append("<td>"+week+"</td>");
				buffer.append("<td>"+day+"</td>");
				buffer.append("</tr>");
				if (--max < 1) break;
			}
			buffer.append("</table>");
			
			buffer.append("</div>");
		}
		return buffer.toString();
	}
	
	public String toHTML(boolean header) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = Config.getBaseUrl()+"css/systems.css";
		Sector  			sector = null;
		
		try {
			sector = new Sector(factory, getSectorId());
		} catch (ObjectNotFoundException e) {
			// No sector defined.
		}
		
		if (header) {
			buffer.append("<html>\n<head>\n<title>"+getName()+" System</title>\n");
			buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
	        buffer.append("<script type=\"text/javascript\" src=\""+Config.getBaseUrl()+"scripts/system.js\"></script>\n");
			buffer.append("</head><body>\n");
		}
		
		buffer.append("<div id=\"header\">\n");
		buffer.append("<h1>"+getName()+"</h1>\n");
		
		buffer.append("<p>\n");
		if (sector != null) {
			String		url = Config.getBaseUrl()+"map.jsp?id="+sector.getId();
			buffer.append("<a href=\""+url+"\">"+sector.getName()+"</a> / "+sector.getSubSectorName(getX(), getY())+" - "+getXAsString()+getYAsString());
		}
		if (getAllegianceData() != null) {
			buffer.append(" ("+getAllegianceData().getName()+")");
		}
		if (getZone() != Zone.Green) {
			buffer.append(" / "+getZone().toString());
		}
		buffer.append("</p>\n");
		buffer.append("</div>\n");
		// Simple map of the whole solar system.
		Vector<Planet>	planets = factory.getPlanetsBySystem(getId());
		buffer.append("<div id=\"map\">\n");
		int		lastX = 0, x = 0;
		for (Star star : getStars()) {
			String		image = Config.getBaseUrl()+"images/stars/"+star.getSpectralType().toString().substring(0, 1)+".png";
			int			ssize = (int)Math.pow((star.getStarClass().getRadius() * 600000), 0.3);
			lastX = 0;
			buffer.append("<table><tr>");
			
			buffer.append("<td><img src=\""+image+"\" width=\""+ssize+"\" height=\""+ssize+"\" title=\""+star.getName()+"\"/></td>");
			for (int i=0; i < planets.size(); i++) {
				Planet		planet = planets.elementAt(i);
				if (planet.getParentId() == star.getId() && !planet.isMoon()) {
					x = planet.getDistance() / 3 - lastX;
					lastX = planet.getDistance() / 3;
					String		pimg = Config.getBaseUrl()+"planet/"+planet.getId()+".jpg?globe";
					int			size = (int)Math.pow(planet.getRadius(), 0.3);
					buffer.append("<td width=\""+x+"px\">");
					buffer.append("<img src=\""+pimg+"\" width=\""+size+"\" height=\""+size+"\" title=\""+planet.getName()+" ("+planet.getType()+")\" align=\"left\" valign=\"center\"/>");
					buffer.append("</td>");
				}
			}
			buffer.append("</tr></table>\n");
		}
		buffer.append("</div>\n");
		
		
		buffer.append("<div id=\"stars\">\n");
		buffer.append("<table id=\"tabs\" cellspacing=\"0\">\n");
		buffer.append("<tr>");
		int		idx = 0;
		String	c = "selected";
		for (Star star : getStars()) {
			String		image = Config.getBaseUrl()+"images/stars/"+star.getSpectralType().toString().substring(0, 1)+".png";
			int			width = (int)(64 * star.getSize());
			buffer.append("<td id=\"star_"+idx+"\" class=\""+c+"\">");
			buffer.append("<img src=\""+image+"\" width=\""+width+"\" height=\""+width+"\" onclick=\"selectStar('"+idx+"')\"/>");
			buffer.append("</td>");
			idx++;
			c = "unselected";
		}
		buffer.append("<td width=\"100%\">&nbsp;</td>");
		buffer.append("</tr></table>\n");
		buffer.append("</div>\n");
		
		buffer.append("<div id=\"planets\">\n");
		idx = 0;
		String	style = "";
		for (Star star : getStars()) {
			buffer.append("<div id=\"planets_"+(idx++)+"\" class=\"planets\" "+style+">\n");
			
			buffer.append("<h2>"+star.getName()+"</h2>");
			buffer.append("<p><b>Star class: </b>"+star.getStarClass()+" ("+star.getStarClass().getDescription()+")</p>");
			buffer.append("<p><b>Spectral type: </b>"+star.getSpectralType()+" ("+star.getSpectralType().getSurfaceTemperature()+" Kelvin)</p>");
			buffer.append("<p><b>Inner orbit: </b>"+star.getInnerLimit()+" Mkm</p>");
			buffer.append("<p><b>Ideal orbit: </b>"+star.getEarthDistance()+" Mkm</p>");
			buffer.append("<p><b>Cold orbit: </b>"+star.getColdPoint()+" Mkm</p>");
			
			for (int i=0; i < planets.size(); i++) {
				Planet	planet = planets.elementAt(i);
				if (planet.getParentId() == star.getId() && !planet.isMoon()) {
					buffer.append(planet.toHTML());
				}
			}

			buffer.append("</div>\n");
			style = "style=\"display: none;\"";
		}
		buffer.append("</div>\n");
		
		if (header) {
			buffer.append("</body></html>\n");
		}
		
		return buffer.toString();
	}
	
	public void dump() {
		Iterator<Planet>	p = planets.iterator();
		
		System.out.println("System "+getName()+" ["+getId()+"]");
		
		while (p.hasNext()) {
			Planet		planet = p.next();
			
			System.out.print(planet.getName()+"\t");
			System.out.print(planet.getDistance()+"Mkm\t");
			System.out.print("("+planet.getType()+")  \t");
			System.out.println(planet.getRadius()+"km");			
		}
	}
	
	
	/**
	 * Get the planet with the given unique id. The id is the planet's id
	 * in the entire universe, not the order of the planet in the star system.
	 * 
	 * @param id		Unique planet id.
	 */
	public Planet getPlanet(int id) {
		for (Planet p : planets) {
			if (p.getId() == id) {
				return p;
			}
		}
		
		return null;
	}
	
	/**
	 * Get the digit code for the largest population in this system.
	 * 
	 * @return		Population size in powers of 10.
	 */
	public int getMaxPopulation() {
		if (factory == null) {
			factory = new ObjectFactory();
		}
		long			max = factory.getMaximum("planet", "population", "system_id="+id);
		
		if (planets == null) {
			planets = factory.getPlanetsBySystem(this.id);
		}
		
		for (Planet p : planets) {
			if (p.getPopulation() > max) {
				max = p.getPopulation();
			}
		}
		
		if (max < 10) {
			return 0;
		} else {
			return (int) Math.log10(max);
		}
	}
	
	/**
	 * Get the highest tech level for worlds in this system.
	 */
	public int getMaxTechLevel() {
		if (factory == null) {
			factory = new ObjectFactory();
		}
		return (int)factory.getMaximum("planet", "tech", "system_id="+id);
	}
	
	public int getPlanetCount() {
		if (factory == null) {
			factory = new ObjectFactory();
		}
		return factory.getPlanetCount(id);
	}
	
	/**
	 * Terraform the star system. Find the most suitable planet, and move it
	 * closer to the best orbit. Then terraform the planet.
	 */
	public void terraform() {
		int		minDistance = 200;
		int		bestDistance = stars.elementAt(0).getEarthDistance();
		Planet	planet = null;
		
		for (Planet p : planets) {
			int		difference = Math.abs(bestDistance - p.getDistance());
			if (difference < minDistance && p.getType().isTerrestrial()) {
				minDistance = difference;
				planet = p;
			}
		}
		
		if (planet == null || planet.getDistance() < 25) {
			// Probably no suitable planets.
			return;
		}
		
		// Move the planet closer to the optimal distance.
		planet.setDistance((bestDistance + planet.getDistance())/2);
		
		// Change the size of the planet to be closer to that of Earth.
		planet.setRadius((planet.getRadius()+6400)/2);
		
		// Set the type of the atmosphere to be standard.
		planet.setAtmosphereType(AtmosphereType.Standard);
		
		planet.setHydrographics(50 + Die.d10(4));
		
		// Change the atmosphere pressure to be nearer that of Earth.
		if (planet.getAtmospherePressure() == AtmospherePressure.None) {
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
		} else if (planet.getAtmospherePressure().isThinnerThan(AtmospherePressure.Standard)) {
			planet.setAtmospherePressure(planet.getAtmospherePressure().getDenser());
		} else if (planet.getAtmospherePressure().isDenserThan(AtmospherePressure.Standard)) {
			planet.setAtmospherePressure(planet.getAtmospherePressure().getThinner());			
		}
		planet.sanityCheck(stars.elementAt(0));
		planet.persist();
		persist();
	}
	
	public static void
	main(String[] args) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		
		StarSystem		sys = new StarSystem(factory, 55819);
		Planet			p = sys.getMainWorld();
		System.out.println(p.getName()+", "+p.getType()+", "+p.getPopulation());
		//System.out.println("eC Mirriam            0303 B9998A6-A B                 A 534 Im G2 V");
		System.out.println(sys.getUWP());

		factory.close();
		
		//UWP		uwp = new UWP("eC Mirriam            0303 B9998A6-A B                 A 534 Im G2 V");
		
		/*
		PostScript		map = new PostScript(new File("map.ps"));
		map.setTopMargin(1500);
		map.setLeftMargin(100);
		map.setScale(75);
		map.plotHexMap(8, 10);
		
		ss.plotSymbols(map);
		
		map.close();
		*/
	}
	
	public int compareTo(Object arg0) {
		return toString().compareToIgnoreCase(arg0.toString());
	}

}
