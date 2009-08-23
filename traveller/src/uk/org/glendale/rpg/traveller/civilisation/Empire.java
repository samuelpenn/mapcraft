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
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.StarportType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;

/**
 * Simulates the rise of empires. Finds all the planets with populations,
 * and grows the populations. Also finds all the planets capable of
 * colonising other worlds, and checks to see if they do. 
 * 
 * @author Samuel Penn
 */
public class Empire {
	public static int		year = 0;
	private int				techLevel = 10;
	private int				intensity = 0;
	private double			populationModifier = 1;
	
	private ObjectFactory factory = null;
	
	public Empire() {
		factory = new ObjectFactory();
	}
	
	public Empire(ObjectFactory factory) {
		this.factory = factory;
	}
	
	public void setTechLevel(int techLevel) {
		if (techLevel > 8 && techLevel < 15) {
			this.techLevel = techLevel;
		}
	}
	
	/**
	 * How intense is the colonisation effort? 0 = normal, 1 = vigorous, 
	 * 2 = highly vigorous.
	 * 
	 * @param intensity		Intensity, 0-2.
	 */
	public void setIntensity(int intensity) {
		this.intensity = intensity;
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
				
				Colony	colony = new Colony(factory, planet);
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

	private void setGovernment(Planet planet) {
		int		pr = (int)Math.log10(planet.getPopulation());
		
		switch (pr) {
		case 0: case 1:
			// Very small colonies, up to 99 people.
			switch (Die.d6(2)) {
			case 2:
				// Everyone does whatever they want.
				planet.setGovernment(GovernmentType.Anarchy);
				planet.setLawLevel(0);
				break;
			case 3: case 4:
				// Small religious sect.
				planet.setGovernment(GovernmentType.TheocraticDictatorship);
				planet.setLawLevel(Die.d4()+2);
				break;
			case 5: case 6: case 7:
				// Small corporate colony.
				planet.setGovernment(GovernmentType.Corporation);
				planet.setLawLevel(Die.d3());
				switch (Die.d6()) {
				case 1: case 2:
					planet.addTradeCode(TradeCode.Mi);
					break;
				case 3:
					planet.addTradeCode(TradeCode.Pi);
					break;
				default:
					planet.addTradeCode(TradeCode.Re);
					break;
				}
				break;
			case 8: case 9: case 10:
				// Small friendly democracy.
				planet.setGovernment(GovernmentType.ParticipatingDemocracy);
				planet.setLawLevel(Die.d3()-1);
				if (Die.d6() < 3) {
					planet.addTradeCode(TradeCode.Mi);
				}
				break;
			case 11:
				// Probably pirates.
				planet.setGovernment(GovernmentType.NonCharismaticLeader);
				planet.setLawLevel(Die.d6()-1);
				if (Die.d6() < 4) {
					planet.addTradeCode(TradeCode.Pi);
				}
				break;
			case 12:
				// A small family unit.
				planet.setGovernment(GovernmentType.Family);
				planet.setLawLevel(Die.d3()-1);
				break;
			}
			break;
		case 2: case 3: case 4:
			// Small colony, from 100 to 99,999 people.
			switch (Die.d6(3)) {
			case 3:
				// Everyone does whatever they want.
				planet.setGovernment(GovernmentType.Anarchy);
				planet.setLawLevel(0);
				break;
			case 4: case 5:
				// Religious sect.
				planet.setGovernment(GovernmentType.TheocraticDictatorship);
				planet.setLawLevel(Die.d3()+3);
				break;
			case 6: case 7: case 8:
				// Large corporate colony.
				planet.setGovernment(GovernmentType.Corporation);
				planet.setLawLevel(Die.d3()+1);
				switch (Die.d6(2)) {
				case 2:
					planet.addTradeCode(TradeCode.Pi);
					break;
				case 3: case 4: case 5:
					planet.addTradeCode(TradeCode.Re);
					break;
				default:
					planet.addTradeCode(TradeCode.Mi);
					break;
				}
				break;
			case 9: case 10:
				// Small friendly democracy.
				planet.setGovernment(GovernmentType.ParticipatingDemocracy);
				if (planet.getPopulation() > 10000 && Die.d2() == 1) {
					planet.setGovernment(GovernmentType.RepresentativeDemocracy);
				}
				planet.setLawLevel(Die.d3());
				if (Die.d6() < 3) {
					planet.addTradeCode(TradeCode.Mi);
				}
				break;
			case 11:
				// Probably pirates.
				planet.setGovernment(GovernmentType.NonCharismaticLeader);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 12:
				planet.setGovernment(GovernmentType.CharismaticLeader);
				planet.setLawLevel(Die.d6());
				break;
			case 13: case 14:
				planet.setGovernment(GovernmentType.CharismaticOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 15:
				planet.setGovernment(GovernmentType.SelfPerpetuatingOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 16:
				planet.setGovernment(GovernmentType.TheocraticOligarchy);
				planet.setLawLevel(Die.d3()+2);
				planet.setTechLevel(planet.getTechLevel()-1);
				break;
			case 17:
				planet.setGovernment(GovernmentType.CivilService);
				planet.setLawLevel(Die.d3()+2);
				break;
			case 18:
				planet.setGovernment(GovernmentType.FeudalTechnocracy);
				planet.setLawLevel(Die.d3()+3);
				planet.setTechLevel(planet.getTechLevel()-Die.d3());
				break;
			}
			break;
		case 5: case 6: case 7:
			// Colony, from 100,000 to 99 million people.
			switch (Die.d6(3)) {
			case 3: case 4:
				// Religious sect.
				planet.setGovernment(GovernmentType.TheocraticDictatorship);
				planet.setLawLevel(Die.d2()+4);
				planet.setTechLevel(planet.getTechLevel()-1);
				break;
			case 5:
				planet.setGovernment(GovernmentType.TheocraticOligarchy);
				planet.setLawLevel(Die.d3()+3);
				planet.setTechLevel(planet.getTechLevel()-1);
				break;
			case 6:
				// Corporate world grown well beyond the colony stage.
				planet.setGovernment(GovernmentType.Corporation);
				planet.setLawLevel(Die.d3()+1);
				break;
			case 7:
				// Small friendly democracy.
				planet.setGovernment(GovernmentType.ParticipatingDemocracy);
				planet.setLawLevel(Die.d3());
				break;
			case 8: case 9: case 10:
				// Friendly democracy.
				planet.setGovernment(GovernmentType.RepresentativeDemocracy);
				planet.setLawLevel(Die.d3()+1);
				break;
			case 11: case 12:
				planet.setGovernment(GovernmentType.SelfPerpetuatingOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 13: case 14:
				planet.setGovernment(GovernmentType.CharismaticOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 15:
				planet.setGovernment(GovernmentType.NonCharismaticLeader);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 16:
				planet.setGovernment(GovernmentType.CharismaticLeader);
				planet.setLawLevel(Die.d6());
				break;
			case 17:
				planet.setGovernment(GovernmentType.CivilService);
				planet.setLawLevel(Die.d3()+2);
				break;
			case 18:
				planet.setGovernment(GovernmentType.ImpersonalBureaucracy);
				planet.setLawLevel(Die.d3()+3);
				planet.setTechLevel(planet.getTechLevel()-Die.d2());
				break;
			}
			break;
		default:
			// 100 million people or more.
			switch (Die.d6(3)) {
			case 3: case 4: case 5:
				planet.setGovernment(GovernmentType.TheocraticOligarchy);
				planet.setLawLevel(Die.d3()+3);
				planet.setTechLevel(planet.getTechLevel()-1);
				break;
			case 6:
				// Small friendly democracy.
				planet.setGovernment(GovernmentType.ParticipatingDemocracy);
				planet.setLawLevel(Die.d3());
				break;
			case 7: case 8: case 9: case 10:
				// Friendly democracy.
				planet.setGovernment(GovernmentType.RepresentativeDemocracy);
				planet.setLawLevel(Die.d3()+1);
				break;
			case 11: case 12:
				planet.setGovernment(GovernmentType.SelfPerpetuatingOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 13: case 14:
				planet.setGovernment(GovernmentType.CharismaticOligarchy);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 15:
				planet.setGovernment(GovernmentType.CivilService);
				planet.setLawLevel(Die.d3()+2);
				break;
			case 16:
				planet.setGovernment(GovernmentType.ImpersonalBureaucracy);
				planet.setLawLevel(Die.d3()+3);
				planet.setTechLevel(planet.getTechLevel()-Die.d2());
				break;
			case 17:
				planet.setGovernment(GovernmentType.NonCharismaticLeader);
				planet.setLawLevel(Die.d4()+1);
				break;
			case 18:
				planet.setGovernment(GovernmentType.CharismaticLeader);
				planet.setLawLevel(Die.d6());
				break;
			}
		}
	}
	
	private void firstWave(Planet planet) {
		if (planet.getPopulation() > 0) {
			return;
		}
		long		idealPopulation = planet.getIdealPopulation();
		if (idealPopulation <= 0) {
			return;
		}
		System.out.println(planet.getName()+": "+planet.getType()+" "+planet.getTemperature()+" "+planet.getAtmosphereType()+" "+planet.getAtmospherePressure()+" "+planet.getIdealPopulation());
		
		if (idealPopulation > 1000000) {
			switch (Die.d6() + intensity) {
			case -1: case 0: case 1:
				planet.setPopulation(idealPopulation / Die.d6());
				planet.setTechLevel(techLevel-3);
				planet.setStarport(StarportType.C);
				break;
			case 2: case 3:
				planet.setPopulation(idealPopulation * Die.d10(2) * 10);
				planet.setTechLevel(techLevel-2);
				planet.setStarport(StarportType.B);
				break;
			case 4: case 5:
				planet.setPopulation(idealPopulation * Die.d10(2) * 10);
				planet.setTechLevel(techLevel-1);
				planet.setStarport(StarportType.B);
				break;
			case 6: case 7:
				planet.setPopulation(idealPopulation * Die.d10(3) * 10);
				planet.setTechLevel(techLevel);
				planet.setStarport(StarportType.A);
				break;
			default:
				planet.setPopulation(idealPopulation * Die.d10(4) * 10);
				planet.setTechLevel(techLevel);
				planet.setStarport(StarportType.A);
			}
		} else if (idealPopulation > 10000) {
			switch (Die.d6() + intensity) {
			case -1: case 0: case 1:
				break;
			case 2: case 3:
				planet.setPopulation(idealPopulation * Die.d10(1) * 10);
				planet.setTechLevel(techLevel-2);
				planet.setStarport(StarportType.E);
				break;
			case 4: case 5:
				planet.setPopulation(idealPopulation * Die.d10(2) * 10);
				planet.setTechLevel(techLevel-2);
				planet.setStarport(StarportType.D);
				break;
			case 6: case 7:
				planet.setPopulation(idealPopulation * Die.d10(2) * 10);
				planet.setTechLevel(techLevel-1);
				planet.setStarport(StarportType.C);
				break;
			default:
				planet.setPopulation(idealPopulation * Die.d10(3) * 10);
				planet.setTechLevel(techLevel);
				planet.setStarport(StarportType.B);
			}			
		} else if (idealPopulation > 100) {
			switch (Die.d6() + intensity) {
			case -1: case 0: case 1: case 2:
				break;
			case 3:
				planet.setPopulation(idealPopulation * Die.d10() * 5);
				planet.setTechLevel(techLevel-3);
				planet.setStarport(StarportType.X);
				break;
			case 4: case 5:
				planet.setPopulation(idealPopulation * Die.d10(2) * 5);
				planet.setTechLevel(techLevel-3);
				planet.setStarport(StarportType.E);
				break;
			case 6: case 7:
				planet.setPopulation(idealPopulation * Die.d10(2) * 5);
				planet.setTechLevel(techLevel-2);
				planet.setStarport(StarportType.D);
				break;
			default:
				planet.setPopulation(idealPopulation * Die.d10(2) * 5);
				planet.setTechLevel(techLevel-1);
				planet.setStarport(StarportType.C);
			}						
		} else {
			
		}
		setGovernment(planet);
		planet.persist();
	}
	
	/**
	 * Create a small population on all worlds in the specified sector.
	 */
	public void firstWave(int sectorId) {
		Vector<StarSystem>	systems = factory.getStarSystemsBySector(sectorId);
		
		for (StarSystem system : systems) {
			Vector<Planet> planets = factory.getPlanetsBySystem(system.getId());
			for (Planet planet : planets) {
				firstWave(planet);
			}
		}
	}
	
	public static void main(String[] args) {
		ObjectFactory		factory = new ObjectFactory();
		Empire				empire = new Empire(factory);
		//empire.grow();
		//empire.colonise();

		empire.setIntensity(+2);
		for (Sector sector : factory.getSectors()) {
			empire.firstWave(sector.getId());
		}
	}

}
