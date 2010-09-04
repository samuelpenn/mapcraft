package uk.org.glendale.worldgen.astro.starsystem;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.org.glendale.rpg.traveller.systems.Zone;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetGenerator;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarAPI;
import uk.org.glendale.worldgen.astro.star.StarGenerator;

/**
 * Creates new star systems.
 * 
 * @author Samuel Penn
 */
public class StarSystemGenerator {
	private EntityManager			entityManager;
	
	public StarSystemGenerator(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public StarSystem createStarSystem(Sector sector, String name, int x, int y) {
		StarSystem system = new StarSystem(sector, name, x, y);
		system.setAllegiance("Un");
		system.setZone(Zone.Green);
		
		EntityTransaction		transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(system);

			generateStars(system);
			
			transaction.commit();
		} catch (Throwable t) {
			t.printStackTrace();
			transaction.rollback();
		}
		
		return system;
	}
	
	/**
	 * Randomly generate stars for this system. Most systems will have a
	 * single star, some will have two and a few three. All binary
	 * systems will have a primary and distant secondary. All triple
	 * systems will have the third star in close orbit around the secondary.
	 * 
	 * Multiple-star systems are kept deliberately rarer than reality for
	 * reasons of simplicity.
	 */
	private void generateStars(StarSystem system) {
		if (system == null) {
			throw new IllegalArgumentException("Star system has not been defined");
		}
		int		numStars = 0;
		switch (Die.d6(3)) {
		case 3:
			// Triple star system.
			numStars = 3;
			break;
		case 4: case 5: case 6:
			// Binary star system.
			numStars = 2;
			break;
		default:
			numStars = 1;
		}
		
		StarGenerator	starGenerator = new StarGenerator(system, numStars > 1);
		Star	primary = starGenerator.generatePrimary();
		entityManager.persist(primary);
		system.addStar(primary);
		if (numStars > 1) {
			Star	secondary = starGenerator.generateSecondary();
			entityManager.persist(secondary);
			system.addStar(secondary);
			if (numStars > 2) {
				Star	tertiary = starGenerator.generateTertiary();
				entityManager.persist(tertiary);
				system.addStar(tertiary);
			}
		}
		
		for (int s = 0; s < system.getStars().size(); s++) {
			generatePlanets(system, s);
		}
	}
	
	/**
	 * Get the orbit number as a Roman numeral. Should work
	 * up to 39.
	 * 
	 * @param orbit		Orbit number, 1+
	 * @return			Roman numeral.
	 */
	private String getOrbitNumber(int orbit) {
		String	x = "";
		
		while (orbit >= 10) {
			x += "X";
			orbit -= 10;
		}
		switch (orbit) {
		case 1: return x+"I";
		case 2: return x+"II";
		case 3: return x+"III";
		case 4: return x+"IV";
		case 5: return x+"V";
		case 6: return x+"VI";
		case 7: return x+"VII";
		case 8: return x+"VIII";
		case 9: return x+"IX";
		}
		
		return x;
	}
	
	/**
	 * Randomly generate planets for a star.
	 * 
	 * @param system		System to generate planets for.
	 * @param starIndex		Star within the system.
	 */
	private void generatePlanets(StarSystem system, int starIndex) {
		Star	star = system.getStars().get(starIndex);
		int		numPlanets = Die.d6(2) - starIndex*3;
		int		increase = StarAPI.getInnerLimit(star) + Die.d6(2);
		int		distance = StarAPI.getInnerLimit(star)+Die.die(increase, 2);
		
		PlanetGenerator		planetGenerator = new PlanetGenerator(entityManager, system, star);
		for (int p=0; p < numPlanets; p++) {
			String	planetName = star.getName()+" "+getOrbitNumber(p+1);
			Planet	planet = planetGenerator.generatePlanet(planetName, p, distance);
			entityManager.persist(planet);
			System.out.println("Persisted planet ["+planet.getId()+"] ["+planet.getName()+"]");
			
			List<Planet>	moons = planetGenerator.generateMoons(planet);
			for (Planet moon : moons) {
				System.out.println("Persisting ["+moon.getName()+"]");
				entityManager.persist(moon);
				System.out.println("Persisted moon ["+moon.getId()+"] ["+moon.getName()+"]");
			}
			
			if (planet.getType().isJovian()) {
				// Give extra room for Jovian worlds.
				distance += Die.die(increase, 2) + Die.d10(3);
			}
			distance += Die.die(increase, 2) + Die.d10(2);
			increase = (int)(increase * (1.0 + Die.d6(2)/10.0)) + Die.d4();
		}

	}
	
}
