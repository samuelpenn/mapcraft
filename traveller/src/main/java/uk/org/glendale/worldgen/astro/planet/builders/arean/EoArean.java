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
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.WetWorld;

/**
 * EoArean worlds are young Mars-like worlds. They are still highly geologically
 * active, and have an atmosphere and possibly an ocean. This is the first stage
 * of their life cycle, before they begin to lose their atmosphere.
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

	/**
	 * Generates resources for this planet. Minerals are mostly silicate based,
	 * with some carbonic and low on ferric ores. There will be water available,
	 * plus organic material in the form of chemicals and gases.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 50 + Die.d20(2));
		if (Die.d6() == 1) {
			addResource("Silicate Crystals", 5 + Die.d6(2));
		}
		addResource("Ferric Ore", 20 + Die.d10(2));
		addResource("Heavy Metals", 5 + Die.d6());
		addResource("Radioactives", 5 + Die.d6());
		addResource("Carbonic Ore", 25 + Die.d12(2));

		addResource("Water", planet.getHydrographics());
		addResource("Organic Gases", Die.d10(2)
				+ planet.getPressure().ordinal() * 5);
		switch (planet.getLifeType()) {
		case None:
			break;
		case Organic:
			addResource("Protobionts", Die.d6(2));
			break;
		case Archaean:
			addResource("Protobionts", Die.d8(3));
			addResource("Prokaryotes", Die.d6(3));
			if (Die.d4() == 1) {
				addResource("Cyanobacteria", Die.d4(2));
			}
			break;
		}
	}

	@Override
	public void generate() {
		validate();
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.setDayLength(40000 + Die.die(10000, 5));

		// Primordial atmosphere. Density will depends on size.
		planet.setAtmosphere(AtmosphereType.Primordial);
		if (planet.getRadius() > 4000) {
			planet.setPressure(AtmospherePressure.Dense);
			planet.setTemperature(planet.getTemperature().getHotter());
		} else if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.Standard);
		} else if (planet.getRadius() > 2000) {
			planet.setPressure(AtmospherePressure.Thin);
		} else {
			planet.setPressure(AtmospherePressure.VeryThin);
		}
		// Inhospitable.
		planet.addTradeCode(TradeCode.H3);

		// Determine type of life on the world. Unlikely to be any, though
		// there is a chance.
		switch (Die.d6(3)) {
		case 18:
			planet.setLifeType(LifeType.Archaean);
			break;
		case 15:
		case 16:
		case 17:
			planet.setLifeType(LifeType.Organic);
			break;
		default:
			planet.setLifeType(LifeType.None);
		}

		setHydrographics(planet.getHydrographics());
		generateMap();
		generateResources();
	}
}
