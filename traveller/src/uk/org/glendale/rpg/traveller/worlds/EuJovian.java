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
 * Generates a map for a Jovian world like Jupiter.
 * 
 * @author Samuel Penn.
 */
public class EuJovian extends WorldBuilder {
	public EuJovian() {
		super();
	}
	
	public EuJovian(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		Terrain[]	bands = new Terrain[] { Terrain.CreamGas, Terrain.WhiteGas, Terrain.CreamGas,
											Terrain.WhiteGas, Terrain.OrangeGas, Terrain.WhiteGas,
											Terrain.WhiteGas, Terrain.CreamGas, Terrain.YellowGas,
											Terrain.WhiteGas, Terrain.CreamGas, Terrain.WhiteGas,
											Terrain.WhiteGas, Terrain.OrangeGas, Terrain.WhiteGas,
											Terrain.CreamGas, Terrain.WhiteGas, Terrain.CreamGas };
		double[]	heights = new double[bands.length];
		
		// Create random 'heights' for each band, which controls the darkness.
		for (int i=0; i < heights.length; i++) {
			heights[i] = Die.d10(2) / 10.0;
		}
		
		// Now mix the bands up a bit.
		for (int i=0; i < bands.length/3; i++) {
			int			b1 = Die.rollZero(bands.length);
			int			b2 = Die.rollZero(bands.length);
			Terrain		t1 = bands[b1];
			Terrain		t2 = bands[b2];
			
			bands[b1] = t2;
			bands[b2] = t1;
			
			switch (Die.d6(3)) {
			case 3:
				bands[b1] = Terrain.GreenGas;
				break;
			case 4: case 5:
				bands[b1] = Terrain.RedGas;
				break;
			}
		}
		
		
		// Generate bands of colour.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				int bandHeight = height/bands.length;
				int	bandNumber = (y+Die.d10(2)-Die.d10(2))/bandHeight;
				if (bandNumber < 0) bandNumber = 0;
				if (bandNumber >= bands.length) bandNumber = bands.length - 1;
				setTerrain(x, y, bands[bandNumber]);
				setHeight(x, y, (int)(getHeight(x, y)*heights[bandNumber]));
			}
		}
	}
}
