package uk.org.glendale.worldgen.astro.planet.builders.ice;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetType;
import uk.org.glendale.worldgen.astro.planet.builders.IceWorld;

/**
 * A moon similar to Europa.
 * 
 * @author Samuel Penn
 */
public class Europan extends IceWorld {
	
	public PlanetType getPlanetType() {
		return PlanetType.Europan;
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
		planet.addTradeCode(TradeCode.H3);
		
		// Work out ecology, if any.
		switch (Die.d6(3)) {
		case 3:
			planet.setLifeType(LifeType.Aerobic);
			break;
		case 4: case 5:
			planet.setLifeType(LifeType.Archaean);
			break;
		case 6: case 7: case 8:
			planet.setLifeType(LifeType.Organic);
			break;
		}
		
		System.out.println("Generate map");
		generateMap();
		System.out.println("Generate resources");
		generateResources();
		System.out.println("Generate description");
		generateDescription();
		System.out.println("Done");
	}

	@Override
	public void generateResources() {
		addResource("Water", 20 + Die.d20(4));
	}

}
