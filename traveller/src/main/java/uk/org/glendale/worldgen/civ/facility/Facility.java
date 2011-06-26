/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import java.util.EnumSet;
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
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.worldgen.civ.commodity.CommodityCode;

/**
 * A Facility describes one facet of a civilisation. They are central to the
 * economic system, since they are the producers and consumers of commodities.
 * 
 * Facilities are defined in the facilities.xml configuration file, though are
 * stored in the database after the database has been initialised.
 * 
 * @author Samuel Penn
 */
@XmlRootElement
@Entity
public class Facility {
	// Unique identifier used as primary key.
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int					id;

	@Column(name = "name")
	private String				name;
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private FacilityType		type;
	@Column(name = "image")
	private String				imagePath;
	@Column(name = "techLevel")
	private int					techLevel;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_ops", joinColumns = @JoinColumn(name = "facility_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<Operation>		operations		= EnumSet
														.noneOf(Operation.class);

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_reqs", joinColumns = @JoinColumn(name = "facility_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<CommodityCode>	requirements	= EnumSet
														.noneOf(CommodityCode.class);

	/*
	 * @MapKey(name="facility_id") @JoinTable(name="facility_requirements",
	 * joinColumns=@JoinColumn(name="facility_id"))
	 * 
	 * @AttributeOverrides({
	 * 
	 * @AttributeOverride(name="key", column=@Column(name="code")),
	 * 
	 * @AttributeOverride(name="value", column=@Column(name="value")) })
	 */

	// @OneToMany @JoinTable(name="facility_requirements")
	// @MapKeyColumn(name="facility_id")
	// private Map<String, Integer> requirementList = new Hashtable<String,
	// Integer>();

	public Facility() {

	}

	public Facility(String name, FacilityType type, int techLevel) {
		this.name = name;
		this.type = type;
		this.techLevel = techLevel;
	}

	/**
	 * Gets the unique id of this type of facility.
	 * 
	 * @return Unique facility id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the name of this type of facility. This is the name that will be
	 * displayed to the users. It is unique.
	 * 
	 * @return Human readable name of the facility.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the facility name.
	 * 
	 * @param name
	 *            Set unique facility name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the type of this facility. The type defines how this facility is
	 * used, and what affect it has on resources and trade goods.
	 * 
	 * @return The type of this facility.
	 */
	public FacilityType getType() {
		return type;
	}

	/**
	 * Sets the type of this facility.
	 * 
	 * @param type
	 *            Facility type.
	 */
	public void setType(FacilityType type) {
		if (type == null) {
			throw new IllegalArgumentException("Facility type cannot be null");
		}
		this.type = type;
	}

	public int getTechLevel() {
		return techLevel;
	}

	public void setTechLevel(int techLevel) {
		this.techLevel = techLevel;
	}

	public void addOperation(Operation op) {

	}
}
