/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.awt.Point;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Barren worlds are rocky worlds with little or no atmosphere, no surface water
 * and no life. They may potentially be rich in mineral resources, but have
 * little else going for them.
 * 
 * @author Samuel Penn
 */
public abstract class BarrenWorld extends WorldBuilder {
	
	protected static final Tile	LIGHT = new Tile ("Light", "#CCCCCC", false);
	protected static final Tile	DARK = new Tile("Dark", "#BBBBBB", false);
	
	private int numCraters = 150;
	private int craterSize = 25;
	private int minCraterSize = 0;
	private int craterSharpness = 2;

	public BarrenWorld() {
		map = new Tile[model.getTotalHeight()][];
		for (int y=0; y < model.getTotalHeight(); y++) {
			map[y] = new Tile[model.getWidthAtY(y)];
			for (int x=0; x < model.getWidthAtY(y); x++) {
				map[y][x] = BLANK;
			}
		}
	}

	/**
	 * Sets the number of craters to be drawn. Defaults to be 150 if not set.
	 * 
	 * @param numCraters
	 *            Number of craters.
	 */
	protected final void setCraterNumbers(int numCraters) {
		this.numCraters = numCraters;
	}

	/**
	 * Sets the average size of craters. Defaults to 25.
	 * 
	 * @param craterSize
	 *            Size of craters.
	 */
	protected final void setCraterSize(int craterSize) {
		this.craterSize = craterSize;
	}

	/**
	 * Sets the minimum size of craters. Craters below this size are not drawn.
	 * Defaults to 0, and should remain so for inactive worlds without an
	 * atmosphere. Allows for small craters to have been eroded by
	 * geological/atmospheric processes.
	 * 
	 * @param minCraterSize
	 *            Minimum crater size.
	 */
	protected final void setCraterMinSize(int minCraterSize) {
		this.minCraterSize = minCraterSize;
	}

	protected final void setCraterSharpness(int craterSharpness) {
		this.craterSharpness = craterSharpness;
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.setPressure(AtmospherePressure.Trace);
		}
		generateMap();
		generateResources();
	}
	
	private void drawRandomBarrenWorld() {
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[model.getWidthAtY(tileY)];
			for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
				if (Die.d20() == 1) {
					map[tileY][tileX] = DARK;
				} else {
					map[tileY][tileX] = LIGHT;
				}
			}
		}

		for (int i=0; i < 4; i++) {
			Tile[][] tmp = new Tile[12][];
			
			for (int tileY=0; tileY < 12; tileY++) {
				tmp[tileY] = new Tile[model.getWidthAtY(tileY)];
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					tmp[tileY][tileX] = map[tileY][tileX];
				}
			}

			for (int tileY=0; tileY < 12; tileY++) {
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					if (tmp[tileY][tileX] == DARK) {
						try {
							switch (Die.d3()) {
							case 1:
								// West.
								map[tileY][model.getWest(tileX, tileY)] = DARK;
								break;
							case 2:
								// East.
								map[tileY][model.getEast(tileX, tileY)] = DARK;
								break;
							case 3:
								// North/South.
								Point p = model.getUpDown(tileX, tileY);
								map[(int)p.getY()][(int)p.getX()] = DARK;
								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							//map[tileY][tileX] = red;
							System.out.println(tileX +", "+ tileY);
						}
					}
				}
			}
		}
		
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			// return;
		}
		drawRandomBarrenWorld();
		
		getImage();
	}


}
