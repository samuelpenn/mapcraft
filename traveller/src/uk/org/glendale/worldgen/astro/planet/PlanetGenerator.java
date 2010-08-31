package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.EntityManager;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hermian;
import uk.org.glendale.worldgen.astro.planet.builders.gaian.Gaian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.EuJovian;
import uk.org.glendale.worldgen.astro.planet.builders.rock.Arean;
import uk.org.glendale.worldgen.astro.star.*;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

public class PlanetGenerator {
	private StarSystem		system;
	private Star			star;
	private EntityManager	entityManager;
	
	public PlanetGenerator(EntityManager entityManager, StarSystem system, Star star) {
		this.entityManager = entityManager;
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
			builder = new Hermian();
			break;
		case VeryHot:
			// Mercury
			builder = new Hermian();
			break;
		case Hot:
		case Warm:
			// Venus.
			builder = new Hermian();
			break;
		case Standard:
			// Earth.
			builder = new Gaian();
			break;
		case Cool:
		case Cold:
			// Mars, Asteroids
			builder = new Arean();
			break;
		case VeryCold:
			// Jupiter, Saturn
			builder = new EuJovian();
			break;
		case ExtremelyCold:
			// Uranus, Neptune.
			builder = new EuJovian();
			break;
		case UltraCold:
			// Kuiper belt
			builder = new EuJovian();
			break;
		}
		builder.setEntityManager(entityManager);
		builder.setPlanet(planet);
		builder.generate();
		
		System.out.println(system.getId()+": "+planet.getName()+" ("+planet.getType()+")");

		return planet;
	}
}
