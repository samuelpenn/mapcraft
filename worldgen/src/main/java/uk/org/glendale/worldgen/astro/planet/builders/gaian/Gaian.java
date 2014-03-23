/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.gaian;

import java.util.Map;
import java.util.Properties;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.TechnologyLevel;
import uk.org.glendale.worldgen.astro.planet.builders.GaianWorld;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.worldgen.civ.facility.FacilityGenerator;

/**
 * Generate an Earth-like world. These are the most suitable planets for
 * human-like life, though they can vary greatly in just how suitable they are.
 * 
 * @author Samuel Penn
 */
public class Gaian extends GaianWorld {

	@Override
	public PlanetType getPlanetType() {
		return PlanetType.Gaian;
	}

	public void generate() {
		planet.setType(getPlanetType());
		int radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2) / 2);
		planet.setDayLength(Die.d6(2) * 10000 + Die.die(30000));
		planet.setAxialTilt(Die.d10(3));

		int populationModifier = 0;

		// Set the type of atmosphere.
		// @formatter:off
		switch (Die.d6(3)) {
		case 3: case 4:
			planet.setAtmosphere(AtmosphereType.LowOxygen);
			populationModifier--;
			break;
		case 5: case 6: case 7:
			planet.setAtmosphere(AtmosphereType.Pollutants);
			populationModifier--;
			break;
		case 14: case 15: case 16:
			planet.setAtmosphere(AtmosphereType.HighOxygen);
			populationModifier++;
			break;
		case 17: case 18:
			planet.setAtmosphere(AtmosphereType.HighCarbonDioxide);
			populationModifier--;
			break;
		default:
			planet.setAtmosphere(AtmosphereType.Standard);
		}
		
		// Set the atmosphere's pressure, modified by planet size.
		switch (Die.d6(2) + planet.getRadius()/2000) {
		case 3:
			planet.setPressure(AtmospherePressure.VeryThin);
			populationModifier-=2;
			break;
		case 4: case 5: case 6:
			planet.setPressure(AtmospherePressure.Thin);
			populationModifier--;
			break;
		case 14: case 15: case 16:
			planet.setTemperature(planet.getTemperature().getHotter());
			planet.setPressure(AtmospherePressure.Dense);
			break;
		case 17: case 18:
			planet.setTemperature(planet.getTemperature().getHotter());
			planet.setPressure(AtmospherePressure.VeryDense);
			populationModifier--;
			break;
		default:
			planet.setTemperature(planet.getTemperature().getHotter());
			planet.setPressure(AtmospherePressure.Standard);			
		}
		// @formatter:on
		planet.setHydrographics(15 + Die.d20(4));
		// setHydrographics(planet.getHydrographics());

		if (planet.getHydrographics() > 50 && planet.getHydrographics() < 85) {
			populationModifier++;
		}
		if (planet.getTemperature() == Temperature.Warm) {
			populationModifier++;
		}

		if (populationModifier < -2) {
			planet.addTradeCode(TradeCode.H2);
			planet.setLifeType(LifeType.SimpleLand);
		} else if (populationModifier < 0) {
			planet.addTradeCode(TradeCode.H1);
			planet.setLifeType(LifeType.ComplexLand);
		} else {
			planet.addTradeCode(TradeCode.H0);
			planet.setLifeType(LifeType.Extensive);
		}

		generateMap();
		generateResources();
		generateDescription();
	}

	protected void addEcology() {
		Tile woodland = new Tile("Woodland", "#44aa44", false);
		Tile jungle = new Tile("Jungle", "#338833", false);
		Tile desert = new Tile("Desert", "#cccc33", false);
		Tile ice = new Tile("Ice", "#f0f0f0", false);
	}

	/**
	 * Sets the basic resources for this type of world. This is irrespective of
	 * the current ecology (though assumes there was some ecology at some point,
	 * in order to generate oil).
	 */
	@Override
	public void generateResources() {
		addResource("Water", planet.getHydrographics());
		if (planet.getAtmosphere() == AtmosphereType.LowOxygen) {
			addResource("Oxygen", 20);
		} else {
			addResource("Oxygen", 40);
		}
		addResource("Silicate Ore", 20 + Die.d10(3));
		addResource("Ferric Ore", 10 + Die.d8(3));
		addResource("Carbonic Ore", 10 + Die.d10(3));
		addResource("Natural oil", 30 + Die.d10(3));
		addEcologicalResources();
	}

	/**
	 * This type of world often has a population. Gets a random culture type
	 * from the properties for this type of planet, and returns the builder for
	 * that civilisation type.
	 */
	public String getFacilityBuilderName(PopulationSize size,
			TechnologyLevel level) {
		Properties properties = getProperties();

		String name = getOneOption(properties, "culture." + size + "." + level);
		if (name == null) {
			name = getOneOption(properties, "culture." + level);
		}
		System.out.println("getFacilityBuilderName: [" + name + "]");
		if (name == null) {
			// No valid facility builders for this combination.
			return null;
		}

		Map<String, String> map = FacilityGenerator
				.getCultureBuilders("Imperium");
		if (map == null) {
			System.out.println("Cannot find culture map for [Imperium]");
			return null;
		}
		String className = map.get(name);

		return className;
	}

}
