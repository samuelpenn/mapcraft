/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders.primitive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetDescription;
import uk.org.glendale.worldgen.civ.facility.Facility;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;

public class NeolithicTribes extends FacilityBuilder {
	
	// Residential.
	private static final String R_NEOLITHIC_TRIBES = "neolithicTribes";
	private static final String R_NEOLITHIC_FARMERS = "neolithicFarmers";
	
	// Agriculture.
	private static final String A_HUNTER_GATHERER = "hunterGatherer";
	private static final String A_NEOLITHIC_FARMING = "neolithicFarming";
	
	// Mining.
	private static final String A_PRIMITIVE_MINING = "primitiveMining";
	
	public NeolithicTribes(FacilityFactory factory, Planet planet, PopulationSize population) {
		super(factory, planet, population);
		
		switch (population) {
		case None:
			throw new IllegalStateException("Cannot have zero population");
		case Tiny: case Small: case Medium: case Large:
			break;
		default:
			throw new IllegalStateException("Population too large");
		}
	}
	
	public void generate() {
		super.generate();
		
		String	 residentialName = R_NEOLITHIC_TRIBES;
		int		 residentialSize = 93 + Die.d6(2);

		switch (population) {
		case None:
			// No population, nothing to generation.
			return;
		case Tiny: case Small:
			break;
		case Medium:
			if (Die.d2() == 1) {
				residentialName = R_NEOLITHIC_FARMERS;
			}
			break;
		case Large:			
			residentialName = R_NEOLITHIC_FARMERS;
			residentialSize -= 5;
			break;
		}

		Facility residential = factory.getFacility(residentialName);
		planet.addFacility(residential, residentialSize);
		
		PlanetDescription	d = getDescription();
		String	key = "description." + residentialName + "." + population;
		String 	text = d.getDescription(key);
		
		planet.setDescription(planet.getDescription() + " " + text);
	}
	
	public static void main(String[] args) {
		NeolithicTribes n = new NeolithicTribes(null, null, null);
		n.generate();
	}

}
