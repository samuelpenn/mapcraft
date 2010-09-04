package uk.org.glendale.worldgen.astro.planet.builders.jovian;

import java.util.ArrayList;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.JovianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;

/**
 * A cold gas giant world, similar to Neptune or Uranus.
 * 
 * @author Samuel Penn
 */
public class CryoJovian extends JovianWorld {
	
	public PlanetType getPlanetType() {
		return PlanetType.CryoJovian;
	}
	
	@Override
	public void generate() {
		
		planet.setType(getPlanetType());
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2)/2);
		planet.setDayLength(2000 + Die.d100()*1000 + Die.die(10000));
		planet.setAxialTilt(Die.d10());
		if (planet.getAxialTilt() == 10) {
			planet.setAxialTilt(Die.d10(3));
		}
		if (planet.getAxialTilt() == 30) {
			planet.setAxialTilt(Die.d20(3));
		}
		
		// Does this world have rings? Most Jovian worlds seem to.
		switch(Die.d6(3)) {
		case 3:
			planet.addFeature(PlanetFeature.BrightIceRings);
			break;
		case 4: case 5:
			planet.addFeature(PlanetFeature.IceRings);
			break;
		case 6: case 7: case 8: case 9:
			planet.addFeature(PlanetFeature.FaintIceRings);
			break;
		case 15: case 16: case 17:
			planet.addFeature(PlanetFeature.PartialIceRings);
			break;
		case 18:
			planet.addFeature(PlanetFeature.ExtensiveIceRings);
			break;
		}
		
		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#008899", false));
		tiles.add(new Tile("Light", "#0099bb", false));
		tiles.add(new Tile("White", "#55ddff", false));
		if (Die.d2() == 1) {
			tiles.add(new Tile("Banding", "#0077aa", false));
		} else {
			tiles.add(new Tile("Banding", "#00aa77", false));
		}
		
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
		addResource("Hydrogen", 40 + Die.d20(2));
		addResource("Inert Gases", 30 + Die.d20(2));
		addResource("Exotic Gases", 20 + Die.d12(2));
		addResource("Water", Die.d10(3));
	}
}
