/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;

/**
 * Defines business interface for managing planetary civilisations. Used
 * to simulate economy of worlds. 
 * 
 * @author Samuel Penn
 */
@Controller
@Transactional
public class CivilisationAPI {
	@Autowired
	private CivilisationFactory		factory;
	
	@Autowired
	private PlanetFactory			planetFactory;
	
	public void simulate(int planetId) {
		Planet			planet = planetFactory.getPlanet(planetId);
		Civilisation	civ = factory.getCivilisation(planet);
		List<Inventory>	inventory = factory.getPlanetInventory(planet);
		
		civ.simulate();
		
		factory.persist(civ.getInventory());
	}
}