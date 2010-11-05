package uk.org.glendale.mapcraft.map;

/**
 * A Thing can exist anywhere on the map and is not part of a tile.
 * However, each thing is positioned relative to a Tile, and has
 * two coordinates - an (x,y) to specify the tile it is attached to,
 * and an (x',y') which is relative to that tile.
 * 
 * Multiple things may be attached to the same tile.
 * 
 * The sub-coordinates are specified in 1/100ths of a tile, specified
 * from the top left (as if the tile was a square).
 * 
 * @author Samuel Penn
 */
public class Thing extends Tile {
	
	public Thing(int id, String name, String title, String image) {
		super(id, name, title, image);
	}

	@Override
	public String getPrefix() {
		return "/things/";
	}
}
