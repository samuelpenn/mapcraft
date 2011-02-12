/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.barren;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;

/**
 * Hermian worlds are similar to Mercury. They are hot, barren rock worlds close
 * to their sun. Relatively rich in heavy elements, but covered in a mantle of
 * silicate rocks. No organic or ice compounds.
 * 
 * @author Samuel Penn
 */
public class Hermian extends BarrenWorld {
	public Hermian() {
		super();
	}

	public PlanetType getPlanetType() {
		return PlanetType.Hermian;
	}

	public void generate() {
		validate();
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.Trace);
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.addTradeCode(TradeCode.Ba);
		} else {
			planet.addTradeCode(TradeCode.Va);
		}

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
		setCraterNumbers(300);
		super.generateMap();
	}

	/**
	 * Resources consist mostly of Silicate and Ferric ores, with other metals
	 * and radioactives.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		addResource("Ferric Ore", 25 + Die.d20(2));
		addResource("Heavy Metals", 15 + Die.d12(2));
		addResource("Radioactives", 10 + Die.d6(2));
		if (Die.d4() == 1) {
			addResource("Rare Metals", 5 + Die.d6(1));
		}
		addResource("Helium 3", Die.d4(2));
	}
}
