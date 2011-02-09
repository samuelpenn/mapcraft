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
import java.io.File;

import javax.swing.*;


import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Gaian world.
 * 
 * @author Samuel Penn.
 */
class Gaian extends Tectonics {
	
	// Terrain types.
	private Terrain		water = null;
	private Terrain		rock = null;
	private Terrain		ice = null;  // This is ice over water.
	private Terrain		snow = null; // Snow and ice over land.
	
	private Terrain		desert = null;
	private Terrain		scrub = null;
	private Terrain		grassland = null;
	private Terrain 	woodland = null;
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
					}
				}
			}
			// Equator and 45 degree lines.
			g.setColor(new Color(255, 0, 0, 128));
			for (int y : new int[] { height/2, height/4, (height*3)/4 }) {
				g.drawLine(0, y*scale, width*scale, y*scale);
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
		
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }
	}
	
	public Gaian(int width, int height) {
		super(width, height);
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
	}
	
	private void draw() {
		//canvas.paint(canvas.getGraphics());		
	}
	
	
	/**
	 * Given a map, returns the value of a given point on that map. Wraps
	 * east to west, and returns 0 for any points off the north or south
	 * edges of the map.
	 */
	private int getNeighbour(int[][] map, int x, int y) {
		if (x < 0) x += width;
		if (x >= width) x -= width;
		if (y < 0 || y >= height) return 0;
		
		return map[x][y];
	}
	
	/**
	 * Given a map, sum the values of all the neighbours of the given point.
	 * The map is assumed to be the same size as the world terrain map, and
	 * wraps east-west but returns 0 for neighbours off the north or south
	 * edges of the map.
	 * 
	 * @param map		Two dimensional array containing the map.
	 * @param x			X coordinate of point.
	 * @param y			Y coordinate of point.
	 * @return			Sum of all the neighbours of (x,y).
	 */
	private int getNeighbourSum(int[][] map, int x, int y) {
		int		sum = 0;
		
		sum += getNeighbour(map, x-1, y);
		sum += getNeighbour(map, x+1, y);
		sum += getNeighbour(map, x, y-1);
		sum += getNeighbour(map, x, y+1);
		
		return sum;
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
	int	seaGrowthRate = 2;
	private void createSea() {
		
		if (planet.getHydrographics() == 0) {
			// Nothing to be done.
			return;
		} else if (planet.getHydrographics() == 100) {
			// Trivial case.
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					setTerrain(x, y, water);
					setHeight(x, y, 40 + getHeight(x, y)/4);
				}
			}
			return;
		}
		
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
			if (numberChanged <= seaGrowthRate*2) {
				if (h < hydrographics/2) {
					seaLevel += seaGrowthRate * 2;
				} else {
					seaLevel += seaGrowthRate;
				}
				draw();
			} else if (numberChanged < (hydrographics - h)/2) {
				//seaLevel += seaGrowthRate;
				//draw();
			} else if ((h < hydrographics) && Die.d100() == 1) {
				seaLevel++;
				draw();
			}
			//draw();
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
				//draw();
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
		
		rock = Terrain.create("Mountain", 50, 50, 50, 0.5, 0.5, 0.5, false);
		desert = Terrain.create("Desert", 100, 100, 0, 0.5, 0.5, 0.1, false);
		scrub = Terrain.create("Scrub", 100, 100, 0, 0.5, 1, 0.1, false);
		grassland = Terrain.create("Grass", 50, 100, 0, 1, 1, 0.1, false);
		woodland = Terrain.create("Woods", 50, 100, 0, 0.3, 1, 0.1, false);
		jungle = Terrain.create("Jungle", 0, 25, 0, 0.1, 0.7, 0.1, false);

		
		// Work out large scale tiles.
		generateContinents();
		copyShelfToTiles();
		setTiles();
		//debugTiles();
		addWater();
		marineClimate();
		makeItRain();
		ecology();

		generateLandscape();
		
		/*
		// Create the oceans of this world, according to the hydrographics
		// setting for the world.
		createSea();
		draw();


		// We might as well set the planet's data to be correct.
		planet.setHydrographics(getHydrographics());
		System.out.println("Hydro: "+planet.getHydrographics()+"%");
		
		generateIceCap();
		generateEcology();
		draw();
		*/
	}
	/**
	 * Given the rough tile map, try and generate a higher resolution
	 * landscape from this, using the tile guide plus the fractal
	 * height map as a basis.
	 */
	protected void generateLandscape() {
		int		tw = width/tileSize;
		int		th = height/tileSize;
		
		for (int y=0; y < th; y++) {
			for (int x=0; x < tw; x++) {
				switch (getTile(x, y)) {
				case DESERT:
					setTile(x, y, new ITileSetter() { 
							public void set(int averageHeight, int x, int y) {
								setTerrain(x, y, desert);
							} });
					break;
				case ARCTIC:
					setTile(x, y, new ITileSetter() { 
							public void set(int averageHeight, int x, int y) {
								setTerrain(x, y, ice);
							} });
					break;
				case MOUNTAINS:
					setTile(x, y, new ITileSetter() { 
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, rock);
							} else {
								//setHeight(x, y, 1.1);
								setTerrain(x, y, desert);
							}
						} });
					break;
				case TEMPERATE:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, grassland);
							} else {
								setTerrain(x, y, woodland);
							}
						} });						
					break;
				case SCRUB:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, desert);
							} else {
								setTerrain(x, y, scrub);
							}
						} });						
					break;
				case JUNGLE:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, jungle);
							} else {
								setTerrain(x, y, woodland);
							}
						} });						
					break;					
				case WATER:
					setTile(x, y, new ITileSetter() { 
						public void set(int averageHeight, int x, int y) { 
							if (getHeight(x, y) < planet.getHydrographics()) {
								setTerrain(x, y, water);
								setHeight(x, y, 0.5);
							} else {
								setTerrain(x, y, water);
							}
						} });
					break;
				}
			}
		}
		
		// Now try and fix mountains.
		int		fixed = 1, thing = 3;
		while (fixed > 0) {
			fixed = 0;
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == rock) continue;
					int		h = getHeight(x, y)+thing;
					if (getHeight(x-1, y) < h && getTerrain(x-1, y) == rock) {
						setTerrain(x, y, rock); fixed++;
					} else if (getHeight(x+1, y) < h && getTerrain(x+1, y) == rock) {
						setTerrain(x, y, rock); fixed++;
					} else if (getHeight(x, y-1) < h && getTerrain(x, y-1) == rock) {
						setTerrain(x, y, rock); fixed++;
					} else if (getHeight(x, y+1) < h && getTerrain(x, y+1) == rock) {						
						setTerrain(x, y, rock); fixed++;
					}
				}
			}
			//if (Die.d6() == 1) thing ++;
		}		
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
					if (latitude + Math.sqrt(landCount[x]) > 90 - getTemperature()*5) {
						setTerrain(x, y, ice);
					}
				} else {
					if (latitude + Math.sqrt(landCount[x]) + Math.sqrt(getHeight(x, y)) > 90 - getTemperature()*5) {
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
		int		snowHeight = 80;

		switch (planet.getTemperature()) {
		case UltraCold:
		case ExtremelyCold:
		case VeryCold:
			// Doesn't matter where we are, these world wills be completely
			// covered with snow and ice.
			snowHeight = 0;
			break;
		case Cold:
			snowHeight = averageSeaLevel + 20 - latitude;
			break;
		case Cool:
		case Standard:
		case Warm:
			snowHeight = averageSeaLevel + 40 - (latitude/2) - getTemperature() * 5;
			break;
		case Hot:
			snowHeight = 95;
			break;
		case VeryHot:
		case ExtremelyHot:
		case UltraHot:
			// Regardless of any other factors, such hot worlds will have
			// no snow and ice.
			snowHeight = 100;
			break;
		}
		
		return snowHeight;
	}
	
	/**
	 * Get the level of fertility of this world, where 100 = Earth. This
	 * is based on the life level and temperature.
	 * 
	 * @return		Fertility, from 0+.
	 */
	private int getFertility() {
		int		fertility = 50;
		
		System.out.println(planet.getLifeLevel()+"/"+planet.getTemperature()+"/"+planet.getAtmospherePressure());
		
		switch (planet.getLifeLevel()) {
		case None: case Organic: case Archaean: case Aerobic:
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
		
		switch (planet.getTemperature()) {
		case UltraCold:
		case ExtremelyCold:
		case VeryCold:
			fertility = 0;
			break;
		case Cold:
			fertility /= 2;
			break;
		case Cool:
			fertility *= 0.75;
			break;
		case Standard:
			break;
		case Warm:
			fertility *= 1.2;
			break;
		case Hot:
			fertility *= 0.5;
			break;
		case VeryHot:
		case ExtremelyHot:
		case UltraHot:
			fertility = 0;
			break;
		}
		
		if (planet.hasFeature(PlanetFeature.Dry)) {
			fertility *= 0.75;
		} else if (planet.hasFeature(PlanetFeature.Wet)) {
			fertility *= 1.25;
		}
		
		if (planet.getHydrographics() < 50) {
			fertility *= 0.5;
		}
		
		return fertility;
	}
	
	/**
	 * Work out the ecological distribution of this world. Basically just colours
	 * the land part of the map according to whether it is desert, grasslands or
	 * jungle.
	 */
	protected void generateEcology() {
		int		temperature = getTemperature();
		
		// Define colours for the terrain types.
		desert = Terrain.create("Desert", 100, 100, 0, 1.2, 1, 0, false);
		scrub = Terrain.create("Grass", 75, 75, 10, 1, 0.5, 0, false);
		grassland = Terrain.create("Grass", 25, 50, 25, 0, 1.5, 0, false);
		woodland = Terrain.create("Grass", 15, 50, 15, 0, 1, 0, false);
		jungle = Terrain.create("Jungle", 0, 25, 0, 0, 1, 0, false);
		
				
		// The first thing we do is figure out where the mountains are.
		// Mountains won't have anything growing on them. Mountains are
		// either 'Dirt' or 'Ice' depending on temperature.
		int		fertility = getFertility();
		int[][]	fertilityMap = new int[width][height];
		
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
				int		h = getHeight(x, y);
				fertilityMap[x][y] = 0;
				if (getTerrain(x, y).isWater() || getTerrain(x, y) == snow) {
					// This is sea, so ignore.
					continue;
				} else if (h > snowHeight) {
					if (Die.d100() >= snowHeight) {
						setTerrain(x, y, land);
					} else {
						setTerrain(x, y, snow);
					}
				} else if (getCoastCount(x, y) > 0 && fertility > latitude * 2) {
					setTerrain(x, y, grassland);
					fertilityMap[x][y] = fertility;// - latitude *2;
				} else {
					setTerrain(x, y, land);
				}
			}
		}

		System.out.println("Fertility: "+fertility);
		
		int		grassLine = fertility - 5;
		int		treeLine = fertility - 10;
		int		tropicsLatitude = planet.getTilt();
		
		boolean		done = false;
		while (!done) {
			draw();
			int		updates = 0;
			for (int y=0; y < height; y++) {
				int		latitude = getLatitude(y);
				int		tropics = 90 - Math.abs(latitude - tropicsLatitude);
				
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y).isWater()) {
						continue;
					}
					if (getTerrain(x, y) == snow || getTerrain(x, y) == rock) {
						continue;
					}
					if (getHeight(x, y) > grassLine) {
						continue;
					}
					int		sum = getNeighbourSum(fertilityMap, x, y);
					int		localFertility = fertility - latitude/2 - (int)((getHeight(x, y) - averageSeaLevel)*1.5);
					
					// Work out the effects of the tropics. These should be dry
					// and mostly desert, though it is affected by the amount of
					// land at that longitude.
					int		tropicalModifier = tropics - 80 + (getLandCount(x)/5);
					if (tropicalModifier > 0) {
						localFertility -= tropicalModifier * 2;
					} else if (latitude < tropicsLatitude) {
						// Anything between the tropics and the equator
						// are forced to be very fertile.
						localFertility *= 1.5;
					}
					
					if (getHeight(x, y) > treeLine) {
						localFertility /= 2;
					}
					
					if (sum > fertilityMap[x][y] && Die.d100() > getHeight(x, y)) {
						if (fertilityMap[x][y] < localFertility) {
							fertilityMap[x][y] += Die.d3();
							updates++;
						}
					}
					
					if (fertilityMap[x][y] > 90) {
						setTerrain(x, y, jungle);
					} else if (fertilityMap[x][y] > 60) {
						setTerrain(x, y, woodland);
					} else if (fertilityMap[x][y] > 30) {
						setTerrain(x, y, grassland);
					} else if (fertilityMap[x][y] > 10) {
						setTerrain(x, y, scrub);
					} else {
						setTerrain(x, y, desert);
					}
				}
			}
			System.out.println(updates);
			if (updates == 0) {
				done = true;
			}
		}
		draw();
	}

	
	public static void main(String[] args) throws Exception {
		Gaian g = new Gaian(512, 256);
		g.setPlanet(new Planet("Bob",PlanetType.Gaian,6400));
		g.planet.setHydrographics(70);
		g.planet.setTilt(22);
		g.planet.setTemperature(Temperature.Standard);
		g.planet.setLifeLevel(LifeType.Extensive);
		g.planet.setAtmosphereType(AtmosphereType.Standard);
		g.planet.setAtmospherePressure(AtmospherePressure.Standard);
		//g.draw();
		g.generate();
		g.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
	}
}
