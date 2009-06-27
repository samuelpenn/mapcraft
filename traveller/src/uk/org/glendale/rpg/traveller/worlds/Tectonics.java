package uk.org.glendale.rpg.traveller.worlds;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.utils.Die;

/**
 * Class which works out the general nature of a world using only the
 * tile map. Creates continents, seas and the like, and works out the
 * temperature and climate of the world.
 * 
 * @author Samuel Penn
 */
class Tectonics extends WorldBuilder {
	private int[][]		shelfMap = null;
	private int			tileWidth = getTileWidth();
	private int			tileHeight = getTileHeight();
	
	public Tectonics(int width, int height) {
		super(width, height);
		createTileMap();
	}
	
	protected void generateContinents() {
		int			number = Die.d6()+2;

		shelfMap = new int[tileWidth][tileHeight];
		
		standardContinents(number);
	}
	
	protected void copyShelfToTiles() {
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				if (shelfMap[x][y] == 0) {
					tileMap[x][y] = TileType.SEABED;
				} else if (shelfMap[x][y] < 0) {
					tileMap[x][y] = TileType.MOUNTAINS;
				} else {
					tileMap[x][y] = TileType.DESERT;
				}
			}
		}		
	}
	
	private int getShelfPercentage() {
		int		size = 0;
		
		for (int y=0; y < tileHeight; y++) {
			int		weight = 1;
			for (int x=0; x < tileWidth; x++) {
				if (shelfMap[x][y]>0) size += weight;
			}
		}

		return (size*100) / (tileWidth * tileHeight);
	}
	
	/**
	 * Get the identity of the continental shelf at the given location.
	 * Always return the positive value of the shelf.
	 *  
	 * @param x		X coordinate of shelf (0=West)
	 * @param y		Y coordinate of shelf (0=North)
	 * @return		Positive id of the shelf.
	 */
	private int getShelfId(int x, int y) {
		if (y < 0) y = 0;
		if (y >= tileHeight) y = tileHeight;
		
		if (x < 0) x+= tileWidth;
		if (x >= tileWidth) x-= tileWidth;
		
		return Math.abs(shelfMap[x][y]);
	}
	
	private void standardContinents(int number) {
		// Seed some continents.
		for (int c=0; c < number; c++) {
			shelfMap[Die.rollZero(tileWidth)][Die.rollZero(tileHeight/2)+tileHeight/4] = c;
		}
		
		// Randomly grow the continental shelves.
		while (getShelfPercentage() < (100-planet.getHydrographics())) {
			for (int y=0; y < tileHeight; y++) {
				for (int x=0; x < tileWidth; x++) {
					if (getShelfId(x,y) > 0 && Die.d10() >= getShelfId(x,y)) {
						int		xx = x + Die.d2() - Die.d2();
						int		yy = y + Die.d2() - Die.d2();
						
						if (yy < 0 || yy >= tileHeight) continue;
						if (xx < 0) xx+= tileWidth;
						if (xx >= tileWidth) xx-= tileWidth;
						
						if (shelfMap[xx][yy] == 0) {
							shelfMap[xx][yy] = shelfMap[x][y];
						}
					}
				}
			}		
		}

		// Allow for a small amount of continental drift.
		for (int c=1; c < number; c++) {
			int		movement = Die.d3()-1;
			int		direction = Die.d4(); // N, S, E, W

			while (movement-- > 0) {
				switch (direction) {
				case 1: // Shelf moves north
					for (int y=0; y < tileHeight-1; y++) {
						for (int x=0; x < tileWidth; x++) {
							if (getShelfId(x, y+1) == c) {
								if (getShelfId(x, y) > 0) {
									shelfMap[x][y] = -c;
									shelfMap[x][y+1] = 0;
								} else {
									shelfMap[x][y] = c;
									shelfMap[x][y+1] = 0;
								}
							}
						}
					}
					break;
				case 2: // Shelf moves south
					for (int y=tileHeight-1; y > 0; y--) {
						for (int x=0; x < tileWidth; x++) {
							if (getShelfId(x, y-1) == c) {
								if (getShelfId(x,y) > 0) {
									shelfMap[x][y] = -c;
									shelfMap[x][y-1] = 0;
								} else {
									shelfMap[x][y] = c;
									shelfMap[x][y-1] = 0;
								}
							}
						}
					}
					break;
				case 3: // Shelf moves east
					for (int x=tileWidth-1; x > 0; x--) {
						for (int y=0; y < tileHeight-1; y++) {
							if (getShelfId(x-1, y) == c) {
								if (getShelfId(x, y) > 0) {
									shelfMap[x][y] = -c;
									shelfMap[x-1][y] = 0;
								} else {
									shelfMap[x][y] = c;
									shelfMap[x-1][y] = 0;
								}
							}
						}
					}
					break;
				case 4: // Shelf moves west
					for (int x=0; x < tileWidth-1; x++) {
						for (int y=0; y < tileHeight-1; y++) {
							if (getShelfId(x+1, y) == c) {
								if (getShelfId(x, y) > 0) {
									shelfMap[x][y] = -c;
									shelfMap[x+1][y] = 0;
								} else {
									shelfMap[x][y] = c;
									shelfMap[x+1][y] = 0;
								}
							}
						}
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Add water to the tile map. Converts the dry seabed to oceans.
	 */
	protected void addWater() {
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				if (tileMap[x][y] == TileType.SEABED) {
					tileMap[x][y] = TileType.WATER;
				}
			}
		}
	}
	
	/**
	 * Shift temperatures around if there is water. Neighbouring water
	 * tiles will mix, averaging out the water temperature. Coast tiles
	 * will then be modified by sea temperature.
	 */
	protected void marineClimate() {
		int		count = 3;
		while (count-- > 0) {
			int[][]		tmpMap = new int[tileWidth][tileHeight];
			for (int y=0; y < tileHeight; y++) {
				for (int x=0; x < tileWidth; x++) {
					if (tileMap[x][y] == TileType.WATER) {
						int		t = getTemperature(x, y);
						t += getTemperature(x-1, y) + getTemperature(x+1, y);
						t += getTemperature(x, y-1) + getTemperature(x, y+1);
						tmpMap[x][y] = t/5;
					} else {
						tmpMap[x][y] = getTemperature(x, y);
					}
				}
			}
			for (int y=0; y < tileHeight; y++) {
				for (int x=0; x < tileWidth; x++) {
					temperatureMap[x][y] = tmpMap[x][y];
				}
			}
		}
	}
	
	protected void makeItRain() {
		// Set up initial parameters. Only water tiles have rain, the amount
		// is based on temperature.
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				if (tileMap[x][y] == TileType.WATER) {
					rainMap[x][y] = 50 + getTemperature(x, y) * 5;
					
					rainMap[x][y] = Math.max(rainMap[x][y], 10);
					rainMap[x][y] = Math.min(rainMap[x][y], 100);
					
					if (planet.hasFeature(PlanetFeature.Dry)) {
						rainMap[x][y] *= 0.5;
					} else if (planet.hasFeature(PlanetFeature.Wet)) {
						rainMap[x][y] *= 1.5;
					}
				} else {
					if (planet.hasFeature(PlanetFeature.Dry)) {
						rainMap[x][y] = 0;
					} else if (planet.hasFeature(PlanetFeature.Wet)) {
						rainMap[x][y] = 30;
					} else {
						rainMap[x][y] = 15;
					}
				}
			}
		}
		
		// Now add weather patterns. This is the complex bit.
		
		int		tropics = 3;
		int		equator = tileHeight / 2;
		int		count = 6;
		while (count-- > 0) {
			for (int y=0; y < tileHeight; y++) {
				System.out.print(y+": ");
				for (int x=0; x < tileWidth; x++) {
					int		r = rainMap[x][y];
					int		t = temperatureMap[x][y];
					
					if (tileMap[x][y] == TileType.MOUNTAINS) {
						// Rain doesn't move off mountains.
					} else if (y >= equator-(tropics/2) && y < equator+(tropics/2)) {
						if (getRainFall(x-1, y) < r) {
							r = (r+getRainFall(x-1, y))/2;
							setRainFall(x-1, y, r);
							setRainFall(x, y, r);
						}
						if (getRainFall(x+1, y) < r) {
							r = (r+getRainFall(x+1, y))/2;
							setRainFall(x+1, y, r);						
							setRainFall(x, y, r);
						}
						if (tileMap[x][y] != TileType.WATER) {
							if (y < equator) {
								System.out.print("N");
								int		m = getRainFall(x, y-1)/10;
								setRainFall(x, y-1, getRainFall(x, y-1)-m);
								setRainFall(x, y, getRainFall(x, y)+m);
							} else {
								System.out.print("S");
								int		m = getRainFall(x, y+1)/10;
								setRainFall(x, y+1, getRainFall(x, y+1)-m);
								setRainFall(x, y, getRainFall(x, y)+m);
							}
						}
					} else if (y >= equator-tropics && y < equator+tropics) {
						if (getRainFall(x, y+1) < r) {
							r = (r+getRainFall(x, y+1))/2;
							setRainFall(x, y+1, r);
							setRainFall(x, y, r);
						}
						if (getRainFall(x, y-1) < r) {
							r = (r+getRainFall(x, y-1))/2;
							setRainFall(x, y-1, r);						
							setRainFall(x, y, r);
						}
						//setRainFall(x, y, (int)(getRainFall(x, y) * 0.9));
					} else {
						int		xx=-1, yy=-1, min = r;
						if (getRainFall(x-1, y) < min) {
							min = getRainFall(x-1, y);
							xx = x-1;
							yy = y;
						}
						if (getRainFall(x+1, y) < min) {
							min = getRainFall(x+1, y);
							xx = x+1;
							yy = y;
						}
						if (getRainFall(x, y-1) < min) {
							min = getRainFall(x, y-1);
							xx = x;
							yy = y-1;
						}
						if (getRainFall(x, y+1) < min) {
							min = getRainFall(x, y+1);
							xx = x;
							yy = y+1;
						}
						if (min < r) {
							if (tileMap[x][y] == TileType.WATER) {
								r = ((getRainFall(xx, yy) + r))/2;
							} else {
								r = ((getRainFall(xx, yy) + r)*6)/10;
							}
							setRainFall(x, y, r);
							setRainFall(xx, yy, r);
							//System.out.println(r);
						}
					}
					if (tileMap[x][y] == TileType.WATER) {
						if (getTemperature( x, y) >= 0) {
							setRainFall(x, y, getRainFall(x, y) + getTemperature(x, y));
						}
					}
				}
				System.out.println("");
			}
		}
	}
	
	protected void ecology() {
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				int		t = temperatureMap[x][y];
				int		r = rainMap[x][y];
				
				if (tileMap[x][y] == TileType.WATER) {
					if (t < -3) {
						tileMap[x][y] = TileType.ARCTIC;
					}
				} else if (tileMap[x][y] == TileType.MOUNTAINS) {
					// Nothing.
				} else {
					if (t > 5) {
						// Hot
						if (r > 50) {
							tileMap[x][y] = TileType.JUNGLE;
						} else if (r > 30) {
							tileMap[x][y] = TileType.SCRUB;
						} else {
							tileMap[x][y] = TileType.DESERT;
						}
					} else if (t > 2) {
						// Warm
						if (r > 40) {
							tileMap[x][y] = TileType.JUNGLE;
						} else if (r > 20) {
							tileMap[x][y] = TileType.TEMPERATE;
						} else if (r > 10) {
							tileMap[x][y] = TileType.SCRUB;
						} else {
							tileMap[x][y] = TileType.DESERT;
						}
					} else if (t > -3) {
						// Temperate
						if (r > 20) {
							tileMap[x][y] = TileType.TEMPERATE;
						} else if (r > 5) {
							tileMap[x][y] = TileType.SCRUB;
						} else {
							tileMap[x][y] = TileType.DESERT;
						}
					} else {
						// Arctic
						tileMap[x][y] = TileType.ARCTIC;
					}
				}
			}
		}
		debugTiles();
	}
	
	private void debugShelfMap() {
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				if (shelfMap[x][y]==0) {
					System.out.print("..");
				} else {
					System.out.print(getShelfId(x,y)+""+getShelfId(x,y));
				}
			}
			System.out.println("");
		}		
		System.out.println("");
	}

	private void debugTemperatureMap() {
		for (int y=0; y < tileHeight; y++) {
			for (int x=0; x < tileWidth; x++) {
				System.out.printf("%+d", temperatureMap[x][y]);
			}
			System.out.println("");
		}		
		System.out.println("");
	}
	
	public static void main(String[] args) throws Exception {
		Tectonics	t = new Tectonics(512, 256);
		t.setPlanet(new Planet("Earth", PlanetType.Gaian,6400));
		t.planet.setHydrographics(70);
		t.planet.setTilt(22);
		t.planet.setTemperature(Temperature.Standard);
		t.planet.setLifeLevel(LifeType.None);
		t.planet.setAtmosphereType(AtmosphereType.Standard);
		t.planet.setAtmospherePressure(AtmospherePressure.Standard);

		t.generateContinents();
		//t.debugShelfMap();
		t.copyShelfToTiles();

		t.setTiles();
		t.debugTiles();
		t.addWater();
		t.marineClimate();
		t.debugTemperatureMap();
	}
}
