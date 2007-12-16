/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.4 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;


import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Gaian world.
 * 
 * @author Samuel Penn.
 */
class Gaian extends WorldBuilder {
	
	// Terrain types.
	private Terrain		water = null;
	private Terrain		rock = null;
	private Terrain		ice = null;  // This is ice over water.
	private Terrain		snow = null; // Snow and ice over land.
	private Terrain		desert = null;
	private Terrain		grasslands = null;
	private Terrain		jungle = null;
	
	private Canvas		canvas = null;
	private int[][]		waterMap = null;
	
	public boolean useImage = false;
	
	private class MapCanvas extends Canvas implements MouseListener {
		private Gaian b = null;
		
		MapCanvas(Gaian b) {
			super();
			this.b = b;
			
			addMouseListener(this);
		}
		public void paint(Graphics g) {
			int scale = 3;
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					int		h = b.getHeight(x, y) * 2;
					if (b.useImage) {
						if (b.getTerrain(x,y) == b.water) {
							g.setColor(new Color(0, 0, 200));
							g.fillRect(x*scale, y*scale, scale, scale);
						} else {
							g.setColor(b.getTerrain(x, y).getColor(b.getHeight(x, y)));
							g.fillRect(x*scale, y*scale, scale, scale);																					
						}
					} else {
						g.setColor(getTerrain(x, y).getColor(h));
						g.fillRect(x*scale, y*scale, scale, scale);
						/*
						if (waterMap != null && waterMap[x][y] > 0) {
							int		d = (getSeaLevel(x, y) - averageSeaLevel)*5 + 120;
							if (d > 255) d = 255;
							if (d < 0) d = 0;
							g.setColor(new Color(0, 0, d));
							g.fillRect(x*scale, y*scale, scale, scale);
						} else {
							g.setColor(new Color(h, h, h));
							g.fillRect(x*scale, y*scale, scale, scale);
						}
						*/
					}
				}
			}
			
		}
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			int		x = e.getX()/3;
			int		y = e.getY()/3;
			if (e.getButton() == MouseEvent.BUTTON1) {
				b.waterMap[x][y] += 10000;
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				System.out.println("["+b.getHeight(x, y)+"] ["+b.waterMap[x][y]+"]");
			}
		}
		
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	public Gaian() {
		super();
	}
	
	public Gaian(int width, int height) {
		super(width, height);
		
		JFrame		frame = new JFrame("Gaian World");
		canvas = new MapCanvas(this);
		canvas.setPreferredSize(new Dimension(width*3, height*3));
		canvas.setVisible(true);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(width*3, height*3));
		frame.setSize(new Dimension(width*3+100, height*3+100));
	}
	
	private void draw() {
		canvas.paint(canvas.getGraphics());		
	}
	
	
	
	
	
	private int getCoastCount(int x, int y) {
		int		neighbours = 0;
		neighbours += (getSeaDepth(x-1, y)>0)?1:0;
		neighbours += (getSeaDepth(x+1, y)>0)?1:0;
		neighbours += (getSeaDepth(x, y-1)>0)?1:0;
		neighbours += (getSeaDepth(x, y+1)>0)?1:0;

		return neighbours;
	}

	
	int	averageSeaLevel = 0;
	int	seaGrowthRate = 5;
	private void createSea() {
		normaliseHeights();
		basicFlood(30);
		// Initialise the map to be empty.
		waterMap = new int[width][height];
		
		int			hydrographics = planet.getHydrographics();
		int			seaLevel = hydrographics/3;
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				setTerrain(x, y, land);
				waterMap[x][y] = 0;
				if (getHeight(x, y) < seaLevel) {
					waterMap[x][y] = seaLevel - getHeight(x, y);
					setTerrain(x, y, water);
				}
			}
		}
		
		boolean		done = false;
		draw();
		while (!done) {
			//draw();
			
			
			int[]	xp = new int[] { -1, +1, 0, 0 };
			int[]	yp = new int[] { 0, 0, -1, +1 };
			
			// The purpose of this section is to level of the water so
			// that high water areas flow down to low water areas. For
			// each square, find the direction which gives the steepest
			// flow, and move half of the difference in that direction.
			int		numberChanged = 0;
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					int		h = getHeight(x, y);
					int		cc = getCoastCount(x, y);

					if (getSeaDepth(x, y) > 0) {
						setSeaDepth(x, y, seaLevel-h);
					} else if (h < seaLevel && cc > 0) {
						setTerrain(x, y, sea);
						setSeaDepth(x, y, seaLevel-getHeight(x, y));
						numberChanged++;
					} else {
						if (cc == 1 && Die.d100() == 1) {
							setHeight(x, y, h-1);
						} else if (cc == 2 && Die.d20() == 1) {
							setHeight(x, y, h-1);
						} else if (cc == 3 && Die.d8() == 1) {
							setHeight(x, y, h-2);
						} else if (cc == 4 && Die.d6() == 1) {
							setHeight(x, y, h-3);
						}
					}
					
				}
			}
			int		h = getHydrographics();
			if (numberChanged == 0) {
				if (h < hydrographics/2) {
					seaLevel += seaGrowthRate * 2;
				} else {
					seaLevel += seaGrowthRate;
				}
				//draw();
			} else if (numberChanged < (hydrographics - h)) {
				seaLevel += seaGrowthRate;
				draw();
			} else if ((h < hydrographics) && Die.d100() == 1) {
				seaLevel++;
				draw();
			}

			int		total = 0, count=0;
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					if (getSeaDepth(x, y) != 0) {
						count++;
						total += getSeaLevel(x, y);
					}
				}
			}
			averageSeaLevel = total / count;
			System.out.println("Hydro: "+h+" av "+averageSeaLevel+" delta "+numberChanged);
			if (h > hydrographics && numberChanged < 50) {
				draw();
				done = true;
			}
		}

		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				if (getSeaDepth(x, y) == 0) {
					setTerrain(x, y, land);
				} else {
					setTerrain(x, y, sea);
				}
			}
		}
		draw();
	}
	
	private int getSeaDepth(int x, int y) {
		if (y < 0 || y >= height) return 0;
		if (x < 0) x+= width;
		if (x >= width) x -= width;
		
		return waterMap[x][y];
	}

	private void setSeaDepth(int x, int y, int depth) {
		if (y < 0 || y >= height) return;
		if (x < 0) x+= width;
		if (x >= width) x -= width;
		
		if (depth < 0) depth = 0;
		
		waterMap[x][y] = depth;
	}

	private int getSeaLevel(int x, int y) {
		if (y < 0 || y >= height) return 0;
		if (x < 0) x += width;
		if (x >= width) x-= width;
		return getHeight(x, y) + waterMap[x][y];
	}
	
	


	/**
	 * Called after generating a random height map. Work out general 
	 * terrain types etc.
	 */
	public void generate() {
		// Figure out the type of seas that we have. This doesn't have any
		// real effect, except to give the water a slightly different colour.
		if (planet.hasFeature(PlanetFeature.BlackWater)) {
			water = Terrain.create("Water", 0, 0, 50, true);
		} else if (planet.hasFeature(PlanetFeature.GreenWater)) {
			water = Terrain.create("Water", 0, 150, 100, true);
		} else if (planet.hasFeature(PlanetFeature.PurpleWater)) {
			water = Terrain.create("Water", 100, 50, 100, true);
		} else {
			switch (Die.d8()) {
			case 1: case 2:
				water = Terrain.create("Water", 50, 50, 150, true);
				break;
			case 3: case 4:
				water = Terrain.create("Water", 100, 100, 250, true);
				break;
			default:
				water = Terrain.create("Water", 100, 100, 200, true);
			}			
		}
		
		ice = Terrain.create("Ice", 255, 255, 255, true);
		snow = Terrain.create("Snow", 200, 200, 200, 0.5, 0.5, 0.5, false);
		
		
		// Create the oceans of this world, according to the hydrographics
		// setting for the world.
		createSea();
		draw();

		/*
		int	waterCount = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getTerrain(x, y) == water) {
					waterCount++;
					if (planet.hasFeature(PlanetFeature.Algae)) {
						int		wetness = getWetness(x, y, true);
						if (wetness < 85) {
							setHeight(x, y, wetness);
							setTerrain(x, y, Terrain);
						}
					}
				}
			}
		}
		*/
		// We might as well set the planet's data to be correct.
		planet.setHydrographics(getHydrographics());
		System.out.println("Hydro: "+planet.getHydrographics()+"%");
		
		generateIceCap();
		//generateEcology();
		draw();
	}
	
	protected void generateIceCap() {
		int		landCount[] = new int[width];
		
		// The more land there is in a given longitude, the greater
		// the extant of the ice cap.
		for (int x=0; x < width; x++) {
			landCount[x] = getLandCount(x);
		}

		for (int y = 0; y < height; y++) {
			int		latitude = getLatitude(y);
			for (int x = 0; x < width; x++) {
				if (getTerrain(x, y).isWater()) {
					if (latitude + Math.sqrt(landCount[x]) > 85 - getTemperature()*5) {
						setTerrain(x, y, ice);
					}
				} else {
					if (latitude + Math.sqrt(landCount[x]) + Math.sqrt(getHeight(x, y)) > 85 - getTemperature()*5) {
						setTerrain(x, y, snow);
					}
				}
			}
		}
	}
	
	/**
	 * Get the maximum height at which vegetation is found. This is based
	 * on the world's atmospheric density; on low density worlds vegetation
	 * can only be found in the lowlands.
	 * 
	 * It is also affected by temperature, which is in turn affected by
	 * latitude.
	 */
	private int getGrassHeight(int latitude) {
		int		grassHeight = 0;
		
		switch (planet.getAtmospherePressure()) {
		case None: grassHeight = 0; break;
		case Trace:	grassHeight = 30; break;
		case VeryThin: grassHeight = 45; break;
		case Thin: grassHeight = 55; break;
		case Standard: grassHeight = 65; break;
		case Dense: grassHeight = 75; break;
		case VeryDense: grassHeight = 85; break;
		case SuperDense: grassHeight = 95; break;
		}

		switch (planet.getTemperature()) {
		case ExtremelyCold: grassHeight = -100; break;
		case VeryCold: grassHeight -= 50; break;
		case Cold: grassHeight -= 15; break;
		case Cool: grassHeight -= 5; break;
		case Warm: grassHeight += 5; break;
		case Hot: grassHeight += 10; break;
		case VeryHot: grassHeight -= 10; break;
		case ExtremelyHot: grassHeight -= 80; break;
		}
		
		grassHeight *=  ((100 - latitude ) / 100.0);
		
		return grassHeight;
	}
	
	private int getSnowHeight(int latitude) {

		int		snowHeight = 0;
		switch (planet.getTemperature()) {
		case ExtremelyCold: snowHeight = -50; break;
		case VeryCold: snowHeight = 20; break;
		case Cold: snowHeight = 40; break;
		case Cool: snowHeight = 65; break;
		case Standard: snowHeight = 80; break;
		case Warm: snowHeight = 90; break;
		case Hot: snowHeight = 120; break;
		case VeryHot: snowHeight = 200; break;
		case ExtremelyHot: snowHeight = 500; break;
		}
		snowHeight *= 0.75;
		
		snowHeight -= latitude/2;
		
		return snowHeight;
	}
	
	/**
	 * Get the level of fertility of this world, where 100 = Earth. This
	 * is based on the life level.
	 * 
	 * @return		Fertility, from 0+.
	 */
	private int getFertility() {
		int		fertility = 50;
		
		switch (planet.getLifeLevel()) {
		case None: case Proteins: case Protozoa: case Metazoa:
			fertility = 0;
			break;
		case ComplexOcean:
			fertility = 10;
			break;
		case SimpleLand:
			fertility = 50;
			break;
		case ComplexLand:
			fertility = 75;
			break;
		case Extensive:
			fertility = 100;
			break;
		}
		
		return fertility;
	}
	
	protected void generateEcology() {
		int		temperature = getTemperature();
		
		//normaliseHeights();
		
		// The first thing we do is figure out where the mountains are.
		// Mountains won't have anything growing on them. Mountains are
		// either 'Dirt' or 'Ice' depending on temperature.
		for (int y=0; y < height; y++) {
			int		latitude = getLatitude(y);
			int		grassHeight = getGrassHeight(latitude);
			int		snowHeight = getSnowHeight(latitude);
			int		tilt = planet.getTilt();
			if (tilt > 180) tilt -= 180;
			if (tilt > 90) tilt = 90 - (tilt - 90);
			
			int		tropical = 90 - (int)(Math.pow(latitude - tilt, 2)/5);
			if (tropical < 0) {
				tropical = 0;
			}

			for (int x=0; x < width; x++) {
				if (getTerrain(x, y).isWater()) {
					// This is sea, so ignore.
					continue;
				} else if (getHeight(x, y) > snowHeight) {
					setHeight(x, y, 99);
					setTerrain(x, y, land);
				//} else if (getHeight(x, y) > grassHeight) {
				//	setTerrain(x, y, Terrain.Dirt);
				} else {
					int		fertility = (int)(getFertility() - tropical);

					if (getHeight(x, y) > grassHeight) {
						fertility *= 0.2;
					} else if (getHeight(x, y) > grassHeight/2) {
						fertility *= 0.5;
					} else if (getHeight(x, y) > grassHeight/3) {
						fertility *= 0.75;
					} else if (getHeight(x, y) < grassHeight/5) {
						fertility *= 1.25;
					}

					fertility += getWetness(x, y, false)/5;
					fertility *= 1.5;
					fertility -= getHeight(x, y);
					
					if (planet.hasFeature(PlanetFeature.Dry)) {
						fertility += (height/4) - getLandCount(x)/3;
					} else if (planet.hasFeature(PlanetFeature.Wet)) {
						fertility += (height/4) - getLandCount(x)/7;
					} else {
						fertility += (height/4) - getLandCount(x)/5;
					}
										
					setHeight(x, y, fertility);
					setTerrain(x, y, land);
				}
			}
		}
		
		
		
	}

	
	public static void main(String[] args) throws Exception {
		Gaian g = new Gaian(513, 257);
		g.setPlanet(new Planet("Bob",PlanetType.Gaian,6400));
		g.planet.setHydrographics(50);
		g.planet.setTemperature(Temperature.Standard);
		//g.draw();
		g.generate();
		//g.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
	}
}
