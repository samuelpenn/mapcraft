/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

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
import javax.persistence.Table;
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
@Entity
@Table(name = "facility")
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

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_codes", joinColumns = @JoinColumn(name = "facility_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<FacilityCode>	codes		= EnumSet
													.noneOf(FacilityCode.class);

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_ops", joinColumns = @JoinColumn(name = "facility_id"))
	private List<Operation>		operations	= new ArrayList<Operation>();
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_req", joinColumns = @JoinColumn(name = "facility_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<CommodityCode>	required = EnumSet.noneOf(CommodityCode.class);

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "facility_consume", joinColumns = @JoinColumn(name = "facility_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<CommodityCode>	consumed = EnumSet.noneOf(CommodityCode.class);
	
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

	public Facility(String name, FacilityType type, String image) {
		this.name = name;
		this.type = type;
		this.imagePath = image;
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

	public void addOperation(String name) {
		addOperation(name, 100);
	}

	public void addOperation(String name, int level) {
		Operation o = new Operation(name, level);
		if (!operations.contains(o)) {
			operations.add(o);
		} else {
			// Operations equality does not check the level, so should replace
			// an existing operation with one with a (possibly) different level.
			operations.remove(o);
			operations.add(o);
		}
	}

	public boolean hasOperation(String name) {
		return operations.contains(new Operation(name, 100));
	}

	public int getOperation(String name) {
		for (Operation o : operations) {
			if (o.getName().equals(name)) {
				return o.getLevel();
			}
		}
		return 0;
	}

	public void addCode(FacilityCode... args) {
		for (FacilityCode code : args) {
			codes.add(code);
		}
	}

	public boolean hasCode(FacilityCode code) {
		return codes.contains(code);
	}
	
	/**
	 * Gets the list of required goods for this facility. If the requirements
	 * aren't met, then output can be greatly reduced. Requirements are by
	 * commodity code, rather than by specific commodities, so may be met in
	 * a variety of ways.
	 * 
	 * @return	List of good types required by the facility.
	 */
	public Set<CommodityCode> getRequiredGoods() {
		return required;
	}
	
	
	public void addRequired(CommodityCode code) {
		required.add(code);
	}
	
	/**
	 * Gets the list of goods which are consumed by this facility. Unlike
	 * requirements, their absence does not affect output. However, the
	 * price of such goods will rise if there is a short fall. They are
	 * often luxury or consumer goods.
	 * 
	 * @return	List of good types consumed by this facility.
	 */
	public Set<CommodityCode> getConsumedGoods() {
		return consumed;
	}

	public void addConsumed(CommodityCode code) {
		consumed.add(code);
	}
}
