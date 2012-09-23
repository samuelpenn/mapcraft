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

@Service
public class NeolithicTribes extends FacilityBuilder {
	@Autowired
	private FacilityFactory	factory;
	
	public NeolithicTribes(Planet planet, PopulationSize population) {
		super(planet, population);
	}
	
	public void generate() {
		super.generate();
		
		Facility residential = factory.getFacility("neolithicTribes");
		int		 residentialSize = 93 + Die.d6(2);
		planet.addFacility(residential, residentialSize);
		
		PlanetDescription		d = getDescription();
		System.out.println(d.getDescription("description"));
	}
	
	public static void main(String[] args) {
		NeolithicTribes n = new NeolithicTribes(null, null);
		n.generate();
	}

}
