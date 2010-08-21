package uk.org.glendale.worldgen.astro.sector;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.text.Names;

public class SectorGenerator {
	private EntityManager		entityManager;
	
	public SectorGenerator(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Create an empty sector which has no star systems defined.
	 * X and Y coordinate must be unique, and so must the name.
	 * 
	 * @param name			Unique name for the sector.
	 * @param x				X coordinate for the sector.
	 * @param y				Y coordinate for the sector.
	 * @param codes			Codes, if any.
	 * @param allegiance	Allegiance, if any.
	 * @return
	 */
	public Sector createEmptySector(String name, int x, int y, String codes, String allegiance) {
		Sector		sector = new Sector(name, x, y, codes, allegiance);
		
		EntityTransaction		transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(sector);
			transaction.commit();
		} catch (Throwable t) {
			transaction.rollback();
		}

		return sector;
	}
	
	public void deleteSector(Sector sector) {
		clearSector(sector);
		EntityTransaction	transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.remove(sector);
		transaction.commit();
	}
	
	/**
	 * Delete all star systems and associated data from the sector.
	 * Leaves the sector itself as intact, but empty.
	 * 
	 * @param sector		Sector to clear.
	 */
	public void clearSector(Sector sector) {
		StarSystemFactory	factory = new StarSystemFactory(entityManager);
		List<StarSystem>	systems = factory.getStarSystemsInSector(sector);

		EntityTransaction	transaction = entityManager.getTransaction();
		transaction.begin();
		for (StarSystem system : systems) {
			System.out.println(system.getName());
			entityManager.remove(system);
		}
		transaction.commit();
	}
	
	/**
	 * Fill an empty sector with random star systems. The chance of any
	 * parsec having a star system is determined by the percentage defined.
	 * 
	 * @param sector		Sector to fill. Should be empty.
	 * @param percentChance	Percentage change each hex has a system.
	 * 
	 * @return				Count of number of systems added.
	 */
	public int fillRandomSector(Sector sector, Names names, int percentChance) {
		int		count = 0;
		
		StarSystemGenerator		systemGenerator = new StarSystemGenerator(entityManager);
		
		for (int x=1; x <= Sector.WIDTH; x++) {
			for (int y=1; y <= Sector.HEIGHT; y++) {
				if (Die.d100() <= percentChance) {
					systemGenerator.createStarSystem(sector, names.getPlanetName(), x, y);
				}
			}
		}
		
		return count;
	}
}
