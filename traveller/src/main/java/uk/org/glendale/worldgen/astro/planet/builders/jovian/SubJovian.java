/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.jovian;

import java.util.ArrayList;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.JovianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;

/**
 * A Saturn like world. These are smaller and lighter than EuJovian worlds.
 * 
 * @author Samuel Penn
 */
public class SubJovian extends JovianWorld {
	public PlanetType getPlanetType() {
		return PlanetType.SubJovian;
	}

	@Override
	public void generate() {

		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.setDayLength(2000 + Die.d100() * 1000 + Die.die(10000));
		planet.setAxialTilt(Die.d10());
		planet.addTradeCode(TradeCode.H5);

		// Does this world have rings? Most Jovian worlds seem to.
		switch (Die.d6(3)) {
		case 3:
			planet.addFeature(PlanetFeature.ExtensiveIceRings);
			break;
		case 4:
			planet.addFeature(PlanetFeature.BrightIceRings);
			break;
		case 5:
		case 6:
			planet.addFeature(PlanetFeature.IceRings);
			break;
		case 7:
		case 8:
		case 9:
		case 10:
			planet.addFeature(PlanetFeature.FaintIceRings);
			break;
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
			planet.addFeature(PlanetFeature.PartialIceRings);
			break;
		}

		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#bbbb99", false));
		tiles.add(new Tile("Light", "#dddddd", false));
		tiles.add(new Tile("White", "#f0f0f0", false));
		tiles.add(new Tile("Yellow", "#aaaa77", false));

		// setFractalColour("#ffffff");

		generateMap();
		generateResources();
	}

	protected Tile getBand(int y) {
		if (y < TILE_HEIGHT / 8 || y >= TILE_HEIGHT - (TILE_HEIGHT / 8)) {
			return tiles.get((y / 2) % 2);
		} else if (y % 8 == 0) {
			return tiles.get(3);
		}
		return tiles.get((y / 2) % 2 + 1);
	}

	/**
	 * Generate resources for Jupiter like worlds.
	 */
	public void generateResources() {
		addResource("Hydrogen", 60 + Die.d20(2));
		addResource("Helium 3", 10 + Die.d8(2));
		addResource("Oxygen", Die.d4(2));
		addResource("Water", Die.d4(2));
	}
}
