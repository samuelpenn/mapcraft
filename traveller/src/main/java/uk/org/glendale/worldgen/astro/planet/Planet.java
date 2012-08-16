/*
 * Copyright (C) 2011,2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.worldgen.astro.planet.MapImage.Projection;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.civ.commodity.Commodity;
import uk.org.glendale.worldgen.civ.facility.Facility;

@Entity
@Table(name = "planet")
public class Planet {
	// Unique identifier used as primary key.
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int					id;

	// Persisted fields.
	@Column(name = "name")
	private String				name;

	// Astronomical data
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "system_id", referencedColumnName = "id")
	private StarSystem			system;
	@Column(name = "parent_id")
	private int					parentId;
	@Column(name = "moon")
	private boolean				isMoon;
	@Column(name = "distance")
	private int					distance;
	@Column(name = "radius")
	private int					radius			= 1000;

	// Planet data
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private PlanetType			type			= PlanetType.Undefined;
	@Enumerated(EnumType.STRING)
	@Column(name = "atmosphere")
	private AtmosphereType		atmosphere		= AtmosphereType.Vacuum;
	@Enumerated(EnumType.STRING)
	@Column(name = "pressure")
	private AtmospherePressure	pressure		= AtmospherePressure.None;
	@Enumerated(EnumType.STRING)
	@Column(name = "life")
	private LifeType			lifeLevel		= LifeType.None;
	@Enumerated(EnumType.STRING)
	@Column(name = "temperature")
	private Temperature			temperature		= Temperature.UltraCold;
	@Column(name = "hydrographics")
	private int					hydrographics;
	@Column(name = "day")
	private int					dayLength		= 86400;
	@Column(name = "tilt")
	private int					axialTilt		= 0;

	// Civilisation data
	@Column(name = "population")
	private long				population;
	@Enumerated(EnumType.STRING)
	@Column(name = "starport")
	private StarportType		starport		= StarportType.X;
	@Enumerated(EnumType.STRING)
	@Column(name = "government")
	private GovernmentType		government		= GovernmentType.Anarchy;
	@Column(name = "law")
	private int					lawLevel;
	@Column(name = "tech")
	private int					techLevel;
	@Column(name = "description")
	private String				description;
	@Column(name = "base")
	private String				baseType;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "planet_codes", joinColumns = @JoinColumn(name = "planet_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<TradeCode>		tradeCodes		= EnumSet
														.noneOf(TradeCode.class);

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "planet_features", joinColumns = @JoinColumn(name = "planet_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<PlanetFeature>	featureCodes	= EnumSet
														.noneOf(PlanetFeature.class);

	@Column(name = "nextevent")
	private long				nextEvent;

	@JsonIgnore
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "resources", joinColumns = @JoinColumn(name = "planet_id"))
	private List<Resource>		resources		= new ArrayList<Resource>();

	@JsonIgnore
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "planet_maps", joinColumns = @JoinColumn(name = "planet_id"))
	private List<MapImage>		map				= new ArrayList<MapImage>();

	@JsonIgnore
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "facilities", joinColumns = @JoinColumn(name = "planet_id"))
	private List<Installation>	facilities		= new ArrayList<Installation>();
	
	
	public Planet() {

	}

	Planet(StarSystem system, int parentId, boolean isMoon, String name) {
		this.system = system;
		this.parentId = parentId;
		this.isMoon = isMoon;
		this.name = name;
	}

	/**
	 * Gets the unique id for this planet.
	 * 
	 * @return Unique planet id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the name of this planet. Names are unique within a star system. By
	 * default, a planet is named after the system, followed by a Roman numeral
	 * denoting its orbit. If it is a moon, this is followed by a letter. If the
	 * system has multiple stars, the planets around non primary stars have the
	 * star designation (Beta, Gamma etc) placed before the roman numeral. e.g.,
	 * Karpaty IV/c is the 3rd moon of the 4th planet in the Karpaty system.
	 * Karpaty Beta II is the 2nd planet of the second star of the same system.
	 * 
	 * @return Name of the planet.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name for this planet.
	 * 
	 * @param name
	 *            New name to give to the planet.
	 */
	public void setName(final String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
		this.name = name.trim();
	}

	/**
	 * Gets the ID of the star system that this planet is in.
	 * 
	 * @return Star system id.
	 */
	public StarSystem getSystem() {
		return system;
	}
	
	public void setSystem(StarSystem system) {
		this.system = system;
	}

	/**
	 * Gets the ID of the object this planet orbits. If this is a planet, then
	 * this will be the ID of a star. If it is a moon (isMoon == true) then this
	 * will be another planet.
	 * 
	 * @return ID of the star or planet that this object orbits.
	 */
	public int getParentId() {
		return parentId;
	}
	
	public void setParentId(final int parentId) {
		this.parentId = parentId;
	}

	/**
	 * Gets the distance of this planet from its primary. If the primary is a
	 * star (isMoon == false), then the distance is measured in millions of km.
	 * If it is a moon (isMoon == true), then it is measured in km.
	 * 
	 * @return Distance from primary, MKm if this is a star, km if it's a
	 *         planet.
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * Sets the distance of this planet from its primary. If the primary is a
	 * star (isMoon == false), then the distance is measured in millions of km,
	 * otherwise this planet is a moon and the units are km.
	 * 
	 * @param distance
	 *            Distance from primary, MKm (if primary is a star) or km.
	 */
	public void setDistance(final int distance) {
		if (distance <= 0) {
			throw new IllegalArgumentException(
					"Distance must be greater than zero");
		}
		this.distance = distance;
	}

	/**
	 * Gets the radius of the planet in kilometres.
	 * 
	 * @return Radius of planet in km.
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the planet.
	 * 
	 * @param radius
	 *            Planet radius, in kilometres.
	 */
	public void setRadius(final int radius) {
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
	 * @return True iff a moon.
	 */
	public boolean isMoon() {
		return isMoon;
	}

	/**
	 * Gets the planet type according to the PCL. This is used to classify a
	 * planet according to its size, geological and ecological features.
	 * 
	 * @return Planetary type.
	 */
	public PlanetType getType() {
		return type;
	}

	public void setType(final PlanetType type) {
		if (type == null) {
			throw new IllegalArgumentException("PlanetType must be valid");
		}
		this.type = type;
	}

	/**
	 * Gets the type of atmosphere for this planet. If there is no atmosphere,
	 * then the type is Vacuum, otherwise it determines the principle components
	 * of the atmosphere.
	 * 
	 * @return Atmosphere type.
	 */
	public AtmosphereType getAtmosphere() {
		return atmosphere;
	}

	/**
	 * Sets the type of atmosphere for this planet. If a null or Vacuum type is
	 * set, then the type is Vacuum and the pressure is automatically changed to
	 * be None.
	 * 
	 * @param atmosphere
	 *            Type of atmosphere.
	 */
	public void setAtmosphere(final AtmosphereType atmosphere) {
		this.atmosphere = atmosphere;
		if (atmosphere == null || atmosphere == AtmosphereType.Vacuum) {
			this.atmosphere = AtmosphereType.Vacuum;
			this.pressure = AtmospherePressure.None;
		}
	}

	/**
	 * Gets the atmospheric pressure of this planet. Together with the
	 * atmosphere type, this can be used to determine the properties of the
	 * world's atmosphere. Pressure ranges from Vacuum through to Super Dense.
	 * Pressures of Thin, Standard and Dense are reasonable for human worlds.
	 * 
	 * @return Atmospheric pressure.
	 */
	public AtmospherePressure getPressure() {
		return pressure;
	}

	public void setPressure(final AtmospherePressure pressure) {
		this.pressure = pressure;
		if (pressure == null || pressure == AtmospherePressure.None) {
			this.atmosphere = AtmosphereType.Vacuum;
			this.pressure = AtmospherePressure.None;
		}
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public void setTemperature(final Temperature temperature) {
		this.temperature = temperature;
	}

	public int getHydrographics() {
		return hydrographics;
	}

	public void setHydrographics(final int hydrographics) {
		if (hydrographics < 0) {
			this.hydrographics = 0;
		} else if (hydrographics > 100) {
			this.hydrographics = 100;
		} else {
			this.hydrographics = hydrographics;
		}
	}

	public int getAxialTilt() {
		return axialTilt;
	}

	public void setAxialTilt(final int axialTilt) {
		this.axialTilt = axialTilt;
	}

	/**
	 * Gets the length of the planet's day, in seconds.
	 * 
	 * @return Length of day in seconds.
	 */
	public int getDayLength() {
		return dayLength;
	}

	/**
	 * Gets the length of day as a pretty formatted string. The period is given
	 * in days, hours, minutes and seconds, where a 'day' is taken to be 24
	 * hours. Units are only given if needed, and accuracy is dropped for very
	 * long days (e.g., minutes are only listed for periods less than 10 days,
	 * hours for periods less than 100 days etc).
	 * 
	 * Returned string will be something like "5d 7h 23m".
	 * 
	 * @return Formatted day length.
	 */
	public String getDayLengthAsString() {
		String day = "";
		int d = dayLength;

		if (d >= 86400) {
			day += (d / 86400) + "d ";
			d = d % 86400;
		}
		// Only show hours is less than 100 days.
		if (d >= 3600 && dayLength < (86400 * 100)) {
			day += (d / 3600) + "h ";
			d = d % 3600;
		}
		// Only show minutes if less than 10 days.
		if (d >= 60 && dayLength < (86400 * 10)) {
			day += (d / 60) + "m ";
			d = d % 60;
		}
		// Only show seconds if less than 12 hours.
		if (d > 0 && dayLength < (43200)) {
			day += d + "s";
		}

		return day.trim();
	}

	/**
	 * Sets the length of day for this planet. The day length is always set in
	 * seconds.
	 * 
	 * @param dayLength
	 *            Length of day in seconds.
	 */
	public void setDayLength(final int dayLength) {
		if (dayLength < 1) {
			throw new IllegalArgumentException("Day length must be positive");
		}
		this.dayLength = dayLength;
	}

	public LifeType getLifeType() {
		return lifeLevel;
	}

	public void setLifeType(final LifeType lifeType) {
		if (lifeType == null) {
			this.lifeLevel = LifeType.None;
		} else {
			this.lifeLevel = lifeType;
		}
	}

	public long getPopulation() {
		return population;
	}

	public void setPopulation(final long population) {
		if (population < 0) {
			throw new IllegalArgumentException("Population cannot be negative");
		}
		this.population = population;
	}

	public StarportType getStarport() {
		return starport;
	}

	public void setStarport(final StarportType starport) {
		this.starport = starport;
	}

	public GovernmentType getGovernment() {
		return government;
	}

	public void setGovernment(final GovernmentType government) {
		this.government = government;
		if (this.government == null) {
			this.government = GovernmentType.Anarchy;
		}
	}

	public int getTechLevel() {
		return techLevel;
	}

	public void setTechLevel(final int techLevel) {
		this.techLevel = techLevel;
		if (this.techLevel < 0) {
			this.techLevel = 0;
		}
	}

	public int getLawLevel() {
		return lawLevel;
	}

	public void setLawLevel(final int lawLevel) {
		this.lawLevel = lawLevel;
		if (this.lawLevel < 0) {
			this.lawLevel = 0;
		} else if (this.lawLevel > 6) {
			this.lawLevel = 6;
		}
	}

	public String getBaseType() {
		return baseType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public long getNextEvent() {
		return nextEvent;
	}

	public Set<TradeCode> getTradeCodes() {
		return tradeCodes;
	}

	public List<TradeCode> getTradeCodeList() {
		List<TradeCode> list = new ArrayList<TradeCode>();

		for (TradeCode c : tradeCodes) {
			list.add(c);
		}
		return list;
	}

	public void addTradeCode(TradeCode code) {
		this.tradeCodes.add(code);
	}

	public void removeTradeCode(TradeCode code) {
		this.tradeCodes.remove(code);
	}

	public Set<PlanetFeature> getFeatureCodes() {
		return featureCodes;
	}

	public boolean hasFeatureCode(PlanetFeature code) {
		return featureCodes.contains(code);
	}

	public void addFeature(PlanetFeature code) {
		this.featureCodes.add(code);
	}

	public void removeFeature(PlanetFeature code) {
		this.featureCodes.remove(code);
	}

	public List<Resource> getResources() {
		return resources;
	}

	public Resource getResource(Commodity commodity) {
		for (Resource r : resources) {
			if (r.getCommodity().equals(commodity)) {
				return r;
			}
		}
		return null;
	}

	public void addResource(Resource resource) {
		removeResource(resource.getCommodity());
		if (resource.getDensity() > 1) {
			resources.add(resource);
		}
	}

	public void addResource(Commodity commodity, int density) {
		removeResource(commodity);
		if (density > 1) {
			resources.add(new Resource(commodity, density));
		}
	}

	public void removeResource(Commodity commodity) {
		for (Resource r : resources) {
			if (r.getCommodity().equals(commodity)) {
				resources.remove(r);
				break;
			}
		}
	}

	public void addImage(MapImage image) {
		this.map.add(image);
	}

	public byte[] getFlatImage() {
		if (map == null || map.size() == 0) {
			return null;
		}
		for (int i = 0; i < map.size(); i++) {
			MapImage mi = map.get(i);
			if (mi.getType() == Projection.Mercator) {
				return mi.getData();
			}
		}
		return null;
	}

	/**
	 * Adds a facility to the planet. If the facility already exists, then it is
	 * replaced. Adding a facility with a 0 size will remove any existing
	 * facilities of the same kind.
	 * 
	 * @param facility
	 *            Facility to add.
	 * @param size
	 *            Size of facility as percentage of optimum.
	 */
	public void addFacility(final Facility facility, final int size) {
		if (facilities.contains(facility)) {
			facilities.remove(facility);
		}
		if (size > 0) {
			facilities.add(new Installation(facility, size));
		}
	}

	/**
	 * Gets the list of facilities that are available on this planet.
	 * 
	 * @return List of facilities.
	 */
	public List<Installation> getFacilities() {
		return facilities;
	}

	public boolean hasTradeCode(TradeCode ag) {
		return tradeCodes.contains(ag);
	}
}
