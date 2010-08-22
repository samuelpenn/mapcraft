package uk.org.glendale.worldgen.astro.planet.builders;

public class Tile {
	private String	name;
	private String	rgb;
	private boolean isWater; 
	
	public Tile(String name, String rgb, boolean isWater) {
		this.name = name;
		this.rgb = rgb;
		this.isWater = isWater;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRGB() {
		return rgb;
	}
	
	private String getHex(int v) {
		if (v > 255) v = 255;
		if (v < 0) v = 0;
		
		return ((v<16)?"0":"")+Integer.toHexString(v);
	}
	
	public String getRGB(int modifier) {
		int		r = Integer.parseInt(rgb.substring(1, 3), 16) + modifier;
		int		g = Integer.parseInt(rgb.substring(3, 5), 16) + modifier;
		int		b = Integer.parseInt(rgb.substring(5, 7), 16) + modifier;

		return "#"+getHex(r)+getHex(g)+getHex(b);
	}
	
	public boolean isWater() {
		return isWater;
	}
}
