/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Hephaestian world such as Io.
 * 
 * @author Samuel Penn.
 */
public class Hephaestian extends WorldBuilder {
	public Hephaestian() {
		super();
	}
	
	public Hephaestian(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		// Snow/Ice world.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, Terrain.Sulphur);
			}
		}
		// Let's have some asteroid impacts.
		impacts(10, 15, 1.5, Terrain.Methane);
		impacts(20, 25, 0.9, Terrain.Larva);
		impacts(30, 25, 0.5, Terrain.Sulphur);
		impacts(100, 15, 1.2, Terrain.Sulphur);
	}
}
