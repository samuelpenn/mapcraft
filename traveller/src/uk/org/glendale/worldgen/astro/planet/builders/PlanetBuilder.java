package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.worldgen.astro.planet.Planet;

public abstract class PlanetBuilder {
	protected Planet		planet;
	
	public PlanetBuilder(Planet planet, PlanetType type) {
		if (planet == null || type == null) {
			throw new IllegalArgumentException("Planet must be set, and have a valid type");
		}
		this.planet = planet;
		
		planet.setType(type);
		planet.setRadius(type.getRadius());
		planet.setAtmosphere(AtmosphereType.Vacuum);
		planet.setPressure(AtmospherePressure.None);
		
		planet.setLifeType(LifeType.None);
	}
	
	/**
	 * Generate standard planetary statistics.
	 */
	public abstract void generate();
	
	/**
	 * Generate surface map for this planet.
	 */
	public abstract void generateMap();
	
	/**
	 * Generate resources for this planet.
	 */
	public abstract void generateResources();
	
	
	public static final int MAP_WIDTH = 1024;
	public static final int MAP_HEIGHT = 512;
	
	public static final int TILE_SIZE = 16;
	
	protected Tile[][]	tileMap = null;
	
	
	
	
}
