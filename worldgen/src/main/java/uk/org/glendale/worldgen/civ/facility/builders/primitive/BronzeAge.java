/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders.primitive;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Installation;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetDescription;
import uk.org.glendale.worldgen.civ.facility.Facility;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;

public class BronzeAge extends FacilityBuilder {

	// Residential.
	private static final String R_TRIBAL_STATES = "tribalStates";
	
	// Agriculture.
	private static final String A_HUNTER_GATHERER = "hunterGatherer";
	private static final String A_PRIMITIVE_FARMING = "primitiveFarming";
	
	// Mining.
	private static final String M_PRIMITIVE_MINING = "primitiveMining";

	public BronzeAge(FacilityFactory factory, Planet planet, PopulationSize population) {
		super(factory, planet, population);
		
		switch (population) {
		case None:
			throw new IllegalStateException("Cannot have zero population");
		case Tiny: case Small: case Medium: case Large: case Huge:
			break;
		default:
			throw new IllegalStateException("Population [" + population + "] too large");
		}
	}

	public void generate() {
		super.generate();
		
		String	 residentialName = R_TRIBAL_STATES;
		int		 residentialSize = 93 + Die.d6(2);

		// Main residential facility.
		Facility residential = factory.getFacility(residentialName);
		addResidential(residential, residentialSize);
		
		// Agriculture and other facilities.
		List<Installation>  list = new ArrayList<Installation>();
		list.add(new Installation(factory.getFacility(A_PRIMITIVE_FARMING), 100));
		list.add(new Installation(factory.getFacility(M_PRIMITIVE_MINING), 50));
		addFacilities(list);
		
		PlanetDescription	d = getDescription();
		String	key = "description." + residentialName;
		description = d.getDescription(key + "." + population);
		if (description == null || description.trim().length() == 0) {
			description = d.getDescription(key);
		}
	}
}
