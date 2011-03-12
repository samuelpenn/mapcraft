/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.arean;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.WetWorld;

/**
 * EoArean worlds are young Mars-like worlds. They are still highly geologically
 * active, and have an atmosphere and possibly an ocean.
 * 
 * @author Samuel Penn
 */
public final class EoArean extends WetWorld {

	/**
	 * This is an EoArean world.
	 */
	@Override
	public PlanetType getPlanetType() {
		return PlanetType.EoArean;
	}

	@Override
	public void generateResources() {
		addResource("Silicate Ore", 50 + Die.d20(2));
		if (Die.d6() == 1) {
			addResource("Silicate Crystals", 5 + Die.d6(2));
		}
		addResource("Ferric Ore", 25 + Die.d10(2));
		addResource("Heavy Metals", 5 + Die.d6());
		addResource("Radioactives", 5 + Die.d6());
		addResource("Carbonic Ore", 25 + Die.d12(2));

		addResource("Water", planet.getHydrographics());
		addResource("Base Organics", Die.d20());
		addResource("Organic Gases", Die.d10(2)
				+ planet.getPressure().ordinal() * 5);
	}

	@Override
	public void generate() {
		validate();
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);

		planet.setAtmosphere(AtmosphereType.Primordial);
		if (planet.getRadius() > 4000) {
			planet.setPressure(AtmospherePressure.Dense);
		} else if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.Standard);
		} else if (planet.getRadius() > 2000) {
			planet.setPressure(AtmospherePressure.Thin);
		} else {
			planet.setPressure(AtmospherePressure.VeryThin);
		}
		// Inhospitable.
		planet.addTradeCode(TradeCode.H3);

		// TODO Auto-generated method stub
		setHydrographics(planet.getHydrographics());
		generateMap();
		generateResources();
	}
}
