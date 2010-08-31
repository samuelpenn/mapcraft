package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Barren worlds are rocky worlds with little or no atmosphere, no
 * surface water and no life. They may potentially be rich in mineral
 * resources, but have little else going for them.
 * 
 * @author Samuel Penn
 */
public abstract class GaianWorld extends PlanetBuilder {
	protected Tile	sea = new Tile("Sea", "#4444aa", true);
	protected Tile	land = new Tile("Land", "#aaaa44", false);
	protected Tile	mountains = new Tile("Mountains", "#B0B0B0", false);
	
	
	public GaianWorld() {
	}
	
	

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.Standard);
			planet.setPressure(AtmospherePressure.Standard);
		}
		generateMap();
		generateResources();
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			return;
		}
		addContinents(sea, land, mountains);
		addEcology();
		
		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);
		
		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		getImage();
	}
	
	protected abstract void addEcology();
	
}
