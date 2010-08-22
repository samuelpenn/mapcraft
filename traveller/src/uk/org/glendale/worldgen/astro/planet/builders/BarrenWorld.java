package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;

/**
 * Barren worlds are rocky worlds with little or no atmosphere, no
 * surface water and no life. They may potentially be rich in mineral
 * resources, but have little else going for them.
 * 
 * @author Samuel Penn
 */
public class BarrenWorld extends PlanetBuilder {
	protected Tile	base = new Tile("Sea", "#606060", false);
	protected Tile	crust = new Tile("Crust", "#909090", false);
	protected Tile	mountains = new Tile("Mountains", "#B0B0B0", false);
	
	private int		numCraters = 150;
	private int		craterSize = 25;
	private int		minCraterSize = 0;
	
	public BarrenWorld() {
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.setPressure(AtmospherePressure.Trace);
		}
	}

	@Override
	public void generateMap() {
		addContinents(base, crust, mountains);
		
		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);
		addCraters();
		
		map = stretchMap(map);
		getImage();
		// TODO Auto-generated method stub

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

	@Override
	public void generateResources() {
		// TODO Auto-generated method stub

	}

}
