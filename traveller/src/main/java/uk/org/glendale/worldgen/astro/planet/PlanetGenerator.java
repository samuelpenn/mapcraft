/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetDescription;
import uk.org.glendale.worldgen.astro.planet.builders.arean.Arean;
import uk.org.glendale.worldgen.astro.planet.builders.arean.EoArean;
import uk.org.glendale.worldgen.astro.planet.builders.arean.MesoArean;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Ferrinian;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hadean;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hermian;
import uk.org.glendale.worldgen.astro.planet.builders.gaian.Gaian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.CryoJovian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.EuJovian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.SubJovian;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.astro.sector.SectorCode;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarAPI;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityGenerator;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Generates a random planet. The type of planet is based on the star and the
 * orbit distance from it. A suitable PlanetBuilder is selected, and then used
 * to build the planet, including any descriptions, maps and so on.
 * 
 * Moons can be generated as a final step. Moons for a planet must be generated
 * immediately after the planet that the moons are for.
 * 
 * @author Samuel Penn
 */
public class PlanetGenerator {
	private PlanetFactory	planetFactory;
	private StarSystem		system;
	private Star			star;
	private Builder			builder	= null;
	private WorldBuilder	lastBuilder = null;

	/**
	 * Create a new PlanetGenerator for a given star in a star system.
	 * 
	 * @param system
	 *            Star system that planets are being generated in.
	 * @param star
	 *            Star in that system that is the primary for these planets.
	 */
	public PlanetGenerator(PlanetFactory factory, StarSystem system, Star star) {
		this.planetFactory = factory;
		this.system = system;
		this.star = star;
	}
	/**
	 * Generate a specific single planet at the given distance with the
	 * specified name. No checks are performed to see if the planet is being
	 * created in an unsuitable location (such as an ice world close to a hot
	 * star), and doing this may cause issues later on.
	 * 
	 * @param name
	 *            Full name of this planet, including suffixes.
	 * @param position
	 *            Orbital position of planet, where 1 is the first orbit.
	 * @param distance
	 *            Distance of the planet in Mkm.
	 * @param builder
	 *            Type of planet to create.
	 * @return Newly generated planet.
	 */
	public Planet generatePlanet(String name, int position, int distance,
			Builder builder) {
		Temperature orbitTemperature = StarAPI.getOrbitTemperature(star,
				distance);

		if (builder instanceof WorldBuilder) {
			this.lastBuilder = (WorldBuilder) builder;
		}

		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Planet name cannot be empty");
		}
		if (position < 1) {
			throw new IllegalArgumentException("Orbit position must be 1+");
		}
		if (distance < 1) {
			throw new IllegalArgumentException("Planet distance must be 1+");
		}

		Planet planet = new Planet(system, star.getId(), false, name);
		planet.setDistance(distance);
		planet.setTemperature(orbitTemperature);

		builder.setPlanet(planet);
		builder.setStar(star);
		builder.setCommodityFactory(planetFactory.getCommodityFactory());
		builder.generate();
		
		Habitability h = Habitability.getHabitability(planet);
		System.out.println(planet.getName() + ": " + planet.getType() + ", " + h);
		PopulationSize		size = PopulationSize.None;
		TechnologyLevel		level = TechnologyLevel.LowTech;
		switch (h) {
		case Ideal:
			// Ideal worlds, tend to have very large populations.
			switch (Die.d6(2)) {
			case 2:	case 3:
				size = PopulationSize.Medium;
				break;
			case 4: case 5:
				size = PopulationSize.Large;
				break;
			case 6: case 7: case 8: case 9: case 10:
				size = PopulationSize.Huge;
				break;
			case 11: case 12:
				size = PopulationSize.Gigantic;
				break;
			}
			break;
		case Habitable:
			// Ideal worlds, tend to have large populations.
			switch (Die.d6(2)) {
			case 2:
				size = PopulationSize.Small;
				break;
			case 3: case 4:
				size = PopulationSize.Medium;
				break;
			case 5: case 6: case 7:
				size = PopulationSize.Large;
				break;
			case 8: case 9: case 10: case 11:
				size = PopulationSize.Huge;
				break;
			case 12:
				size = PopulationSize.Gigantic;
				break;
			}
			break;
		default:
			size = PopulationSize.None;
			break;
		}
		FacilityBuilder facilityBuilder = null;
		String 			culture = lastBuilder.getFacilityBuilderName(size, level);
		if (culture != null) {
			System.out.println(culture);
			facilityBuilder = getFacilityBuilder(culture, planet, size);
			if (facilityBuilder != null) {
				facilityBuilder.generate();
			} else {
				System.out.println("!! No builder for culture");
			}
		}
		//planetFactory.getFacilityGenerator().generateFacilities(planet, size);
		//System.out.println("TechLevel: "+planet.getTechLevel());

		PlanetDescription  description = new PlanetDescription(builder);
		String text = description.getFullDescription();
		
		if (facilityBuilder != null) {
			text += " " + facilityBuilder.getDescriptionText();
		}
		planet.setDescription(text);

