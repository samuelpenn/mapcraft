package uk.org.glendale.worldgen.astro.planet.builders.rock;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;

public class Arean extends BarrenWorld {
	
	public Arean() {
		super();
	}
	
	public void generate() {
		if (planet == null) {
			throw new IllegalStateException("Use setPlanet() to set the planet first");
		}
		planet.setType(PlanetType.Arean);
		int		radius = PlanetType.Arean.getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2));
		planet.addTradeCode(TradeCode.Ba);
		if (planet.getRadius() > 4000) {
			planet.setPressure(AtmospherePressure.Thin);
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
			planet.setTemperature(planet.getTemperature().getHotter());
		} else if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.VeryThin);
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
		} else if (planet.getRadius() > 2000) {
			planet.setPressure(AtmospherePressure.Trace);
			planet.setAtmosphere(AtmosphereType.CarbonDioxide);
		}
		generateMap();
		generateResources();
		generateDescription();
	}

	@Override
	public void generateResources() {
		addResource("Silicate Ore", 25 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		addResource("Carbonic Ore", 10 + Die.d12(3));
		addResource("Ferric Ore", 10 + Die.d12(2));
		if (Die.d4() == 1) {
			addResource("Radioactives", Die.d6(2));
		}
		int		water = Die.d10(2);
		addResource("Water", water);
		if (water > 5) {
			planet.addTradeCode(TradeCode.Ic);
		}
	}
	
	@Override
	public void generateDescription() {
		planet.setDescription("Like Mars");
	}

}