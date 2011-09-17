package uk.org.glendale.worldgen.astro.planet.builders.gaian;

import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.StarportType;
import uk.org.glendale.worldgen.astro.planet.builders.GaianWorld;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * Generate an Earth-like world. These are the most suitable planets for
 * human-like life, though they can vary greatly in just how suitable they
 * are.
 * 
 * @author Samuel Penn
 */
public class Gaian extends GaianWorld {
	
	public PlanetType getPlanetType() {
		return PlanetType.Gaian;
	}
	
	public void generate() {
		planet.setType(getPlanetType());
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius/2 + Die.die(radius, 2)/2);
		planet.setDayLength(Die.d6(2)*10000 + Die.die(30000));
		planet.setAxialTilt(Die.d10(3));
		
		int		populationModifier = 0;
		
		// Set the type of atmosphere.
		switch (Die.d6(3)) {
		case 3: case 4:
			planet.setAtmosphere(AtmosphereType.LowOxygen);
			populationModifier--;
			break;
		case 5: case 6: case 7:
			planet.setAtmosphere(AtmosphereType.Pollutants);
			populationModifier--;
			break;
		case 14: case 15: case 16:
			planet.setAtmosphere(AtmosphereType.HighOxygen);
			populationModifier++;
			break;
		case 17: case 18:
			planet.setAtmosphere(AtmosphereType.HighCarbonDioxide);
			populationModifier--;
			break;
		default:
			planet.setAtmosphere(AtmosphereType.Standard);
		}
		
		// Set the atmosphere's pressure, modified by planet size.
		switch (Die.d6(2) + planet.getRadius()/2000) {
		case 3: case 4:
			planet.setPressure(AtmospherePressure.VeryThin);
			populationModifier-=2;
			break;
		case 5: case 6: case 7:
			planet.setPressure(AtmospherePressure.Thin);
			populationModifier--;
			break;
		case 14: case 15: case 16:
			planet.setPressure(AtmospherePressure.Dense);
			break;
		case 17: case 18:
			planet.setPressure(AtmospherePressure.VeryDense);
			populationModifier--;
			break;
		default:
			planet.setPressure(AtmospherePressure.Standard);			
		}
		
		
		planet.setTemperature(planet.getTemperature().getHotter());
		planet.setHydrographics(15 + Die.d20(4));
		setHydrographics(planet.getHydrographics());
		
		if (planet.getHydrographics() > 50 && planet.getHydrographics() < 85) {
			populationModifier++;
		}
		if (planet.getTemperature() == Temperature.Warm){
			populationModifier++;
		}

		if (populationModifier < -2) {
			planet.addTradeCode(TradeCode.H2);
			planet.setLifeType(LifeType.SimpleLand);
		} else if (populationModifier < 0) {
			planet.addTradeCode(TradeCode.H1);
			planet.setLifeType(LifeType.ComplexLand);
		} else {
			planet.addTradeCode(TradeCode.H0);
			planet.setLifeType(LifeType.Extensive);
		}
		
		sea = new Tile("Sea", "#4444aa", true);
		land = new Tile("Land", "#aaaa44", false);
		mountains = new Tile("Mountains", "#B0B0B0", false);
		
		generateMap();
		generateResources();
		
		// Temporary population adding fix.
		switch (Die.d6(2)) {
		case -2: case -1: case 0: case 1:
			planet.setPopulation(Die.d100() * 100);
			planet.setStarport(StarportType.E);
			planet.setTechLevel(Die.d4()+2);
			planet.addTradeCode(TradeCode.Ag);
			planet.addTradeCode(TradeCode.Ni);
			break;
		case 2: case 3:
			planet.setPopulation(Die.d100() * 10000);
			planet.setStarport(StarportType.E);
			planet.setTechLevel(Die.d4()+3);
			planet.addTradeCode(TradeCode.Ag);
			planet.addTradeCode(TradeCode.Ni);
			break;
		case 4: case 5:
			planet.setPopulation(Die.d10() * 1000000);
			planet.setStarport(StarportType.D);
			planet.setTechLevel(Die.d4()+4);
			planet.addTradeCode(TradeCode.Ag);
			break;
		case 6: case 7:
			planet.setPopulation(Die.d10() * 10000000L);
			planet.setStarport(StarportType.C);
			planet.setTechLevel(Die.d3()+5);
			break;
		case 8 :case 9:
			planet.setPopulation(Die.d10() * 100000000L);
			planet.setStarport(StarportType.B);
			planet.setTechLevel(Die.d3()+6);
			planet.addTradeCode(TradeCode.In);
			break;
		case 10: case 11:
			planet.setPopulation(Die.d10() * 1000000000L);
			planet.setStarport(StarportType.A);
			planet.setTechLevel(Die.d4()+8);
			planet.addTradeCode(TradeCode.In);
			planet.addTradeCode(TradeCode.Na);
			break;
		default:
			planet.setPopulation(Die.d10() * 10000000000L);
			planet.setStarport(StarportType.A);
			planet.setTechLevel(Die.d3()+9);
			planet.addTradeCode(TradeCode.In);
			planet.addTradeCode(TradeCode.Na);
		}
		switch (Die.d6(2)) {
		case 2: case 3:
			planet.setGovernment(GovernmentType.Balkanization);
			planet.setLawLevel(Die.d3());
			planet.addTradeCode(TradeCode.Po);
			break;
		case 4: case 5:
			planet.setGovernment(GovernmentType.CharismaticLeader);
			planet.setLawLevel(Die.d3()+3);
			break;
		case 6: case 7: case 8:
			planet.setGovernment(GovernmentType.RepresentativeDemocracy);
			planet.setLawLevel(Die.d3()+1);
			break;
		case 9: case 10:
			planet.setGovernment(GovernmentType.ImpersonalBureaucracy);
			planet.setLawLevel(Die.d2()+4);
			break;
		case 11: case 12:
			planet.setGovernment(GovernmentType.TheocraticOligarchy);
			planet.setLawLevel(Die.d4()+2);
			break;		
		}
	}
	
	protected void addEcology() {
		Tile	woodland = new Tile("Woodland", "#44aa44", false);
		Tile	jungle = new Tile("Jungle", "#338833", false);
		Tile	desert = new Tile("Desert", "#cccc33", false);
		Tile	ice = new Tile("Ice", "#f0f0f0", false);
		
		for (int y=0; y < TILE_HEIGHT; y++) {
			int		latitude = getLatitude(y, TILE_HEIGHT);
			for (int x=getWest(y); x < getEast(y); x++) {
				if (latitude > 70 && planet.getTemperature().isColderThan(Temperature.Warm)) {
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
