/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import java.util.List;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.Resource;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;

/**
 * Provides the APIs for processing facilities.
 * 
 * @author Samuel Penn
 */
public class FacilityAPI {
	private FacilityFactory		facilityFactory;
	private CommodityFactory	commodityFactory;

	public FacilityAPI() {
		facilityFactory = new FacilityFactory();
		commodityFactory = new CommodityFactory();
	}

	public void processPlanet(Planet planet) {
		List<Resource> resources = planet.getResources();
	}

}
