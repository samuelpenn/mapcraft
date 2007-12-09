package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Hadean world. These are generally small, hot worlds close
 * to their star. They have lost most of their mantle, leaving just a core of heavy
 * metals. They are lifeless and airless.
 * 
 * @author Samuel Penn.
 */
public class Hadean extends WorldBuilder {
	public Hadean() {
		super();
	}
	
	public Hadean(int width, int height) {
		super(width, height);
	}
	
	public void generate() {
		// Let's have some asteroid impacts.
		impacts(100, 25, 0.9, Terrain.Dark);
		impacts(500, 10, 0.7, Terrain.Rust);
		
		// Now set the colours.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				if (getTerrain(x, y) == Terrain.Dark) {
					// Do nothing.
				} else if (20 + Die.d20(2) > getHeight(x, y)) {
					setTerrain(x, y, Terrain.Rust);
				} else {
					setHeight(x, y, (int)(getHeight(x, y) * 0.75));
					setTerrain(x, y, Terrain.Rock);
				}
			}
		}
	}
}
