package uk.org.glendale.worldgen.astro.planet.builders.gaian;

import sun.security.action.GetLongAction;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.GaianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;


public class Gaian extends GaianWorld {
	
	public void generate() {
		planet.setType(PlanetType.Gaian);
		int		radius = PlanetType.Gaian.getRadius();
		planet.setRadius(radius/2 + Die.die(radius, 2)/2);
		
		// Set the type of atmosphere.
		switch (Die.d6(3)) {
		case 3: case 4:
			planet.setAtmosphere(AtmosphereType.LowOxygen);
			break;
		case 5: case 6: case 7:
			planet.setAtmosphere(AtmosphereType.Pollutants);
			break;
		case 14: case 15: case 16:
			planet.setAtmosphere(AtmosphereType.HighOxygen);
			break;
		case 17: case 18:
			planet.setAtmosphere(AtmosphereType.HighCarbonDioxide);
			break;
		default:
			planet.setAtmosphere(AtmosphereType.Standard);
		}
		
		// Set the atmosphere's pressure, modified by planet size.
		switch (Die.d6(2) + planet.getRadius()/2000) {
		case 3: case 4:
			planet.setPressure(AtmospherePressure.VeryThin);
			break;
		case 5: case 6: case 7:
			planet.setPressure(AtmospherePressure.Thin);
			break;
		case 14: case 15: case 16:
			planet.setPressure(AtmospherePressure.Dense);
			break;
		case 17: case 18:
			planet.setPressure(AtmospherePressure.VeryDense);
			break;
		default:
			planet.setPressure(AtmospherePressure.Standard);			
		}
		
		planet.setTemperature(planet.getTemperature().getHotter());
		planet.setHydrographics(15 + Die.d20(4));
		setHydrographics(planet.getHydrographics());
		planet.setLifeType(LifeType.Extensive);
		
		sea = new Tile("Sea", "#4444aa", true);
		land = new Tile("Land", "#aaaa44", false);
		mountains = new Tile("Mountains", "#B0B0B0", false);
		
		generateMap();
		generateResources();
	}
	
	protected void addEcology() {
		Tile	woodland = new Tile("Woodland", "#44aa44", false);
		Tile	jungle = new Tile("Jungle", "#338833", false);
		Tile	desert = new Tile("Desert", "#cccc33", false);
		Tile	ice = new Tile("Ice", "#f0f0f0", false);
		
		for (int y=0; y < TILE_HEIGHT; y++) {
			int		latitude = getLatitude(y, TILE_HEIGHT);
			for (int x=getWest(y); x < getEast(y); x++) {
				if (latitude > 70) {
					map[y][x] = ice;
				} else if (latitude > 35 && map[y][x] == land) {
					map[y][x] = woodland;
				} else if (latitude > 15 && map[y][x] == land) {
					map[y][x] = desert;
				} else if (map[y][x] == land) {
					map[y][x] = jungle;
				}
			}
		}
	}
	
	@Override
	public void generateResources() {
		addResource("Water", planet.getHydrographics());
		if (planet.getAtmosphere() == AtmosphereType.LowOxygen) {
			addResource("Oxygen", 20);
		} else {
			addResource("Oxygen", 40);
		}
		addResource("Silicate Ore", 20 + Die.d10(3));
		addResource("Ferric Ore", 10 + Die.d8(3));
		addResource("Carbonic Ore", 10 + Die.d10(3));
		addEcologicalResources();
	}

}
