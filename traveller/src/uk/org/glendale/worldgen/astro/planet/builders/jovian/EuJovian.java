package uk.org.glendale.worldgen.astro.planet.builders.jovian;

import java.util.ArrayList;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.JovianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;

/**
 * A Jupiter like world.
 * 
 * @author Samuel Penn
 */
public class EuJovian extends JovianWorld {
	public EuJovian() {
	}
	
	@Override
	public void generate() {
		
		planet.setType(PlanetType.EuJovian);
		
		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#aaaa77", false));
		tiles.add(new Tile("Light", "#cccc99", false));
		tiles.add(new Tile("White", "#ddddcc", false));
		tiles.add(new Tile("Orange", "#aa6666", false));
		
		//setFractalColour("#ffffff");
		
		generateMap();
		generateResources();
	}
	
	protected Tile getBand(int y) {
		if (y < TILE_HEIGHT/8 || y >= TILE_HEIGHT - (TILE_HEIGHT/8)) {
			return tiles.get((y/2)%2);
		} else if (y%8 == 0) {
			return tiles.get(3);
		}
		return tiles.get((y/2)%2+1);
	}
	
	public void generateResources() {
		addResource("Hydrogen", 60 + Die.d20(2));
		addResource("Helium 3", 10 + Die.d10(2));
		addResource("Oxygen", Die.d6(2));
		addResource("Water", Die.d6(2));
	}
}
