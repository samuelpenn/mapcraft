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
 * MesoArean worlds are young Mars-like worlds, in the second stage of their
 * life cycle. At this point they are rapidly cooling as they lose heat and
 * atmosphere to space. Life may have started but rarely develops into anything
 * extensive.
 * 
 * @author Samuel Penn
 */
public final class MesoArean extends WetWorld {

	/**
	 * This is a MesoArean world.
	 */
	@Override
	public PlanetType getPlanetType() {
		return PlanetType.MesoArean;
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

		switch (planet.getAtmosphere()) {

		}

		switch (planet.getLifeType()) {
		case None:
			break;
		case Organic:
			addResource("Protobionts", Die.d6(3));
			break;
		case Archaean:
			addResource("Protobionts", Die.d8(4));
			addResource("Prokaryotes", Die.d6(4));
			addResource("Cyanobacteria", Die.d6(3));
			break;
		case Aerobic:
			addResource("Cyanobacteria", Die.d6(5));
			switch (Die.d4()) {
			case 1:
				addResource("Algae", Die.d8(4));
				break;
			case 2:
				addResource("Algae", Die.d8(3));
				addResource("Cnidarians", Die.d6(2));
				break;
			case 3:
				addResource("Algae", Die.d4(4));
				addResource("Cnidarians", Die.d6(2));
				addResource("Echinoderms", Die.d4(2));
				break;
			case 4:
				addResource("Algae", Die.d4(4));
				addResource("Cnidarians", Die.d6(2));
				addResource("Echinoderms", Die.d6(3));
				addResource("Arthropods", Die.d4(2));
				break;
			}
			break;
		case ComplexOcean:
			addResource("Algae", Die.d6(3));
			addResource("Cnidarians", Die.d6(2));
			addResource("Echinoderms", Die.d6(3));
			addResource("Arthropods", 5 + Die.d6(3));
			addResource("Fish", 20 + Die.d10(3));
			if (Die.d4() == 1) {
				addResource("Lichen", Die.d4());
			}
			break;
		case SimpleLand:
			addResource("Algae", Die.d6(2));
			addResource("Cnidarians", Die.d4(2));
			addResource("Echinoderms", Die.d4(3));
			addResource("Arthropods", 5 + Die.d6(2));
			addResource("Fish", 30 + Die.d10(3));
			switch (Die.d4()) {
			case 1:
				addResource("Lichen", Die.d6(2));
				addResource("Moss", Die.d4());
				break;
			case 2:
				addResource("Moss", 10 + Die.d6(4));
				break;
			case 3:
				addResource("Moss", 10 + Die.d6(2));
				addResource("Ferns", 25 + Die.d8(4));
				break;
			case 4:
				addResource("Moss", 10 + Die.d6(2));
				addResource("Fungi", 25 + Die.d8(4));
				break;
			}
			addResource("Insects", (Die.d10() - 5) * 3);
			break;
		}
	}

	@Override
	public void generate() {
		validate();
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.setDayLength(50000 + Die.die(10000, 5));

		// Default to primordial atmosphere. Density will depends on size.
		planet.setAtmosphere(AtmosphereType.Primordial);
		if (planet.getRadius() > 4000) {
			planet.setPressure(AtmospherePressure.Standard);
		} else if (planet.getRadius() > 2700) {
			planet.setPressure(AtmospherePressure.Thin);
		} else {
			planet.setPressure(AtmospherePressure.VeryThin);
		}
		// Inhospitable.
		planet.addTradeCode(TradeCode.H3);

		// Determine type of life on the world. Unlikely to be any, though
		// there is a chance.
		// @formatter:off
		switch (Die.d6(3)) {
		case 17: case 18:
			planet.setLifeType(LifeType.SimpleLand);
			switch (Die.d6()) {
			case 1: case 2:
				planet.setAtmosphere(AtmosphereType.LowOxygen);
				break;
			case 3: case 4:
				planet.setAtmosphere(AtmosphereType.HighCarbonDioxide);
				break;
			case 5:
				planet.setAtmosphere(AtmosphereType.OrganicToxins);
				break;
			case 6:
				planet.setAtmosphere(AtmosphereType.Standard);
				break;
			}
			break;
		case 14: case 15: case 16: 
			planet.setLifeType(LifeType.ComplexOcean);
			switch (Die.d8()) {
			case 1: case 2: case 3:
				planet.setAtmosphere(AtmosphereType.LowOxygen);
				break;
			case 4: case 5:
				planet.setAtmosphere(AtmosphereType.HighCarbonDioxide);
				break;
			case 6:
				planet.setAtmosphere(AtmosphereType.OrganicToxins);
				break;
			case 7:
				planet.setAtmosphere(AtmosphereType.NitrogenCompounds);
				break;
			case 8:
				planet.setAtmosphere(AtmosphereType.Standard);
				break;
			}
			break;
		case 10: case 11: case 12: case 13: 
			planet.setLifeType(LifeType.Aerobic);
			switch (Die.d4()) {
			case 1:
				planet.setAtmosphere(AtmosphereType.LowOxygen);
				break;
			case 2:
				planet.setAtmosphere(AtmosphereType.OrganicToxins);
				break;
			default:
				// Stick with primordial.
			}
			break;
		case 8: case 9:
			planet.setLifeType(LifeType.Archaean);
			break;
		case 6: case 7:
			planet.setLifeType(LifeType.Organic);
			break;
		default:
			planet.setLifeType(LifeType.None);
		}
		//@formatter:on

		// TODO Auto-generated method stub
		setHydrographics(planet.getHydrographics());
		generateMap();
		generateResources();
	}
}
