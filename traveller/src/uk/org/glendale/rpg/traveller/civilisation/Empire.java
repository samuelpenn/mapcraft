/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.civilisation;

import java.sql.*;
import java.util.Vector;

import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Simulates the rise of empires. Finds all the planets with populations,
 * and grows the populations. Also finds all the planets capable of
 * colonising other worlds, and checks to see if they do. 
 * 
 * @author Samuel Penn
 */
public class Empire {
	public static int		year = 0;
	
	private ObjectFactory factory = null;
	
	public Empire() {
		factory = new ObjectFactory();
	}
	
	public Empire(ObjectFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Grow all planets in the universe which have a population.
	 */
	public void grow() {
		try {
			ResultSet		rs = factory.read("planet", "population > 0");
			
			while (rs.next()) {
				Planet 		planet = new Planet(rs);
				System.out.println("Growing planet "+planet.getName()+": "+planet.getTechLevel()+"/"+planet.getStarport()+", "+(int)(Math.log10(planet.getPopulation())));
				
				Colony	colony = new Colony(planet);
				colony.grow();
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			factory.close();
		}		
	}
	
	private Vector<StarSystem> findNearbyStars(ObjectFactory factory, StarSystem system, int distance) {
		Vector<StarSystem>	list = new Vector<StarSystem>();
		
		try {
			Sector		sector = new Sector(system.getSectorId());
			// Get the universal coordinate of this system. Need to translate to
			// zero based positioning for the sector.
			int		x = sector.getX()*32 + system.getX()-1;
			int		y = sector.getY()*40 + system.getY()-1;
			
			for (Sector s : factory.getSectors()) {
				for (StarSystem ss : factory.getStarSystemsBySector(s.getId(), false)) {
					int		sx = s.getX()*32 + ss.getX()-1;
					int		sy = s.getY()*40 + ss.getY()-1;
					
					int		d = (int)Math.sqrt((sx-x)*(sx-x) + (sy-y)*(sy-y));
					if (d <= distance && system.getId() != ss.getId()) {
						System.out.println("Found: "+ss.getName());
						list.add(ss);
					}
				}
			}			
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		} finally {
		}
		
		return list;
	}
	
	/**
	 * Find worlds which want to colonise other worlds. And run a
	 * simulation of the colonisation.
	 */
	public void colonise() {
		try {
			ResultSet		rs = factory.read("planet", "starport in ('A', 'B', 'C')");
			while (rs.next()) {
				Planet 		planet = new Planet(rs);
				StarSystem	home = new StarSystem(factory, planet.getSystemId());
				if (planet.getPopulation() > 100000000) {
					Vector<StarSystem>		list = findNearbyStars(factory, home, 2);
				}				
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		} finally {
			factory.close();
		}
	}
	
	private void firstWave(ObjectFactory factory, Planet planet) {
		if (planet.getPopulation() > 0) {
			return;
		}
		long		idealPopulation = planet.getIdealPopulation();
		if (idealPopulation > 0) {
			System.out.println(planet.getName()+": "+planet.getType()+" "+planet.getTemperature()+" "+planet.getAtmosphereType()+" "+planet.getAtmospherePressure()+" "+planet.getIdealPopulation());
		}
	}
	
	/**
	 * Create a small population on all worlds in the specified sector.
	 */
	public void firstWave(int sectorId) {
		Vector<StarSystem>	systems = factory.getStarSystemsBySector(sectorId);
		
		for (StarSystem system : systems) {
			Vector<Planet> planets = factory.getPlanetsBySystem(system.getId());
			for (Planet planet : planets) {
				firstWave(factory, planet);
			}
		}
	}
	
	public static void main(String[] args) {
		ObjectFactory		factory = new ObjectFactory();
		Empire				empire = new Empire(factory);
		//empire.grow();
		//empire.colonise();

		for (Sector sector : factory.getSectors()) {
			empire.firstWave(sector.getId());
		}
	}

}
