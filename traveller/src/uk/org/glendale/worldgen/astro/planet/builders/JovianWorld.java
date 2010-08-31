package uk.org.glendale.worldgen.astro.planet.builders;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.server.AppManager;

public class JovianWorld extends PlanetBuilder {
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

}
