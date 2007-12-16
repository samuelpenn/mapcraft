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
 * Generates a map for a barren world. Barren worlds are dry rock worlds
 * with craters and mountains but little in the way of other features.
 * This class supports a large number of planet types, including
 * rocky planetoids (aka dwarf planets).
 * 
 * Barren worlds can consist of:
 *   Craters   : Impact craters from asteroid bombardments.
 *   Flats     : Ancient lava flows long since cooled and hardened.
 *   Highlands : Remanents of a crust, for worlds which have had their
 *               crust stripped away.
 *   Ejecta    : Surface shaped by ejecta from asteroid impacts.
 *   Bumps     : Misshapen surface, common planetoids.
 * 
 * Selenian (Planetoid)
 * 
 *   Similar to our moon, no tectonic activity, no atmosphere, and no water.
 *   A cratered surface with a few ancient lava flats and a dusty surface.
 *   
 * Hermian
 * 
 *   Similar to Mercury. Hard hot rock, otherwise dead. Plenty of craters.
 *   
 * Ferrinian
 * 
 *   Very heavy metals, with some hard rock. Cratered, no dust or atmosphere.
 *   
 * Hadean (Planetoid)
 * 
 *   Planetoid, halfway between asteroid and a full planet.
 *   
 * Cerean (Planetoid)
 * 
 * Vestian (Planetoid)
 * 
 * Kuiperian (Planetoid)
 * 	 
 *   Methane ice world, similar to Pluto.
 *   
 * Hephaestian (Planetoid)
 * 
 * 	 Active, volcanic world like Io.
 * 
 * @author Samuel Penn.
 */
class Barren extends WorldBuilder {
	private PlanetType		type = PlanetType.Selenian;
	private Terrain			terrain = null;
	private Terrain			lava = null;
	private Terrain			ejecta = null;
	private Terrain			impact = null;
	private Terrain			highlands = null;
	private	int				craters = 500;
	private int				bumps = 0;
	
	private int				craterSize = 12;
	private double			craterDepth = 0.7;
	
	private int				flats = 0;
	private int				flatSize = 0;
	
	private int				highlandLower = 0;
		
	Barren() {
		super();
		System.out.println("Creating Barren world");
	}

	/*
	Hermian(230, 180, 115, -1, -1, -1, false),
	HermianFlats(210, 190, 135, -0.7, -1, -1, false),
	Ferrinian(120, 120, 50, 3, 2.0, 2.0, false),
	FerrinianEjecta(100, 100, 50, 1.5, 1.0, 0.5, false),
	Hadean(60, 30, 30, 0.8, 0.4, 0.3, false),
	HadeanImpact(40, 20, 20, 0.4, 0.2, 0.1, false),
	HadeanEjecta(90, 60, 60, 1, 0.8, 0.8, false),
	Cerean(50, 50, 50, 2, 2, 1.5, false),
	Vestian(90, 90, 90, 2, 2, 1.5, false),
	*/
	
	Barren(Planet planet, int width, int height) {
		super(width, height);
		this.planet = planet;
		this.type = planet.getType();
		
		// Firstly, set basic variables.
		switch (type) {
		case Selenian:
			terrain = Terrain.create("Selenian", 100, 100, 100, 1.5, 1.5, 1.5, false);
			lava = Terrain.create("SelenianFlats", 140, 140, 140, -0.25, -0.25, -0.25, false);
			ejecta = Terrain.create("SelenianEjecta", 130, 130, 130, 2, 2, 2, false);
			impact = Terrain.create("SelenianImpact", 100, 100, 100, 1, 1, 1, false);
			flats = Die.d10();
			flatSize = Die.d100(5);
			break;
		case Hermian:
			terrain = Terrain.create("Hermian", 230, 180, 115, -1, -1, -1, false);
			lava = Terrain.create("HermianFlats", 210, 190, 135, -0.7, -1, -1, false);
			impact = Terrain.create("HermianImpact", 230, 180, 115, -0.75, -1, -1, false);
			craters = 500;
			craterSize = 7;
			craterDepth = 0.7;
			flats = Die.d8();
			flatSize = Die.d100(3);
			break;
		case Ferrinian:
			terrain = Terrain.create("Ferrinian", 120, 120, 50, 3, 2, 2, false);
			impact = terrain;
			ejecta = Terrain.create("FerrinianEjecta", 100, 100, 50, 1.5, 1.0, 0.5, false);
			craters = 1500;
			craterSize = 6;
			craterDepth = 0.8;
			break;
		case Hadean:
			terrain = Terrain.create("Hadean", 60, 30, 30, 0.8, 0.4, 0.3, false);
			impact = Terrain.create("HadeanImpact", 40, 20, 20, 0.4, 0.2, 0.1, false);
			ejecta = Terrain.create("HadeanEjecta", 90, 60, 60, 1, 0.8, 0.8, false);
			highlands = Terrain.create("HadeanHighlands", 50, 50, 50, 1.5, 1.25, 1.25, false);
			craters = 100;
			craterSize = 20;
			highlandLower = 80;
			bumps = Die.d6(2);
			break;
		case Cerean:
			terrain = Terrain.create("Cerean", 50, 50, 50, 2, 2, 1.5, false);
			impact = Terrain.create("CereanImpact", 25, 25, 25, 1, 1, 0.75, false);
			ejecta = Terrain.create("CereanEjecta", 150, 150, 150, 2, 2, 1.5, false);
			craters = 200;
			craterSize = 15;
			craterDepth = 0.9;
			highlands = Terrain.create("CereanHighlands", 75, 75, 75, 1, 1, 0.75, false);
			highlandLower = 75;
			bumps = Die.d4(2);
			break;
		case Vestian:
			terrain = Terrain.create("Vestian", 90, 90, 90, 2, 2, 1.5, false);
			impact = terrain;
			lava = Terrain.create("VestianFlats", 70, 70, 70, 1.5, 1.5, 1, false);;
			craters = 100;
			craterSize = 15;
			craterDepth = 0.5;
			highlands = Terrain.create("VestianHighlands", 90, 90, 90, 1.5, 1.5, 1.5, false);
			highlandLower = 75;
			flats = Die.d8(2);
			flatSize = 100 + Die.d100(3);
			bumps = Die.d4(1);
		case Kuiperian:
			terrain = Terrain.create("KuiperianIces", 100, 75, 50, 1.5, 1.5, 1, false);
			//impact = Terrain.create("KuiperianDark", 25, 25, 25, 2, 2, 0.5, false);
			craters = Die.d100();
			ejecta = Terrain.create("KuiperianEjecta", 100, 75, 50, 3, 3, 2, false);
			lava = Terrain.create("KuiperianLava", 50, 20, 10, 0.5, 0.5, 0.5, false);
			flats = Die.d8();
			flatSize = Die.d100(5);
			break;
		case Hephaestian:
			terrain = Terrain.create("Hephaestian", 100, 75, 50, 3, 2, 0.5, false);
			craterDepth = 0.85;
			break;
		}
	}
	
