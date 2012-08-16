/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

/**
 * A minimal Star representation for use in REST data transfer.
 */
public class StarTO {
	public final int id;
	public final String name;
	public final int parentId;
	public final int distance;
	public final StarClass classification;
	public final StarForm  form;

	public StarTO(Star star) {
		this.id = star.getId();
		this.name = star.getName();
		this.classification = star.getClassification();
		this.parentId = star.getParentId();
		this.form = star.getForm();
		this.distance = star.getDistance();
	}
}
