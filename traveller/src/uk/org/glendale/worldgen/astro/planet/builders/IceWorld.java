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
public abstract class IceWorld extends PlanetBuilder {
	protected Tile	base = new Tile("Sea", "#D0D0D7", false);
	protected Tile	crust = new Tile("Crust", "#E9E9E9", false);
	protected Tile	mountains = new Tile("Mountains", "#FAFAFA", false);
	
	private int		numCraters = 150;
	private int		craterSize = 25;
	private int		minCraterSize = 0;
	
	public IceWorld() {
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
		setHydrographics(5 + Die.d20(3));
		addContinents(base, crust, mountains);
		
		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);
		addCraters();
		
		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		getImage();
	}
		
	private void addCraters() {
		Tile	crater = new Tile("Crater", "#D0D0D0", false);
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
