package uk.org.glendale.worldgen.astro.planet.builders;

public class Tile {
	private String	name;
	private int		rgb;
	private boolean isWater; 
	
	public Tile(String name, int rgb, boolean isWater) {
		this.name = name;
		this.rgb = rgb;
		this.isWater = isWater;
	}
}
