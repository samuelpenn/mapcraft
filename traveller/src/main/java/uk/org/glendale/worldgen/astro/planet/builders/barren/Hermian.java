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
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

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

		switch (Die.d6(2)) {
		case 2:
			planet.addFeature(PlanetFeature.Dust);
			break;
		case 6:
		case 7:
		case 8:
			planet.addFeature(PlanetFeature.HeavilyCratered);
			break;
		case 10:
			planet.addFeature(PlanetFeature.GiantCrater);
			break;
		case 12:
			planet.addFeature(PlanetFeature.Smooth);
			break;
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
		if (planet.hasFeatureCode(PlanetFeature.Dust)) {
			properties.put(CRATER_MODIFIER, -2);
		}


		if (planet.hasFeatureCode(PlanetFeature.HeavilyCratered)) {
			properties.put(CRATER_MODIFIER, +4);
		} else if (planet.hasFeatureCode(PlanetFeature.Smooth)) {
			properties.put(CRATER_MODIFIER, -2);
		} else if (planet.hasFeatureCode(PlanetFeature.GiantCrater)) {
			//setCraterNumbers(100 + Die.d100());
		} else {
			properties.put(CRATER_MODIFIER, +2);
		}

		System.out.println(getPlanetType());
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
