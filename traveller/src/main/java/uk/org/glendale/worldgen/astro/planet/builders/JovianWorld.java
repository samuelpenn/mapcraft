package uk.org.glendale.worldgen.astro.planet.builders;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.ice.Europan;
import uk.org.glendale.worldgen.server.AppManager;

public abstract class JovianWorld extends PlanetBuilder {
	protected List<Tile>	tiles;

	public JovianWorld() {
	}
	
	@Override
	public void generate() {
		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#999977", false));
		tiles.add(new Tile("Light", "#cccc99", false));
		
		
		generateMap();
		generateResources();
	}
	
	protected Tile getBand(int y) {
		return tiles.get((y/2) % tiles.size());
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			return;
		}
		map = new Tile[TILE_HEIGHT][TILE_WIDTH];
		heightMap = new int[TILE_HEIGHT][TILE_WIDTH];
		for (int y=0; y < TILE_HEIGHT; y++) {
			for (int x=0; x < TILE_WIDTH; x++) {
				if (x < getWest(y) || x >= getEast(y)) {
					map[y][x] = OUT_OF_BOUNDS;
					heightMap[y][x] = 0;
				} else {
					map[y][x] = getBand(y);
					heightMap[y][x] = Die.d4();
				}
			}
		}
		map = scaleMap(map, TILE_SIZE);
		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		
		getImage();
	}

	@Override
	public void generateResources() {
		addResource("Hydrogen", 60 + Die.d20(2));
	}
	
	private PlanetBuilder[]		moonBuilders = null;
	
	/**
	 * Gets a list of planet types typically found as moons of a Jovian world.
	 */
	public PlanetBuilder[] getMoonBuilders() {
		if (moonBuilders != null) {
			return moonBuilders;
		}
		int				numMoons = Die.d3(2);
		
		System.out.println("JovianWorlds: Adding "+numMoons+" moons");
		
		moonBuilders = new PlanetBuilder[numMoons];
		
		for (int i=0; i < numMoons; i++) {
			moonBuilders[i] = new Europan();
		}
		
		return moonBuilders;
	}

}
