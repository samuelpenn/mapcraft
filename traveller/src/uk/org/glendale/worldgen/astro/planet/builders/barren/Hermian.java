package uk.org.glendale.worldgen.astro.planet.builders.barren;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.civ.commodity.Commodity;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;

/**
 * Hermian worlds are similar to Mercury. They are hot, barren rock worlds
 * close to their sun. Relatively rich in heavy elements, but covered in
 * a mantle of silicate rocks. No organic or ice compounds.
 * 
 * @author Samuel Penn
 */
public class Hermian extends BarrenWorld {
	public Hermian() {
		super();
	}
	
	public PlanetType getPlanetType() {
		return PlanetType.Hermian;
	}
		
	public void generate() {
		if (planet == null) {
			throw new IllegalStateException("Use setPlanet() to set the planet first");
		}
		planet.setType(getPlanetType());
		int		radius = getPlanetType().getRadius();
		planet.setRadius(radius / 2 + Die.die(radius, 2)/2);
		if (planet.getRadius() > 3000) {
			planet.setPressure(AtmospherePressure.Trace);
			planet.setAtmosphere(AtmosphereType.InertGases);
			planet.addTradeCode(TradeCode.Ba);
		} else {
			planet.addTradeCode(TradeCode.Va);			
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
	 * Resources consist of Silicate and Ferric ores.
	 */
	@Override
	public void generateResources() {
		addResource("Silicate Ore", 30 + Die.d20(3));
		if (Die.d2() == 1) {
			addResource("Silicate Crystals", 10 + Die.d10(2));
		}
		addResource("Ferric Ore", 20 + Die.d20(2));
		if (Die.d2() == 1) {
			addResource("Heavy Metals", 10 + Die.d12(2));
		}
		addResource("Radioactives", 5 + Die.d6(2));
		if (Die.d4() == 1) {
			addResource("Rare Metals", 5 + Die.d6(2));
		}
		addResource("Helium 3", Die.d6(2));
	}
}
