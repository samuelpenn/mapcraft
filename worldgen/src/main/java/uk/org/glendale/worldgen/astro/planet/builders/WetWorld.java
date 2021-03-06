/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import static java.lang.Math.sqrt;
import static uk.org.glendale.rpg.utils.Die.d100;
import static uk.org.glendale.rpg.utils.Die.d4;
import static uk.org.glendale.rpg.utils.Die.d6;
import static uk.org.glendale.rpg.utils.Die.rollZero;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * A WetWorld is one that has, or has had, an ocean of some sort. It's landscape
 * has been shaped by hydrographic processes.
 * 
 * @author Samuel Penn
 */
public abstract class WetWorld extends PlanetBuilder {
	protected Tile sea = new Tile("Sea", "#4444aa", true);
	protected Tile seabed = new Tile("Seabed", "#777766", false);
	protected Tile land = new Tile("Land", "#999988", false);
	protected Tile mountains = new Tile("Mountains", "#aaaa99", false);

	private int initialHydrographics = 0;
	private int finalHydrographics = 0;

	@Override
	public void generate() {
		// TODO Auto-generated method stub
		setHydrographics(70, 10);
		generateMap();
	}

	private int getHydrographics() {
		int waterCount = 0;
		int landCount = 0;

		for (int y = 0; y < MAP_HEIGHT; y++) {
			for (int x = 0; x < MAP_WIDTH; x++) {
				if (map[y][x] == OUT_OF_BOUNDS) {
					// Ignore.
				} else if (map[y][x].isWater()) {
					waterCount++;
				} else {
					landCount++;
				}
			}
		}
		return (waterCount * 100) / (waterCount + landCount);
	}

	private void addWater() {
		for (int y = 0; y < MAP_HEIGHT; y++) {
			for (int x = 0; x < MAP_WIDTH; x++) {
				if (map[y][x].isWater()) {
					heightMap[y][x] /= 4;
				}
			}
		}
		int waterCount = 0;
		int landCount = 0;

		for (int y = 0; y < MAP_HEIGHT; y++) {
			for (int x = 0; x < MAP_WIDTH; x++) {
				if (map[y][x] == OUT_OF_BOUNDS) {
					// Ignore.
				} else if (map[y][x].isWater()) {
					waterCount++;
				} else {
					landCount++;
				}
			}
		}
		int totalSurface = waterCount + landCount;
		int desiredWater = (totalSurface * initialHydrographics) / 100;

		System.out.println(getHydrographics());

		// Add more water if it is required.
		addWaterLoops: while (waterCount < desiredWater) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				for (int x = 0; x < MAP_WIDTH; x++) {
					if (!map[y][x].isWater() && map[y][x] != OUT_OF_BOUNDS) {
						if (d6() <= getWaterCount(map, x, y)) {
							map[y][x] = sea;
							waterCount++;
							if (waterCount >= desiredWater) {
								break addWaterLoops;
							}
						}
					}
				}
			}
		}
		System.out.println(getHydrographics());

		if (initialHydrographics <= finalHydrographics) {
			return;
		}

		landCount = totalSurface - waterCount;
		int desiredLand = (totalSurface * (100 - finalHydrographics)) / 100;

		// Add land back in. This assumes that the sea has dried up since
		// initial coastal formation, so exposed dry sea bed.
		addLandLoops: while (landCount < desiredLand) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				for (int x = 0; x < MAP_WIDTH; x++) {
					if (map[y][x].isWater() && map[y][x] != OUT_OF_BOUNDS) {
						/*
						 * if (Die.d100(3) <= 3 + getLandCount(map, x, y) * 50)
						 * { map[y][x] = seabed; landCount++; if (landCount >=
						 * desiredLand) { break addLandLoops; } }
						 */
						if (getLandCount(map, x, y) > 0 && d4() == 1) {
							map[y][x] = seabed;
							if (landCount++ >= desiredLand) {
								break addLandLoops;
							}
						} else if (d100(3) <= 5) {
							map[y][x] = seabed;
							if (landCount++ >= desiredLand) {
								break addLandLoops;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the hydrographics percentage for this world.
	 */
	protected final void setHydrographics(int hydrographics) {
		super.setHydrographics(hydrographics);
		this.initialHydrographics = hydrographics;
		this.finalHydrographics = hydrographics;
	}

	/**
	 * Set the world's hydrographics. The first value was the world at its
	 * wettest point, the second value is the current value. If they are
	 * different, then dry sea bed is drawn around the coast line to account for
	 * the difference.
	 * 
	 * @param starting
	 *            Wettest hydrographics point.
	 * @param ending
	 *            Current hydrographics. Must not be larger than starting.
	 */
	protected final void setHydrographics(int starting, int ending) {
		if (starting > 100) {
			starting = 100;
		}
		if (ending > 100) {
			ending = 100;
		}
		if (starting < 0) {
			starting = 0;
		}
		if (ending < 0) {
			ending = 0;
		}
		if (ending > starting) {
			throw new IllegalArgumentException(
					"Final hydrographics percentage must not be larger than initial percentage");
		}
		this.initialHydrographics = starting;
		this.finalHydrographics = ending;
	}

	private int numberCraters = 100;
	private int minCraterSize = 10;
	private int maxCraterSize = 20;

	private void addCraters() {
		for (int c = 0; c < numberCraters; c++) {
			int x = rollZero(MAP_WIDTH);
			int y = rollZero(MAP_HEIGHT / 2) + MAP_HEIGHT / 4;

			int r = rollZero(maxCraterSize - minCraterSize) + minCraterSize;

			int h = fractalMap[y][x] - 20;
			boolean isWater = false;

			for (int xx = x - r; xx < x + r; xx++) {
				for (int yy = y - r; yy < y + r; yy++) {
					int d = (int) sqrt((xx - x) * (xx - x) + (yy - y)
							* (yy - y));
					if (d + d4() < r * 0.9) {
						setHeight(xx, yy, 0.6);
						if (getTile(map, yy, xx).isWater()) {
							System.out.println("Water!");
							isWater = true;
						}
					} else if (d < r) {
						setHeight(xx, yy, 0.8);
					}
				}
			}
			if (isWater) {
				System.out.println("Is Water!!!");
				for (int xx = x - r; xx < x + r; xx++) {
					for (int yy = y - r; yy < y + r; yy++) {
						if (Math.sqrt((xx - x) * (xx - x) + (yy - y) * (yy - y))
								+ d4() < r) {
							setTile(map, yy, xx, sea);
						}
					}
				}

			}
		}
	}

	@Override
	public final void generateMap() {
		if (!AppManager.getDrawMap()) {
			// If map drawing is disabled, don't do it.
			return;
		}
		setFractalColour("#000000");
		super.setHydrographics(initialHydrographics);
		addContinents(sea, land, mountains);
		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);
		addCraters();
		addWater();

		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		getImage();
	}

	@Override
	public abstract void generateResources();
}
