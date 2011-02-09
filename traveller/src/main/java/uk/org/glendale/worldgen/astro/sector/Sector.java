package uk.org.glendale.worldgen.astro.sector;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.Entity;

/**
 * Defines a sector of space. A sector in the Traveller universe consists of
 * a region 32 parsecs wide by 40 high. Location 0101 is the top left of the
 * sector, and 3240 is the bottom right.
 * 
 * Each sector is divided into 16 sub-sectors, labelled a-p, each 8x10 parsecs
 * in size. A parsec is a represented on a Sector map is a single hex, which
 * may contain zero or one stars systems.
 * 
 * @author Samuel Penn
 */
@Entity @Table(name="sector")
@XmlRootElement(name="sector")
public class Sector {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int			id;
	
	// Persisted fields.
	@Column(name="name")		private String		name;
	@Column(name="x")			private int			x;
	@Column(name="y")			private	int			y;
	@Column(name="codes")		private String		codes;
	@Column(name="allegiance")	private String		allegiance;
	
	public static final	int			WIDTH = 32;
	public static final int			HEIGHT = 40;
	
	public Sector() {
		this.id = 0;
		this.name = "Unnamed";
		this.x = 0;
		this.y = 0;
		this.codes = "";
		this.allegiance = "Un";
	}
	
	Sector(String name, int x, int y, String codes, String allegiance) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.codes = codes.trim();
		this.allegiance = allegiance.trim();
	}
	
	/**
	 * Gets the unique internal id for this star sector, as stored
	 * in the database. If it is zero, then the sector has not yet
	 * been persisted.
	 * 
	 * @return		Internal sector id.
	 */
	@XmlAttribute
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the unique name for the star sector. A sector name can
	 * consist of numerics, alphabetic and punctuation, but may not
	 * consist entirely of numerics.
	 * 
	 * @return		Common sector name.
	 */
	@XmlElement
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the X coordinate of the sector, where x=0 is the core
	 * sector. The coordinate will be positive for sectors trailing
	 * the core ('East'), and negative for sectors spinward of the
	 * core ('West'). All coordinates are in numbers of sectors,
	 * not in parsecs.
	 * 
	 * @return		X coordinate of this sector.
	 */
	@XmlElement
	public int getX() {
		return x;
	}
	
	/**
	 * Gets the Y coordinate of the sector, where y=0 is the core
	 * sector. The coordinate will be positive for sectors coreward
	 * the core ('North'), and negative for sectors rimward of the
	 * core ('South'). All coordinates are in numbers of sectors,
	 * not in parsecs.
	 * 
	 * @return		Y coordinate of this sector.
	 */
	@XmlElement
	public int getY() {
		return y;
	}
	
	public String toString() {
		return name;
	}
}
