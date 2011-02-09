/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

public enum CommodityType {
	Food("Basic food stuffs"),
	Living("Household goods"),
	Mineral("Unrefined ores"),
	Luxury("Luxury goods with no purpose");
	
	private String description = null;
	CommodityType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
