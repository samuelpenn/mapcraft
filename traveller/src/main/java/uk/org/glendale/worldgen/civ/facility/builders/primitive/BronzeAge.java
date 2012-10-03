/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility.builders.primitive;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;

public class BronzeAge extends FacilityBuilder {

	public BronzeAge(FacilityFactory factory, Planet planet, PopulationSize population) {
		super(factory, planet, population);
	}
	
	public static void main(String[] args) {
		NeolithicTribes n = new NeolithicTribes(null, null, null);
		BronzeAge b = new BronzeAge(null, null, null);

	}

}
