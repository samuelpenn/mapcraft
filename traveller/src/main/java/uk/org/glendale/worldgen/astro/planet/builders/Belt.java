/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Asteroid belts are quite common. Can be rocky or icy.
 * 
 * @author Samuel Penn
 */
public abstract class Belt extends PlanetBuilder {
	public Belt() {
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.setPressure(AtmospherePressure.Trace);
		}
		generateResources();
	}

	@Override
	public void generateMap() {
		// No maps for belts.
	}
}