	private void bumps(int number, int size) {
		for (int bump = 0; bump < number; bump++) {
			int		px = Die.die(width);
			int		py = Die.die((int)(height*0.8)) + (int)(height * 0.2);

			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					int		dx = Math.abs(px - x);
					if (dx > width/2) {
						dx -= width;
					}
					int		dy = py - y;
					int		d = (int)(Math.sqrt(dx*dx + dy*dy));
					int		inc = (int)(size * Math.sin(d/20.0));
					
					setHeight(x, y, getHeight(x, y)+inc);
				}
			}
		}
	}
	
	/**
	 * Generate a number of larva flats of a given size at random points
	 * on the planet's surface.
	 */
	private void basaltFlats(int number, int size) {
		for (int flat=0; flat < number; flat++) {
			int		x = Die.die(width);
			int		y = Die.die((int)(height*0.8)) + (int)(height * 0.2);
			
			setHeight(x, y, getHeight(x, y) + Die.d10());			
			generateLava(x, y, getHeight(x, y), size);
		}
	}
	
	/**
	 * Recursively generate lava flats, until we hit another lava flat
	 * or the size counter drops below zero.
	 */
	private void generateLava(int x, int y, int h, int size) {
		if (size < 0) {
			return;
		}
		if (x < 0) x += width;
		if (x >= width) x -= width;
		if (y < 0 || y >= height) return;
		
		if (getTerrain(x, y) == lava) return;
		setTerrain(x, y, lava);
		
		try {
			if (getHeight(x-1, y) <= h) {
				setHeight(x-1, y, (getHeight(x-1, y) + h)/2);
				generateLava2(x-1, y, h, size - Die.d2());
			}
			if (getHeight(x+1, y) <= h) {
				setHeight(x+1, y, (getHeight(x+1, y) + h)/2);
				generateLava2(x+1, y, h, size - Die.d2());
			}
			if (getHeight(x, y+1) <= h) {
				setHeight(x, y+1, (getHeight(x, y+1) + h)/2);
				generateLava2(x, y+1, h, size - Die.d2());
			}
			if (getHeight(x, y-1) <= h) {
				setHeight(x, y-1, (getHeight(x, y-1) + h)/2);
				generateLava2(x, y-1, h, size - Die.d2());
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("generateLava: "+x+","+y);
		}
	}

	/**
	 * The same as generataLava(), except the order is reversed.
	 * If we don't do this, we get lateral bias in the growth of the
	 * flat. There's probably a better way of doing this.
	 */
	private void generateLava2(int x, int y, int h, int size) {
		if (size < 0) {
			return;
		}
		if (x < 0) x += width;
		if (x >= width) x -= width;
		if (y < 0 || y >= height) return;
		
		if (getTerrain(x, y) == lava) return;
		setTerrain(x, y, lava);
		
		try {
			if (getHeight(x, y-1) <= h) {
				setHeight(x, y-1, (getHeight(x, y-1) + h)/2);
				generateLava(x, y-1, h, size - Die.d2());
			}
			if (getHeight(x, y+1) <= h) {
				setHeight(x, y+1, (getHeight(x, y+1) + h)/2);
				generateLava(x, y+1, h, size - Die.d2());
			}
			if (getHeight(x+1, y) <= h) {
				setHeight(x+1, y, (getHeight(x+1, y) + h)/2);
				generateLava(x+1, y, h, size - Die.d2());
			}
			if (getHeight(x-1, y) <= h) {
				setHeight(x-1, y, (getHeight(x-1, y) + h)/2);
				generateLava(x-1, y, h, size - Die.d2());
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("generateLava: "+x+","+y);
		}
	}
		
	public void generate() {
		// Now set the colours.
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, terrain);
				if (highlands != null && getHeight(x, y) >= highlandLower) {
					setTerrain(x, y, highlands);
				}
			}
		}
		// Bumpy?
		if (bumps > 0) {
			bumps(bumps, 25);
		}
		
		// Let's have some asteroid impacts.
		impacts(craters, craterSize, craterDepth, impact, ejecta);

		// If there are flats defined, then generate some.
		if (lava != null) {
			basaltFlats(flats, flatSize);
		}
	}
}