		return planet;
	}
	
	private FacilityBuilder getFacilityBuilder(String className, Planet planet, PopulationSize size) {
		FacilityBuilder builder = null;
		
		try {
			Constructor c = Class.forName(className).getConstructor(new Class[] {
					FacilityFactory.class,
					Planet.class,
					PopulationSize.class
			});
			builder = (FacilityBuilder) c.newInstance(new Object[] {
					planetFactory.getFacilityFactory(),
					planet,
					size
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return builder;
	}


	/**
	 * Generate a single planet at the given distance with the specified name.
	 * The type of planet is randomly determined based on the orbital
	 * characteristics (mostly temperature).
	 * 
	 * @param name
	 *            Full name of this planet, including suffixes.
	 * @param position
	 *            Orbital position of planet, where 0 is the first orbit.
	 * @param distance
	 *            Distance of the planet in Mkm.
	 * @return Newly generated planet.
	 */
	public Planet generatePlanet(String name, int position, int distance) {
		Temperature orbitTemperature = StarAPI.getOrbitTemperature(star,
				distance);

		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Planet name cannot be empty");
		}
		if (position < 0) {
			throw new IllegalArgumentException("Orbit position must be 1+");
		}
		if (distance < 1) {
			throw new IllegalArgumentException("Planet distance must be 1+");
		}

		Planet planet = new Planet(system, star.getId(), false, name);
		planet.setDistance(distance);
		planet.setTemperature(orbitTemperature);

		builder = null;
		switch (orbitTemperature) {
		case UltraHot:
		case ExtremelyHot:
			switch (Die.d6()) {
			case 1:
				// XXX: builder = new Hermian();
				break;
			case 2:
			case 3:
			case 4:
				// XXX: builder = new Ferrinian();
				break;
			default:
				// XXX: builder = new Hadean();
				break;
			}
			break;
		case VeryHot:
			// Mercury's orbit.
			switch (Die.d6()) {
			case 1:
			case 2:
			case 3:
				// XXX: builder = new Hermian();
				break;
			default:
				// XXX: builder = new Ferrinian();
				break;
			}
			break;
		case Hot:
		case Warm:
			// Venus' orbit.
			// XXX: builder = new Hermian();
			break;
		case Standard:
			// Earth's orbit.
			// XXX: builder = new Gaian();
			break;
		case Cool:
			switch (Die.d10()) {
			case 1:
			case 2:
				// XXX: builder = new Gaian();
				break;
			case 3:
			case 4:
				builder = new EoArean();
				break;
			case 5:
			case 6:
				builder = new MesoArean();
				break;
			default:
				// XXX: builder = new Arean();
			}
			break;
		case Cold:
			// Mars, Asteroids
			// XXX: builder = new Arean();
			break;
		case VeryCold:
			// Jupiter, Saturn
			switch (Die.d4()) {
			case 1:
				// XXX: builder = new EuJovian();
				break;
			default:
				// XXX: builder = new SubJovian();
				break;
			}
			break;
		case ExtremelyCold:
			// Uranus, Neptune.
			// XXX: builder = new CryoJovian();
			break;
		case UltraCold:
			// Kuiper belt
			// XXX: builder = new CryoJovian();
			break;
		}
		builder.setPlanet(planet);
		builder.setStar(star);
		builder.generate();

		System.out.println(system.getId() + ": " + planet.getName() + " ("
				+ planet.getType() + ")");

		return planet;
	}

	/**
	 * Create a set of moons for this planet.
	 * 
	 * @param planet
	 *            Planet to generate moons for.
	 * @return List of moons that have been generated.
	 */
	public List<Planet> generateMoons(Planet planet, Set<SectorCode> codes) {
		List<Planet> moons = new ArrayList<Planet>();
		Builder[] builders = lastBuilder.getMoonBuilders();

		if (builders != null) {
			// If this is a sparse sector, halve number of moons.
			if (codes.contains(SectorCode.Sp) && builders.length > 1) {
				WorldBuilder[] nb = new WorldBuilder[builders.length / 2];
				for (int i = 0; i < nb.length; i++) {
					nb[i] = (WorldBuilder) builders[i];
				}
				builders = nb;
			}

			String[] names = { "a", "b", "c", "d", "e", "f", "g", "h", "i",
					"j", "k", "l", "m", "n", "o", "p" };
			int distance = lastBuilder.getFirstMoonDistance();
			for (int i = 0; i < builders.length; i++) {
				Temperature orbitTemperature = StarAPI.getOrbitTemperature(
						star, planet.getDistance());

				WorldBuilder moonBuilder = (WorldBuilder) builders[i];
				String moonName = planet.getName() + "/" + names[i];
				System.out.println(moonName);
				Planet moon = new Planet(planet.getSystem(), planet.getId(),
						true, moonName);
				moon.setDistance(distance);
				moon.setTemperature(orbitTemperature);
				moonBuilder.setPlanet(moon);
				moonBuilder.setStar(star);
				moonBuilder.setCommodityFactory(planetFactory.getCommodityFactory());
				moonBuilder.generate();
				
				PlanetDescription  description = new PlanetDescription(moonBuilder);
				moon.setDescription(description.getFullDescription());

				moons.add(moon);

				distance *= 1.5;
			}
		}
		System.out.println("Built " + moons.size() + " moons");

		return moons;
	}
}
