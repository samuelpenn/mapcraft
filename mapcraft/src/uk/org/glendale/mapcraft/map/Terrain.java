package uk.org.glendale.mapcraft.map;

public class Terrain {
	private int			id;
	private String		name;
	private String		image;
	private int			height;
	
	public Terrain(int id, String name, String image, int height) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.height = height;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getImage() {
		return image;
	}
	
	public int getHeight() {
		return height;
	}
}
