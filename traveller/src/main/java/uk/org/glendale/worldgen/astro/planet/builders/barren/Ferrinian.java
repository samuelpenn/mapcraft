/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.barren;

import static uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature.GiantCrater;
import static uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature.HeavilyCratered;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * A Ferrinian world is a small barren planet close to its parent star. They are
 * rich in heavy metals.
 * 
 * @author Samuel Penn
 */
public class Ferrinian extends BarrenWorld {

	@Override
	public PlanetType getPlanetType() {
		return PlanetType.Ferrinian;
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

		switch (Die.d6()) {
		case 1:
		case 2:
		case 3:
			planet.addFeature(HeavilyCratered);
			break;
		case 6:
			planet.addFeature(GiantCrater);
			break;
		}

		generateMap();
		generateResources();
		generateDescription();
	}

	@Override
	public void generateMap() {
		base = new Tile("Sea", "#706050", false);
		crust = new Tile("Crust", "#907060", false);
		mountains = new Tile("Mountains", "#C0B0B0", false);
		crater = new Tile("Crater", "#807060", false);

		setNumberOfContinents(5);
		setCraterNumbers(500);
		if (planet.hasFeatureCode(HeavilyCratered)) {
			setCraterNumbers(500 + Die.d100(5));
		}
		super.generateMap();
	}

	/**
	 * Resources consist mostly of Silicate and Ferric ores, with other metals
	 * and radioactives.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d20(2));
		if (Die.d6() == 1) {
			addResource("Silicate Crystals", 5 + Die.d6(2));
		}
		addResource("Ferric Ore", 35 + Die.d20(3));
		addResource("Heavy Metals", 25 + Die.d20(2));
		addResource("Radioactives", 15 + Die.d12(3));
		addResource("Rare Metals", 5 + Die.d6(3));
	}

}
