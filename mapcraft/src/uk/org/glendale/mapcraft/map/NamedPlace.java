package uk.org.glendale.mapcraft.map;

/**
 * A place is where a Thing (village, town) is found.
 * 
 * @author Samuel Penn
 */
public class NamedPlace {
	private int		id;
	private int		thingId;
	private String	name;
	private int		x;
	private int		y;
	private int		sx;
	private int		sy;
	
	public NamedPlace(int id, int thingId, String name, int x, int y, int sx, int sy) {
		this.id = id;
		this.thingId = thingId;
		this.name = name;
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
	}
	
	public int getId() {
		return id;
	}
	
	public int getThingId() {
		return thingId;
	}
	
	public String getName() {
		return name;
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
