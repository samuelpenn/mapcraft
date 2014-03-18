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

/**
 * An operation is performed by a Facility on a Resource or Commodity. The
 * result of the operation is to generate other commodities.
 * 
 * @author Samuel Penn
 */
@Embeddable
class Operation {
	@Column(name = "operation")
	private String	name;

	@Column(name = "efficiency")
	private int		level;

	private Operation() {
		// Default constructor for JPA.
	}

	Operation(final String name, final int level) {
		this.name = name;
		this.level = level;
	}

	/**
	 * Gets the name of this operation. Normally a 1-4 character code which
	 * describes the operation that the facility performs.
	 * 
	 * @return Name of the operation.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the efficiency level of the operation. This defaults to 100, but may
	 * be less if this is a side aspect of the facility.
	 * 
	 * @return Efficiency level as a percentage.
	 */
	public int getLevel() {
		return level;
	}

	public boolean equals(Operation o) {
		return name.equals(o.getName());
	}

	public int hashCode() {
		return name.hashCode();
	}
}
