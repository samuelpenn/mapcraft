package uk.org.glendale.worldgen.astro.planet.builders.barren;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.civ.commodity.Commodity;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;

/**
 * HSelenian worlds are similar to the Moon. They have a limited
 * amount of useful resources, and are generally barren and dry.
 * 
 * @author Samuel Penn
 */
public class Selenian extends BarrenWorld {
	public Selenian() {
		super();
	}
	
	public PlanetType getPlanetType() {
		return PlanetType.Selenian;
	}
		
	public void generate() {
		if (planet == null) {
			throw new IllegalStateException("Use setPlanet() to set the planet first");
		}
		planet.setType(getPlanetType());
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2)/2);
		planet.setAtmosphere(AtmosphereType.Vacuum);
		planet.addTradeCode(TradeCode.Va);
		if (planet.getTemperature().isHotterThan(Temperature.ExtremelyHot)) {
			planet.addTradeCode(TradeCode.H4);
		} else {
			planet.addTradeCode(TradeCode.In);
		}

		generateMap();
		generateResources();
		generateDescription();
	}
	
	@Override
	public void generateMap() {
		setCraterNumbers(300);
		super.generateMap();
	}

	/**
	 * Resources consist of Silicate ores.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		addResource("Helium 3", Die.d4(2));
	}
}
