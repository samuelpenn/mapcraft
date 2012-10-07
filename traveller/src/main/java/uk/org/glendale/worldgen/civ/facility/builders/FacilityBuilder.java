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
	
	protected String			description;

	/**
	 * Read all the configuration properties from the resource file.
	 * If the file has already been read, then don't read it again.
	 */
	private void readConfig() throws MissingResourceException {
		String	resourceName = this.getClass().getName();
		
		if (config.size() > 0) {
			return;
		}
		
		System.out.println("Reading resource [" + resourceName + "]");

		ResourceBundle	bundle = ResourceBundle.getBundle(resourceName);
		Enumeration<String>		e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String	key = e.nextElement();
			config.setProperty(key, bundle.getString(key));
		}
	}
	
	protected final PlanetDescription getDescription() {
		return new PlanetDescription(planet, config);
	}
	
	public final String getDescriptionText() {
		return description;
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
		long	maximum = 0;
		long	attempts = 1;
		long	divisor = 1;
		String  option = getOptionByPopulation("population");
		// Allow for a '+' suffix to get the highest of two rolls, or a '-' to
		// generate smaller numbers.
		if (option.indexOf("+") > -1) {
			attempts = 2;
		} else if (option.indexOf("-") > -1) {
			divisor = 2;
		}
		option = option.replaceAll("[^0-9]", "");
		int	  	code = Integer.parseInt(option);

		// Population is given as a power of 10. Randomly generate a suitable
		// number to 4 significant figures.
		for (int i = 0; i < attempts; i++) {
			switch (code) {
			case 0:
				number = Die.die(9) / divisor;
				break;
			case 1:
				number = 10 + Die.rollZero(90) / divisor;
				break;
			case 2:
				number = 100 + Die.rollZero(900) / divisor;
				break;
			default:
				number = (1000 + Die.rollZero(9000) / divisor) * (long)Math.pow(10, code - 3);
				break;
			}
			if (number > maximum) {
				maximum = number;
			}
		}
		return maximum;
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
		System.out.println("addResidential: [" + residential.getName() + "]");
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
	
	public void generate() {
		readConfig();
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
		String value = config.getProperty(key);
		if ( value != null && value.trim().length() > 0) {
			String[] options = value.split(" ");
			if (options.length > 1) {
				value = options[Die.rollZero(options.length)];
			}
			return value.trim();
		}
		return null;
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
