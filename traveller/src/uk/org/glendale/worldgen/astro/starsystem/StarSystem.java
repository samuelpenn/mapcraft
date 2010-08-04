package uk.org.glendale.worldgen.astro.starsystem;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.rpg.traveller.systems.StarSystem.Zone;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.sector.Sector;

/**
 * Represents a star system within a sector. A star system will have one or
 * more stars and zero or more planets associated with it.
 * 
 * @author Samuel Penn
 */
@Entity @Table(name="system")
@XmlRootElement(name="system")
public class StarSystem {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int			id;
	
	// Persisted fields.
	@Column(name="name")		private String		name;
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="sector_id", referencedColumnName = "id")
	private Sector		sector;
	@Column(name="x")			private int			x;
	@Column(name="y")			private int			y;
	@Column(name="allegiance")	private String		allegiance;
	@Column(name="zone")		private Zone		zone;
	@Column(name="base")		private String		base;
	@Column(name="uwp")			private String		uwp;
	@Column(name="selection")	private int			selection;
	
	@OneToMany(mappedBy="system")
	private List<Planet>	planets;
	

	public StarSystem() {
		
	}
	
	/**
	 * Gets the unique ID for this stat system.
	 * 
	 * @return	Unique ID of the star system.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the name of this star system. This is not guaranteed to be
	 * unique, though will generally be within a given sector.
	 * 
	 * @return		Name of this star system.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the sector to which this star system belongs.
	 * 
	 * @return		Sector which this system is in.
	 */
	public Sector getSector() {
		return sector;
	}
	
	/**
	 * Gets the X coordinate of the system within the sector, ranging
	 * from 1 through to 40 inclusive.
	 * 
	 * @return	X coordinate, 1-40.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Gets the Y coordinate of the system within the sector, ranging
	 * from 1 through to 32 inclusive.
	 * 
	 * @return	Y coordinate, 1-32.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Gets the two letter allegiance code for this star system. If
	 * the allegiance is unknown, or the system is unaligned, then 'Un'
	 * will be returned. 'Im' is Imperium.
	 * 
	 * @return		Two character allegiance code.
	 */
	public String getAllegiance() {
		return allegiance;
	}
	
	/**
	 * Gets the zone rating of this system. This is normally Red, Amber
	 * or Green. By far the majority of systems will be Green.
	 * 
	 * @return	Safety designation of this system.
	 */
	public Zone getZone() {
		return zone;
	}
	
	public String getBase() {
		return base;
	}
	
	public int getSelection() {
		return selection;
	}
	
	public String getUWPAsString() {
		return uwp;
	}
	
	public List<Planet> getPlanets() {
		return planets;
	}
	
	public String toString() {
		return String.format("%s (%02d%02d)", name, x, y);
	}
}
