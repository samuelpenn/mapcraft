/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import uk.org.glendale.worldgen.civ.commodity.Commodity;

/**
 * Used to map production of commodities by a facility.
 * 
 * TODO: This should be an embedded type in Facility, but JPA doesn't seem
 * to like this for some reason. Need to fix this at some point.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name = "facility_map")
class ProductionMap {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "facility_id", nullable = false)
	private Facility	facility;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "from_id", nullable = true)
	private Commodity	from;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "to_id", nullable = false)
	private Commodity   to;
	
	@Column(name = "level")
	private int			level;
	

	private ProductionMap() {
		// Default constructor for JPA.
	}

	ProductionMap(final Facility facility, final Commodity from, final Commodity to, final int level) {
		this.facility = facility;
		this.from = from;
		this.to = to;
		this.level = level;
	}
	
	public Commodity getFrom() {
		return from;
	}
	
	public Commodity getTo() {
		return to;
	}
	
	public int getLevel() {
		return level;
	}

	public boolean equals(ProductionMap p) {
		return p.equals(from) && p.equals(to) && p.level == level; 
	}

	public int hashCode() {
		long total =  from.hashCode() * to.hashCode() + level;
		
		return (int) (total % Integer.MAX_VALUE);
	}
}
