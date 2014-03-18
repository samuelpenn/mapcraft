/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

/**
 * Transfer object for a Facility, to be used in REST APIs. Also includes
 * the installation size, since this will generally be used for specific
 * instances of facilities on planets. Does not include the logic, just
 * display characteristics.
 */
public class FacilityTO {
	public final int 			id;
	public final String			name;
	public final String			title;
	public final FacilityType	type;
	public final String			image;
	public final int			installation_size;
	
	public FacilityTO(final Facility facility) {
		this.id = facility.getId();
		this.name = facility.getName();
		this.title = facility.getTitle();
		this.type = facility.getType();
		this.image = facility.getImagePath();
		this.installation_size = 0;
	}

	public FacilityTO(final Facility facility, int size) {
		this.id = facility.getId();
		this.name = facility.getName();
		this.title = facility.getTitle();
		this.type = facility.getType();
		this.image = facility.getImagePath();
		this.installation_size = size;
	}
}
