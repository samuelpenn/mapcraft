/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;


/**
 * Super class for all the asteroids. Handles basic asteroid generation,
 * since they're all pretty much the same except for a couple of parameters.
 * 
 * @author Samuel Penn
 */
class Asteroid extends WorldBuilder {
	private PlanetType		type = PlanetType.Carbonaceous;
	
	Asteroid() {
		super();
	}
	
	Asteroid(int width, int height) {
		super(width, height);
	}
	
	Asteroid(Planet planet, int width, int height) {
		super(width, height);
		this.planet = planet;
		this.type = planet.getType();
	}
	
	/**
	 * Generate the surface map for an asteroid of the type specified.
	 * The asteroid type mostly affects the colour, roughness of the
	 * surface and number and size of impact craters.
	 */
	public void generate() {
		Terrain		terrain = Terrain.Carbon;
		int			impacts = Die.d100(2);
		double		raise = 1.0;
		int			smooth = 0;
		
		switch (type) {
		case Carbonaceous:
			// The default.
			impacts = 0;
			raise = 0.75;
			break;
		case Basaltic:
			terrain = Terrain.Basaltic;
			impacts = Die.d10(2);
			raise = 0.5;
			smooth = 50;
			break;
		case Vulcanian:
			terrain = Terrain.Vulcanian;
			impacts = Die.d100(3);
			break;
		case Silicaceous:
			terrain = Terrain.Silicaceous;
			impacts = Die.d100(3);
			break;
		case Sideritic:
			terrain = Terrain.Vulcanian;
			impacts = Die.d100(3);
			break;
		case Enceladean:
			terrain = Terrain.Ice;
			impacts = Die.d10(3);
			raise = 0.5;
			smooth = 50;
			break;
		case Mimean:
			terrain = Terrain.Ice;
			impacts = Die.d20(5);
			break;
		case Oortean:
			terrain = Terrain.Ice;
			impacts = Die.d20(3);
			raise = 0.75;
			break;
		}
		
		// Let's have some asteroid impacts.
		impacts(impacts, 25, 0.7, terrain);
		
		// Now set the colours.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setHeight(x, y, (int)(getHeight(x, y) * raise) + smooth);
				setTerrain(x, y, terrain);
			}
		}
	}
}
