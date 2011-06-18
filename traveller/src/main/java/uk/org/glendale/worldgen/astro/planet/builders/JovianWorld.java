/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.ice.Europan;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Abstract class for creating Jovian worlds such as Jupiter or Saturn. There
 * are several sub types, including EuJovian, SubJovian and CryoJovian. World
 * maps are simply the outer cloud layers, no surface maps are generated.
 * 
 * Currently, cloud maps consist of simple stripes with no weather patterns.
 * 
 * @author Samuel Penn
 */
public abstract class JovianWorld extends PlanetBuilder {
	protected List<Tile> tiles;

	public JovianWorld() {
	}

	@Override
	public void generate() {
		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#999977", false));
		tiles.add(new Tile("Light", "#cccc99", false));

		generateMap();
		generateResources();
	}

	protected Tile getBand(int y) {
		return tiles.get((y / 2) % tiles.size());
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			return;
		}
		map = new Tile[TILE_HEIGHT][TILE_WIDTH];
		heightMap = new int[TILE_HEIGHT][TILE_WIDTH];
		for (int y = 0; y < TILE_HEIGHT; y++) {
			for (int x = 0; x < TILE_WIDTH; x++) {
				if (x < getWest(y) || x >= getEast(y)) {
					map[y][x] = OUT_OF_BOUNDS;
					heightMap[y][x] = 0;
				} else {
					map[y][x] = getBand(y);
					heightMap[y][x] = Die.d4();
				}
			}
		}
		map = scaleMap(map, TILE_SIZE);
		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}

		getImage();
	}

	@Override
	public void generateResources() {
		addResource("Hydrogen", 60 + Die.d20(2));
	}

	private PlanetBuilder[] moonBuilders = null;

	/**
	 * Gets a list of planet types typically found as moons of a Jovian world.
	 * Jovian worlds can have lots of moons, though generally only the larger
	 * ones will be listed here. It can be assumed that there will also be a
	 * large number of captured asteroids.
	 */
	public PlanetBuilder[] getMoonBuilders() {
		if (moonBuilders != null) {
			return moonBuilders;
		}
		int numMoons = Die.d3(2);

		System.out.println("JovianWorlds: Adding " + numMoons + " moons");

		moonBuilders = new PlanetBuilder[numMoons];

		for (int i = 0; i < numMoons; i++) {
			moonBuilders[i] = new Europan();
		}

		return moonBuilders;
	}

}
