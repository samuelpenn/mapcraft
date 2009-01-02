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
	Il("Illegal"),
	Tl("TL dependant"),
	TL("Very TL dependant"),
	Lo("Lo-tech (1-5)"),
	Mi("Mid-tech (6-8)"),
	Hi("High-tech (8-10)"),
	Ul("Ultra-tech (10+)"),
	In("Industrial"),
	Ag("Agricultural"),
	Mn("Mining"),
	Sp("Space"),
	Vi("Vital"),
	Lu("Luxury"),
	Pe("Perisable"),
	Fr("Fragile"),
	Hz("Hazardous"),
	HZ("Extremely Hazardous"),
	Fo("Food"),
	Or("Ore");
	
	private String name = null;
	
	CommodityCode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
