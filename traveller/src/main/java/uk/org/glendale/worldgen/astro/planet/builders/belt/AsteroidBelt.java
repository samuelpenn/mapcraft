/*
 * Copyright (C) 2011, 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.belt;

import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.Belt;

/**
 * An Asteroid Belt is an orbiting collection of rocky fragments.
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
		planet.setRadius(Die.d6(3));

		planet.addTradeCode(TradeCode.As);

		generateMap();
		generateResources();
		generateDescription();
	}

	/**
	 * Resources consist mostly of Silicate and Ferric ores, with other metals
	 * and possibly radioactives.
	 */
	@Override
	public void generateResources() {
		int		base = planet.getRadius() * 5;
		
		switch (planet.getTemperature()) {
		case UltraHot:
		case ExtremelyHot:
		case VeryHot:
		case Hot:
			addResource("Silicate Ore", base / 5 + Die.d6());
			addResource("Ferric Ore", base + Die.d6());
			addResource("Heavy Metals", base / 2 + Die.d6());
			addResource("Rare Metals", base / 3 + Die.d6());
			addResource("Radioactives", base / 4 + Die.d6());
			if (Die.d2() == 1) addResource("Precious Metals", base / 5 + Die.d6());
			break;
		case Warm:
		case Standard:
		case Cool:
			addResource("Silicate Ore", base + Die.d6());
			addResource("Ferric Ore", base  / 2 + Die.d6());
			addResource("Carbonic Ore", base  / 3 + Die.d6());
			addResource("Water", base / 2 + Die.d6());
			if (Die.d3() == 1) addResource("Heavy Metals", base / 3 + Die.d6());
			if (Die.d3() == 1) addResource("Rare Metals", base / 5 + Die.d6());				
			if (Die.d3() == 1) addResource("Precious Metals", base / 10 + Die.d4());
			if (Die.d4() == 1) addResource("Silicate Crystals", base/ 5 + Die.d6());
			break;
		case Cold:
		case VeryCold:
			addResource("Silicate Ore", base + Die.d6());
			addResource("Ferric Ore", base  / 3 + Die.d6());
			addResource("Carbonic Ore", base  / 2 + Die.d6());
			addResource("Water", base + Die.d6());
			if (Die.d2() == 1) addResource("Silicate Crystals", base/ 5 + Die.d6());
			if (Die.d6() == 1) addResource("Rare Metals", base / 5 + Die.d6());
			break;
		case ExtremelyCold:
		case UltraCold:
			addResource("Silicate Ore", base / 2 + Die.d6());
			addResource("Ferric Ore", base  / 6 + Die.d6());
			addResource("Carbonic Ore", base  / 3 + Die.d6());
			addResource("Water", base + Die.d6());
			addResource("Silicate Crystals", base / 3 + Die.d6());
			break;
		}
	}
}
