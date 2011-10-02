/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import org.springframework.stereotype.Service;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;

/**
 * Manages the weekly processes for a planet.
 * 
 * @author Samuel Penn
 */
public class Civilisation {
	private Planet	planet;
	private CommodityFactory	commodityFactory;
	private FacilityFactory		facilityFactory;
	
	final void setCommodityFactory(CommodityFactory factory) {
		this.commodityFactory = factory;
	}
	
	final void setFacilityFactory(FacilityFactory factory) {
		this.facilityFactory = factory;
	}
	
	Civilisation(Planet planet) {
		this.planet = planet;
	}
	
	
}
