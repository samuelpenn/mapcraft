/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.starsystem;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.rpg.traveller.systems.Zone;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.planet.PlanetGenerator;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.arean.Arean;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hermian;
import uk.org.glendale.worldgen.astro.planet.builders.belt.AsteroidBelt;
import uk.org.glendale.worldgen.astro.planet.builders.gaian.Gaian;
import uk.org.glendale.worldgen.astro.planet.builders.hot.Cytherean;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.EuJovian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.SubJovian;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorCode;
import uk.org.glendale.worldgen.astro.star.SpectralType;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarAPI;
import uk.org.glendale.worldgen.astro.star.StarClass;
import uk.org.glendale.worldgen.astro.star.StarFactory;
import uk.org.glendale.worldgen.astro.star.StarForm;
import uk.org.glendale.worldgen.astro.star.StarGenerator;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.text.Names;

/**
 * Creates new star systems.
 * 
 * @author Samuel Penn
 */
@Controller
public class StarSystemGenerator {
	
	@Autowired
	private StarSystemFactory		factory;
	
	@Autowired
	private PlanetFactory			planetFactory;
	
	@Autowired
	private StarFactory				starFactory;
	
	@Autowired
	private StarSystemAPI			starSystemAPI;
	
	@Autowired
	private FacilityFactory			facilityFactory;
	
	public StarSystemGenerator() {
	}
	
	/**
	 * Generate a new StarSystem object, without persisting it. This is a
	 * wrapper to the normal constructor, and if the name or coordinates are
	 * invalid then random options are selected instead.
	 * 
	 * @param sector	Sector to generate system in.
	 * @param name		Name of the system.
	 * @param x			X coordinate, 1-32.
	 * @param y			Y coordinate, 1-40.
	 * @return			Star system object.
	 */
	private StarSystem generateSystemTemplate(Sector sector, String name, int x, int y) {
		StarSystem		template = null;
		
		if (x == 0 || y == 0 || x > 32 || y > 40) {
			Set<String>	 locations = new HashSet<String>();
			List<StarSystem> existingSystems = factory.getStarSystemsInSector(sector);
			
			for (int xx=1; xx <= 32; xx++) {
				for (int yy=1; yy <= 40; yy++) {
					locations.add(String.format("%02d%02d", xx, yy));
				}
			}
			for (StarSystem system : existingSystems) {
				locations.remove(system.getXY());
			}
			
			System.out.println("Remaining locations: "+locations.size());
			if (locations.size() == 0) {
				throw new IllegalStateException("No free locations in this sector");
			} else {
				String coord = locations.toArray(new String[0])[Die.rollZero(locations.size())];
				System.out.println("Random location is: "+coord);
				x = Integer.parseInt(coord.substring(0, 2));
				y = Integer.parseInt(coord.substring(2, 4));
			}
		}
		if (name == null || name.trim().length() == 0) {
			try {
				Names names = new Names("names");
				name = names.getPlanetName();
			} catch (MalformedURLException e) {
				// Only thrown if giving it a URL, which we're not.
				name = "NGC "+Die.die(1000000);
			}
		}
		template = new StarSystem(sector, name.trim(), x, y);
		
		return template;
	}


	/**
	 * Creates an empty star system with no stars or planets. Not generally used
	 * except in testing. If name or coordinates are not properly set, then
	 * random ones are chosen.
	 * 
	 * @param sector
	 *            Sector this system is in.
	 * @param name
	 *            Name of the system.
	 * @param x
	 *            X coordinate of the system.
	 * @param y
	 *            Y coordinate of the system.
	 * @return New empty star system.
	 */
	@Transactional
	public StarSystem createEmptySystem(Sector sector, String name, int x, int y) {
		StarSystem system = generateSystemTemplate(sector, name, x, y);
		system.setAllegiance("Un");
		system.setZone(Zone.Green);

		factory.persist(system);

		return system;
	}
	
	/**
	 * Create a simple star system with a single star and a few planets.
	 * 
	 * @param sector	Sector to create system in.
	 * @param name		Name to give to the system.
	 * @param x			X coordinate, 1-32.
	 * @param y			Y coordinate, 1-40.
	 * @return			Newly created star system.
	 */
	@Transactional
	public int createSimpleSystem(Sector sector, String name, int x, int y) {
		System.out.println("Creating simple system ["+name+"]");
		
		StarSystem system = generateSystemTemplate(sector, name, x, y);
		system.setAllegiance("Un");
		system.setZone(Zone.Green);

		Star primary = new Star();//starGenerator.generateSimplePrimary();
		primary.setSystem(system);
		primary.setName(system.getName());
		primary.setClassification(StarClass.V);
		primary.setSpectralType(SpectralType.G2);
		primary.setForm(StarForm.Star);		
		system.addStar(primary);
		
		PlanetBuilder	builder;
		String			planetName;
		int				position = 0;
		int				distance = 0;
		
		// Setup a new planet generator for this star system.
		PlanetGenerator	generator = new PlanetGenerator(planetFactory, system, primary);
		Planet			planet = null;
		
		// Mercury planet.
		if (Die.d2() == 1) {
			planetName = system.getName() + " " + getOrbitNumber(++position);
			distance = 40 + Die.d10(2);
			planet = generator.generatePlanet(planetName, position, distance, new Hermian());
			system.addPlanet(planet);
			factory.persist(system);
		}
		
		// Venus planet.
		if (Die.d2() == 1) {
			planetName = system.getName() + " " + getOrbitNumber(++position);
			distance = 80 + Die.d10(2);
			planet = generator.generatePlanet(planetName, position, distance, new Cytherean());
			system.addPlanet(planet);
			factory.persist(system);
		}

		// Earth planet.
		planetName = system.getName() + " " + getOrbitNumber(++position);
		distance = 130 + Die.d20(2);
		planet = generator.generatePlanet(planetName, position, distance, new Gaian());
		system.addPlanet(planet);
		factory.persist(system);
		
		// Mars or Belt
		if (Die.d3() == 1) {
			planetName = system.getName() + " " + getOrbitNumber(++position);
			distance = 200 + Die.d20(3);
			planet = generator.generatePlanet(planetName, position, distance, new Arean());
		} else {
			planetName = system.getName() + " " + getOrbitNumber(++position);
			distance = 200 + Die.d20(3);
			planet = generator.generatePlanet(planetName, position, distance, new AsteroidBelt());
		}
		system.addPlanet(planet);
		factory.persist(system);

		// Gas giant.
		planetName = system.getName() + " " + getOrbitNumber(++position);
		distance = 700 + Die.d100(2);
		if (Die.d2() == 1) {
			planet = generator.generatePlanet(planetName, position, distance, new EuJovian());
		} else {
			planet = generator.generatePlanet(planetName, position, distance, new SubJovian());
		}
		system.addPlanet(planet);
		factory.persist(system);
		
		System.out.println(system.getId()+": "+system.getStars().get(0).getId());
		
		return system.getId();
	}

