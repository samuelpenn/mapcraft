/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/01/01 11:04:14 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.worldgen.astro.star.SpectralType;

/**
 * Generates a map for a barren world. Barren worlds are dry rock worlds
 * with craters and mountains but little in the way of other features.
 * This class supports a large number of planet types.
 * 
 * @author Samuel Penn.
 */
public class Star extends WorldBuilder {
	private Terrain				terrain = null;
	private Terrain				sunspot = null;

	public Star() {
		super();
	}

	public Star(int width, int height, SpectralType spectral) {
		super(width, height);
		
		String	letter = spectral.toString().substring(0, 1);
		
		if (letter.equals("O")) {
			terrain = Terrain.create("O", 170, 170, 220, 1.5, 1.5, 1.5, false);
			sunspot = Terrain.create("Ospot", 17, 17, 22, 1.5, 1.5, 2, false);
		} else if (letter.equals("B")) {
			terrain = Terrain.create("B", 160, 180, 250, 1.5, 1.5, 1, false);
			sunspot = Terrain.create("Bspot", 16, 18, 25, 1.5, 1.5, 2, false);
		} else if (letter.equals("A")) {
			terrain = Terrain.create("A", 220, 220, 220, 1, 1, 1, false);
			sunspot = Terrain.create("Aspot", 22, 22, 22, 2, 2, 2, false);
		} else if (letter.equals("F")) {
			terrain = Terrain.create("F", 210, 210, 170, 1.6, 1.6, 1.2, false);
			sunspot = Terrain.create("Fspot", 21, 21, 17, 2, 2, 1.5, false);
		} else if (letter.equals("G")) {
			terrain = Terrain.create("G", 200, 200, 160, 1.4, 1.4, 1, false);
			sunspot = Terrain.create("Gspot", 20, 20, 16, 2, 2, 1.5, false);
		} else if (letter.equals("K")) {
			terrain = Terrain.create("K", 200, 160, 150, 1.5, 1.5, 1.3, false);
			sunspot = Terrain.create("Kspot", 20, 16, 15, 2, 1.5, 1.3, false);
		} else if (letter.equals("M")) {
			terrain = Terrain.create("M", 200, 120, 120, 1.5, 1.5, 1.5, false);
			sunspot = Terrain.create("Mspot", 20, 12, 12, 2, 1.5, 1.5, false);
		}
		
	}
		
	public void generate() {
		// Now set the colours.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				if (getHeight(x, y) < 23) {
					setTerrain(x, y, sunspot);
				} else {
					setTerrain(x, y, terrain);
				}
			}
		}
	}
}
