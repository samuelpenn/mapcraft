package uk.org.glendale.rpg.traveller.worlds;

import java.io.File;

import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * A barren world with no hydrographics.
 * 
 * @author Samuel Penn
 *
 */
public class RockWorld extends Tectonics {
	public RockWorld(int width, int height) {
		super(width, height);
	}
	
	/**
	 * Simulate asteroid bombardment by creating a number of random craters
	 * across the map of the world. Craters will have a specified depth, and
	 * should have a rim and random ejecta trails.
	 * 
	 * @param number		Number of craters to make.
	 * @param size			Average size of each crater.
	 * @param force			How deep is the hole? 0 = very deep, 1 = no impact.
	 * @param terrain		If non-null, set terrain to this.
	 */
	protected void generateImpacts(int number, int size, double force, Terrain terrain, Terrain ejecta) {
		for (int i=0; i < number; i++) {
			int		 x = Die.rollZero(width);
			int		 y = (int)(Die.rollZero(height) * 0.9 + height * 0.05);
			int		 radius = Die.die(size, 2) / 2;
			
			addCrater(x, y, radius, force, terrain, ejecta);
		}
	}
	
	protected void addCrater(int x, int y, int radius, double force, Terrain terrain, Terrain ejecta) {
		for (int xx = x - radius; xx <= x + radius; xx++) {
			for (int yy = y - radius; yy <= y + radius; yy++) {
				int 		px = xx;
				int			py = yy;
				boolean		ridge = false;	
				
				if (py < 0 || py >= height) {
					continue;
				}

				int		d = (int)Math.sqrt( (x - px) * (x - px) + (y - py) * (y - py));
				// Give a fuzzy edge to the craters.)
				if (d == radius) {
					ridge = true;
				} else if (d > radius || Die.die(d,2) > radius) {
					continue;
				}
				
				// Wrap around west and east.
				if (px < 0) px += width;
				if (px >= width) px -= width;
				
				if (terrain != null) {
					setTerrain(px, py, terrain);
				}
				if (ridge && Die.d3()!=1) {
					setHeight(px, py, (int)(getHeight(px, py) / force));
					if (ejecta != null && Die.d3()!=1) {
						setTerrain(px, py, ejecta);
					}
				} else {
					setHeight(px, py, (int)(getHeight(px, py) * force));
				}
			}
		}
		
		if (ejecta != null && Die.die(radius) > 5 && Die.d2() == 1) {
			for (int e = 0; e < Math.pow(radius, 1.2); e++) {
				int		xx = x + Die.die(radius*10) - Die.die(radius*10);
				int		yy = y + Die.die(radius*10) - Die.die(radius*10);
				int		d = (int)(Math.sqrt((xx - x) * (xx - x) + (yy - y) * (yy - y)));
				
				for (int l=radius; l < d; l++) {
					int		px = x + ((xx - x) * l)/d;
					int		py = y + ((yy - y) * l)/d;
					
					if (px < 0) px += width;
					if (px >= width) px -= width;
					if (py < 0) break;
					if (py >= height) break;
					
					setHeight(px, py, (int)(getHeight(px, py) * force));
					setTerrain(px, py, ejecta);
					xx += Die.d2() - Die.d2();
					yy += Die.d2() - Die.d2();
				}
			}
		}
	}
	
	public void generate() {
		generateContinents();
		copyShelfToTiles();
		setTiles();

		fractalLandscape();

		generateImpacts();
	}

	public static void main(String[] args) throws Exception {
		RockWorld	t = new RockWorld(512, 256);
		t.setPlanet(new Planet("Earth", PlanetType.Gaian, 6400));
		t.planet.setHydrographics(30);
		t.planet.setTilt(22);
		t.planet.setTemperature(Temperature.Cold);
		t.planet.setLifeLevel(LifeType.None);
		t.planet.setAtmosphereType(AtmosphereType.Standard);
		t.planet.setAtmospherePressure(AtmospherePressure.Standard);

		t.generate();
		t.getWorldMap(2).save(new File("/home/sam/gaian.jpg"));
	}
}
