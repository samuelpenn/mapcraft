/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems.codes;

/**
 * Defines the various trade codes used by the Imperium to describe worlds.
 * Some of these are from Classic Traveller, some are unique to here.
 * 
 * @author Samuel Penn
 */
public enum TradeCode {
	Ra ("Radioactive"),
	Bx ("Bio-toxins"),
	Or ("Orbital Civilisation"),
	Mi ("Mining Colony"),
	Re ("Research Colony"),
	Pi ("Pirate base"),
	Co ("Local Colony"),
	Ba ("Barren"),
	Lo ("Low Population"),
	Hi ("High Population"),
	Ag ("Agricultural"),
	Na ("Non-agricultural"),
	In ("Industrial"),
	Ni ("Non-industrial"),
	Ri ("Rich"),
	Po ("Poor"),
	Wa ("Water world"),
	De ("Desert"),
	As ("Asteroid"),
	Va ("Vacuum world"),
	Ic ("Ice world"),
	Jv ("Jovian world"),
	Fl ("Non-water fluid oceans"),
	Cp ("Subsector capital"),
	Cx ("Sector capital");
	
	private String		description = null;
	
	TradeCode(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
