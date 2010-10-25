package uk.org.glendale.mapcraft.map;

/**
 * Represents a feature, which is the second layer on the hex map.
 * Features modify the terrain, and are normally hills, mountains
 * or other types of landscapes.
 * 
 * @author Samuel Penn
 */
public class Feature extends Tile {

	public Feature(int id, String name, String title, String image) {
		super(id, name, title, image);
	}

	public String getPrefix() {
		return "/features/";
	}
}
