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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JFrame;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
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
 * Unique Features:
 * 
 * Baren worlds may (rarely) have the following unique features:
 * UA: One side of the world is heavily blackened.
 * UB: A circle of blackness on the equator.
 * 
 * 
 * @author Samuel Penn.
 */
class Barren extends WorldBuilder {
	private PlanetType		type = PlanetType.Selenian;
	private Terrain			terrain = null;
	private Terrain[]		lava = null;
	private Terrain			ejecta = null;
	private Terrain			impact = null;
	private Terrain			highlands = null;
	private	int				craters = 500;
	private int				bumps = 0;
	
	private int				craterSize = 12;
	private double			craterDepth = 0.7;
	
	private int				flats = 0;
	private int				flatSize = 0;
	private int				stressMarks = 0;
	
	private int				highlandLower = 0;
	
	private FloodPlain[]	plains = null;
		
	Barren() {
		super();
		System.out.println("Creating Barren world");
	}

	private Canvas		canvas = null;
	private int[][]		waterMap = null;
	
	public boolean useImage = false;
	
	private class FloodPlain {
		int		number = 0;
		int		height = 0;
		Terrain	terrain = null;
		
		FloodPlain(Terrain terrain, int number, int height) {
			this.terrain = terrain;
			this.number = number;
			this.height = height;
		}		
	}
	
	private class MapCanvas extends Canvas {
		private WorldBuilder b = null;
		
