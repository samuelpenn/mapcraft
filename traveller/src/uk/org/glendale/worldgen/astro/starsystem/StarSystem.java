package uk.org.glendale.worldgen.astro.starsystem;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.*;

import uk.org.glendale.rpg.traveller.systems.Zone;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.star.Star;

/**
 * Represents a star system within a sector. A star system will have one or
 * more stars and zero or more planets associated with it.
 * 
 * @author Samuel Penn
 */
@Entity @Table(name="system")
@XmlRootElement(name="system")
@FilterDef(name="noMoons")
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
	@Enumerated(EnumType.STRING)
	@Column(name="zone")		private Zone		zone;
	@Column(name="base")		private String		base; // Isn't used.
	@Column(name="uwp")			private String		uwp;
	@Column(name="selection")	private int			selection;
	
	@OneToMany(mappedBy="system", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@Where(clause="moon=0")
	private List<Planet>	planets;
	
	@OneToMany(mappedBy="system", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<Star>		stars;
	

	public StarSystem() {
		
	}
	
	public StarSystem(Sector sector, String name, int x, int y) {
		if (sector == null) {
			throw new IllegalArgumentException("StarSystem must belong to a valid Sector");
		}
		this.sector = sector;
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name must be valid");
		}
		this.name = name;
		if (x < 1 || x > 32) {
			throw new IllegalArgumentException("X coordinate must be between 1 and 40");
		}
		this.x = x;
		if (y < 1 || y > 40) {
			throw new IllegalArgumentException("Y coordinate must be between 1 and 32");
		}
		this.y = y;
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
	 * from 1 through to 32 inclusive.
	 * 
	 * @return	X coordinate, 1-32.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Gets the Y coordinate of the system within the sector, ranging
	 * from 1 through to 40 inclusive.
	 * 
	 * @return	Y coordinate, 1-40.
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
	
	public void setAllegiance(String allegiance) {
		this.allegiance = allegiance;
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
	
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	
	public int getSelection() {
		return selection;
	}
	
	public void setSelection(int selection) {
		this.selection = selection;
	}
	
	public String getUWPAsString() {
		return uwp;
	}
	
	/**
	 * Gets the list of planets in this star system. Only major planets are
	 * returned, moons are not listed and must be fetched from the Planet
	 * itself.
	 * 
	 * @return	List of major planets in this system.
	 */
	public List<Planet> getPlanets() {
		return planets;
	}
	
	public void addPlanet(Planet planet) {
		if (planets == null) {
			planets = new ArrayList<Planet>();
		}
		planets.add(planet);
	}
	
	public void setPlanets(List<Planet> planets) {
		this.planets = planets;
	}
	
	/**
	 * Gets the list of stars in this star system.
	 * 
	 * @return	List of stars in this system.
	 */
	public List<Star> getStars() {
		return stars;
	}
	
	public void addStar(Star star) {
		if (stars == null) {
			stars = new ArrayList<Star>();
		}
		stars.add(star);
	}
	
	public void setStars(List<Star> stars) {
		this.stars = stars;
	}
	
	public String toString() {
		return String.format("%s (%02d%02d)", name, x, y);
	}
}
