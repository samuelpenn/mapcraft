/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.barren;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;

/**
 * A Vesperian world is a dwarf world with a rocky crust and metal rich core.
 * 
 * @author Samuel Penn
 */
public class Vesperian extends BarrenWorld {

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
		// Inhospitable.
		planet.addTradeCode(TradeCode.H3);

		generateMap();
		generateResources();
		generateDescription();
	}

	@Override
	public void generateMap() {
		setCraterNumbers(200);
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
		addResource("Ferric Ore", 25 + Die.d12(3));
		addResource("Heavy Metals", 10 + Die.d12(2));
		if (Die.d4() == 1) {
			addResource("Radioactives", 3 + Die.d6(2));
			addResource("Rare Metals", 3 + Die.d6());
		}
	}

}
