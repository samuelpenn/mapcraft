/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.barren;

import static uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature.HeavilyCratered;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * HSelenian worlds are similar to the Moon. They have a limited amount of
 * useful resources, and are generally barren and dry.
 * 
 * @author Samuel Penn
 */
public class Selenian extends BarrenWorld {
	public Selenian() {
		super();
	}

	public PlanetType getPlanetType() {
		return PlanetType.Selenian;
	}

	public void generate() {
		if (planet == null) {
			throw new IllegalStateException(
					"Use setPlanet() to set the planet first");
		}
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.setAtmosphere(AtmosphereType.Vacuum);
		planet.addTradeCode(TradeCode.Va);
		if (planet.getTemperature().isHotterThan(Temperature.ExtremelyHot)) {
			planet.addTradeCode(TradeCode.H4);
		} else {
			planet.addTradeCode(TradeCode.In);
		}

		generateMap();
		generateResources();
		generateDescription();
	}

	@Override
	public void generateMap() {
		LIGHT.setRGB("#D0D0D0");
		DARK.setRGB("#C0C0C0");		
		properties.put(CRATER_COLOUR, "#B0B0B0");
		if (planet.hasFeatureCode(HeavilyCratered)) {
			properties.put(CRATER_MODIFIER, +3);
		}
		super.generateMap();
	}

	/**
	 * Resources consist of Silicate ores.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		if (Die.d2() == 1) {
			addResource("Oxygen", Die.d4(2));
		}
		addResource("Helium 3", Die.d4(2));
	}
}
