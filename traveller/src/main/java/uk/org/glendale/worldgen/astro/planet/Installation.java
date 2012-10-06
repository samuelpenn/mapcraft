/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import uk.org.glendale.worldgen.civ.facility.Facility;

/**
 * An installation is an instance of a facility on a planet. It defines the size
 * of the facility plus any other planet specific data.
 * 
 * @author Samuel Penn
 */
@Embeddable
public class Installation {
	/** The type of facility. */
	@ManyToOne
	@JoinColumn(name = "facility_id")
	private Facility	facility;

	/** The size of this installation. */
	@Column(name = "size")
	private int			size;

	@SuppressWarnings("unused")
	private Installation() {
		// Empty constructor for JPA.
	}

	/**
	 * Create a new installation for the given facility. The size of this
	 * installation must be specified as well.
	 * 
	 * @param facility
	 *            Facility which this installation represents.
	 * @param size
	 *            Size of facility, must be 1+, maximum around 100.
	 */
	public Installation(final Facility facility, final int size) {
		if (facility == null) {
			throw new IllegalArgumentException("Facility must be set");
		}
		if (size < 1) {
			throw new IllegalArgumentException("Size must be strictly positive");
		}
		this.facility = facility;
		this.size = size;
	}

	/**
	 * Gets the type of facility. This may be some form of residential,
	 * agricultural, industrial or other type of facility.
	 * 
	 * @return Type of facility, never null.
	 */
	public Facility getFacility() {
		return facility;
	}

	/**
	 * Gets the size of this installation. This will always be strictly
	 * positive. Rich worlds that specialise in that type of facility may have a
	 * size slightly higher than 100, poor worlds may have lower sizes. The size
	 * is a percentage multiplier of effectiveness.
	 * 
	 * @return Percentage size of installation, 1+.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gets a useful string to describe this installation. Consists of the
	 * facility name plus the installation size.
	 */
	public String toString() {
		return facility.getName() + "[" + size + "%]";
	}
}