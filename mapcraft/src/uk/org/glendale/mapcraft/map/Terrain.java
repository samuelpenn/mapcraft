package uk.org.glendale.mapcraft.map;

public class Terrain {
	private int			id;
	private String		name;
	private String		title;
	private String		image;
	
	private int			altitude;
	private int			climate;
	private int			water;
	private int			forest;
	private int			vegetation;
	
	public Terrain(int id, String name, String title, String image, int altitude) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.image = image;
		this.altitude = altitude;
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
	
	/**
	 * Gets the typical altitude of this terrain type. Zero is sea
	 * <ul>
	 * <li>0 = Sea level</li>
	 * <li>1 = Lowlands</li>
	 * <li>2 = Hills</li>
	 * <li>3 = Low mountains</li>
	 * <li>4 = Moderate mountains, alpine</li>
	 * <li>5 = High mountains</li>
	 * </ul>
	 * 
	 * @return		Typical altitude, 0-5 
	 */
	public int getAltitude() {
		return altitude;
	}
	
	/**
	 * Gets the typical climate of this terrain type.
	 * <ul>
	 * <li>-5 = Arctic</li>
	 * <li>-3 = Tundra</li>
	 * <li>-1 = Cold temperate</li>
	 * <li> 0 = Temperate</li>
	 * <li> 1 = Warm temperate</li>
	 * <li> 3 = Sub tropical</li>
	 * <li> 5 = Tropical</li>
	 * </ul>
	 * 
	 * @return		Typical climate, -5 to +5.
	 */
	public int getClimate() {
		return climate;
	}
}
