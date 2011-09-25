/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.arean;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;

/**
 * A barren, Mars-like world.
 * 
 * @author Samuel Penn
 */
public class Arean extends BarrenWorld {

	public PlanetType getPlanetType() {
		return PlanetType.Arean;
	}

	public void generate() {
		if (planet == null) {
			throw new IllegalStateException(
					"Use setPlanet() to set the planet first");
		}
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.addTradeCode(TradeCode.Ba);
		planet.addTradeCode(TradeCode.H3);
		if (planet.getRadius() > 4000) {
			planet.setPressure(AtmospherePressure.Thin);
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
			planet.setTemperature(planet.getTemperature().getHotter());
		} else if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.VeryThin);
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
		} else if (planet.getRadius() > 2000) {
			planet.setPressure(AtmospherePressure.Trace);
			if (Die.d2() == 1) {
				planet.setAtmosphere(AtmosphereType.CarbonDioxide);
			} else {
				planet.setAtmosphere(AtmosphereType.InertGases);
			}
		}
		generateMap();
		generateResources();
		generateDescription();
	}

	public void generateMap() {
		setCraterMinSize(10);
		setCraterNumbers(50);

		base = new Tile("Base", "#3c3b36", false);
		crust = new Tile("Crust", "#d8b476", false);
		mountains = new Tile("Mountains", "#e9be7a", false);

		super.generateMap();
	}

	@Override
	public void generateResources() {
		addResource("Silicate Ore", 25 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		addResource("Carbonic Ore", 10 + Die.d12(3));
		addResource("Ferric Ore", 10 + Die.d12(2));
		if (Die.d4() == 1) {
			addResource("Radioactives", Die.d6(2));
		}
		int water = Die.d10(2);
		addResource("Water", water);
		if (water > 5) {
			planet.addTradeCode(TradeCode.Ic);
		}
		int gases = 0;
		switch (planet.getPressure()) {
		case Trace:
			gases = Die.d4();
			break;
		case VeryThin:
			gases = 2 + Die.d4(2);
			break;
		case Thin:
			gases = 5 + Die.d6(2);
			break;
		}
		addResource("Inert Gases", gases);
	}
}
