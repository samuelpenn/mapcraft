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
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * A Hadean world is a small barren planet close to its parent star which has
 * lost its crust. They consist of a small core of heavy metals.
 * 
 * @author Samuel Penn
 */
public class Hadean extends BarrenWorld {

	@Override
	public PlanetType getPlanetType() {
		return PlanetType.Hadean;
	}

	public void generate() {
		validate();
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.addTradeCode(TradeCode.Va);

		if (planet.getTemperature().isHotterThan(Temperature.ExtremelyHot)) {
			// Hostile world.
			planet.addTradeCode(TradeCode.H4);
		} else {
			// Inhospitable.
			planet.addTradeCode(TradeCode.H3);
		}

		generateMap();
		generateResources();
		generateDescription();
	}

	@Override
	public void generateMap() {
		LIGHT.setRGB("#A09080");
		DARK.setRGB("#908070");		
		properties.put(CRATER_COLOUR, "#908070");
		if (planet.hasFeatureCode(HeavilyCratered)) {
			properties.put(CRATER_MODIFIER, +4);
		}
		super.generateMap();
	}

	/**
	 * Resources consist mostly of Silicate and Ferric ores, with other metals
	 * and radioactives.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 15 + Die.d8(2));
		addResource("Ferric Ore", 65 + Die.d20(2));
		addResource("Heavy Metals", 45 + Die.d20(2));
		addResource("Radioactives", 10 + Die.d8(3));
	}

}
