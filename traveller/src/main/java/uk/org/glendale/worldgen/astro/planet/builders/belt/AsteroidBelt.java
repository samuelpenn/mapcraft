/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.belt;

import static uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature.GiantCrater;
import static uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature.HeavilyCratered;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Belt;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * A Ferrinian world is a small barren planet close to its parent star. They are
 * rich in heavy metals.
 * 
 * @author Samuel Penn
 */
public class AsteroidBelt extends Belt {

	@Override
	public PlanetType getPlanetType() {
		return PlanetType.AsteroidBelt;
	}

	public void generate() {
		planet.setType(getPlanetType());
		planet.setRadius(Die.d4(2));

		planet.addTradeCode(TradeCode.As);

		generateResources();
		generateDescription();
	}

	/**
	 * Resources consist mostly of Silicate and Ferric ores, with other metals
	 * and radioactives.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d12(2));
		if (Die.d6() == 1) {
			addResource("Silicate Crystals", 5 + Die.d6(2));
		}
		addResource("Ferric Ore", 15 + Die.d6(3));
		addResource("Heavy Metals", 15 + Die.d6(2));
		addResource("Rare Metals", 10 + Die.d6(3));
	}
}
