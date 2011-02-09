package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Barren worlds are rocky worlds with little or no atmosphere, no
 * surface water and no life. They may potentially be rich in mineral
 * resources, but have little else going for them.
 * 
 * @author Samuel Penn
 */
public abstract class BarrenWorld extends PlanetBuilder {
	protected Tile	base = new Tile("Sea", "#606060", false);
	protected Tile	crust = new Tile("Crust", "#909090", false);
	protected Tile	mountains = new Tile("Mountains", "#B0B0B0", false);
	
	private int		numCraters = 150;
	private int		craterSize = 25;
	private int		minCraterSize = 0;
	
	public BarrenWorld() {
	}
	
	/**
	 * Sets the number of craters to be drawn. Defaults to be 150
	 * if not set.
	 * 
	 * @param numCraters		Number of craters.
	 */
	protected void setCraterNumbers(int numCraters) {
		this.numCraters = numCraters;
	}
	
	/**
	 * Sets the average size of craters. Defaults to 25.
	 * 
	 * @param craterSize		Size of craters.
	 */
	protected void setCraterSize(int craterSize) {
		this.craterSize = craterSize;
	}
	
	/**
	 * Sets the minimum size of craters. Craters below this size
	 * are not drawn. Defaults to 0, and should remain so for
	 * inactive worlds without an atmosphere. Allows for small
	 * craters to have been eroded by geological/atmospheric
	 * processes.
	 * 
	 * @param minCraterSize		Minimum crater size.
	 */
	protected void setCraterMinSize(int minCraterSize) {
		this.minCraterSize = minCraterSize;
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.setPressure(AtmospherePressure.Trace);
		}
		generateMap();
		generateResources();
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			return;
		}
		addContinents(base, crust, mountains);
		
		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);
		addCliffs();
		addCraters();
		
		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		getImage();
	}
	
	private void addCliffs() {
		Tile	cliff = new Tile("Cliff", "#655545", false);
		for (int y=0; y < MAP_HEIGHT; y++) {
			for (int x=0; x < MAP_WIDTH; x++) {
				if (map[y][x] == OUT_OF_BOUNDS) {
					continue;
				}
				int		minHeight = 1000;
				if (getTile(map, y-1, x) != OUT_OF_BOUNDS) {
					minHeight = Math.min(minHeight, getTileHeight(heightMap, y-1, x));
				}
				if (getTile(map, y+1, x) != OUT_OF_BOUNDS) {
					minHeight = Math.min(minHeight, getTileHeight(heightMap, y+1, x));
				}
				if (getTile(map, y, x+1) != OUT_OF_BOUNDS) {
					minHeight = Math.min(minHeight, getTileHeight(heightMap, y, x+1));
				}
				if (getTile(map, y, x-1) != OUT_OF_BOUNDS) {
					minHeight = Math.min(minHeight, getTileHeight(heightMap, y, x-1));
				}
				if (minHeight+2 < getTileHeight(heightMap, y, x)) {
					map[y][x] = cliff;
				}
			}
		}
	}
	
	private void addCraters() {
		Tile	crater = new Tile("Crater", "#656060", false);
		for (int c=0; c < numCraters; c++) {
			// We don't want a crater right on the poles.
			int	y = Die.rollZero((int)(MAP_HEIGHT * 0.9)) + (int)(MAP_HEIGHT * 0.05);
			int x = Die.rollZero(getEast(y, MAP_HEIGHT) - getWest(y, MAP_HEIGHT)) + getWest(y, MAP_HEIGHT);
			
			int	r = Die.rollZero(craterSize);
			if (r < minCraterSize) {
				continue;
			}
			for (int yy=y-r; yy < y+r; yy++) {
				if (yy < 0 || yy >= MAP_HEIGHT) {
					continue;
				}
				for (int xx=x-r; xx < x+r; xx++) {
					if (xx < 0 || xx >= MAP_WIDTH) {
						continue;
					}
					if (Math.hypot(x-xx, y-yy) < (r + Die.die(r))/2) {
						if (map[yy][xx] != crater && map[yy][xx] != OUT_OF_BOUNDS) {
							heightMap[yy][xx] -= r;
							map[yy][xx] = crater;
						}
					}
				}
			}
		}
	}
}
