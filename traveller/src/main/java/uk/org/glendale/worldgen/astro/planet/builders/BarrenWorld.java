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
import java.util.HashMap;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.TechnologyLevel;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Barren worlds are rocky worlds with little or no atmosphere, no surface water
 * and no life. They may potentially be rich in mineral resources, but have
 * little else going for them.
 * 
 * @author Samuel Penn
 */
public abstract class BarrenWorld extends WorldBuilder {
	
	protected final Tile	LIGHT;
	protected final Tile	DARK;
	
	protected static final String CRATER_COLOUR = "craterColour";
	protected static final String CRATER_MODIFIER = "craterModifier"; 
	
	public BarrenWorld() {
		LIGHT = new Tile("Light", "#CCCCCC", false) {
			@SuppressWarnings("rawtypes")
			public void addDetail(SimpleImage image, int x, int y, int w, int h, HashMap map) {
				String craterColour = (String)map.get(CRATER_COLOUR);
				int    craterModifier = (Integer)map.get(CRATER_MODIFIER);
				int	   radius = w / 3;
				
				switch (Die.d6(2) + craterModifier) {
				case -2: case -1: case 0: case 1:
				case 2: case 3: case 4: case 5:
					// No craters.
					break;
				case 6: case 7: case 8:
					// One crater.
					x += Die.d10() - Die.d10();
					y += Die.d10() - Die.d10();
					radius += Die.d10();
					image.circle(x + w, y + h/2, radius, craterColour);
					break;
				case 9: case 10:
					// Two craters.
					image.circle(x + w + Die.d10() - Die.d10(), 
							     y + h/2 + Die.d10(2), radius, craterColour);
					image.circle(x + w + Die.d10(), 
						     y + h/3, radius, craterColour);
					break;
				case 11:
					// Double crater.
					x += Die.d10() - Die.d10();
					y += Die.d10() - Die.d10();
					image.circle(x + w, y + h/2, radius, craterColour);
					x += Die.d10();
					y += Die.d10();
					radius -= Die.d4();
					image.circle(x + w, y + h/2, radius, craterColour);
					break;
				default:
					// Three craters.
				}
			}
		};
		DARK = new Tile("Dark", "#BBBBBB", false);

		map = new Tile[model.getTotalHeight()][];
		for (int y=0; y < model.getTotalHeight(); y++) {
			map[y] = new Tile[model.getWidthAtY(y)];
			for (int x=0; x < model.getWidthAtY(y); x++) {
				map[y][x] = BLANK;
			}
		}
		
		properties.put(CRATER_COLOUR, "#C0C0C0");
		properties.put(CRATER_MODIFIER, 0);
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.setPressure(AtmospherePressure.Trace);
		}
		generateMap();
		generateResources();
	}
	
	private void drawRandomBarrenWorld() {
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[model.getWidthAtY(tileY)];
			for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
				if (Die.d20() == 1) {
					map[tileY][tileX] = DARK;
				} else {
					map[tileY][tileX] = LIGHT;
				}
			}
		}

		for (int i=0; i < 4; i++) {
			Tile[][] tmp = new Tile[12][];
			
			for (int tileY=0; tileY < 12; tileY++) {
				tmp[tileY] = new Tile[model.getWidthAtY(tileY)];
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					tmp[tileY][tileX] = map[tileY][tileX];
				}
			}

			for (int tileY=0; tileY < 12; tileY++) {
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					if (tmp[tileY][tileX] == DARK) {
						try {
							switch (Die.d3()) {
							case 1:
								// West.
								map[tileY][model.getWest(tileX, tileY)] = DARK;
								break;
							case 2:
								// East.
								map[tileY][model.getEast(tileX, tileY)] = DARK;
								break;
							case 3:
								// North/South.
								Point p = model.getUpDown(tileX, tileY);
								map[(int)p.getY()][(int)p.getX()] = DARK;
								break;
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
		if (!AppManager.getDrawMap()) {
			// return;
		}
		drawRandomBarrenWorld();
		
		getImage();
	}

	/**
	 * By default, worlds of this type have no population.
	 */
	public String getFacilityBuilderName(PopulationSize size, TechnologyLevel level) {
		return null;
	}

}
