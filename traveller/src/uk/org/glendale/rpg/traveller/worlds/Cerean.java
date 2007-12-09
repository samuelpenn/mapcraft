package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Cerean world. These are dwarf planets, little more
 * than large asteroids, with just enough gravity to pull them into a sphere.
 * They are mostly carbonic, with a few other elements.
 * 
 * @author Samuel Penn.
 */
public class Cerean extends WorldBuilder {
	public Cerean() {
		super();
	}
	
	public Cerean(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		// Let's have some asteroid impacts.
		impacts(500, 25, 0.7, Terrain.Dark);
		
		// Now set the colours.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, Terrain.Dark);
			}
		}
	}
}
