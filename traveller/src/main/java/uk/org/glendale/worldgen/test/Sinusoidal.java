package uk.org.glendale.worldgen.test;

import uk.org.glendale.worldgen.astro.planet.builders.Tile;

public class Sinusoidal {
	public static final int MAP_WIDTH = 1024;
	public static final int MAP_HEIGHT = MAP_WIDTH / 2;

	public static final int TILE_SIZE = 16;

	public static final int TILE_WIDTH = MAP_WIDTH / TILE_SIZE;
	public static final int TILE_HEIGHT = MAP_HEIGHT / TILE_SIZE;

	/** Detailed surface map of the world */
	protected Tile[][] map = null;
	/** Low res surface map of the world */
	protected Tile[][] tileMap = null;
	/** High res fractal height map of the world */
	protected int[][] heightMap = null;
	
	private int[]		rowWidth = new int[MAP_HEIGHT];
	
	private int getLatitude(int y) {
		return (MAP_HEIGHT/2) - y; 
	}
	
	private int getWidth(int y) {
		return 0;
	}
	
	public Sinusoidal() {
		for (int y=0; y < MAP_HEIGHT; y++) {
			
		}
	}
}
