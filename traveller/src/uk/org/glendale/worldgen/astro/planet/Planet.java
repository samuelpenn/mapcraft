package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.StarportType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;


@Entity @Table(name="planet")
@XmlRootElement(name="planet")
public class Planet {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int			id;
	
	// Persisted fields.
	@Column(name="name")			private String				name;

	// Astronomical data
	@ManyToOne @JoinColumn(name="system_id", referencedColumnName = "id")
	private StarSystem			system;
	@Column(name="parent_id")		private int					parentId;
	@Column(name="moon")			private boolean				isMoon;
	@Column(name="distance")		private int					distance;
	@Column(name="radius")			private int					radius;
	
	// Planet data
	@Column(name="type")			private PlanetType			type;
	@Column(name="atmosphere")		private AtmosphereType		atmosphere;
	@Column(name="pressure")		private AtmospherePressure	pressure;
	@Column(name="life")			private LifeType			lifeLevel;
	@Column(name="temperature")		private Temperature			temperature;
	@Column(name="hydrographics")	private int					hydrographics;
	@Column(name="day")				private int					dayLength;
	
	// Civilisation data
	@Column(name="population")		private long				population;
	@Column(name="starport")		private StarportType		starport;
	@Column(name="government")		private GovernmentType		government;
	@Column(name="law")				private int					lawLevel;
	@Column(name="tech")			private int					techLevel;
	@Column(name="description")		private String				description;
	@Column(name="base")			private String				baseType;
	@Column(name="trade")			private String				tradeCodes;
	@Column(name="features")		private String				featureCodes;
	@Column(name="nextevent")		private long				nextEvent;
	
	/**
	 * Gets the unique id for this planet.
	 * @return		Unique planet id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the name of this planet. Names are unique within a star system.
	 * By default, a planet is named after the system, followed by a Roman
	 * numeral denoting its orbit. If it is a moon, this is followed by a
	 * letter. If the system has multiple stars, the planets around non
	 * primary stars have the star designation (Beta, Gamma etc) placed
	 * before the roman numeral. e.g., Karpaty IV/c is the 3rd moon of the
	 * 4th planet in the Karpaty system. Karpaty Beta II is the 2nd planet
	 * of the second star of the same system.
	 * 
	 * @return		Name of the planet.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name for this planet.
	 * 
	 * @param name		New name to give to the planet.
	 */
	public void setName(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
		this.name = name;
	}

	/**
	 * Gets the ID of the star system that this planet is in.
	 * @return	Star system id.
	 */
	public StarSystem getSystem() {
		return system;
	}
	
	/**
	 * Gets the ID of the object this planet orbits. If this is a planet,
	 * then this will be the ID of a star. If it is a moon (isMoon == true)
	 * then this will be another planet.
	 * 
	 * @return		ID of the star or planet that this object orbits.
	 */
	public int getParentId() {
		return parentId;
	}
	
	/**
	 * Gets the distance of this planet from its primary. If the primary is
	 * a star (isMoon == false), then the distance is measured in millions
	 * of km. If it is a moon (isMoon == true), then it is measured in km.
	 * 
	 * @return	Distance from primary, MKm if this is a star, km if it's a planet. 
	 */
	public int getDistance() {
		return distance;
	}
	
	public void setDistance(int distance) {
		if (distance <= 0) {
			throw new IllegalArgumentException("Distance must be greater than zero");
		}
		this.distance = distance;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("Radius cannot be negative");
		}
		this.radius = radius;
	}
	
	/**
	 * Is this planet a moon? If true, then this planet orbits another planet
	 * and is therefore considered to be a moon. If false, then the planet's
	 * primary is a star. Affects the distance units used.
	 * 
	 * @return		True iff a moon.
	 */
	public boolean isMoon() {
		return isMoon;
	}
	
	public PlanetType getType() {
		return type;
	}
	
	public void setType(PlanetType type) {
		if (type == null) {
			throw new IllegalArgumentException("PlanetType must be valid");
		}
		this.type = type;
	}
	
	public AtmosphereType getAtmosphere() {
		return atmosphere;
	}
	
	public void setAtmosphere(AtmosphereType atmosphere) {
		this.atmosphere = atmosphere;
		if (atmosphere == null || atmosphere == AtmosphereType.Vacuum) {
			this.atmosphere = AtmosphereType.Vacuum;
			this.pressure = AtmospherePressure.None;
		}
	}
	
	public AtmospherePressure getPressure() {
		return pressure;
	}
	
	public void setPressure(AtmospherePressure pressure) {
		this.pressure = pressure;
		if (pressure == null || pressure == AtmospherePressure.None) {
			this.atmosphere = AtmosphereType.Vacuum;
			this.pressure = AtmospherePressure.None;
		}
	}
	
	public Temperature getTemperature() {
		return temperature;
	}
	
	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}
	
	public int getHydrographics() {
		return hydrographics;
	}
	
	public void setHydrographics(int hydrographics) {
		if (hydrographics < 0) {
			this.hydrographics = 0;
		} else if (hydrographics > 100) {
			this.hydrographics = 100;
		} else {
			this.hydrographics = hydrographics;
		}
	}

	/**
	 * Gets the length of the planet's day, in seconds.
	 * 
	 * @return		Length of day in seconds.
	 */
	public int getDayLength() {
		return dayLength;
	}
	
	public void setDayLength(int dayLength) {
		if (dayLength < 1) {
			throw new IllegalArgumentException("Day length must be positive");
		}
		this.dayLength = dayLength;
	}
	
	public LifeType getLifeType() {
		return lifeLevel;
	}
	
	public void setLifeType(LifeType lifeType) {
		if (lifeType == null) {
			this.lifeLevel = LifeType.None;
		} else {
			this.lifeLevel = lifeType;
		}
	}
	
	public long getPopulation() {
		return population;
	}
	
	public void setPopulation(long population) {
		if (population < 0) {
			throw new IllegalArgumentException("Population cannot be negative");
		}
		this.population = population;
	}
	
	public StarportType getStarport() {
		return starport;
	}
	
	public void setStarport(StarportType starport) {
		this.starport = starport;
	}
	
	public GovernmentType getGovernment() {
		return government;
	}
	
	public void setGovernment(GovernmentType government) {
		this.government = government;
	}
	
	public int getTechLevel() {
		return techLevel;
	}
	
	public void setTechLevel(int techLevel) {
		this.techLevel = techLevel;
	}
	
	public int getLawLevel() {
		return lawLevel;
	}
	
	public void setLawLevel(int lawLevel) {
		this.lawLevel = lawLevel;
	}
	
	public String getBaseType() {
		return baseType;
	}
	
	public String getDescription() {
		return description;
	}
	
	public long getNextEvent() {
		return nextEvent;
	}
	
	public String getTradeCodes() {
		return tradeCodes;
	}
	
	public String getFeatureCodes() {
		return featureCodes;
	}
}
