/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

/**
 * These codes describe types of goods which are shipped between worlds.
 * They present everything from how hard/easy a good is to produce, to the
 * level of demand for it, who wants it and how easy it is to transport.
 */
public enum CommodityCode {
	// Production
	Tl("TL dependant"),
	TL("Very TL dependant"),
	// Legal
	Il("Illegal"),
	// Used by tech levels
	Pt("Pre-tech (1-3)"),
	Lt("Lo-tech (4-5)"),
	Mt("Mid-tech (6-8)"),
	Ht("High-tech (8-10)"),
	Ut("Ultra-tech (10+)"),
	// Used by world types
	In("Industrial"),
	Ag("Agricultural"),
	Mn("Mining"),
	Sp("Space ports D+"),
	SP("Space ports B+"),
	Re("Research"),
	Nv("Navy bases"),
	Sc("Scout bases"),
	Va("Vacuum environments"),
	Ho("Hot environments"),
	Co("Cold environments"),
	Po("Polluted environments"),
	Ex("Extreme environments"),
	De("Dry environments"),
	Wa("Water worlds"),
	Ge("General product"),
	// Type
	Vi("Vital"),
	Lu("Luxury"),
	Fo("Food"),
	Cl("Clothing"),
	Or("Ore"),
	Fi("Personal firearms"),
	En("Entertainment"),
	Me("Medical"),
	Ma("Machinary"),
	El("Electronics"),
	// Quality
	Hq("High quality"),
	Lq("Low quality"),
	// Trade/storage codes
	Pe("Perisable"),
	Fr("Fragile"),
	Hz("Hazardous"),
	HZ("Extremely Hazardous"),
	// Food codes
	FV("Vegetable"),
	FA("Animal"),
	FC("Crop"),
	FM("Marine"),
	FS("Swarming"),
	FO("Simple organic"),
	FQ("Squick"),
	// Other
	Uq("Unique");
	
	private String name = null;
	
	CommodityCode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
