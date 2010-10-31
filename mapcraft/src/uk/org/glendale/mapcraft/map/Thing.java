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
public class Thing {
	private int		id;
	private String	name;
	private String	title;
	private int		x;
	private int		y;
	private int		sx;
	private int		sy;
	
	public Thing(int id, String name, String title, int x, int y, int sx, int sy) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getSubX() {
		return sx;
	}
	
	public int getSubY() {
		return sy;
	}
}
