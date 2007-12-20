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
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for an ice world. These are worlds large enough to be
 * considered at least dwarf planets, probably with some sort of atmosphere
 * and some structured core and crust. They may be totally frozen, or have
 * liquid oceans beneath their crust.
 * 
 * @author Samuel Penn.
 */
class IceWorld extends WorldBuilder {
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
	
	private int				highlandLower = 0;
	
	private FloodPlain[]	plains = null;
		
	IceWorld() {
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
		canvas.paint(canvas.getGraphics());		
	}
	
	
	IceWorld(Planet planet, int width, int height) {
		super(width, height);
		this.planet = planet;
		this.type = planet.getType();

		JFrame		frame = new JFrame("Ice World");
		canvas = new MapCanvas(this);
		canvas.setPreferredSize(new Dimension(width*3, height*3));
		canvas.setVisible(true);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(width*3, height*3));
		frame.setSize(new Dimension(width*3+100, height*3+100));
		
		// Firstly, set basic variables.
		switch (type) {
		case LithicGelidian:
			terrain = Terrain.create("LithicGelidian", 0, 0, 0, 3, 2, 1.5, false);
			impact = Terrain.create("LithicGelidianImpact", 25, 20, 0, 3, 2, 1, false);
			craters = 100;
			craterSize = 10;
			craterDepth = 0.8;
			break;
		case Europan:
			terrain = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		case EuTitanian:
		case MesoTitanian:
		case TitaniLacustric:
			terrain = Terrain.create("Ice", 50, 50, 50, 1, 1, 1, false);
			break;
		case Iapetean:
			terrain = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		case JaniLithic:
			terrain = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		case Tritonic:
			terrain = Terrain.create("Ice", 150, 150, 150, 1, 1, 1.2, false);
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
	}

	public static void main(String[] args) throws Exception {
		Planet	p = new Planet("Bob", PlanetType.Hephaestian, 4000);
		IceWorld w = new IceWorld(p, 513, 257);
		w.planet.setTilt(22);
		w.planet.setTemperature(Temperature.Standard);
		//g.draw();
		w.generate();
		//g.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
		Thread.sleep(10000);
	}
}
