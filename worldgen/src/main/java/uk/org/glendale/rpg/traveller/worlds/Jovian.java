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

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;

/**
 * Generates a map for a Jovian world.
 * 
 * @author Samuel Penn.
 */
public class Jovian extends WorldBuilder {
	
	public Jovian(Planet planet, int width, int height) {
		super(width, height);
		this.planet = planet;
	}
	
	/**
	 * Generate the cloud banding. Different types of Jovian worlds will
	 * tend to have different banding schemes.
	 */
	public void generate() {
		int			numberOfBands = 10;
		
		int			red = 100, redVar = 2;
		int			green = 100, greenVar = 2;
		int			blue = 100, blueVar = 2;
		
		PlanetType	type = planet.getType();
		switch (type) {
		case EuJovian:
			// Standard, as above.
			numberOfBands = 8 + Die.d4();
			break;
		case SubJovian:
			// Less variance.
			red = 200;
			green = 200;
			blue = 120;
			redVar = greenVar = 2;
			blueVar = 1;
			numberOfBands = 4 + Die.d4();
			break;
		case SuperJovian:
			numberOfBands = 8 + Die.d6();
			redVar = 3;
			break;
		case MacroJovian:
			numberOfBands = 8 + Die.d8();
			redVar = 4;
			greenVar = blueVar = 3;
			break;
		case CryoJovian:
			numberOfBands = 3 + Die.d8();
			redVar = 0;
			red = 100;
			green = blue = 120;
			break;
		case EpiStellarJovian:
			numberOfBands = 2 + Die.d8(2);
			redVar = 3;
			green = blue = 50;
			break;
		}
		
		Terrain[]	bands = new Terrain[numberOfBands];
		for (int band=0; band < numberOfBands; band++) {
			bands[band] = Terrain.create("Band"+band, red+Die.d20(redVar), green+Die.d20(greenVar), blue+Die.d20(blueVar), 0.5, 0.5, 0.5, false);
		}
				
		// Generate bands of colour.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				int bandHeight = height/bands.length;
				int	bandNumber = (y+Die.d10(2)-Die.d10(2))/bandHeight;
				if (bandNumber < 0) bandNumber = 0;
				if (bandNumber >= bands.length) bandNumber = bands.length - 1;
				setTerrain(x, y, bands[bandNumber]);
			}
		}
	}
}
