package uk.org.glendale.worldgen.astro.planet;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.BarrenWorld;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hermian;
import uk.org.glendale.worldgen.astro.planet.builders.gaian.Gaian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.CryoJovian;
import uk.org.glendale.worldgen.astro.planet.builders.jovian.EuJovian;
import uk.org.glendale.worldgen.astro.planet.builders.rock.Arean;
import uk.org.glendale.worldgen.astro.star.*;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

/**
 * Generates a random planet. The type of planet is based on the star and
 * the orbit distance from it. A suitable PlanetBuilder is selected, and
 * then used to build the planet, including any descriptions, maps and so on.
 * 
 * Moons can be generated as a final step. Moons for a planet must be
 * generated immediately after the planet that the moons are for.
 * 
 * @author Samuel Penn
 */
public class PlanetGenerator {
	private StarSystem		system;
	private Star			star;
	private EntityManager	entityManager;
	private PlanetBuilder	builder = null;
	
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

		builder = null;
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
			switch (Die.d6()) {
			case 1: case 2:
				builder = new Gaian();
				break;
			default:
				builder = new Arean();
			}
			break;
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
			builder = new CryoJovian();
			break;
		case UltraCold:
			// Kuiper belt
			builder = new CryoJovian();
			break;
		}
		builder.setEntityManager(entityManager);
		builder.setPlanet(planet);
		builder.setStar(star);
		builder.generate();
		
		System.out.println(system.getId()+": "+planet.getName()+" ("+planet.getType()+")");

		return planet;
	}

	public List<Planet> generateMoons(Planet planet) {
		List<Planet>	moons = new ArrayList<Planet>();
		PlanetBuilder[]	builders = builder.getMoonBuilders();
		
		if (builders != null) {
			String[]	names = { "a", "b", "c", "d", "e", "f", "g", "h",
			        	          "i", "j", "k", "l", "m", "n", "o", "p" };
			int			distance = builder.getFirstMoonDistance();
			for (int i=0; i < builders.length; i++) {
				Temperature		orbitTemperature = StarAPI.getOrbitTemperature(star, planet.getDistance());
				
				PlanetBuilder	moonBuilder = builders[i];
				String			moonName = planet.getName()+"/"+names[i];
				System.out.println(moonName);
				Planet			moon = new Planet(planet.getSystem(), planet.getId(), true, moonName);
				moon.setDistance(distance);
				moon.setTemperature(orbitTemperature);
				moonBuilder.setEntityManager(entityManager);
				moonBuilder.setPlanet(moon);
				moonBuilder.setStar(star);
				moonBuilder.generate();
				moons.add(moon);
				
				distance *= 1.5;
			}
		}
		System.out.println("Built "+moons.size()+" moons");
		
		return moons;
	}
}
