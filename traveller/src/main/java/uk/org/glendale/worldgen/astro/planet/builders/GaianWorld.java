/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.awt.Point;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Gaian worlds tend to be rich in life.
 * 
 * @author Samuel Penn
 */
public abstract class GaianWorld extends WorldBuilder {
	protected Tile SEA;
	protected Tile LAND;
	protected Tile MOUNTAINS;
	protected Tile ICE;

	public GaianWorld() {
		SEA = new Tile("Sea", "#4444aa", true);
		LAND = new Tile("Land", "#77aa33", false);
		MOUNTAINS = new Tile("Mountains", "#B0B0B0", false);
		ICE = new Tile("Ice", "#F0F0F0", false);
		
		map = new Tile[model.getTotalHeight()][];
		for (int y=0; y < model.getTotalHeight(); y++) {
			map[y] = new Tile[model.getWidthAtY(y)];
			for (int x=0; x < model.getWidthAtY(y); x++) {
				map[y][x] = SEA;
			}
		}
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.Standard);
			planet.setPressure(AtmospherePressure.Standard);
		} else {
			planet.setAtmosphere(AtmosphereType.Standard);
			planet.setPressure(AtmospherePressure.Thin);			
		}
		generateMap();
		generateResources();
		generateDescription();
	}
	
	protected void addContinents() {
		// Ice caps
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[model.getWidthAtY(tileY)];
			for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
				if (tileY < 1 || tileY == model.getTotalHeight() -1) {
					map[tileY][tileX] = ICE;
				} else if (tileY > 3 && tileY < 8 && Die.d10() == 1) {
					map[tileY][tileX] = LAND;
				} else {
					map[tileY][tileX] = SEA;
				}
			}
		}
		
		for (int i=0; i < 5; i++) {
			Tile[][] tmp = new Tile[12][];
			
			for (int tileY=0; tileY < 12; tileY++) {
				tmp[tileY] = new Tile[model.getWidthAtY(tileY)];
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					tmp[tileY][tileX] = map[tileY][tileX];
				}
			}

			for (int tileY=0; tileY < 12; tileY++) {
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					if (tmp[tileY][tileX] == LAND) {
						try {
							switch (Die.d4()) {
							case 1:
								// West.
								map[tileY][model.getWest(tileX, tileY)] = LAND;
								break;
							case 2:
								// East.
								map[tileY][model.getEast(tileX, tileY)] = LAND;
								break;
							case 3:
								// North/South.
								Point p = model.getUpDown(tileX, tileY);
								map[(int)p.getY()][(int)p.getX()] = LAND;
								break;
							default:
								// Do nothing.
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							//map[tileY][tileX] = red;
							System.out.println(tileX +", "+ tileY);
						}
					}
				}
			}
		}

		
	}

	@Override
	public void generateMap() {
		addContinents();
		//addEcology();

		getImage();
	}

	protected abstract void addEcology();

	/**
	 * Add resources based on the ecology. This is based pretty much on the
	 * LifeType of the world.
	 */
	protected void addEcologicalResources() {
		switch (planet.getLifeType()) {
		case None:
			// No life. Not really a Gaian world then.
			addResource("Protobionts", Die.d10());
			break;
		case Organic:
			// Basic organic compounds. May be actual life.
			if (Die.d2() == 1) {
				addResource("Protobionts", Die.d20(4));
			} else {
				addResource("Prokaryotes", Die.d10(5));
			}
			break;
		case Archaean:
			addResource("Cyanobacteria", 20 + Die.d20(3));
			if (Die.d3() == 1) {
				addResource("Algae", Die.d12(3));
			}
			break;
		case Aerobic:
			addResource("Cyanobacteria", Die.d12(4));
			addResource("Algae", Die.d12(3));
			if (Die.d3() == 1) {
				addResource("Cnidarians", 30 + Die.d20(3));
			} else {
				addResource("Cnidarians", 20 + Die.d20(2));
				addResource("Echinoderms", 30 + Die.d20(3));
				if (Die.d2() == 1) {
					addResource("Marine Arthropods", Die.d12(2));
				}
			}
			break;
		case ComplexOcean:
			addResource("Algae", Die.d6(3));
			addResource("Cnidarians", 5 + Die.d8(3));
			addResource("Echinoderms", 10 + Die.d8(4));
			addResource("Marine Arthropods", 20 + Die.d20(3));
			addResource("Fish", 20 + Die.d20(3));
			break;
		case SimpleLand:
			addResource("Algae", Die.d6(3));
			addResource("Cnidarians", 5 + Die.d8(3));
			addResource("Echinoderms", 10 + Die.d8(4));
			addResource("Marine Arthropods", 10 + Die.d12(4));
			addResource("Fish", 20 + Die.d20(3));
			
			switch (Die.d6(2)) {
			case 2:
				addResource("Fungi", Die.d8(3));
				break;
			case 3:
				addResource("Moss", 5 + Die.d10(3));
				break;
			case 4: case 5:
				addResource("Fungi", Die.d6(2));
				addResource("Moss", 10 + Die.d10(3));
				addResource("Arthropods", Die.d10(2));
				break;
			case 6: case 7:
				addResource("Moss", 10 + Die.d10(3));
				addResource("Ferns", 20 + Die.d10(3));
				if (Die.d2() == 1) {
					addResource("Arthropods", 10 + Die.d10(2));
				} else {
					addResource("Arthropods", Die.d8(2));
					addResource("Aquafauna", 5 + Die.d10(2));
				}
				break;
			case 8: case 9:
				addResource("Moss", Die.d10(2));
				addResource("Ferns", 20 + Die.d20(3));
				addResource("Arthropods", Die.d20(2));
				addResource("Aquafauna", 10 + Die.d12(2));
				addResource("Microfauna", 10 + Die.d6(3));
				break;
			case 10: case 11:
				addResource("Ferns", 30 + Die.d20(3));
				addResource("Arthropods", Die.d12(2));
				addResource("Aquafauna", 10 + Die.d6(2));
				addResource("Microfauna", 20 + Die.d8(3));
				addResource("Mesofauna", Die.d6(2));
				break;
			case 12:
				addResource("Ferns", 30 + Die.d20(3));
				addResource("Trees", Die.d10(2));
				addResource("Arthropods", Die.d4(2));
				addResource("Aquafauna", 5 + Die.d6(2));
				addResource("Microfauna", 30 + Die.d8(3));
				addResource("Mesofauna", 10 + Die.d10(2));
				break;				
			}
			break;
		case ComplexLand:
			addResource("Algae", Die.d6(2));
			addResource("Cnidarians", 5 + Die.d8(2));
			addResource("Echinoderms", 10 + Die.d8(3));
			addResource("Marine Arthropods", 10 + Die.d12(4));
			addResource("Fish", 30 + Die.d20(3));
			addResource("Trees", 40 + Die.d12(4));
			addResource("Grasses", 40 + Die.d12(4));
			addResource("Arthropods", Die.d4(2));
			addResource("Aquafauna", 5 + Die.d6(2));
			addResource("Microfauna", 20 + Die.d10(3));
			addResource("Mesofauna", 20 + Die.d10(4));
			break;
		case Extensive:
			addResource("Algae", Die.d6(2));
			addResource("Cnidarians", 5 + Die.d8(2));
			addResource("Echinoderms", 10 + Die.d8(3));
			addResource("Marine Arthropods", 10 + Die.d12(4));
			addResource("Fish", 30 + Die.d20(3));
			addResource("Trees", 40 + Die.d12(4));
			addResource("Grasses", 40 + Die.d12(4));
			addResource("Arthropods", Die.d4(2));
			addResource("Aquafauna", 5 + Die.d6(2));
			addResource("Microfauna", 20 + Die.d10(3));
			if (Die.d6() == 1) {
				addResource("Mesofauna", 10 + Die.d10(2));
				addResource("Megafauna", 30 + Die.d12(5));				
			} else {
				addResource("Mesofauna", 30 + Die.d12(4));
				addResource("Megafauna", Die.d4(2));
			}
			break;
		}
	}

}
