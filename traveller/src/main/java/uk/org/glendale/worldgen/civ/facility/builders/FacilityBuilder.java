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
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.StarportType;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetDescription;

public abstract class FacilityBuilder {
	protected Planet			planet;
	protected PopulationSize	population;
	
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
	
	protected GovernmentType getGovernment() {
		return GovernmentType.valueOf(getOneOption("government"));
	}
	
	protected int getLawLevel() {
		return Integer.parseInt(getOneOption("lawLevel"));
	}
	
	protected int getTechLevel() {
		return Integer.parseInt(getOneOption("techLevel"));
	}
	
	protected StarportType getStarport() {
		return StarportType.valueOf(getOneOption("starPort"));
	}
	
	protected void generate() {
		planet.setGovernment(getGovernment());
		planet.setTechLevel(getTechLevel());
		planet.setLawLevel(getLawLevel());
		planet.setStarport(getStarport());
		
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
	
	public FacilityBuilder(Planet planet, PopulationSize population) {
		this.planet = planet;
		this.population = population;
		readConfig();
	}
}
