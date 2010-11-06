package uk.org.glendale.mapcraft.map;

/**
 * Represents a type of terrain. Terrain is the bottom layer of the hex map,
 * and denotes the basic land type. This is generally either water (sea or ocean)
 * or a vegetation type if on land. 
 * 
 * @author Samuel Penn
 *
 */
public class Terrain extends Tile {
	private int			altitude;
	private int			climate;
	private String		colour;
	
	public Terrain(int id, String name, String title, String image, int altitude) {
		super(id, name, title, image);
		this.altitude = altitude;
	}
	
	public Terrain(int id, String name, String title, String image, int water, int woods, int hills, int fertility, String colour) {
		super(id, name, title, image, water, woods, hills, fertility);
		this.colour = colour;
	}
	
	public String getColour() {
		return colour;
	}
	
	public String getPrefix() {
		return "/terrain/";
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
