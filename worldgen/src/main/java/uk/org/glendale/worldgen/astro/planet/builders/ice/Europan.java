/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders.ice;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.IceWorld;

/**
 * A moon similar to Europa.
 * 
 * @author Samuel Penn
 */
public class Europan extends IceWorld {
	
	public PlanetType getPlanetType() {
		return PlanetType.Europan;
	}

	public void generate() {
		if (planet == null) {
			throw new IllegalStateException("Use setPlanet() to set the planet first");
		}
		planet.setType(getPlanetType());
		
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2)/2);
		if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.Trace);
			switch (Die.d3()) {
			case 1:
				planet.setAtmosphere(AtmosphereType.Oxygen);
				break;
			case 2:
				planet.setAtmosphere(AtmosphereType.WaterVapour);
				break;
			case 3:
				planet.setAtmosphere(AtmosphereType.InertGases);
				break;
			}
			planet.addTradeCode(TradeCode.Ba);
		} else {
			planet.setPressure(AtmospherePressure.None);
			planet.addTradeCode(TradeCode.Va);			
		}
		planet.addTradeCode(TradeCode.H3);
		
		// Work out ecology, if any.
		switch (Die.d6(2)) {
		case 2:
			planet.setLifeType(LifeType.Aerobic);
			break;
		case 3: case 4:
			planet.setLifeType(LifeType.Archaean);
			break;
		case 5: case 6: case 7:
			planet.setLifeType(LifeType.Organic);
			break;
		}
		
		System.out.println("Generate map");
		generateMap();
		System.out.println("Generate resources");
		generateResources();
		System.out.println("Generate description");
		generateDescription();
		System.out.println("Done");
	}

	@Override
	public void generateResources() {
		addResource("Water", 20 + Die.d20(4));
		
		int level = 0;
		switch (planet.getLifeType()) {
		case Organic:
			addResource("Organic Chemicals", Die.d20());
			break;
		case Archaean:
			level = Die.d10() + 10;
			addResource("Organic Chemicals", level);
			switch (Die.d3()) {
			case 1:
				addResource("Protobionts", level / Die.d3() + 1);
				break;
			case 2:
				addResource("Protobionts", level / 2);
				addResource("Cyanobacteria", level / 3);
				break;
			case 3:
				addResource("Protobionts", level / 2);
				addResource("Cyanobacteria", level + Die.d6());
				break;
			}
			break;
		case Aerobic:
			level = 12 + Die.d6(3);
			addResource("Cyanobacteria", level + Die.d6());
			addResource("Algae", level + Die.d4() - Die.d6());
			addResource("Cnidarians", level - Die.d6(2));
			break;
		}
	}

}
