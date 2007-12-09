package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Vestian world. They have a core, mantle and crust,
 * but are long since geologically dead.
 * 
 * @author Samuel Penn.
 */
public class Vestian extends WorldBuilder {
	public Vestian() {
		super();
	}
	
	public Vestian(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		// Let's have some asteroid impacts.
		impacts(100, 20, 1.2, Terrain.Snow);
		impacts(200, 25, 0.7, Terrain.Dark);
		impacts(500, 15, 0.9, Terrain.Rock);
		impacts(200, 15, 0.5, Terrain.Rock);
		
		// Now set the colours.
		/*
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, Terrain.Dark);
			}
		}
		*/
	}
}
