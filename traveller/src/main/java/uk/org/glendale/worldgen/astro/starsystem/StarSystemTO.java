/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.starsystem;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.traveller.systems.Zone;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetTO;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarTO;

/**
 * Simplified StarSystem object for JSON transfer. This is to get around
 * problems with the automatic JSON marshaling of entity beans, and to 
 * provide more control over exactly what data is transferred via REST
 * calls. 
 */
public class StarSystemTO {
	private int id;
	private String name;
	private String sector;
	private int x;
	private int y;
	private String allegiance;
	private Zone zone;
	
	private int numberOfStars;
	private int numberOfPlanets;

	private List<StarTO> stars;
	private PlanetTO mainWorld;
	private List<PlanetTO> planets;

	/**
	 * Create a minimal representation of a StarSystem. Does not include
	 * any planets. Designed for use when detailing whole sectors.
	 */
	public StarSystemTO(StarSystem system) {
		this(system, false);
	}

	public StarSystemTO(StarSystem system, boolean detail) {
		this.id = system.getId();
		this.name = system.getName();
		this.sector = system.getSector().getName();
		this.x = system.getX();
		this.y = system.getY();
		this.allegiance = system.getAllegiance();
		this.zone = system.getZone();
		this.numberOfStars = system.getStars().size();
		this.numberOfPlanets = system.getPlanets().size();
		
		stars = new ArrayList<StarTO>();
		for (Star star : system.getStars()) {
			stars.add(new StarTO(star));
		}
		this.mainWorld = new PlanetTO(system.getMainWorld());
		
		if (detail) {
			planets = new ArrayList<PlanetTO>();
			
			for (Planet planet : system.getPlanets()) {
				planets.add(new PlanetTO(planet));
			}
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSectorName() {
		return sector;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getAllegiance() {
		return allegiance;
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public int getNumberOfStars() {
		return numberOfStars;
	}

	public int getNumberOfPlanets() {
		return numberOfPlanets;
	}
	
	public List<StarTO> getStars() {
		return stars;
	}
	
	public PlanetTO getMainWorld() {
		return mainWorld;
	}
	
	public List<PlanetTO> getPlanets() {
		return planets;
	}
}
