/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders.lowtech;

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

/**
 * Define facilities for a medieval culture. Medieval cultures are TL3, and
 * generally consist of multiple large kingdoms.
 * 
 * @author Samuel Penn
 */
public class Medieval extends FacilityBuilder {

	// Residential.
	private static final String R_FEUDAL_KINGDOMS = "feudalKingdoms";
	
	// Agriculture.
	private static final String A_SIMPLE_FARMING = "simpleFarming";
	private static final String A_COASTAL_FISHING = "coastalFishing";
	
	// Mining.
	private static final String M_SIMPLE_MINING = "simpleMining";
	
	// Others.
	private static final String WARFARE = "warfare";
	private static final String RELIGION = "religion";

	public Medieval(FacilityFactory factory, Planet planet, PopulationSize population) {
		super(factory, planet, population);
		
		switch (population) {
		case None: case Tiny: case Small:
			throw new IllegalStateException("Population [" + population + "] too small");
		case Medium: case Large: case Huge:
			break;
		default:
			throw new IllegalStateException("Population [" + population + "] too large");
		}
	}

	public void generate() {
		super.generate();
		
		String	 residentialName = R_FEUDAL_KINGDOMS;
		int		 residentialSize = 93 + Die.d6(2);

		// Main residential facility.
		Facility residential = factory.getFacility(residentialName);
		addResidential(residential, residentialSize);

		// Agriculture and other facilities.
		List<Installation>  list = new ArrayList<Installation>();
		list.add(new Installation(factory.getFacility(A_SIMPLE_FARMING), 100));
		list.add(new Installation(factory.getFacility(M_SIMPLE_MINING), 50));
		if (planet.getHydrographics() > 50) {
			list.add(new Installation(factory.getFacility(A_COASTAL_FISHING), 
					planet.getHydrographics() / 3));
		}
		addFacilities(list);
		
		PlanetDescription	d = getDescription();
		String	key = "description." + residentialName;
		description = d.getDescription(key + "." + population);
		if (description == null || description.trim().length() == 0) {
			description = d.getDescription(key);
		}
	}
}
