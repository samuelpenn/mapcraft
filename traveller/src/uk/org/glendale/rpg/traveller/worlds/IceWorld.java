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
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
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
	private PlanetType		type = PlanetType.Europan;
	
	// Ice worlds commonly have the following terrain types.

	private Terrain			cleanIce = null;  // Base ice types.
	private Terrain			impureIce = null; // Coloured by impurities.
	private Terrain			ejecta = null;    // Thrown up by impacts.
	private Terrain			snow = null;      // Freshly laid ice/snow.
	private Terrain			rock = null;      // Actual rock.
	private Terrain			flatIce = null;
	
	private	int				craters = 500;
	private int				stressMarks = 0;  // Straight lines which cross the surface.
	private int				impureLimit = 0;  // This height or lower, use impure.
	
	private int				craterSize = 12;
	private double			craterDepth = 0.7;
	private int				volcanoes = 0;
	private int				volcanoDensity = 0;
	private int				volcanoRadius = 0;
	
	private FloodPlain[]	plains = null;
		
	IceWorld() {
		super();
		System.out.println("Creating Barren world");
	}

	private Canvas		canvas = null;
	
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
			cleanIce = Terrain.create("LithicGelidian", 0, 0, 0, 3, 2, 1.5, false);
			impureIce = Terrain.create("LithicGelidianImpact", 25, 20, 0, 3, 2, 1, false);
			craters = 100;
			craterSize = 10;
			craterDepth = 0.8;
			break;
		case Europan:
			cleanIce = Terrain.create("Ice", 140, 140, 150, 1, 1, 1, false);
			impureIce = Terrain.create("Dirty Ice", 120, 60, 40, 1.2, 1.4, 1.6, false);
			ejecta = Terrain.create("Ejecta", 150, 150, 160, 2, 2, 2, false);
			snow = Terrain.create("Flat Ice", 240, 240, 255, 0.1, 0.1, 0.1, false);
			
			// We want about half the map to be 'impure' ice. Find a suitable
			// height (before any alterations) which gives us this.
			impureLimit = 40;
			int halfMapSize = width * height / 2;
			for (int i=0; i < 100; i++) {
				halfMapSize -= heightBuckets[i];
				if (halfMapSize < 0) {
					impureLimit = i;
					break;
				}
			}
			// Few craters, due to active cryology.
			craters = Die.d6()*2;
			if (planet.hasFeature(PlanetFeature.HeavilyCratered)) {
				craters *= 3;
			}
			// Tidal stress marks.
			if (planet.hasFeature(PlanetFeature.TidalStressMarks)) {
				stressMarks = Die.d10() * 5;
			}
			// Active cryovolcanism
			if (planet.hasFeature(PlanetFeature.CryoVolcanism)) {
				volcanoes = Die.d6()+2;
				volcanoRadius = 15 + Die.d10(2);
				volcanoDensity = 500 + Die.d8(2) * 100;
			}
			break;
		case EuTitanian:
		case MesoTitanian:
		case TitaniLacustric:
			cleanIce = Terrain.create("Ice", 50, 50, 50, 1, 1, 1, false);
			break;
		case Iapetean:
			cleanIce = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		case JaniLithic:
			cleanIce = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		case Tritonic:
			cleanIce = Terrain.create("Ice", 150, 150, 150, 1, 1, 1.2, false);
			break;
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
	
	public void generate() {
		// Now set the colours.
		for (int y=0; y < height; y++) {
			int		limit = impureLimit;
			if (y < 50) limit -= (50 - y);
			if (y > (height - 50)) limit -= (50 - (height - y));
			for (int x=0; x < width; x++) {
				setTerrain(x, y, cleanIce);
				if (impureIce != null && getHeight(x, y) <= limit) {
					setTerrain(x, y, impureIce);
				}
			}
		}
		
		// Let's have some asteroid impacts.
		if (craters > 0) impacts(craters, craterSize, craterDepth, impureIce, ejecta);
		if (stressMarks > 0) createStressMarks(stressMarks, impureIce);
		if (volcanoes > 0) createVolcanoes(volcanoes, volcanoRadius, volcanoDensity, snow);
		
		if (plains != null) {
			for (FloodPlain p : plains) {
				floodWithLava(p.terrain, p.number, p.height);
				draw();
			}
		}
	}
	
	/**
	 * Stress marks are lines which criss-cross the surface of the world,
	 * caused by tidal forces cracking the crust. They are common on
	 * Europan worlds, where they tend to be dark. They are generated as
	 * mostly straight lines of random length. 
	 * 
	 * @param stressMarks		Number of stress lines.
	 * @param terrain			Type of terrain to use for these lines.
	 */
	private void createStressMarks(int stressMarks, Terrain terrain) {
		for (int i=0; i < stressMarks; i++) {
			double	x = Die.rollZero(width);
			double	y = Die.rollZero(height);
			double	xd = Die.d10()/10.0;
			double	yd = Die.d10()/20.0;
			
			if (x > width/2) xd *= -1;
			if (y > height/2) yd *= -1;
			
			int		length = width/2 + Die.rollZero(width);
			
			for (; length > 0; length--) {
				int		xx = (int)x+Die.d2()-Die.d2();
				int		yy = (int)y+Die.d2()-Die.d2();

				setTerrain(xx, yy, terrain);
				setHeight(xx, yy, (int)(getHeight(xx, yy) * 0.5));
				x += xd;
				y += yd;
				
				xd += (Die.d10() - Die.d10()) / 1000.0; 
				yd += (Die.d10() - Die.d10()) / 1000.0;
				
				if (y <= 0 || y >= height-1) {
					yd *= -1;
					xd *= -1;
					if (y <= 0) y = 0;
					if (y >= height-1) y = height-1;
					x += (width/2);
					if (x > width) x -= width;
				}
			}
		}
	}
	
	private void createVolcanoes(int number, int radius, int density, Terrain terrain) {
		for (int i=0; i < number; i++) {
			int		x = Die.rollZero(width);
			int		y = Die.rollZero(height/2) + height/4;
			
			for (int j=0; j < density; j++) {
				int		px = x + Die.rollZero(radius)*2 - Die.rollZero(radius);
				int		py = y + Die.rollZero(radius) - Die.rollZero(radius);
				setTerrain(px, py, snow);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Planet	p = new Planet("Bob", PlanetType.Europan, 4000);
		p.setTilt(22);
		p.setTemperature(Temperature.VeryCold);
		p.addFeature(PlanetFeature.TidalStressMarks);
		
		IceWorld w = new IceWorld(p, 513, 257);
		//g.draw();
		w.generate();
		//g.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
		Thread.sleep(10000);
	}
}
