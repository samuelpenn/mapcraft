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

import java.util.*;

/**
 * Planetary features describe something about a world, normally either
 * geographical or ecological. They can be used to match world descriptions
 * (which can be generated randomly) to the actual world maps (also generated
 * randomly). This allows descriptive text to mention something like "The world
 * has a single polar ocean", and for the map to actually represent this.
 * 
 * Any given world may have zero or more features. Some are contradictory,
 * and these can be marked as such when defined.
 * 
 * @author Samuel Penn
 */
public enum PlanetFeature {
	Pangaea("Pa"),
	ManyIslands("Is", Pangaea),
	SingleSea("Ss", Pangaea, ManyIslands),
	PolarLand("PL", Pangaea, ManyIslands, SingleSea),
	EquatorialLand("EL", Pangaea, ManyIslands, SingleSea, PolarLand),
	Algae("Al"),
	PurpleWater("Wp"),
	GreenWater("Wg", PurpleWater),
	BlackWater("Wb", PurpleWater, GreenWater),
	CrateredSeas("CS"),
	Dry("Dr"),
	Wet("We"),
	Dust("Du"),
	PartialRings("R0"),
	FaintRings("R1"),
	Rings("R2"),
	BrightRings("R3"),
	ExtensiveRings("R4"),
	ThickClouds("C1"),
	DenseClouds("C2"),
	HeavilyCratered("Cr"),
	Volcanic("Vo"),
	ExtremeVolcanism("Vx"),
	CryoVolcanism("Cv"),
	TidalStressMarks("Ts"),
	FastRotation("FR"),
	EquatorialRidge("Re"),
	PolarRidge("Rp"),
	Smooth("Sm"),
	Fractured("Fr"),
	Hexagons("Hx"),
	Spirals("Sp"),
	GiantCrater("Gc"), // Large crater on the equator, big collision.
	AR("Ruins"),
	AC("Ruins of colony"),
	AA("Ancients Ruins"),
	AX("Alien Ruins"),
	// Finally, the following are unique codes which vary depending
	// on the type of the world. They represent very rare features.
	UA, UB, UC, UD, UE, UF, UG, UH, UI, UJ, UK, UL, UM, 
	UN, UO, UP, UQ, UR, US, UT, UU, UV, UW, UX, UY, UZ;
	
	private String 					code = null;
	private EnumSet<PlanetFeature>	excludes = null;
	
	PlanetFeature() {
		this.code = this.name();
	}
	
	PlanetFeature(String code, PlanetFeature...exclusions) {
		this.code = code;
		/*
		this.excludes = EnumSet.noneOf(PlanetFeature.class);
		
		if (exclusions != null) {
			for (PlanetFeature feature : exclusions) {
				excludes.add(feature);
			}
		}
		*/
	}
	
	/**
	 * Get the unique two character code which describes this feature.
	 * 
	 * @return		Short character code.
	 */
	public String getCode() {
		return code;
	}
	
	public static PlanetFeature getByCode(String c) {
		for (PlanetFeature f : values()) {
			if (f.getCode().equals(c)) {
				return f;
			}
		}
		
		return null;
	}
	
	/**
	 * Are these two features contradictory? Returns true if a single world
	 * shouldn't have both features. Checks both to see if this feature
	 * excludes the specified one, and if the specified one excludes this
	 * feature.
	 * 
	 * Both must be checked, since when exlusions are defined, Java only allows
	 * Enums that already exist to be specified.
	 */
	public boolean excludes(PlanetFeature feature) {
		if (feature.excludes.contains(this)) {
			return true;
		} else if (excludes.contains(feature)) {
			return true;
		}
		return false;
	}
	
	public boolean excludedBy(EnumSet<PlanetFeature> features) {
		for (PlanetFeature f : features) {
			if (excludes(f)) {
				return true;
			}
		}
		
		return false;
	}
}
