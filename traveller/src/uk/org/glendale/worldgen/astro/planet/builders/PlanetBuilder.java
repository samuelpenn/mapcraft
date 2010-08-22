package uk.org.glendale.worldgen.astro.planet.builders;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;

public abstract class PlanetBuilder {
	protected Planet		planet;
	
	public PlanetBuilder() {
		generateFractalHeightMap();
	}
	
	public void setPlanet(Planet planet) {
		this.planet = planet;
		
		planet.setType(PlanetType.Undefined);
		planet.setRadius(3200);
		planet.setAtmosphere(AtmosphereType.Vacuum);
		planet.setPressure(AtmospherePressure.None);
		
		planet.setLifeType(LifeType.None);
	}
	
	/**
	 * Generate standard planetary statistics.
	 */
	public abstract void generate();
	
	/**
	 * Generate surface map for this planet.
	 */
	public abstract void generateMap();
	
	/**
	 * Generate resources for this planet.
	 */
	public abstract void generateResources();
	
	
	public static final int MAP_WIDTH = 1024;
	public static final int MAP_HEIGHT = MAP_WIDTH / 2;
	
	public static final int TILE_SIZE = 16;
	
	public static final int TILE_WIDTH = MAP_WIDTH / TILE_SIZE;
	public static final int	TILE_HEIGHT = MAP_HEIGHT / TILE_SIZE;
	
	protected Tile[][]	map = null;
	protected Tile[][]	tileMap = null;
	protected int[][]	heightMap = null;
	
	public static final Tile  OUT_OF_BOUNDS = new Tile("OOB", "#000000", false);
	