		MapCanvas(WorldBuilder b) {
			super();
			this.b = b;
		}
		public void paint(Graphics g) {
			int scale = 3;
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					int		h = b.getHeight(x, y);
					g.setColor(getTerrain(x, y).getColor(h));
					g.fillRect(x*scale, y*scale, scale, scale);
				}
			}
		}
	}
	
	private void draw() {
		//canvas.paint(canvas.getGraphics());		
	}
	
	/**
	 * Large craters are huge depressions. They don't change the terrain
	 * type, but lower a roughly circular area.
	 */
	private void addLargeCraters(int number) {
		for (int crater=0; crater < number; crater++) {
			int		cx = Die.rollZero(width);
			int		cy = Die.rollZero(height/2) + height/4;
			int		radius = Die.d20(5);
			
			for (int x=cx-radius-1; x < cx+radius+1; x++) {
				for (int y=cy-radius-1; y < cy+radius+1; y++) {
					int		d = (int)Math.sqrt((cx-x) * (cx-x) + (cy-y) * (cy-y));
					if ((d + getHeight(x, y)/2) < radius) {
						setHeight(x, y, 0.5);
					}
				}
			}
		}
	}

	/**
	 * Selenian worlds are dry, barren worlds like the Moon. They are
	 * rocky, dusty and often heavily cratered from meteorite impacts.
	 * They are geologically dead, and have a rock core with few metals.
	 */
	private void processSelenian() {
		terrain = Terrain.create("Selenian", 50, 50, 50, 2, 2, 2, false);		
		addLargeCraters(Die.d6(2));
		
		
		lava = new Terrain[] { Terrain.create("SelenianFlats", 140, 140, 140, -0.25, -0.25, -0.25, false) };
		ejecta = Terrain.create("SelenianEjecta", 130, 130, 130, 2, 2, 2, false);
		impact = Terrain.create("SelenianImpact", 100, 100, 100, 1, 1, 1, false);
		craters = 300 + Die.d100(4);
		flats = Die.d10();
		flatSize = Die.d100(5);
		
		ejecta = impact = null;
		lava = null;
		craters = flats = 0;
		
		
		if (planet.hasFeature(PlanetFeature.HeavilyCratered)) {
			craters *= 3;
			craterSize *= 0.75;
		} else if (planet.hasFeature(PlanetFeature.Smooth)) {
			// Planet is smooth, with a dusty surface which has filled in
			// most old craters.
			craters /= 10;
			craterSize *= 0.5;
		}
		if (planet.hasFeature(PlanetFeature.TidalStressMarks)) {
			stressMarks = 3 + Die.d6();
		}
	}
	
	
	Barren(Planet planet, int width, int height) {
		super(width, height);
		this.planet = planet;
		this.type = planet.getType();
/*
		JFrame		frame = new JFrame("Gaian World");
		canvas = new MapCanvas(this);
		canvas.setPreferredSize(new Dimension(width*3, height*3));
		canvas.setVisible(true);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(width*3, height*3));
		frame.setSize(new Dimension(width*3+100, height*3+100));
*/		
		// Firstly, set basic variables.
		switch (type) {
		case Selenian:
			processSelenian();
			break;
		case Hermian:
			terrain = Terrain.create("Hermian", 230, 180, 115, -1, -1, -1, false);
			impact = Terrain.create("HermianImpact", 230, 180, 115, -0.75, -1, -1, false);
			craters = 500;
			craterSize = 7;
			craterDepth = 0.7;
			plains = new FloodPlain[] { new FloodPlain(Terrain.create("HermianLava", 210, 190, 135, -0.7, -1, -1, false), 50, 15)};
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
			lava = new Terrain[] { Terrain.create("VestianFlats", 70, 70, 70, 1.5, 1.5, 1, false) };
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
			craters = Die.d100();
			ejecta = Terrain.create("KuiperianEjecta", 100, 75, 50, 3, 3, 2, false);
			lava = new Terrain[] { Terrain.create("KuiperianLava", 50, 20, 10, 0.5, 0.5, 0.5, false) };
			flats = Die.d8();
			flatSize = Die.d100(5);
			break;
		case Hephaestian:
			terrain = Terrain.create("Hephaestian", 100, 75, 0, 2.5, 2.5, 2, false);
			ejecta = impact = Terrain.create("HephaestianCrater", 100, 75, 0, 2.5, 2, 1.5, false);
			craters = Die.d20(2);
			craterSize = 10;
			craterDepth = 0.7;
			
			plains = new FloodPlain[] { new FloodPlain(Terrain.create("HephaestianIces", 150, 100, 25, 2.5, 2.5, 3, false), 100, 25),
					                    new FloodPlain(Terrain.create("HephaestianLava", 150, 75, 0, 4, 1.5, 1.5, false), 25, 10) };
			
			break;
		}
	}
	
	/**
	 * Add tidal stress marks to the world. These are caused by gravitational
	 * flexing which breaks the planet's crust. Normally around the poles.
	 * They are represented as 'impact' terrain.
	 */
	private void stressMarks(int number) {
		for (int i=0; i < number; i++) {
			int		x = Die.die(width-1);
			int		y = 20 + Die.d20(2);
			int		length = 20 + Die.d8(number);
			
			for ( ; length > 0; length--) {
				setTerrain(x, y, impact); setTerrain(x-1, y, impact); setTerrain(x+1, y, impact);
				setHeight(x, y, 0.5); setHeight(x-1, y, 0.5); setHeight(x+1, y, 0.5);
				y++;
				x += Die.d2() - Die.d2();
			}
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
	
	private int countNeighbours(int x, int y, Terrain t) {
		int		count = 0;
		
		if (getTerrain(x-1, y) == t) count++;
		if (getTerrain(x+1, y) == t) count++;
		if (getTerrain(x, y-1) == t) count++;
		if (getTerrain(x, y+1) == t) count++;
		
		return count;
	}

	private int countNeighboursHigher(int x, int y, Terrain t) {
		int		count = 0;
		int		h = getHeight(x, y);
		
		if (getTerrain(x-1, y) == t && getHeight(x-1, y) >= h) count++;
		if (getTerrain(x+1, y) == t && getHeight(x+1, y) >= h) count++;
		if (getTerrain(x, y-1) == t && getHeight(x, y-1) >= h) count++;
		if (getTerrain(x, y+1) == t && getHeight(x, y+1) >= h) count++;
		
		return count;
	}
	
	private void floodWithLava(Terrain t, int number, int max) {
		for (int i = 0; i < number; i++) {
			setTerrain(Die.rollZero(width), Die.rollZero(height), t);
		}
		
		int		lowest = 100;
		boolean done = false;
		while (!done) {
			int		count = 0;
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) != t && countNeighboursHigher(x, y, t) > 0) {
						setTerrain(x, y, t);
						count++;
						if (getHeight(x, y) < lowest) lowest = getHeight(x,y);
					}
				}
			}
			System.out.println("Flow: "+count);
			if (count == 0) done = true;
			draw();
		}
		
		for (int h=lowest; h < 90; h++) {
			done = false;
			while (!done) {
				int		count = 0;
				int		total = 0;
				for (int y=0; y < height; y++) {
					for (int x=0; x < width; x++) {
						if (getTerrain(x, y) == t) {
							total++;
						} else if (getHeight(x, y) <= h && countNeighbours(x, y, t) > 0) {
							setTerrain(x, y, t);
							count++;
							total++;
						}
					}
				}
				System.out.println(h+": "+count);
				if (count == 0) {
					done = true;
					if (total*100 >= (max * height * width)) h=100;
				}
			}			
			draw();
		}
	}
	
	/**
	 * Generate a number of larva flats of a given size at random points
	 * on the planet's surface.
	 */
	private void basaltFlats(int number, int size, Terrain l) {
		for (int flat=0; flat < number; flat++) {
			int		x = Die.die(width);
			int		y = Die.die((int)(height*0.8)) + (int)(height * 0.2);
			
			setHeight(x, y, getHeight(x, y) + 10);
			generateLava(x, y, getHeight(x, y), size, l);
		}
	}
	
	/**
	 * Recursively generate lava flats, until we hit another lava flat
	 * or the size counter drops below zero.
	 */
	private void generateLava(int x, int y, int h, int size, Terrain l) {
		if (size < 0) {
			return;
		}
		if (x < 0) x += width;
		if (x >= width) x -= width;
		if (y < 0 || y >= height) return;
		
		if (getTerrain(x, y) == l) return;
		setTerrain(x, y, l);
		
		try {
			if (getHeight(x-1, y) <= h) {
				setHeight(x-1, y, (getHeight(x-1, y)*2 + h)/3);
				generateLava2(x-1, y, h, size - Die.d2(), l);
			}
			if (getHeight(x+1, y) <= h) {
				setHeight(x+1, y, (getHeight(x+1, y)*2 + h)/3);
				generateLava2(x+1, y, h, size - Die.d2(), l);
			}
			if (getHeight(x, y+1) <= h) {
				setHeight(x, y+1, (getHeight(x, y+1)*2 + h)/3);
				generateLava2(x, y+1, h, size - Die.d2(), l);
			}
			if (getHeight(x, y-1) <= h) {
				setHeight(x, y-1, (getHeight(x, y-1)*2 + h)/3);
				generateLava2(x, y-1, h, size - Die.d2(), l);
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
	private void generateLava2(int x, int y, int h, int size, Terrain l) {
		if (size < 0) {
			return;
		}
		if (x < 0) x += width;
		if (x >= width) x -= width;
		if (y < 0 || y >= height) return;
		
		if (getTerrain(x, y) == l) return;
		setTerrain(x, y, l);
		
		try {
			if (getHeight(x, y-1) <= h) {
				setHeight(x, y-1, (getHeight(x, y-1)*2 + h)/3);
				generateLava(x, y-1, h, size - Die.d2(), l);
			}
			if (getHeight(x, y+1) <= h) {
				setHeight(x, y+1, (getHeight(x, y+1)*2 + h)/3);
				generateLava(x, y+1, h, size - Die.d2(), l);
			}
			if (getHeight(x+1, y) <= h) {
				setHeight(x+1, y, (getHeight(x+1, y)*2 + h)/3);
				generateLava(x+1, y, h, size - Die.d2(), l);
			}
			if (getHeight(x-1, y) <= h) {
				setHeight(x-1, y, (getHeight(x-1, y)*2 + h)/3);
				generateLava(x-1, y, h, size - Die.d2(), l);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("generateLava: "+x+","+y);
		}
	}
	
	/**
	 * Actually do the generation of the map given the set parameters.
	 */
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
			for (Terrain l : lava) {
				basaltFlats(flats, flatSize, l);
			}
		}
		
		if (plains != null) {
			for (FloodPlain p : plains) {
				floodWithLava(p.terrain, p.number, p.height);
				draw();
			}
		}
		
		if (stressMarks > 0) {
			System.out.println(stressMarks);
			stressMarks(stressMarks);
		}
		
		// Unique Features
		if (planet.hasFeature(PlanetFeature.UA)) {
			Terrain		black = Terrain.create("Black", 25, 25, 25, 0.2, 0.2, 0.2, false);
			int		mid = width/2, left = 0;
			for (int y=0; y < height; y++) {
				mid += Die.d4() - Die.d4();
				left += Die.d4() - Die.d4();
				for (int x=left; x < mid; x++) {
					setTerrain(x, y, black);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		
		
		Planet	p = new Planet("Bob", PlanetType.Selenian, 4000);
		p.setTilt(22);
		p.setTemperature(Temperature.Standard);
		//p.addFeature(PlanetFeature.HeavilyCratered);
		//p.addFeature(PlanetFeature.TidalStressMarks);
		
		
		String		path = "/home/sam/tmp/planets/"+p.getType();
		File		d = new File(path);
		if (!d.exists()) {
			d.mkdirs();
		}
		
		for (int i=0; i < 10; i++) {
			Barren w = new Barren(p, 513, 257);
			w.generate();
			
			GraphicsEnvironment.isHeadless();
			w.getWorldMap(4).save(new java.io.File(path+"/image"+i+".jpg"));
		}
	}
}
