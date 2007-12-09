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

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.SpectralType;
import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a barren world. Barren worlds are dry rock worlds
 * with craters and mountains but little in the way of other features.
 * This class supports a large number of planet types.
 * 
 * @author Samuel Penn.
 */
public class Star extends WorldBuilder {
	private SpectralType		spectral = null;
	private Terrain				terrain = null;
	private Terrain				sunspot = null;

	public Star() {
		super();
	}
	
	public Star(int width, int height, SpectralType spectral) {
		super(width, height);
		
		this.spectral = spectral;
		String	letter = spectral.toString().substring(0, 1);
		this.terrain = Terrain.valueOf(letter);
		this.sunspot = Terrain.valueOf(letter+"spot");
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