	protected void generateSurface(Tile base, Tile shelf, Tile mountains) {
		int		height = MAP_HEIGHT/TILE_SIZE;
		int		width = MAP_WIDTH/TILE_SIZE;
		tileMap = new Tile[height][width];
		heightMap = new int[height][width];
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				tileMap[y][x] = base;
				heightMap[y][x] = Die.d4()-Die.d4();
			}
		}
		
		addContinents(base, shelf, mountains);
	}
	
	protected int[][]	fractalMap;
	/**
	 * Use a fractal algorithm to generate a random height map.
	 * The height and width must be a power of 2 + 1.
	 */
	protected void generateFractalHeightMap() {
		int			width = MAP_WIDTH+1;
		int			height = MAP_HEIGHT+1;
		
		int[][]		h = new int[width][height];
		
		// Initiate everything to be '-1', which is used as a null value.
		// This is actually an illegal height, so we musn't copy it into
		// the real map at the end.
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				h[x][y] = -1;
			}
		}
		
		// Now start at the four corners. Actually, there are only two
		// distinct corners since the map wraps around East-West.
		h[0][0] = h[width-1][0] = Die.d20(2) + 30;
		h[0][height-1] = h[width-1][height-1] = Die.d20(2) + 30;
		
		// HACK:
		//h[0][0] = h[width-1][0] = 50;
		//h[0][height-1] = h[width-1][height-1] = 50;
		
		//System.out.println(h[0][0]);
		
		// Now run the fractal algorithm.
		int		step = width-1;
		while (step > 1) {
			for (int x = 0; x < width-1; x += step) {
				int		ystep = (step / 2); // Calculated here to make code cleaner later.
				
				for (int y = 0; y < height-1; y += ystep) {
					// Skip over tiles which have already been set.
					//if (h[x][y] != -1) continue;
					
					//System.out.println("("+x+","+y+") Step "+step);
					
					// SQUARE STEP
					// Generate a value for the middle of the square.
					int		px = x + (step/2);
					int		py = y + (step/4);
					
					//if (px >= width || px < 0) continue;
					//if (py >= height || py < 0) continue;
					
					int		total = 0;
					// Sum the four corners of the square.
					total = h[x][y];
					total += h[x+step][y];
					total += h[x][y+ystep]; 
					total += h[x+step][y+ystep];

					// Find the average.
					total /= 4;
					//System.out.println("Square: ("+px+","+py+"): "+total);

					// Add a random amount.
					int		random = 10;
					if (random < 0) random = 2;
					total += Die.die(random) - Die.die(random);
					if (total < 0) total = 0;
					if (total > 99) total = 99;
					if (h[px][py] == -1) {
						h[px][py] = total;
						//System.out.println(total);
					}
					
					// DIAMOND STEP
					// Generate values for the 4 vertices of the diamond.
					
					// Top vertex
					if (h[px][y] == -1) {
						total = h[px][py] + h[x][y] + h[x+step][y];
						h[px][y] = total/3 + Die.die(random) - Die.die(random);
					}
					// Left vertex
					if (h[x][py] == -1) {
						total = h[x][y] + h[px][py] + h[x][y+ystep];
						h[x][py] = total/3 + Die.die(random) - Die.die(random);
						if (x == 0) {
							h[width-1][py] = h[x][py];
						}
					}
					// Right vertex
					if (h[x+step][y] == -1) {
						total = h[x+step][y] + h[px][py] + h[x+step][y+ystep];
						h[x+step][py] = total/3 + Die.die(random) - Die.die(random);
					}
					// Bottom vertex
					if (h[px][y+ystep] == -1) {
						total = h[x][y+ystep] + h[px][py] + h[x+step][y+ystep];
						h[px][y+ystep] = total/3 + Die.die(random) - Die.die(random);
					}
				}  // y
			} // x
			step /= 2;
		}
		
		// Now set the real map.
		fractalMap = new int[MAP_HEIGHT][MAP_WIDTH];
		for (int x=0; x < MAP_WIDTH; x++) {
			for (int y=0; y < MAP_HEIGHT; y++) {
				fractalMap[y][x] = h[x][y];
			}
		}		
		
	}
	
	
	protected int getShelfPercentage(int[][] map) {
		int count = 0;
		int	size = 0;
		
		for (int y=0; y < map.length; y++) {
			for (int x=0; x < map[0].length; x++) {
				if (map[y][y] >= 0) {
					size++;
					if (map[y][x] > 0) {
						count ++;
					}
				}
			}
		}
		
		return (count * 100) / size;
	}
	
	/**
	 * Gets the western boundary of the tile map at the given row, assuming
	 * a sinusoidal projection of the world's surface.
	 * 
	 * @param y		Tile y coordinate.
	 * @return		Western most (left) edge of the map.
	 */
	protected int getWest(int y) {
		int		latitude = (int)(90 * Math.abs(TILE_HEIGHT/2 - (y + 0.5)) / (TILE_HEIGHT/2));
		int		w = (int)Math.ceil(Math.PI * TILE_WIDTH * Math.cos(Math.toRadians(latitude)) / (2 * Math.PI));
		
		return TILE_WIDTH/2 - w;
	}
	
	protected int getWest(int y, int height) {
		int		latitude = (int)(90 * Math.abs(height/2 - (y + 0.5)) / (height/2));
		int		w = (int)Math.ceil(Math.PI * height * 2 * Math.cos(Math.toRadians(latitude)) / (2 * Math.PI));
		
		return height - w;		
	}

	/**
	 * Gets the eastern boundary of the tile map at the given row, assuming
	 * a sinusoidal projection of the world's surface.
	 * 
	 * @param y		Tile y coordinate.
	 * @return		Eastern most (right) edge of the map.
	 */
	protected int getEast(int y) {
		int		latitude = (int)(90 * Math.abs(TILE_HEIGHT/2 - (y + 0.5)) / (TILE_HEIGHT/2));
		int		w = (int)Math.ceil(Math.PI * TILE_WIDTH * Math.cos(Math.toRadians(latitude)) / (2 * Math.PI));
		
		return TILE_WIDTH/2 + w;
	}

	protected int getEast(int y, int height) {
		int		latitude = (int)(90 * Math.abs(height/2 - (y + 0.5)) / (height/2));
		int		w = (int)Math.ceil(Math.PI * height * 2 * Math.cos(Math.toRadians(latitude)) / (2 * Math.PI));
		
		return height + w;
	}
	
	protected Tile getTile(Tile[][] map, int y, int x) {
		if (y < 0) y = 0;
		if (y > map.length-1) y = map.length-1;
		
		if (x < 0) x += map[0].length;
		if (x >= map[0].length) x -= map[0].length;
		//if (x < getWest(y)) x = getEast(y)-1;
		//if (x >= getEast(y)) x = getWest(y);
		
		return map[y][x];
	}

	protected int getTileHeight(int[][] map, int y, int x) {
		if (y < 0) y = 0;
		if (y > map.length-1) y = map.length-1;
		
		if (x < 0) x += map[0].length;
		if (x >= map[0].length) x -= map[0].length;
		//if (x < getWest(y)) x = getEast(y)-1;
		//if (x >= getEast(y)) x = getWest(y);
		
		return map[y][x];
	}
	
	/**
	 * Scale the tile map, randomly filling in extra detail. The scaling factor
	 * must be a power of two. New tiles randomly take the feature from one of
	 * the neighbouring tiles. Calls itself recursively, doubling height and
	 * width each time.
	 * 
	 * @param original		Original map to be scaled.
	 * @param scale			Scaling factor to use.
	 * @return				New, scaled map.
	 */
	protected Tile[][] scaleMap(Tile[][] original, int scale) {
		if (scale < 2) {
			return original;
		}
		int		height = original.length;
		int		width = original[0].length;
		Tile[][]	scaled = new Tile[height*2][width*2];
		int[][]		scaledHeight = new int[height*2][width*2];
		
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				if (original[y][x] == OUT_OF_BOUNDS) {
					scaled[y*2][x*2] = OUT_OF_BOUNDS;
					scaled[y*2][x*2+1] = OUT_OF_BOUNDS;
					scaled[y*2+1][x*2] = OUT_OF_BOUNDS;
					scaled[y*2+1][x*2+1] = OUT_OF_BOUNDS;
					continue;
				}
				// Top left
				switch (Die.d4()) {
				case 1:	scaled[y*2][x*2] = getTile(original, y-1, x); break;
				case 2: scaled[y*2][x*2] = getTile(original, y, x-1); break;
				default: scaled[y*2][x*2] = getTile(original, y, x); break;
				}
				scaledHeight[y*2][x*2] = (getTileHeight(heightMap, y, x) + getTileHeight(heightMap, y-1, x) + getTileHeight(heightMap, y, x-1))/3;
				if (scaled[y*2][x*2] == OUT_OF_BOUNDS && original[y][x] != OUT_OF_BOUNDS) {
					scaled[y*2][x*2] = original[y][x];
				}
				// Top right
				switch (Die.d4()) {
				case 1:	scaled[y*2][x*2+1] = getTile(original, y-1, x); break;
				case 2: scaled[y*2][x*2+1] = getTile(original, y, x+1); break;
				default: scaled[y*2][x*2+1] = getTile(original, y, x); break;
				}
				scaledHeight[y*2][x*2+1] = (getTileHeight(heightMap, y, x) + getTileHeight(heightMap, y-1, x) + getTileHeight(heightMap, y, x+1))/3;
				if (scaled[y*2][x*2+1] == OUT_OF_BOUNDS && original[y][x] != OUT_OF_BOUNDS) {
					scaled[y*2][x*2+1] = original[y][x];
				}
				// Bottom left
				switch (Die.d4()) {
				case 1:	scaled[y*2+1][x*2] = getTile(original, y+1, x); break;
				case 2: scaled[y*2+1][x*2] = getTile(original, y, x-1); break;
				default: scaled[y*2+1][x*2] = getTile(original, y, x); break;
				}
				scaledHeight[y*2+1][x*2] = (getTileHeight(heightMap, y, x) + getTileHeight(heightMap, y+1, x) + getTileHeight(heightMap, y, x-1))/3;
				if (scaled[y*2+1][x*2] == OUT_OF_BOUNDS && original[y][x] != OUT_OF_BOUNDS) {
					scaled[y*2+1][x*2] = original[y][x];
				}
				// Bottom right
				switch (Die.d4()) {
				case 1:	scaled[y*2+1][x*2+1] = getTile(original, y+1, x); break;
				case 2: scaled[y*2+1][x*2+1] = getTile(original, y, x+1); break;
				default: scaled[y*2+1][x*2+1] = getTile(original, y, x); break;
				}
				scaledHeight[y*2+1][x*2+1] = (getTileHeight(heightMap, y, x) + getTileHeight(heightMap, y+1, x) + getTileHeight(heightMap, y, x+1))/3;
				if (scaled[y*2+1][x*2+1] == OUT_OF_BOUNDS && original[y][x] != OUT_OF_BOUNDS) {
					scaled[y*2+1][x*2+1] = original[y][x];
				}
			}
		}
		heightMap = scaledHeight;
		
		return scaleMap(scaled, scale/2);
	}

	/**
	 * Stretch the map to give a cylindrical projection. After this operation,
	 * the map will completely fill the rectangle and have no 'out-of-bounds'
	 * areas.
	 * 
	 * @param map	Map to be stretched.
	 * @return		Stretched version of map.
	 */
	protected Tile[][] stretchMap(Tile[][] map) {
		int			height = map.length;
		int			width = map[0].length;
		Tile[][]	stretched = new Tile[height][width];
		int[][]		heightStretched = new int[height][width];
		
		for (int y=0; y < height; y++) {
			int		latitude = (y * TILE_HEIGHT) / height;
			
			int		west = (int)(getWest(latitude) * width / TILE_WIDTH);
			int		east = (int)(getEast(latitude) * width / TILE_WIDTH);

			for (int x=0; x < width; x++) {
				int		xx = (int)west + (x * (east - west)) / width;
				stretched[y][x] = map[y][xx];
				heightStretched[y][x] = heightMap[y][xx];
			}
		}
		heightMap = heightStretched;
		
		return stretched;
	}
	
	protected void addContinents(Tile base, Tile shelf, Tile mountains) {
		int		num = 9;
		int		percentage = 20+Die.d20(3);
		
		// The shelf map is used to grow continents.
		// A value of 0 is seabed, -1 is out of bounds.
		// A +ve value is the continent number.
		int[][]	shelfMap = new int[TILE_HEIGHT][TILE_WIDTH];
		heightMap = new int[TILE_HEIGHT][TILE_WIDTH];

		for (int y=0; y < TILE_HEIGHT; y++) {
			for (int x=0; x < TILE_WIDTH; x++) {
				shelfMap[y][x] = 0;
				heightMap[y][x] = -Die.d4();
				
				if (x < getWest(y) || x >= getEast(y)) {
					shelfMap[y][x] = -1;
					heightMap[y][x] = 0;
				}
			}
		}

		while (num > 0) {
			int x = Die.rollZero(TILE_WIDTH);
			int y = Die.rollZero(TILE_HEIGHT);
			if (shelfMap[y][x] == 0) {
				shelfMap[y][x] = num--;
				heightMap[y][x] = Die.d3();
			}
		}
		
		// Randomly grow the continental shelves.
		while (getShelfPercentage(shelfMap) < (100-percentage)) {
			for (int y=0; y < TILE_HEIGHT; y++) {
				for (int x=0; x < TILE_WIDTH; x++) {
					if (shelfMap[y][x] < 0) {
						continue;
					}
					if (shelfMap[y][x] > 0 && Die.d10() >= 5) {
						int		xx = x + Die.d2() - Die.d2();
						int		yy = y + Die.d2() - Die.d2();
						
						if (yy < 0 || yy >= TILE_HEIGHT) continue;
						if (xx < getWest(yy)) xx = getEast(yy)-1;
						if (xx >= getEast(yy)) xx = getWest(yy);
						
						if (shelfMap[yy][xx] == 0) {
							shelfMap[yy][xx] = shelfMap[y][x];
						} else if (shelfMap[yy][xx] > 0 && shelfMap[yy][xx] != shelfMap[y][x]) {
							heightMap[yy][xx] += Die.d3();
							heightMap[y][x] += 1;
						}
					}
				}
			}		
		}

		map = new Tile[TILE_HEIGHT][TILE_WIDTH];
		for (int y=0; y < TILE_HEIGHT; y++) {
			for (int x=0; x < TILE_WIDTH; x++) {
				if (shelfMap[y][x] < 0) {
					map[y][x] = OUT_OF_BOUNDS;
				} else if (shelfMap[y][x] == 0) {
					map[y][x] = base;
				} else {
					map[y][x] = shelf;
				}
			}
		}
	}
	
	public SimpleImage getImage() {
		//map = scaleMap(map, 4);
		//map = stretchMap(map);
		MapDrawer drawer = new MapDrawer(map, 1);
		//drawer.setFractalMap(fractalMap, "#997777");
		drawer.setHeightMap(heightMap);
		SimpleImage image = drawer.getWorldMap();
		try {
			image.save(new File("/home/sam/b.jpg"));

			drawer.getWorldGlobe(2).save(new File("/home/sam/globe.jpg"));
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println(GraphicsEnvironment.isHeadless());
		PlanetBuilder	barren = new BarrenWorld();
		barren.setPlanet(new Planet());
		barren.generateMap();
		System.exit(0);
		
		/*
		Tile	sea = new Tile("Sea", "#505050", false);
		Tile	crust = new Tile("Crust", "#909090", false);
		Tile	mountains = new Tile("Mountains", "#B0B0B0", false);
		barren.addContinents(sea, crust, mountains);
		barren.getImage();
		*/
	}
	
}
