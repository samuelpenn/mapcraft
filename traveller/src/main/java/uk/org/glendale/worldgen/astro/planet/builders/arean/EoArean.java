/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.arean;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.WetWorld;

/**
 * EoArean worlds are young Mars-like worlds. They are still highly geologically
 * active, and have an atmosphere and possibly an ocean.
 * 
 * @author Samuel Penn
 */
public class EoArean extends WetWorld {

	/**
	 * This is an EoArean world.
	 */
	@Override
	public PlanetType getPlanetType() {
		return PlanetType.EoArean;
	}

	@Override
	public void generate() {
		// TODO Auto-generated method stub
		setHydrographics(planet.getHydrographics());
		generateMap();
	}
}
