package uk.org.glendale.worldgen.astro.starsystem;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarGenerator;

/**
 * Creates new star systems.
 * 
 * @author Samuel Penn
 */
public class StarSystemGenerator {
	private StarSystem				system;
	
	public StarSystemGenerator(Sector sector, String name, int x, int y) {
		system = new StarSystem(sector, name, x, y);
	}
	
	/**
	 * Randomly generate stars for this system. 50% of systems will have a
	 * single star, 33% will have 2 stars and the rest 3 stars. All double
	 * systems will have a primary and distant secondary. All tertiary
	 * systems will have the third star in close orbit around the secondary.
	 */
	void generateStars() {
		if (system == null) {
			throw new IllegalArgumentException("Star system has not been defined");
		}
		int		numStars = 0;
		switch (Die.d6()) {
		case 1: case 2: case 3:
			numStars = 1;
			break;
		case 4: case 5:
			numStars = 2;
			break;
		case 6:
			numStars = 3;
			break;
		}
		
		StarGenerator	starGenerator = new StarGenerator(system.getName(), numStars > 1);
		system.addStar(starGenerator.generatePrimary());
		if (numStars > 1) {
			system.addStar(starGenerator.generateSecondary());
			if (numStars > 2) {
				system.addStar(starGenerator.generateTertiary());
			}
		}
	}
	
}
