package uk.org.glendale.mapcraft.map;

/**
 * Defines a basic type of hex tile. Must be extended to be used.
 * Current sub-types are Terrain and Feature.
 * 
 * @author Samuel Penn
 */
public abstract class Tile {
	private int			id;
	private String		name;
	private String		title;
	private String		image;
		
	public Tile(int id, String name, String title, String image) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.image = image;
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
	
	public String getImage() {
		return image;
	}
	
	public abstract String getPrefix();
}
