package uk.org.glendale.worldgen.astro.planet;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.star.*;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

public class PlanetGenerator {
	private StarSystem		system;
	private Star			star;
	
	public PlanetGenerator(StarSystem system, Star star) {
		this.system = system;
		this.star = star;
	}
	
	public Planet generatePlanet(String name, int position, int distance) {
		Temperature		orbitTemperature = StarAPI.getOrbitTemperature(star, distance);
		
		Planet			planet = new Planet(system, star.getId(), false, name);
		planet.setDistance(distance);
		planet.setTemperature(orbitTemperature);

		PlanetBuilder	builder = null;
		switch (orbitTemperature) {
		case UltraHot:
		case ExtremelyHot:
			// Unlikely
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case VeryHot:
			// Mercury
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case Hot:
		case Warm:
			// Venus.
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case Standard:
			// Earth.
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case Cool:
		case Cold:
			// Mars, Asteroids
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case VeryCold:
			// Jupiter, Saturn
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case ExtremelyCold:
			// Uranus, Neptune.
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		case UltraCold:
			// Kuiper belt
			builder = new BarrenWorld(planet, PlanetType.Hermian);
			break;
		}

		return planet;
	}
}