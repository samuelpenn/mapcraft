/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.server;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tracks global universal numbers, such as the current time. These numbers
 * are stored in the database, and are treated as name-value pairs, where the
 * value is always a long value.
 * 
 * Property names are defined by Universe.Property.
 */
@Entity
@Table(name = "numbers")
public class UniversalNumber {
	@Id @Column(name="property")
	private String name;
	
	@Column(name="value")
	private long value;
	
	@SuppressWarnings("unused")
	private UniversalNumber() {
		this.name = null;
		this.value = 0;
	}
	
	/**
	 * Define a new universal number. Only has package scope, since only the
	 * core server should have direct access to fundamental numbers
	 * 
	 * @param name		Unique name of the property.
	 * @param value		Current value of the property.
	 */
	UniversalNumber(final String name, final long value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Gets the unique name of this property.
	 * 
	 * @return	Property name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the current value of this property.
	 * 
	 * @return	Property value.
	 */
	public long getValue() {
		return value;
	}
	
	/**
	 * Sets the new current value for this property. This is limited to package
	 * scope so higher level logic cannot change the universal properties.
	 * 
	 * @param value		New value for the property.
	 */
	void setValue(final long value) {
		this.value = value;
	}
}
