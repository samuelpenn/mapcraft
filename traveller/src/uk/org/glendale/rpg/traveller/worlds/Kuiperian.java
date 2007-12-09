/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Kuiperian world such as Pluto. Methane ices
 * and other cold stuff.
 * 
 * @author Samuel Penn.
 */
public class Kuiperian extends WorldBuilder {
	public Kuiperian() {
		super();
	}
	
	public Kuiperian(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		// Snow/Ice world.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, Terrain.Ice);
			}
		}
		// Let's have some asteroid impacts.
		impacts(500, 25, 0.7, Terrain.Methane);
		impacts(300, 25, 0.8, Terrain.DirtyMethane);
		impacts(100, 20, 0.6, Terrain.Dark);
		impacts(20, 20, 1.1, Terrain.Methane);
		impacts(100, 20, 1.5, Terrain.Ice);
		impacts(300, 20, 1.5, Terrain.DirtyMethane);
	}
}
