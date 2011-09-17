/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.worlds;


import java.io.File;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * Generate a map for Arean and EoArean worlds. These are often desert
 * worlds. EoArean will have water, though it will often be either
 * shallow or limited in extent.
 * 
 * Planetary features which should be supported:
 *   EquatorialRidge
 *   GiantCrater
 *   HeavilyCratered
 *   PolarRidge
 *   SingleSea
 *   EquatorialSea
 *   Smooth
 * 
 * @author Samuel Penn.
 */
class Arean extends WorldBuilder {
	
	// Terrain types.
	private Terrain		water = null;
	private Terrain		rock = null;
	private Terrain		ice = null;  // This is ice over water.
	private Terrain		snow = null; // Snow and ice over land.
	private Terrain		desert = null;
	private Terrain		seabed = null;
	
	// Vegetation
	private Terrain		scrub = null;
	private Terrain		grass = null;
	private Terrain		woodland = null;
	
	private int[][]		waterMap = null;
	
	public boolean useImage = false;
	
	public Arean() {
		super();
	}

	/**
	 * Create a new empty world with a simple fractal height map.
	 */
	public Arean(int width, int height) {
		super(width, height);
	}
	

	
	private void generateSeas() {
		int		desiredHydrographics = planet.getHydrographics();
		
		if (desiredHydrographics == 100) {
			// Trivial case. Really shouldn't happen.
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					setTerrain(x, y, water);
					setHeight(x, y, 40 + getHeight(x, y)/4);
				}
			}
			return;
		} else {
			// We want to create some dried sea bed later on, so initially
			// make too much water.
			desiredHydrographics = (desiredHydrographics*2 + 100) / 3;
		}
		

		int			hydrographics = 0;
		int			seaHeight = 0;
		
		int[]	xp = new int[] { -1, +1, 0, 0 };
		int[]	yp = new int[] { 0, 0, -1, +1 };

		waterMap = new int[width][height];
		
		while (hydrographics < desiredHydrographics) {
			int		count = 0;
			// Try and grow the amount of water
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == water) {
						int		h = getHeight(x, y);
						for (int p=0; p < 4; p++) {
							int		xx = x+xp[p];
							int		yy = y+yp[p];
							if ((getHeight(xx, yy) <= (h+seaHeight)) && getTerrain(xx, yy) != water) {
								setSeaDepth(xx, yy, 1);
							} else if (Die.d20() == 1) {
								setHeight(xx, yy, getHeight(xx, yy)-1);
							}
						}
					}
				}
			}
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (waterMap[x][y] == 1) {
						setTerrain(x, y, water);
						count++;
					}
					waterMap[x][y] = 0;
				}
			}
			if (count < 10) {
				seaHeight += 5;
			} else {
				seaHeight /= 2;
			}

			// Work out percentage of water surface area
			hydrographics = 0;
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == water) hydrographics++;
				}
			}
			hydrographics = (100*hydrographics) / (width * height);
			//System.out.println(hydrographics+"% / "+count);
		}
		
		
		desiredHydrographics = planet.getHydrographics();
		
		// Arean worlds are always desert planets.
		if (planet.getType() == PlanetType.Arean) desiredHydrographics /= 2;
		
		if (desiredHydrographics == 0) {
			// Quick and simple. All seas have dried up.
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == water) setTerrain(x, y, seabed);
				}
			}
		} else while (hydrographics > desiredHydrographics) {
			int		count = 0;
			// Try and shrink the amount of water
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == water) {
						int		coasts = 0;
						for (int p=0; p < 4; p++) {
							int		xx = x+xp[p];
							int		yy = y+yp[p];
							if (getTerrain(xx, yy) != water) coasts++;
						}
						if (Die.d6() <= coasts) setTerrain(x, y, seabed);
					}
				}
			}

			// Work out percentage of water surface area
			hydrographics = 0;
			for (int y=0; y < height; y++) {
				for (int x=0; x < width; x++) {
					if (getTerrain(x, y) == water) hydrographics++;
				}
			}
			hydrographics = (100*hydrographics) / (width * height);
			System.out.println(hydrographics+"% / "+count);
		}
		
		debugTiles();
	}
	
	private void setSeaDepth(int x, int y, int depth) {
		if (y < 0 || y >= height) return;
		if (x < 0) x+= width;
		if (x >= width) x -= width;
		
		if (depth < 0) depth = 0;
		
		waterMap[x][y] = depth;
	}
	
	/**
	 * Initially, generate a rough low resolution map of general terrain type.
	 * This consists of 16x16 pixel tiles. After we have a general idea of
	 * what the planet looks like, these tiles are used to generate the high
	 * resolution height and terrain map.
	 */
	private void generateTiles() {
		int		tw = getTileWidth();
		int		th = getTileHeight();
		
		createTileMap();
		
		// If we have water, assign some.
		if (planet.getHydrographics() > 0) {
			int		waterTiles = (tw * th * planet.getHydrographics()) / 100;
			
			if (waterTiles < 2) {
				tileMap[Die.rollZero(tw)][Die.rollZero(th/2)+(th/4)] = TileType.WATER;
			} else for (int w = 0; w < waterTiles; w++) {
				// Randomly assign water away from the poles.
				tileMap[Die.rollZero(tw)][Die.rollZero(th-4)+2] = TileType.WATER;				
			}
		}
		
		// Now generate some mountains.)
		int		numRanges = Die.d6()+3;
		
		if (planet.hasFeature(PlanetFeature.EquatorialRidge)) {
			// Planet has a ridge of mountains running around the equator.
			for (int x=0; x < tw; x++) {
				setTile(x, th/2, TileType.MOUNTAINS);
			}
			numRanges /= 2;
		} else if (planet.hasFeature(PlanetFeature.PolarRidge)) {
			for (int y=0; y < th; y++) {
				setTile(tw/4, y, TileType.MOUNTAINS);
				setTile((tw*3)/4, y, TileType.MOUNTAINS);
			}
			numRanges /= 3;
		} else if (planet.hasFeature(PlanetFeature.Smooth)) {
			// No mountains of any worth.
			numRanges = 0;
		}
		
		while (numRanges > 0) {
			int		x = Die.rollZero(tw-4)+2;
			int		y = Die.rollZero(th-4)+2;
			if (tileMap[x][y] != TileType.WATER) {
				while (getTile(x, y) != TileType.WATER) {
					setTile(x, y, TileType.MOUNTAINS);
					switch (Die.d4()) {
					case 1: x++; break;
					case 2: x--; break;
					case 3: y++; break;
					case 4: y--; break;
					}
					if (Die.d6() == 1) break;
				}
				numRanges--;
			}
		}
		debugTiles();
	}

	/**
	 * Called after generating a random height map. Work out general 
	 * terrain types etc.
	 */
	public void generate() {
		// Generate a rough map of the surface.
		generateTiles();
		
		// Figure out the type of seas that we have. This doesn't have any
		// real effect, except to give the water a slightly different colour.
		switch (Die.d8()) {
		case 1:
			water = Terrain.create("Water", 75, 75, 150, true);
			break;
		case 2: case 3: case 4: case 5:
			water = Terrain.create("Water", 120, 100, 250, true);
			break;
		default:
			water = Terrain.create("Water", 100, 100, 200, true);
		}			
		
		ice = Terrain.create("Ice", 255, 255, 255, true);
		snow = Terrain.create("Snow", 200, 200, 200, 0.5, 0.5, 0.5, false);

		switch (planet.getType()) {
		case EoArean:
			desert = Terrain.create("Desert", 125, 100, 10, 1, 1, 0.2, false);
			rock = Terrain.create("Rock", 75, 50, 20, 0.5, 0.5, 0.5, false);
			ice = Terrain.create("Ice", 150, 150, 100, 1, 0.75, 0.75, false);
			
			scrub = Terrain.create("Scrub", 100, 125, 10, 0.75, 1, 0.2, false);
			grass = Terrain.create("Grass", 90, 125, 10, 0.5, 1, 0.1, false);
			woodland = Terrain.create("Woodland", 50, 100, 0, 0.25, 0.75, 0.1, false);
			break;
		case AreanLacustric:
			desert = Terrain.create("Desert", 125, 75, 50, 1, 0.6, 0.3, false);
			rock = Terrain.create("Rock", 75, 50, 20, 0.5, 0.5, 0.5, false);
			ice = Terrain.create("Ice", 150, 150, 150, 1, 1, 1, false);
			break;
		default:
			// Should just be type 'Arean' left.
			desert = Terrain.create("Desert", 125, 75, 10, 1, 0.5, 0.2, false);
			rock = Terrain.create("Rock", 75, 50, 20, 0.5, 0.5, 0.5, false);
			ice = Terrain.create("Ice", 150, 100, 100, 1, 0.75, 0.75, false);
		}
		seabed = desert; // Looks the same, but we need to tell them apart.
		
		generateLandscape();

		// Create the oceans of this world, according to the hydrographics
		// setting for the world.
		generateSeas();

		// We might as well set the planet's data to be correct.
		planet.setHydrographics(getHydrographics());
		
		generateIceCap();
		generateEcology();
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
				case WATER:
					setTile(x, y, new ITileSetter() { 
						public void set(int averageHeight, int x, int y) { 
							if (getHeight(x, y) < planet.getHydrographics()) {
								setTerrain(x, y, water);
								setHeight(x, y, 0.5);
							} else if (Die.d100()==1) {
								setTerrain(x, y, water);
							} else {
								setTerrain(x, y, desert);
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
		
		// Add craters
		int		numCraters = Die.d100(3);
		if (planet.hasFeature(PlanetFeature.Smooth)) {
			numCraters /= 10;
		} else if (planet.hasFeature(PlanetFeature.HeavilyCratered)) {
			numCraters *= 3;
		}

		impacts(numCraters, 10, 0.8, desert);

		if (planet.hasFeature(PlanetFeature.GiantCrater)) {
			int		x = width/2 + Die.d20(2) - Die.d20(2);
			int		y = height/2 + Die.d20(3) - Die.d20(3);
			
			addCrater(x, y, Die.d20(3)+30, 0.5, desert, null);
		}		
	}
	
	protected void generateIceCap() {
				
		int		iceLatitude = 0;
		int		snowLatitude = 0;
		
		switch (planet.getTemperature()) {
		case Hot:
		case VeryHot:
			iceLatitude = 120;
			snowLatitude = 120;
			break;
		case Warm:
			iceLatitude = 85;
			snowLatitude = 95;
			break;
		case Standard:
		case Cool:
			iceLatitude = 60;
			snowLatitude = 90;
			break;
		case Cold:
			iceLatitude = 45;
			snowLatitude = 80;
			break;
		case VeryCold:
			iceLatitude = 30;
			snowLatitude = 75;
			break;
		case ExtremelyCold:
		case UltraCold:
			iceLatitude = 0;
			snowLatitude = 70;
			break;
		}

		if (planet.getType() == PlanetType.AreanLacustric) {
			// AreanLacustric worlds are always completely frozen.
			iceLatitude = 0;
		} else if (planet.getType() == PlanetType.Arean) {
			// Arean worlds are dry, so little surface ice. 
			snowLatitude = (snowLatitude + 90)/2;
		}

		for (int y = 0; y < height; y++) {
			int		latitude = getLatitude(y);
			for (int x = 0; x < width; x++) {
				if (latitude+Die.d6() > iceLatitude && getTerrain(x, y) == water) {
					setTerrain(x, y, ice);
				}
				if (latitude+Die.d6() > snowLatitude && getTerrain(x, y) != water) {
					if (Die.d3() == 1) setTerrain(x, y, ice);
				}
			}
		}
	}
	
	/**
	 * On Arean subtypes, any land life is limited to being on the old
	 * sea beds. Actual Arean worlds should have no life at all. 
	 */
	protected void generateEcology() {
		int		latitude = 0;
		int		temperature = getTemperature();
		int		planetFertility = 0;
		
		switch (planet.getLifeLevel()) {
		case None: case Organic: case Archaean: case Aerobic:
			planetFertility = -10;
			break;
		case ComplexOcean:
			planetFertility = -9;
			break;
		case SimpleLand:
			planetFertility = -5;
			break;
		}
		
		for (int y=0; y < height; y++) {
			latitude = getLatitude(y);
			for (int x=0; x < width; x++) {
				if (getTerrain(x, y) == seabed) {
					int		fertility = planetFertility + 2*temperature;

					if (getTerrain(x-1, y) == sea) fertility++;
					if (getTerrain(x+1, y) == sea) fertility++;
					if (getTerrain(x, y-1) == sea) fertility++;
					if (getTerrain(x, y+1) == sea) fertility++;
					
					fertility -= latitude/15;
					
					switch (Die.d6()+fertility) {
					case 1: case 2: case 3:
						setTerrain(x, y, scrub);
						break;
					case 4: case 5: case 6:
						setTerrain(x, y, grass);
						break;
					case 7: case 8: case 9: case 10: case 11: case 12:
						setTerrain(x, y, woodland);
						break;
					}
				}
			}
		}
	}
	

	public static void main(String[] args) throws Exception {
		Arean g = new Arean(512, 256);
		g.setPlanet(new Planet("Bob",PlanetType.EoArean,2400));
		g.planet.setHydrographics(20);
		g.planet.setTilt(22);
		g.planet.setTemperature(Temperature.Warm);
		g.planet.setLifeLevel(LifeType.None);
		g.planet.setAtmosphereType(AtmosphereType.Standard);
		g.planet.setAtmospherePressure(AtmospherePressure.VeryThin);
		
		//g.planet.addFeature(PlanetFeature.GiantCrater);
		//g.draw();
		g.generate();
	    g.getWorldMap(2).save(new File("/home/sam/Arean.jpg"));
	}
}
