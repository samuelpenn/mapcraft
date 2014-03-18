/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders.primitive;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Installation;
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
	private static final String M_PRIMITIVE_MINING = "primitiveMining";
	
	public NeolithicTribes(FacilityFactory factory, Planet planet, PopulationSize population) {
		super(factory, planet, population);
		
		switch (population) {
		case None:
			throw new IllegalStateException("Cannot have zero population");
		case Tiny: case Small: case Medium: case Large:
			break;
		default:
			throw new IllegalStateException("Population [" + population + "] too large");
		}
	}
	
	public void generate() {
		super.generate();
		
		String	 residentialName = R_NEOLITHIC_TRIBES;
		int		 residentialSize = 93 + Die.d6(2);

		List<Installation>  list = new ArrayList<Installation>();
		switch (population) {
		case None:
			// No population, nothing to generation.
			return;
		case Tiny: case Small:
			list.add(new Installation(factory.getFacility(A_HUNTER_GATHERER), 100));
			break;
		case Medium:
			list.add(new Installation(factory.getFacility(A_HUNTER_GATHERER), 75));
			if (Die.d2() == 1) {
				residentialName = R_NEOLITHIC_FARMERS;
				list.add(new Installation(factory.getFacility(A_NEOLITHIC_FARMING), 100));
			} else {
				list.add(new Installation(factory.getFacility(A_NEOLITHIC_FARMING), 25));				
			}
			break;
		case Large:	 case Huge: case Gigantic:	
			residentialName = R_NEOLITHIC_FARMERS;
			residentialSize -= 5;
			list.add(new Installation(factory.getFacility(A_HUNTER_GATHERER), 25));
			list.add(new Installation(factory.getFacility(A_NEOLITHIC_FARMING), 100));
			list.add(new Installation(factory.getFacility(M_PRIMITIVE_MINING), 25));
			break;
		}
		
		// Main residential facility.
		Facility residential = factory.getFacility(residentialName);
		addResidential(residential, residentialSize);
		
		// Agriculture and other facilities.
		addFacilities(list);
		
		PlanetDescription	d = getDescription();
		String	key = "description." + residentialName;
		description = d.getDescription(key + "." + population);
		if (description == null || description.trim().length() == 0) {
			description = d.getDescription(key);
		}
	}
	
	public static void main(String[] args) throws Exception {
		//NeolithicTribes n = new NeolithicTribes(null, null, null);
		//n.generate();
		String className = "uk.org.glendale.worldgen.civ.facility.builders.primitive.NeolithicTribes";
		
		Class  cls = Class.forName(className);
		System.out.println(cls.getName());
		Constructor c = cls.getConstructor(new Class[] {
				FacilityFactory.class,
				Planet.class,
				PopulationSize.class
		});
		System.out.println(c.getName());

	}

}
