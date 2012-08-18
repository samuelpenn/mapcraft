/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.hot;

import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.StarportType;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.GaianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * Builder for a Cytherean world. These are hot worlds with thick clouds
 * much like Venus.
 * 
 * @author Samuel Penn
 */
public class Cytherean extends BarrenWorld {
	
	public PlanetType getPlanetType() {
		return PlanetType.Cytherean;
	}
	
	public void generate() {
		planet.setType(getPlanetType());
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius/2 + Die.die(radius, 2)/2);
		planet.setDayLength(Die.d6(2)*10000 + Die.die(30000));
		planet.setAxialTilt(Die.d10(3));
		
		// Set the type of atmosphere.
		switch (Die.d6(2)) {
		case 2: case 3: case 4:
			planet.setAtmosphere(AtmosphereType.SulphurCompounds);
			break;
		case 5: case 6: case 7: case 8: case 9:
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
			break;
		case 10: case 11: case 12:
			planet.setAtmosphere(AtmosphereType.Exotic);
			addResource("Exotic Gases", Die.d20(3));
			break;
		}
		
		// Set the atmosphere's pressure.
		switch (Die.d6()) {
		case 1: case 2:
			planet.setPressure(AtmospherePressure.Dense);
			planet.setTemperature(Temperature.VeryHot);
			break;
		case 3: case 4: case 5:
			planet.setPressure(AtmospherePressure.VeryDense);
			planet.setTemperature(Temperature.VeryHot);
			break;
		case 6:
			planet.setPressure(AtmospherePressure.SuperDense);
			planet.setTemperature(Temperature.ExtremelyHot);
			break;
		}

		planet.setHydrographics(0);
		planet.addTradeCode(TradeCode.H5);
		planet.setLifeType(LifeType.None);
		planet.addFeature(PlanetFeature.DenseClouds);

		mountains = new Tile("Mountains", "#B0B0B0", false);
		setCraterNumbers(Die.d6(2));
		setCraterMinSize(15);
		
		generateMap();
		generateResources();
	}
		
	@Override
	public void generateResources() {
		addResource("Corrosive Gases", 30 + Die.d20(2));
		addResource("Corrosive Chemicals", 20 + Die.d12(2));

		addResource("Silicate Ore", 20 + Die.d10(3));
		addResource("Silicate Crystals", 5 + Die.d6(2));
		addResource("Ferric Ore", 10 + Die.d8(3));
	}

}
