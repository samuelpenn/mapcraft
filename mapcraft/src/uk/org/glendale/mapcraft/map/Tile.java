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
	
	private int			water = 0;
	private int			woods = 0;
	private int			hills = 0;
	private int			fertility = 0;
		
	public Tile(int id, String name, String title, String image) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.image = image;
	}

	public Tile(int id, String name, String title, String image, int water, int woods, int hills, int fertility) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.image = image;
		this.water = water;
		this.woods = woods;
		this.hills = hills;
		this.fertility = fertility;
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
	
	/**
	 * Gets the water content of this tile. This will be 0 for dry land,
	 * 20-50 for marshland, and 100 for sea tiles.
	 *  
	 * @return	Water coverage, 0-100.
	 */
	public int getWater() {
		return water;
	}
	
	/**
	 * Gets the tree coverage for this tile. Will be zero for deserts,
	 * 5-10 for most temperate grasslands, 20-30 for light woods and
	 * 50+ for most heavy woodland.
	 * 
	 * @return	Tree coverage, 0-100.
	 */
	public int getWoods() {
		return woods;
	}
	
	/**
	 * Gets how hilly the tile is. Flat land will be less than 10, low
	 * hills about 15, high hills 30, low mountains 50, high mountains
	 * 75 and the tallest mountains 90+.
	 * 
	 * @return	How hilly the tile is, 0-100.
	 */
	public int getHills() {
		return hills;
	}
	
	/**
	 * Gets the fertility of the tile. This will be 75+ for good temperate
	 * land, about 25 for scrubland and less than 10 for deserts.
	 * 
	 * @return	Fertility of tile, 0-100.
	 */
	public int getFertility() {
		return fertility;
	}
	
	public abstract String getPrefix();
}
