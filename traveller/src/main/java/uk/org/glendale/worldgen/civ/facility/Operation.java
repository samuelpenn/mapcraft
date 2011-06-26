/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

/**
 * An operation is performed by a Facility on a Resource or Commodity. The
 * result of the operation is to generate other commodities.
 * 
 * @author Samuel Penn
 */
public enum Operation {
	// @formatter:off
	Mi("Mining"), 
	Ag("Agriculture"), 
	HuGa("Hunter Gathering"), 
	No("Nomadic Herdsmen"), 
	Null("Null");
	// @formatter:on 

	private String	title;

	private Operation(final String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
