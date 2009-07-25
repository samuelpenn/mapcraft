/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.civilisation;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.*;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Class which simulates colonisation of a world.
 */
public class Colony {
	private Planet		planet = null;
	
	public Colony(Planet planet) {
		this.planet = planet;
	}
	
	/**
	 * Terraform a world. 
	 * 1: Clean the atmosphere.
	 * 
	 * @param level		Level of terraforming performed.
	 */
	public void terraform(int level) {
		if (!planet.getType().isTerrestrial()) {
			// Can only terraform terrestrial worlds.
			return;
		}
	}
	
	/**
	 * Get the population density of the planet. This is the ecological load of
	 * the population on the planet, and 1.0 represents a comfortable level.
	 * Lower than this and the world is underpopulated, higher and there is
	 * overpopulation.
	 * 
	 * @return		Population density of the planet.
	 */
	private double getPopulationDensity() {
		double		density = 0.0;
		long		population = planet.getPopulation();
		long		area = planet.getRadius() * planet.getRadius();
		
		if (population == 0) {
			return 0.0;
		}
		
		if (area < 1) {
			// Something is really wrong here.
			return 10.0;
		}
		// This should give Earth a value of 1.0 if it had 4 billion.
		density = (1.0 * population) / area / 100.0;
		
		// Modify by the suitability of the world.
		density *= (getSuitability() / 100.0);
		
		// If there is too little or too much water, then increase density.
		if (planet.getHydrographics() < 5) {
			density *= 10;
		} else if (planet.getHydrographics() < 10) {
			density *= 5;
		} else if (planet.getHydrographics() < 40) {
			density *= 2;
		} else if (planet.getHydrographics() < 80) {
			// No effect.
		} else if (planet.getHydrographics() < 90) {
			density *= 2;
		} else if (planet.getHydrographics() < 95) {
			density *= 5;
		} else {
			// Not much land.
			density *= 10;
		}
		
		// Modify by the TL. High TLs can support more people.
		switch (planet.getTechLevel()) {
		case 0:	 density *= 25;   break;
		case 1:	 density *= 12;   break;
		case 2:	 density *= 7;    break;
		case 3:	 density *= 4;    break;
		case 4:	 density *= 3;    break;
		case 5:  density *= 2.5;  break;
		case 6:  density *= 2.0;  break;
		case 7:  density *= 1.5;  break;
		case 8:  density *= 1.0;  break;
		case 9:  density *= 0.75; break;
		case 10: density *= 0.5;  break;
		case 11: density *= 0.25; break;
		case 12: density *= 0.15; break;
		default:
			density *= 0.1;
			break;
		}
		
		// Finally, modify by life type. A poor ecosystem cannot support much
		// of a population.
		switch (planet.getLifeLevel()) {
		case None:         density *= 0.05;  break;
		case Organic:     density *= 0.10; break;
		case Archaean:     density *= 0.15; break;
		case Aerobic:      density *= 0.20; break;
		case ComplexOcean: density *= 0.30; break;
		case SimpleLand:   density *= 0.50; break;
		case ComplexLand:  density *= 0.75; break;
		case Extensive:    density *= 1.00; break;
		}
		
		return density;
	}

