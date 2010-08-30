package uk.org.glendale.worldgen.astro.starsystem;

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
			transaction.rollback();
		}
		
		return system;
	}
	
	/**
	 * Randomly generate stars for this system. Most systems will have a
	 * single star, some will have two and a few three. All binary
	 * systems will have a primary and distant secondary. All triple
	 * systems will have the third star in close orbit around the secondary.
	 */
	private void generateStars(StarSystem system) {
		if (system == null) {
			throw new IllegalArgumentException("Star system has not been defined");
		}
		int		numStars = 0;
		switch (Die.d10()) {
		case 1: case 2: case 3:
		case 4: case 5: case 6:
			numStars = 1;
			break;
		case 7: case 8: case 9:
			numStars = 2;
			break;
		case 10:
			numStars = 3;
			break;
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
	
	private void generatePlanets(StarSystem system, int starIndex) {
		Star	star = system.getStars().get(starIndex);
		int		numPlanets = Die.d6(2) - starIndex*3;
		int		increase = StarAPI.getInnerLimit(star);
		int		distance = StarAPI.getInnerLimit(star)+Die.die(increase, 2);
		
		PlanetGenerator		planetGenerator = new PlanetGenerator(entityManager, system, star);
		for (int p=0; p < numPlanets; p++) {
			String	planetName = star.getName()+" "+(p+1);
			Planet	planet = planetGenerator.generatePlanet(planetName, p, distance);
			entityManager.persist(planet);
		}

	}
	
}
