/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders;

import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Installation;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.StarportType;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetDescription;
import uk.org.glendale.worldgen.civ.facility.Facility;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;

public abstract class FacilityBuilder {
	protected Planet			planet;
	protected PopulationSize	population;
	protected FacilityFactory	factory;
	
	private   Properties	config = new Properties();

	/**
	 * Read all the configuration properties from the resource file.
	 * If the file has already been read, then don't read it again.
	 */
	private void readConfig() throws MissingResourceException {
		String	resourceName = this.getClass().getName();

		ResourceBundle	bundle = ResourceBundle.getBundle(resourceName);
		Enumeration<String>		e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String	key = e.nextElement();
			config.setProperty(key, bundle.getString(key));
		}
	}
	
	protected PlanetDescription getDescription() {
		return new PlanetDescription(planet, config);
	}
	
	/**
	 * Gets a value from the given option by key. First looks for the key
	 * with the population modifier, and if that fails then looks for the
	 * key by itself.
	 * 
	 * @param 		key		Key prefix to look for.
	 * @return		One of the valid options specified.
	 */
	private String getOptionByPopulation(String key) {
		String value = getOneOption(key + "." + population);
		if (value == null) {
			value = getOneOption(key);
		}
		return value;
	}
	
	protected GovernmentType getGovernment() {
		return GovernmentType.valueOf(getOptionByPopulation("government"));
	}
	
	protected int getLawLevel() {
		return Integer.parseInt(getOptionByPopulation("lawLevel"));
	}
	
	protected int getTechLevel() {
		return Integer.parseInt(getOptionByPopulation("techLevel"));
	}
	
	protected StarportType getStarport() {
		return StarportType.valueOf(getOptionByPopulation("starPort"));
	}
	
	protected long getPopulation() {
		long	number = 0;
		int	  	code = Integer.parseInt(getOptionByPopulation("population"));

		// Population is given as a power of 10. Randomly generate a suitable
		// number to 4 significant figures.
		switch (code) {
		case 0:
			planet.setPopulation(Die.die(9));
			break;
		case 1:
			planet.setPopulation(10 + Die.rollZero(90));
			break;
		case 2:
			planet.setPopulation(100 + Die.rollZero(900));
			break;
		default:
			planet.setPopulation((1000 + Die.rollZero(9000)) * (long)Math.pow(10, code - 3));
			break;
		}

		return number;
	}
	
	/**
	 * Add the primary residential facility to this planet. The size of the
	 * facility is modified by generic parameters based on trade codes and
	 * technology level. Tech levels of 5+ provide a bonus, since advanced
	 * industrialisation enables a more effective workforce.
	 * 
	 * @param residential		Type of residential facility to add.
	 * @param residentialSize	Base size of the facility.
	 */
	protected void addResidential(Facility residential, int residentialSize) {
		if (planet.hasTradeCode(TradeCode.Po)) {
			residentialSize -= Die.d6(3);
		} else if (planet.hasTradeCode(TradeCode.Ri)){
			residentialSize += Die.d6(3);
		}
		switch (planet.getTechLevel()) {
		case 0:
			residentialSize -= Die.d6(2);
			break;
		case 1:
			residentialSize -= Die.d4();
			break;
		case 2: case 3: case 4:
			// No change.
			break;
		case 5:
			residentialSize += 5;
			break;
		case 6:
			residentialSize += 10;
			break;
		default:
			residentialSize += planet.getTechLevel() * 2;
		}
		
		planet.addFacility(residential, residentialSize);
	}
	
	/**
	 * Add all non-residential facilities to the planet. The total of all
	 * facilities should add up to about 100 (modified by technology level).
	 * The base size of the provided facilities will be re-scaled to fit.
	 * A facility of size 100, plus a facility of size 50 would be rescaled
	 * to be about size 66 and 33 for example.
	 *  
	 * @param residential
	 * @param residentialSize
	 * @param installations
	 */
	protected void addFacilities(List<Installation> installations) {
		
		int totalSize = 0;
		for (Installation i : installations) {
			totalSize  += i.getSize();
		}

		double	maxSize = 100.0;
		int		techLevel = planet.getTechLevel();
		if (techLevel > 4) {
			maxSize += Math.sqrt(techLevel - 4) * 5.0;
		}
		double multiplier = maxSize / totalSize;
		for (Installation i : installations) {
			Facility	facility = i.getFacility();
			int			facilitySize = i.getSize();

			facilitySize *= multiplier;
			facilitySize += Die.d6() - Die.d6();
			if (facilitySize < 2) {
				facilitySize = Die.d4();
			}
			planet.addFacility(facility, facilitySize);
		}
	}
	
	protected void generate() {
		planet.setGovernment(getGovernment());
		planet.setTechLevel(getTechLevel());
		planet.setLawLevel(getLawLevel());
		planet.setStarport(getStarport());
		planet.setPopulation(getPopulation());
		
		for (String code : getAllOptions("trade")) {
			try {
				planet.addTradeCode(TradeCode.valueOf(code));
			} catch (Throwable e) {
				// Catch illegal trade codes.
			}
		}
	}
	
	/**
	 * Get one option from the configuration. A configuration value may consist
	 * of multiple space separated options. If there is more than one, chose one
	 * at random and return that. If the key is unset, returns null.
	 * 
	 * @param key		Key to retrieve from the configuration.
	 * @return			One of the listed options from the value.
	 */
	protected String getOneOption(String key) {
		String value = config.getProperty(key, "");
		if (value.trim().length() > 0) {
			String[] options = value.split(" ");
			if (options.length > 1) {
				value = options[Die.rollZero(options.length)];
			}
		}
		return value.trim();
	}
	
	protected String[] getAllOptions(String key) {
		String value = config.getProperty(key, "");

		if (value.trim().length() > 0) {
			String[] options = value.split(" ");
			if (options.length > 0) {
				return options;
			}
		}
		return new String[0];
	}
	
	public FacilityBuilder(FacilityFactory factory, Planet planet, PopulationSize population) {
		this.factory = factory;
		this.planet = planet;
		this.population = population;
		
		readConfig();
	}
}