	/**
	 * A colony will have an effect on the world. If there is a suitably large
	 * population, it will force the ecology to evolve and grow even without
	 * trying (just life growing out of the colony's rubbish). If it has a high
	 * tech level, then it can consciously expand the growth as well. 
	 */
	private void growEcology() {
		int		tl = planet.getTechLevel();
		int		chance = (int)(5000 / Math.log(planet.getPopulation()));
		
		switch (planet.getLifeLevel()) {
		case None:
			if (tl > 10 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 9 && Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 7 && Die.die(chance) < 5) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (Die.die(chance) < 2) {
				planet.setLifeLevel(LifeType.Organic);
			}
			break;
		case Organic:
			if (tl > 10 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 9 && Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 7 && Die.die(chance) < 5) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (Die.die(chance) < 3) {
				planet.setLifeLevel(LifeType.Archaean);
			}
			break;
		case Archaean:
			if (tl > 10 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 9 && Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (tl > 7 && Die.die(chance) < 5) {
				planet.setLifeLevel(LifeType.Aerobic);
			} else if (Die.die(chance) < 3) {
				planet.setLifeLevel(LifeType.Aerobic);
			}
			break;
		case Aerobic:
			if (tl > 10 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.SimpleLand);
			} else if (tl > 9 && Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.SimpleLand);
			} else if (tl > 7 && Die.die(chance) < 5) {
				planet.setLifeLevel(LifeType.ComplexOcean);
			} else if (Die.die(chance) < 3) {
				planet.setLifeLevel(LifeType.ComplexOcean);
			}
			break;
		case ComplexOcean:
			// Everything is now in place to support land life, so
			// this jump is quite quick.
			if (tl > 10 && Die.die(chance) < 100) {
				planet.setLifeLevel(LifeType.SimpleLand);
			} else if (tl > 9 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.SimpleLand);
			} else if (tl > 7 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.SimpleLand);
			} else if (Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.SimpleLand);
			}
			break;
		case SimpleLand:
			if (tl > 10 && Die.die(chance) < 50) {
				planet.setLifeLevel(LifeType.ComplexLand);
			} else if (tl > 9 && Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.ComplexLand);
			} else if (Die.die(chance) < 5) {
				planet.setLifeLevel(LifeType.ComplexLand);
			}
			break;
		case ComplexLand:
			// This mostly just takes time, and is difficult to hurry.
			if (tl > 9 && Die.die(chance) < 20) {
				planet.setLifeLevel(LifeType.Extensive);
			} else if (Die.die(chance) < 10) {
				planet.setLifeLevel(LifeType.Extensive);
			}
			break;
		case Extensive:
			// As extensive as it can get.
			break;
		}	
	}
	
	private void growTechnology() {
		long		population = planet.getPopulation();
		double		density = getPopulationDensity();
		int		    tl = planet.getTechLevel();
		long		minPopulation = (long)Math.pow(2.5, tl) * 100000;
		
		// First, check that the current tech level can be supported. If it
		// can't, then we're not going to be improving.
		if (tl > 3) {
			if (population < minPopulation) {
				int		chance = 1 + (int) Math.sqrt(minPopulation / population);
				if (chance > 10) chance = 10;
				if (Die.d100() <= chance) {
					tl -= 1;
					planet.setTechLevel(tl);
				}
				return;
			}
		}
		
		// Now check for the space port.
		StarportType	starport = planet.getStarport();
		if (tl < starport.getMinimumTechLevel() && Die.d100() < 10) {
			planet.setStarport(starport.getWorse());
		} else if (tl > starport.getMinimumTechLevel() && population > minPopulation && Die.d100() <= (tl-starport.getMinimumTechLevel())*5) {
			if (tl > 9) {
				planet.setStarport(starport.getBetter());
			}
		}
		
		// Chance of TL improving.
		int		chance = 0;
		if (population > minPopulation * 100000) {
			chance = 500;
		} else if (population > minPopulation * 10000) {
			chance = 250;
		} else if (population > minPopulation * 1000) {
			chance = 100;
		} else if (population > minPopulation * 100) {
			chance = 20;
		} else if (population > minPopulation * 10) {
			chance = 4;
		} else if (population > minPopulation * 2) {
			chance = 2;
		} else if (population > minPopulation) {
			chance = 1;
		}
		System.out.println("TL chance is "+chance);
		if (Die.die(1000) <= chance) {
			tl+=1;
			planet.setTechLevel(tl);
		}
	}

	/**
	 * For a world with a colony, see if it wants to colonise other worlds.
	 * Assumes about a year of growth.
	 */
	public void grow() {
		long		population = planet.getPopulation();
		double		density = getPopulationDensity();
		int		    tl = planet.getTechLevel();
		
		if (population == 0) {
			// If there's nobody here, then there's nothing to do.
			return;
		}
		
		double growth = 1.00;
		if (density > 10) {
			growth = 1000 + Die.d10(1) - Die.d10(2);
			if (Die.d100() == 1) {
				// Something nasty happens.
				tl-=3;
				planet.setTechLevel(tl);
				growth = 800;
			}
		} else if (density > 5) {
			growth = 1000 + Die.d10(1) - Die.d10(2);
			if (Die.d100() == 1) {
				// Something nasty happens.
				tl -= 1;
				planet.setTechLevel(tl);
				growth = 900;
			}
		} else if (density > 2) {
			growth = 1000 + Die.d10(2) - Die.d10(2);
		} else if (density > 1.5) {
			growth = 1000 + Die.d10(3) - Die.d10(2);
		} else if (density > 1.2) {
			growth = 1000 + Die.d10(4) - Die.d10(2);
		} else if (density > 0.8) {
			growth = 1000 + Die.d10(5) - Die.d10(2);
		} else if (density > 0.5) {
			growth = 1000 + Die.d10(6) - Die.d10(2);
		} else if (density > 0.2) {
			growth = 1000 + Die.d10(7) - Die.d10(2);
		} else if (density > 0.1) {
			growth = 1000 + Die.d10(8) - Die.d10(2);
		} else {
			growth = 1000 + Die.d10(10) - Die.d10(2);
		}		
		
		// Change the population.
		population *= (growth / 1000.0);
		planet.setPopulation(population);
		
		growEcology();
		growTechnology();
		
		planet.persist();
		
	}
	
	/**
	 * Try and start a colony on this world. First check to see how suitable it is,
	 * then try to colonise it if possible.
	 */
	public void colonise(int tl) {
		int		suitable = getSuitability();
		long	population = (int)Math.pow(suitable, 2.0) + planet.getPopulation();
		
		planet.setPopulation(population);
		planet.setTechLevel(tl);
		planet.persist();
	}
	
	private int getSuitability() {
		if (!planet.getType().isTerrestrial()) {
			return 0;
		}
		
		int			suitable = 100;
		
		suitable *= planet.getAtmosphereType().getSuitability();
		suitable *= planet.getAtmospherePressure().getSuitability();
		suitable *= planet.getTemperature().getSuitability();
		
		if (planet.getHydrographics() < 25) {
			suitable *= (0.5 + (planet.getHydrographics() * 0.02));
		}
		
		if (planet.getRadius() < 5000) {
			suitable *= Math.pow(planet.getRadius()/5000.0, 3.0);
		} else if (planet.getRadius() > 7500) {
			suitable *= Math.pow(7500.0/planet.getRadius(), 3.0);
		}
		
		return suitable;
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		Colony		c = new Colony(new Planet(factory, 16091));
		//c.colonise();
		for (int i=0; i<100; i++) {
			c.grow();
		}
		
		/*
		for (int i=1; i < 100; i++) {
			Colony		c = new Colony(new Planet(i));
			c.colonise();
		}
		*/
	}
}
