package uk.org.glendale.rpg.traveller.worlds;

import java.io.File;
import java.util.Hashtable;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.star.Temperature;

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
	private int			seaPercentage = 0;

	private Hashtable<TileType, Terrain>	terrain = new Hashtable<TileType, Terrain>();
	
	public Tectonics(int width, int height) {
		super(width, height);
		createTileMap();
		
		terrain.put(TileType.SEABED, Terrain.create("seabed", 70, 70, 70, 0.5, 0.5, 0.5, false));
		terrain.put(TileType.DESERT, Terrain.create("desert", 100, 100, 100, 1, 1, 1, false));
		terrain.put(TileType.MOUNTAINS, Terrain.create("mountains", 100, 100, 100, 1, 1, 1, false));
		terrain.put(TileType.WATER, Terrain.create("water", 0, 0, 200, 0.1, 0.2, 0, true));
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
	
	/**
	 * Calculates how much of the world surface is actually covered by
	 * continental shelf.
	 * 
	 * @return	Percentage, 0 to 100.
	 */
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
		while (getShelfPercentage() < (100-seaPercentage)) {
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
	
	protected void fractalLandscape() {
		// The size of the current tile map.
		int		tw = width/tileSize;
		int		th = height/tileSize;

		// The size of the next tile map.
		int		nw = tw * 2;
		int		nh = th * 2;
		
		WorldBuilder.TileType[][]   oldMap = tileMap;
		WorldBuilder.TileType[][]	newMap = new TileType[nw][nh]; 
		
		boolean done = false;
		while (!done) {
			for (int y=0; y < nh; y++) {
				for (int x=0; x < nw; x++) {
					int		oldy = (y + Die.d3() - 2)/2;
					int		oldx = (x + Die.d3() - 2)/2;
					
					if (oldx < 0) oldx += tw;
					if (oldx >= tw) oldx -= tw;
					if (oldy < 0) oldy = 0;
					if (oldy > th-1) oldy = th-1;
					newMap[x][y] = oldMap[oldx][oldy];
				}
			}
			oldMap = newMap;
			if (nw < width) {
				tw = nw; th = nh;
				nw *= 2; nh *= 2;
				newMap = new TileType[nw][nh];
			} else {
				done = true;
			}
		}
		
		// Get rid of single pixels
		for (int y=1; y < nh-1; y++) {
			for (int x=1; x < nw-1; x++) {
				if (newMap[x-1][y] == newMap[x+1][y] && newMap[x][y+1] == newMap[x][y-1] && newMap[x-1][y] == newMap[x][y-1]) {
					newMap[x][y] = newMap[x-1][y];
				}
			}
		}
		
		// Generate the graphical landscape
		int		div = width / nw;
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				setTerrain(x, y, terrain.get(oldMap[x/div][y/div]));
			}
		}
		
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
								setTerrain(x, y, terrain.get(TileType.DESERT));
							} });
					break;
				case SEABED:
					setTile(x, y, new ITileSetter() { 
							public void set(int averageHeight, int x, int y) {
								setTerrain(x, y, terrain.get(TileType.SEABED));
							} });
					break;
				case ARCTIC:
					setTile(x, y, new ITileSetter() { 
							public void set(int averageHeight, int x, int y) {
								setTerrain(x, y, terrain.get(TileType.ARCTIC));
							} });
					break;
				case MOUNTAINS:
					setTile(x, y, new ITileSetter() { 
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, terrain.get(TileType.MOUNTAINS));
							} else {
								//setHeight(x, y, 1.1);
								setTerrain(x, y, terrain.get(TileType.DESERT));
							}
						} });
					break;
				case TEMPERATE:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, terrain.get(TileType.TEMPERATE));
							} else {
								setTerrain(x, y, terrain.get(TileType.TEMPERATE));
							}
						} });						
					break;
				case SCRUB:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, terrain.get(TileType.DESERT));
							} else {
								setTerrain(x, y, terrain.get(TileType.SCRUB));
							}
						} });						
					break;
				case JUNGLE:
					setTile(x, y, new ITileSetter() {
						public void set(int averageHeight, int x, int y) {
							if (getHeight(x, y) > averageHeight) {
								setTerrain(x, y, terrain.get(TileType.JUNGLE));
							} else {
								setTerrain(x, y, terrain.get(TileType.TEMPERATE));
							}
						} });						
					break;					
				case WATER:
					setTile(x, y, new ITileSetter() { 
						public void set(int averageHeight, int x, int y) { 
							if (getHeight(x, y) < planet.getHydrographics()) {
								setTerrain(x, y, terrain.get(TileType.WATER));
								setHeight(x, y, 0.5);
							} else {
								setTerrain(x, y, terrain.get(TileType.WATER));
							}
						} });
					break;
				}
			}
		}
		
		/*
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
		*/		
	}

	public void generate() {
		if (seaPercentage == 0 && planet.getHydrographics() > 0) {
			seaPercentage = planet.getHydrographics();
		} else if (seaPercentage ==  0) {
			seaPercentage = Die.d20(3)+20;
		}
		// Work out large scale tiles.
		generateContinents();
		copyShelfToTiles();
		setTiles();

		//marineClimate();
		//makeItRain();

		fractalLandscape();
		
	}
	
	public static void main(String[] args) throws Exception {
		Tectonics	t = new Tectonics(512, 256);
		t.setPlanet(new Planet("Earth", PlanetType.Gaian, 6400));
		t.planet.setHydrographics(30);
		t.planet.setTilt(22);
		t.planet.setTemperature(Temperature.Cold);
		t.planet.setLifeLevel(LifeType.None);
		t.planet.setAtmosphereType(AtmosphereType.Standard);
		t.planet.setAtmospherePressure(AtmospherePressure.Standard);

		t.generate();
		t.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
	}
}