	public StarSystem createStarSystem(Sector sector, String name, int x, int y) {
		StarSystem system = generateSystemTemplate(sector, name, x, y);
		system.setAllegiance("Un");
		system.setZone(Zone.Green);

		generateStars(system, sector.getCodes());
		factory.persist(system);

		return system;
	}

	/**
	 * Randomly generate stars for this system. Most systems will have a single
	 * star, some will have two and a few three. All binary systems will have a
	 * primary and distant secondary. All triple systems will have the third
	 * star in close orbit around the secondary.
	 * 
	 * Multiple-star systems are kept deliberately rarer than reality for
	 * reasons of simplicity.
	 */
	private void generateStars(StarSystem system, Set<SectorCode> codes) {
		if (system == null) {
			throw new IllegalArgumentException(
					"Star system has not been defined");
		}
		int numStars = 0;
		switch (Die.d6(3)) {
		case 3:
			// Triple star system.
			numStars = 3;
			break;
		case 4:
		case 5:
		case 6:
			// Binary star system.
			numStars = 2;
			break;
		default:
			numStars = 1;
		}
		if (codes.contains(SectorCode.Sp) && numStars > 1) {
			numStars -= 1;
		}

		StarGenerator starGenerator = new StarGenerator(system, numStars > 1);
		Star primary = starGenerator.generatePrimary();
		system.addStar(primary);
		factory.persist(system);
		if (numStars > 1) {
			Star secondary = starGenerator.generateSecondary();
			system.addStar(secondary);
			factory.persist(system);
			if (numStars > 2) {
				Star tertiary = starGenerator.generateTertiary();
				system.addStar(tertiary);
				factory.persist(system);
			}
		}

		for (int s = 0; s < system.getStars().size(); s++) {
			generatePlanets(system, s, codes);
		}
	}

	/**
	 * Get the orbit number as a Roman numeral. Should work up to 39.
	 * 
	 * @param orbit
	 *            Orbit number, 1+
	 * @return Roman numeral.
	 */
	private String getOrbitNumber(int orbit) {
		String x = "";

		// Only works up to 39. It is very unlikely that there will
		// ever be more planets than this around a single star.
		while (orbit >= 10) {
			x += "X";
			orbit -= 10;
		}
		switch (orbit) {
		case 1:
			return x + "I";
		case 2:
			return x + "II";
		case 3:
			return x + "III";
		case 4:
			return x + "IV";
		case 5:
			return x + "V";
		case 6:
			return x + "VI";
		case 7:
			return x + "VII";
		case 8:
			return x + "VIII";
		case 9:
			return x + "IX";
		}

		return x;
	}

	/**
	 * Randomly generate planets for a star.
	 * 
	 * @param system
	 *            System to generate planets for.
	 * @param starIndex
	 *            Star within the system.
	 */
	private void generatePlanets(StarSystem system, int starIndex,
			Set<SectorCode> codes) {
		Star star = system.getStars().get(starIndex);
		int numPlanets = Die.d6(2) - starIndex * 3;
		int increase = StarAPI.getInnerLimit(star) + Die.d6(2);
		int distance = StarAPI.getInnerLimit(star) + Die.die(increase, 2);

		if (codes.contains(SectorCode.Sp)) {
			numPlanets /= 2;
		}

		PlanetGenerator planetGenerator = new PlanetGenerator(planetFactory, system, star);
		for (int p = 0; p < numPlanets; p++) {
			String planetName = star.getName() + " " + getOrbitNumber(p + 1);
			Planet planet = planetGenerator.generatePlanet(planetName, p,
					distance);
			//entityManager.persist(planet);
			System.out.println("Persisted planet [" + planet.getId() + "] ["
					+ planet.getName() + "]");

			List<Planet> moons = planetGenerator.generateMoons(planet, codes);
			for (Planet moon : moons) {
				System.out.println("Persisting [" + moon.getName() + "]");
				//entityManager.persist(moon);
				System.out.println("Persisted moon [" + moon.getId() + "] ["
						+ moon.getName() + "]");
			}

			if (planet.getType().isJovian()) {
				// Give extra room for Jovian worlds.
				distance += Die.die(increase, 2) + Die.d10(3);
			}
			distance += Die.die(increase, 2) + Die.d10(2);
			increase = (int) (increase * (1.0 + Die.d6(2) / 10.0)) + Die.d4();
		}

	}

}
