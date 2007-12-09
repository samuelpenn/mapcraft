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

import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.utils.Die;

/**
 * Generates a map for a Gaian world.
 * 
 * @author Samuel Penn.
 */
class Gaian extends WorldBuilder {
	public Gaian() {
		super();
	}
	
	public Gaian(int width, int height) {
		super(width, height);
	}
	
	private int[][]  plateMap = null;
	
	/**
	 * Initialise the map of the continental plates to be blank, then
	 * seed it with some random continents. The map is simple a low
	 * resolution version of the world, and a 'continent' is grown from
	 * these initial seeds.
	 * 
	 * @param number		Number of continental plates to seed.
	 */
	void seedContinents(int number) {
		// Initialise the map to be empty.
		plateMap = new int[width][height];
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				plateMap[x][y] = 0;
			}
		}
		
		// Set a number of squares to be non-zero. We currently
		// identify each plate with a number, though this information
		// isn't really used except as inheritence by child squares.
		for (int c=0; c < number; c++) {
			int		x = Die.rollZero(width);
			int		y = (Die.rollZero(height) + Die.rollZero(height))/2;
			int		id = Die.rollZero(c+1)+1;
			
			if (planet.hasFeature(PlanetFeature.PolarLand)) {
				if (c%2 == 0) y = 0;
				if (c%2 == 1) y = height-1;
			} else if (planet.hasFeature(PlanetFeature.EquatorialLand)) {
				y = height/2;
			} else if (planet.hasFeature(PlanetFeature.CrateredSeas)) {
				if (Die.d10() < 8) {
					id = -id;
					number++;
				}
			} else if (Die.d4() == 1) {
				// Add a forced sea.
				id = -id;
				number++;
			}
			plateMap[x][y] = id;

			if (Die.d4() == 1 && !planet.hasFeature(PlanetFeature.PolarLand) && !planet.hasFeature(PlanetFeature.EquatorialLand)) {
				// Add a string of seeds.
				int		xp = Die.d12();
				int		yp = Die.d8();
				while (Die.die(100) > 25) {
					xp += Die.d8() - Die.d8();
					yp += Die.d8() - Die.d6();
					
					x += xp;
					y += yp;
					
					if (y >= height || y < 0) break;
					if (x >= width) x -= width;
					if (x < 0) x += width;
					
					plateMap[x][y] = id;
				}
			}
		}		
	}
	
	private void growInto(int plateId, int x, int y) {
		// If we go off the North/South edge, then randomly come back in
		// elsewhere on that edge. These are the poles, so the map gets
		// strange anyway at this point.
		if (y < 0) {
			y = 0;
			x = Die.rollZero(width);
		}
		if (y >= height) {
			y = height-1;
			x = Die.rollZero(width);
		}
		// However, we wrap around the East/West edges.
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		// Simulate plates crashing into each other and raising mountains.
		if (plateId > 100 && Die.d100() > 20) {
			return;
		}
		
		if (plateMap[x][y] == 0) {
			plateMap[x][y] = plateId;
		} else if (plateMap[x][y] > 0 && plateMap[x][y] < 100 && plateMap[x][y] != (plateId)) {
			plateMap[x][y] = 100 + plateId;
		}
	}
	
	void growContinents(int baseChance) {
		boolean		done = false;
		// We don't want perfect continents, so only partially fill the land
		// allotment, and let island generation fill the rest.
		int			hydroToAimFor = planet.getHydrographics() + Die.d8(2);
		while (!done) {
			int[][]		tempMap = new int[width][height];
			
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					tempMap[x][y] = plateMap[x][y];
				}
			}
			
			int		free = 0;
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					int		chance = baseChance + getHeight(x, y)/3;
					if (tempMap[x][y] != 0) {
						// Try to grow each non-zero square.
						if (Die.d100() <= chance) growInto(plateMap[x][y], x-1, y);
						if (Die.d100() <= chance) growInto(plateMap[x][y], x+1, y);
						if (Die.d100() <= chance) growInto(plateMap[x][y], x, y+1);
						if (Die.d100() <= chance) growInto(plateMap[x][y], x, y-1);
					} else {
						free++;
					}
				}
			}
			
			int		count = 0;
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					if (plateMap[x][y] > 0) count ++;
					if (free == 0 && plateMap[x][y] < 0) {
						plateMap[x][y] = 0;
					}
				}
			}
			count = (count*100)/(width * height);
			//System.out.println("Grown continents to "+count);
			if (count >= (100 - hydroToAimFor)) done = true;
		}
	}
	
	/**
	 * Create some random continents on this world.
	 */
	void makeContinents() {
		int		continents = (100 - planet.getHydrographics())/5;
		if (continents < 1) continents = 1;
		
		if (planet.hasFeature(PlanetFeature.Pangaea)) {
			System.out.println("Pangaea!");
			continents = 1;
		} else if (planet.hasFeature(PlanetFeature.EquatorialLand)) {
			// We want to try and encourage a band of unbroken land.
			continents *= 3;
		} else if (planet.hasFeature(PlanetFeature.PolarLand)) {
			continents = 4;
		}
		seedContinents(continents);
		growContinents(10);
	}
	
	private int getPlateValue(int x, int y) {
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		if (y < 0) y = 0;
		if (y >= height) y = height -1;
		
		return plateMap[x][y];
	}

	/**
	 * Called after generating a random height map. Work out general 
	 * terrain types etc.
	 */
	public void generate() {
		// Make some continental plates.
		makeContinents();

		land = Terrain.Grass;
		// Figure out the type of seas that we have.
		switch (Die.d8()) {
		case 1: case 2:
			water = Terrain.WaterLight;
			break;
		case 3: case 4:
			water = Terrain.WaterDark;
			break;
		default:
			water = Terrain.Water;
		}
		if (planet.hasFeature(PlanetFeature.BlackWater)) {
			water = Terrain.WaterBlack;
		} else if (planet.hasFeature(PlanetFeature.GreenWater)) {
			water = Terrain.WaterGreen;
		} else if (planet.hasFeature(PlanetFeature.PurpleWater)) {
			water = Terrain.WaterPurple;
		}
		
		// Now use these plates to modify our height map. Any area with a
		// continent will be raised, other areas will be lowered.
		int		maxHeight = 0;
		double  mountainMultiplier = 0.5;
		double	multiplier = 0.4;
		double	seaMultiplier = 0.3;
		if (planet.hasFeature(PlanetFeature.Pangaea)) {
			multiplier = 0.75;
		} else if (planet.hasFeature(PlanetFeature.ManyIslands)) {
			multiplier = 0.5;
			seaMultiplier = 0.4;
		}
		

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int		height = getHeight(x, y);
				if (getPlateValue(x, y) > 100) {
					setTerrain(x, y, Terrain.Rock);
					setHeight(x, y, 30 + (int)(height * mountainMultiplier));
				} else if (getPlateValue(x, y) > 0) {
					setTerrain(x, y, land);
					setHeight(x, y, 5 + (int)(height * multiplier));
				} else {
					setHeight(x, y, (int)(height*seaMultiplier));
					setTerrain(x, y, water);
				}
				if (getHeight(x, y) > maxHeight) maxHeight = getHeight(x, y);
			}
		}


		flood(planet.getHydrographics());

		int	waterCount = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getTerrain(x, y) == water) {
					waterCount++;
					if (planet.hasFeature(PlanetFeature.Algae)) {
						int		wetness = getWetness(x, y, true);
						if (wetness < 85) {
							setHeight(x, y, wetness);
							setTerrain(x, y, Terrain.Algae);
						}
					}
				}
			}
		}
		System.out.println("Hydro: "+(int)(100 * waterCount / (width * height))+"%");
		
		generateIceCap();
		generateEcology();
	}
	
	protected void generateIceCap() {
		int		landCount[] = new int[width];
		for (int x=0; x < width; x++) {
			landCount[x] = getLandCount(x);
		}
		for (int y = 0; y < height; y++) {
			int		latitude = getLatitude(y) - getTemperature()*10;
			for (int x = 0; x < width; x++) {
				if (getTerrain(x, y).isWater()) {
					if (latitude + landCount[x]/10 + getHeight(x, y) > 80) {
						setHeight(x, y, Die.d20(2)+65);
						setTerrain(x, y, Terrain.Ice);
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
		
		normaliseHeights();
		
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
					setTerrain(x, y, Terrain.Snow);
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
					setTerrain(x, y, Terrain.Gaian);
				}
			}
		}
		
		
		
	}

	
	public static void main(String[] args) throws Exception {
		Gaian g = new Gaian(513, 257);
		
		g.makeContinents();
		
	}
}
