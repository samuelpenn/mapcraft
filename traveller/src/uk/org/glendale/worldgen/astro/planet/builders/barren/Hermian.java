package uk.org.glendale.worldgen.astro.planet.builders.barren;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;

public class Hermian extends BarrenWorld {
	public Hermian() {
		super();
	}
	
	public void setPlanet(Planet planet) {
		planet.setType(PlanetType.Hermian);
		super.setPlanet(planet);
	}
}
