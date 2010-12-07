package uk.org.glendale.mapcraft.map;

/**
 * A place is where a Thing (village, town) is found. Each named place is
 * unique and has a set of coordinates on the map. Coordinates are identified
 * by the hex tile the place is attached to, and sub-coordinates within that.
 * Sub-coordinates range from 0-99, with 0,0 being top left.
 * 
 * @author Samuel Penn
 */
public class NamedPlace {
	private int		id;
	private int		thingId;
	private String	name;
	private String	title;
	private short	importance;
	private int		x;
	private int		y;
	private int		sx;
	private int		sy;
	
	public NamedPlace(int id, int thingId, String name, String title, short importance, int x, int y, int sx, int sy) {
		this.id = id;
		this.thingId = thingId;
		this.name = name;
		this.title = title;
		this.importance = importance;
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
	}
	
	/**
	 * Gets the unique id used to identify place.
	 * 
	 * @return		Unique id of this place.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the id of the type of thing this place is.
	 * 
	 * @return	Id of type of thing.
	 */
	public int getThingId() {
		return thingId;
	}
	
	/**
	 * Gets the unique name of this place. Each name is unique, and is
	 * used to reference the place externally. A place name should be
	 * a lower case uri (e.g. 'london-city') that makes sense.
	 * 
	 * @return	The unique name for this place.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the human readable name for this place. It does not have to
	 * be unique, and is only used for display purposes.
	 * 
	 * @return	Display name for this place.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Gets the importance of this place. The importance is on a scale of
	 * 0-3, with 3 being the most important. This is used to define when
	 * places are drawn on a map - least important places won't be displayed
	 * on larger scale maps.
	 * 
	 * @return	Importance, 0-3.
	 */
	public short getImportance() {
		return importance;
	}

	/**
	 * Gets the X coordinate of the tile this place is attached to.
	 * 
	 * @return	X coordinate of tile.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the Y coordinate of the tile this place is attached to.
	 * 
	 * @return	Y coordinate of tile.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Gets the X coordinate within the tile for this place. Sub
	 * coordinates range from 0-99, with 0,0 being top left.
	 * 
	 * @return	Gets the X sub-coordinate, 0-99.
	 */
	public int getSubX() {
		return sx;
	}

	/**
	 * Gets the Y coordinate within the tile for this place. Sub
	 * coordinates range from 0-99, with 0,0 being top left.
	 * 
	 * @return	Gets the Y sub-coordinate, 0-99.
	 */
	public int getSubY() {
		return sy;
	}
}
