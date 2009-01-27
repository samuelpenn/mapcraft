/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

import java.text.NumberFormat;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;

/**
 * Calculates trade requirements for a planet. Only applies to worlds with
 * a population, since unpopulated worlds neither gather resources, or have
 * a need to consume them.
 * 
 * There are two main factors in operation:
 *   Production: How much of a commodity is produced each week.
 *   Consumption: How much of a commodity is used each week.
 * 
 * Every planet has:
 *   Resources: What can be naturally gathered. Normally this is minerals
 *              and basic food.
 *   Commodities: Everything, including resources. A commodity can be
 *                traded, produced or consumed.
 *   Demand: How much of a commodity is required for normal operation.
 *   Production Capacity: How much effort can be put into production.
 * 
 * @author Samuel Penn
 */
public class Trade {
	private ObjectFactory		factory = null;
	private Planet				planet = null;
	private Hashtable<Integer,Commodity>	commodities = null;
	private Hashtable<Integer,Integer>		resources = null;
	
	private static NumberFormat		format = NumberFormat.getInstance();
	
	public Trade(ObjectFactory factory, Planet planet) {
		this.factory = factory;
		this.planet = planet;
		
		commodities = factory.getAllCommodities();
		resources = factory.getResources(planet.getId());
		
		Hashtable<Integer,TradeGood>	amounts = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int key : amounts.keySet()) {
			TradeGood	good = amounts.get(key);
			
			Commodity	c = commodities.get(key);
			c.setAmount(good.amount);
			c.setActualPrice(good.price);
		}
	}
		
	public Planet getPlanet() {
		return planet;
	}
	
	/**
	 * The production capacity of a planet is roughly proportional to
	 * the square root of its population. Bigger populations tend to
	 * have more people invold in service industries. The TL and
	 * Government Type also affect things.
	 */
	public long getProductionCapacity() {
		long		effectivePopulation = planet.getPopulation();
		long		production = 0;
		
		if (effectivePopulation == 0) {
			return 0;
		}
		
		// PC doubles every TL over 6, it is reduced for TLs below 6.
		if (planet.getTechLevel() < 4) {
			effectivePopulation /= 100;
		} else if (planet.getTechLevel() == 4) {
			effectivePopulation /= 10;
		} else if (planet.getTechLevel() == 5) {
			effectivePopulation /= 2;
		} else if (planet.getTechLevel() > 6) {
			effectivePopulation *= Math.pow(2, planet.getTechLevel()-6);
		}
		
		// The government can effect production, normally badly.
		switch (planet.getGovernment()) {
		case Anarchy:
			effectivePopulation /= 100;
			break;
		case Balkanization:
		case Captive:
		case FeudalTechnocracy:
			effectivePopulation /= 10;
			break;
		case TheocraticDictatorship:
		case TheocraticOligarchy:
		case TotalitarianOligarchy:
			effectivePopulation /= 5;
			break;
		case CivilService:
		case ImpersonalBureaucracy:
			effectivePopulation /= 3;
			break;
		case Corporation:
		case NonCharismaticLeader:
			effectivePopulation *= 2;
			break;
		}
		
		// Strict laws can reduce production.
		if (planet.getLawLevel() > 1) {
			effectivePopulation /= planet.getLawLevel();
		}
		
		// Finally, poor planetary conditions make life hard.
		switch (planet.getLifeLevel()) {
		case None:
			effectivePopulation /= 10;
			break;
		case Proteins:
		case Protozoa:
		case Metazoa:
			effectivePopulation /= 5;
			break;
		case ComplexOcean:
			effectivePopulation /= 4;
			break;
		case SimpleLand:
			effectivePopulation /= 3;
			break;
		case ComplexLand:
			effectivePopulation /= 2;
			break;
		}
		
		production = (int)Math.sqrt(effectivePopulation);
		if (production < 1) {
			production = 1;
		}
		
		return production;
	}
	
	private long getWorkersRequired(Commodity c) {
		return getWorkersRequired(c, 100);
	}
	
	private long getWorkersRequired(Commodity c, int density) {
		if (density < 1) {
			return Long.MAX_VALUE;
		} else if (density > 100) {
			density = 100;
		}
		
		long		workersRequired = c.getProductionRate() * 10000 / (int)Math.pow(density, 2);
		
		if (planet.getTechLevel() < c.getTechLevel()-1) {
			// Tech level is way too low, return infinite number of workers.
			return Long.MAX_VALUE;
		} else if (planet.getTechLevel() == c.getTechLevel()-1) {
			// May be able to make something, unless commodity is heavily TL dependant.
			if (c.hasCode(CommodityCode.Tl)) {
				return Long.MAX_VALUE;
			}
			workersRequired *= 5;
		} else if (planet.getTechLevel() > c.getTechLevel()) {
			if (c.hasCode(CommodityCode.Tl)) {
				workersRequired /= (1 + planet.getTechLevel() - c.getTechLevel());
			} else {
				workersRequired /= Math.sqrt(1 + planet.getTechLevel() - c.getTechLevel());
			}
		}
		
		if (c.getSource() == Source.Ag) {
			if (planet.hasTradeCode(TradeCode.Ag)) {
				workersRequired /= 2;
			} else if (planet.hasTradeCode(TradeCode.Na)) {
				workersRequired *= 3;
			}
		} else if (c.getSource() == Source.In) {
			if (planet.hasTradeCode(TradeCode.In)) {
				workersRequired /= 2;
			} else if (planet.hasTradeCode(TradeCode.Ni)) {
				workersRequired *= 10;
			} else {
				workersRequired *= 3;
			}
		} else if (c.getSource() == Source.Mi) {
			if (planet.hasTradeCode(TradeCode.Mi)) {
				// This is assumed to be a dedicated mining colony, where
				// everyone is focused on mining, so less support people
				// are needed.
				workersRequired /= 100;
			} else if (planet.hasTradeCode(TradeCode.In)) {
				// No change.
			} else if (planet.hasTradeCode(TradeCode.Ni)) {
				workersRequired *= 4;
			} else if (planet.hasTradeCode(TradeCode.Ag)) {
				workersRequired *= 2;
			}
		}
		
		if (planet.getLawLevel() > c.getLegality()) {
			workersRequired *= Math.pow(10, planet.getLawLevel() - c.getLegality());
		}
		
		return workersRequired;
	}
	
	/**
	 * Get the amount of local demand for the given commodity. Demand is
	 * how many units of the commodity will be 'consumed' each week.
	 * 
	 * @param c		Commodity to calculate demand for.
	 * 
	 * @return		Demand, as number of units to consume per week.
	 */
	public long getLocalDemand(Commodity c) {
		long		consumersAvailable = planet.getPopulation() / c.getConsumptionRate();

		// If the commodity has tech level restrictions, then reduce demand
		// based on how far out this planet is, if required. Note that a
		// commodity can apply to one or more tech ranges, or all of them if
		// none is specified.
		if (c.hasCode(CommodityCode.Lt) || c.hasCode(CommodityCode.Mt) || c.hasCode(CommodityCode.Ht) || c.hasCode(CommodityCode.Ut)) {
			int		min = 15, max = 0;
			if (c.hasCode(CommodityCode.Lt)) {
				min = 1; max = 5;
			}
			if (c.hasCode(CommodityCode.Mt)) {
				min = Math.min(min, 6);
				max = Math.max(max, 8);
			}
			if (c.hasCode(CommodityCode.Ht)) {
				min = Math.min(min, 8);
				max = Math.max(max, 10);
			}
			if (c.hasCode(CommodityCode.Ut)) {
				min = Math.min(min, 10);
				max = 15;
			}
			if (c.getTechLevel() < min) {
				consumersAvailable /= 10^(min-c.getTechLevel());
			}
			if (c.getTechLevel() > max) {
				consumersAvailable /= 10^(min-c.getTechLevel());
			}
		}

		if (c.hasCode(CommodityCode.Ag)) {
			// Commodity is of use to agricultural worlds.
			if (planet.hasTradeCode(TradeCode.Na)) {
				consumersAvailable /= 100;
			} else if (!planet.hasTradeCode(TradeCode.Ag)) {
				consumersAvailable /= 10;
			} 
		}
		if (c.hasCode(CommodityCode.In)) {
			// Commodity is of use to industrial worlds.
			if (planet.hasTradeCode(TradeCode.Ni)) {
				consumersAvailable /= 100;
			} else if (!planet.hasTradeCode(TradeCode.In)) {
				consumersAvailable /= 10;
			} 
		}
		if (c.hasCode(CommodityCode.Mn)) {
			// Specialist mining tools.
			if (!planet.hasTradeCode(TradeCode.Mi)) {
				consumersAvailable /= 1000;
			}
		}
		
		if (planet.getLawLevel() > c.getLegality()) {
			consumersAvailable /= 100^(planet.getLawLevel() - c.getLegality());
		}
		
		return consumersAvailable;
	}
	
	/**
	 * Work out what resources are gathered this week from the planet's
	 * stock of natural resources.
	 */
	public void gatherResources() {
		// For each commodity, work out how much is gathered.
		System.out.println("Production rates");
		for (int i : resources.keySet()) {
			Commodity	c = commodities.get(i); 
			
			if (c == null) {
				c = factory.getCommodity(i);
			}
			
			int			density = resources.get(i);
			long		workersRequired = getWorkersRequired(c, density);
			long		produced = planet.getPopulation() / workersRequired;
			
			long		amount = c.getAmount();
			if (c.hasCode(CommodityCode.Pe)) {
				amount *= 0.5;
			} else {
				amount *= 0.95;
			}
			c.setAmount(amount + produced);
			System.out.println("  "+c.getName() + "("+c.getAmount()+") - "+produced);
			factory.setCommodity(planet.getId(), c.getId(), c.getAmount(), c.getActualPrice());
		}
	}
	
	public long getProductionRate(Commodity c) {
		return planet.getPopulation() / getWorkersRequired(c, resources.get(c.getId()));
	}
	

	
	public void consumeResources() {
		Hashtable<Integer,Integer> list = factory.getResources(planet.getId());

		System.out.println("Consumption rates");
		for (int i : list.keySet()) {
			Commodity	c = commodities.get(i);
			
			System.out.println("  "+c.getName()+" ("+c.getAmount()+" @ "+c.getCost()+"Cr)");
			
			if (c == null) {
				c = factory.getCommodity(i);
			}
			long		demand = getLocalDemand(c);			
			long		amount = c.getAmount();
			
			int		basePrice = getStandardPrice(c.getId());
			c.setActualPrice(basePrice);
			if (demand > amount) {
				c.setAmount(0);
			} else {
				c.setAmount(amount - demand);
			}
			//System.out.println("  "+c.getName() + "("+c.getAmount()+") - "+demand+" = "+basePrice+"Cr");
			factory.setCommodity(planet.getId(), c.getId(), c.getAmount(), c.getActualPrice());
		}
	}

	public void productionAbility() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			effortRequired = c.getProductionRate();
			
			if (effortRequired == 0 || planet.getPopulation() == 0) {
				System.out.println("  "+c.getName()+" not wanted");
				continue;
			}
			
			if (planet.hasTradeCode(TradeCode.Ri)) {
				effortRequired *= 0.5;
			} else if (planet.hasTradeCode(TradeCode.Po)) {
				effortRequired *= 1.2;
			}
			
			// Tech level requirements
			int techGap = planet.getTechLevel() - c.getTechLevel();
			if (techGap < -1) {
				continue;
			} else if (techGap == -1) {
				effortRequired *= 10;
			} else if (techGap == 0) {
				effortRequired *= 2;
			} else {
				if (c.hasCode(CommodityCode.Tl)) {
					effortRequired /= techGap+1;
				} else if (c.hasCode(CommodityCode.TL)) {
					effortRequired /= ((techGap+1) * (techGap+1));
				}
			}
			
			// Legal requirements
			int legality = planet.getLawLevel() - c.getLegality();
			if (legality < -2) {
				continue;
			} else if (legality == -2) {
				effortRequired *= 100;
			} else if (legality == -1) {
				effortRequired *= 10;
			}
			if (c.hasCode(CommodityCode.Il)) {
				effortRequired *= 10;
			}
			
			if (c.getSource() == Source.Ag) {
				if (planet.hasTradeCode(TradeCode.Na)) {
					effortRequired *= 25;
				} else if (!planet.hasTradeCode(TradeCode.Ag)) {
					effortRequired *= 5;
				}
			}
			if (c.getSource() == Source.In) {
				if (planet.hasTradeCode(TradeCode.Ni)) {
					effortRequired *= 100;
				} else if (!planet.hasTradeCode(TradeCode.In)) {
					effortRequired *= 10;
				}
			}
			if (c.getSource() == Source.Mi) {
				if (planet.hasTradeCode(TradeCode.Ag)) {
					effortRequired *= 3;
				}
				if (planet.hasTradeCode(TradeCode.Ni)) {
					effortRequired *= 3;
				}
			}
			
			long		production = planet.getPopulation() / effortRequired;
			c.setProduction(production);
			//System.out.println("  "+c.getName()+" : "+format.format(production));
		}
	}
	
	public void demand() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			demand = c.getConsumptionRate();
			
			if (demand == 0 || planet.getPopulation() == 0) {
				System.out.println("  "+c.getName()+" not wanted");
				continue;
			}
			
			if (planet.hasTradeCode(TradeCode.Ri)) {
				if (c.hasCode(CommodityCode.Lu)) {
					demand *= 0.9;
				}
			} else if (planet.hasTradeCode(TradeCode.Po)) {
				if (c.hasCode(CommodityCode.Lu)) {
					demand *= 5;
				}
			}
			
			// Tech level requirements
			if (c.hasCode(CommodityCode.Lt)) {
				// Ultra-tech
				if (planet.getTechLevel() > 6) {
					demand *= Math.pow(10, (planet.getTechLevel()-6));
				}
			} else if (c.hasCode(CommodityCode.Mt)) {
				if (planet.getTechLevel() < 4) {
					demand = 0;
				} else if (planet.getTechLevel() < 6) {
					demand *= 100;
				} else if (planet.getTechLevel() == 6) {
					demand *= 3;
				} else if (planet.getTechLevel() == 9) {
					demand *= 3;
				} else if (planet.getTechLevel() > 9) {
					demand *= 10;
				}
			} else if (c.hasCode(CommodityCode.Ht)) {
				if (planet.getTechLevel() < 7) {
					demand = 0;
				} else if (planet.getTechLevel() == 7) {
					demand *= 30;
				} else if (planet.getTechLevel() == 8) {
					demand *= 3;
				} else if (planet.getTechLevel() == 11) {
					demand *= 3;
				} else if (planet.getTechLevel() > 11) {
					demand *= 10;
				}
			} else if (c.hasCode(CommodityCode.Ut)) {
				if (planet.getTechLevel() < 10) {
					demand = 0;
				} else if (planet.getTechLevel() == 10) {
					demand *= 10;
				}
			}
			
			if (c.hasCode(CommodityCode.In)) {
				if (planet.hasTradeCode(TradeCode.Ni)) {
					demand *= 1000;
				} else if (!planet.hasTradeCode(TradeCode.In)) {
					demand *= 10;
				}
			}
			if (c.hasCode(CommodityCode.Ag)) {
				if (planet.hasTradeCode(TradeCode.Na)) {
					demand *= 1000;
				} else if (!planet.hasTradeCode(TradeCode.Ag)) {
					demand *= 10;
				}
			}
			
			long		desired = 0;
			if (demand > 0) desired = planet.getPopulation() / demand;
			c.setDesired(desired);
			//System.out.println("  "+c.getName()+" : "+format.format(desired));
		}
	}
	
	public void results() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			desired = c.getDesired();
			long			production = c.getProduction();
			
			System.out.println("  "+c.getName()+" : "+format.format(production - desired));
		}
	}
	
	/**
	 * Get the price a commodity is worth on this world.
	 *  
	 * @param commodityId		Commodity to calculate worth of.
	 */
	public int getStandardPrice(int commodityId) {
		int								price = 0;
		Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());
		Commodity						c = factory.getCommodity(commodityId);
		long							amount = 0;

		// If the planet has any already in stock, find the amount available.
		for (int i : goods.keySet()) {
			if (goods.get(i).getCommodityId() == commodityId) {
				amount = goods.get(i).getAmount();
				break;
			}
		}
		
		// Get the level of demand for the item.
		long		demand = getLocalDemand(c);
		System.out.println("    Demand: "+demand+"/"+amount);

		// Work out prices.
		price = c.getCost();
		if (amount > 0) {
			double var = Math.pow(1.0*demand/amount, 1.0/3.0);
			if (var > 5) var = 5;
			if (var < 0.2) var = 0.2;
			
			price *= var;
		} else if (demand > 10) {
			System.out.println("    "+Math.log10(demand));
			price *= Math.log10(demand);
		} else if (demand > 0) {
			// Just use the base price.
		} else {
			// Nobody wants it, so can't be sold for very much.
			price /= 10;
		}
		System.out.println("    Price: "+price);
		
		return price;
	}
	
	public int getPricePlanetSellsAt(int commodityId) {
		return getStandardPrice(commodityId);
	}

	public int getPricePlanetBuysAt(int commodityId) {
		return (int)(getStandardPrice(commodityId) * 1.1);
	}
	
	public int sellToPlanet(int commodityId, int amount) {
		int		price = 0;
		
		return price;
	}

	public int buyFromPlanet(int commodityId, int amount) {
		int		price = 0;
		return price;
	}
	
	private void foodRequirements() {
		// Basic amount of required food equal to population.
		long		demand = planet.getPopulation();

		// VeryCold climate requires more food.
		if (planet.getTemperature().isColderThan(Temperature.Cold)) {
			demand *= 1.1;
		}
		System.out.println("  foodRequirements: "+format.format(demand));
		
		// Work out what the different food types are.
		Vector<Commodity>	vitalFoods = new Vector<Commodity>();
		Vector<Commodity>	standardFoods = new Vector<Commodity>();
		Vector<Commodity>	luxuryFoods = new Vector<Commodity>();
		Vector<Commodity>	poorFoods = new Vector<Commodity>();
		for (Commodity c : commodities.values()) {
			if (c.hasCode(CommodityCode.Fo)) {
				long		amount = c.getAmount();
				long		resource = resources.contains(c.getId())?resources.get(c.getId()):0;
				
				System.out.println("    "+c.getName()+" ("+format.format(amount)+"/"+resource+") @ "+c.getCost()+"Cr");
				if (c.hasCode(CommodityCode.Vi)) {
					vitalFoods.add(c);
				} else if (c.hasCode(CommodityCode.Lq)) {
					poorFoods.add(c);
				} else if (c.hasCode(CommodityCode.Lu)) {
					luxuryFoods.add(c);
				} else {
					standardFoods.add(c);
				}
			}
		}
		// The split of demand is 70% vital foods, 21% standard and 9% luxury.
		// Poor foods will only be eaten if nothing else is available.
		boolean		noMoreFood = true;
		while (demand > 0) {
			long		vitalDemand = (demand > 10)?(long)(demand * 0.7):demand;
			long		each = vitalDemand / vitalFoods.size();
			
			if (noMoreFood) {
				break;
			}
		}
		
	}
	
	/**
	 * Try and work out what a planet needs.
	 */
	private void calculatePlanetRequirements() {
		System.out.println("calculatePlanetRequirements: ["+planet.getName()+"]");
		foodRequirements();
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		try {
			//Vector<StarSystem> list = factory.getStarSystemsBySector(103);
			
			//for (int s=0; s < list.size(); s++) {
				StarSystem	system = factory.getStarSystem(14062);
				Planet		planet = system.getMainWorld();
				
				Trade		trade = new Trade(factory, planet);
				trade.calculatePlanetRequirements();
				//trade.gatherResources();
				//trade.consumeResources();
			//}
			/*
			for (int id : new int[] { 212031, 212304} ) {
				Planet			planet = factory.getPlanet(id);
				Trade			trade = new Trade(factory, planet);

				System.out.print(planet.getName()+" ("+planet.getType()+")  "+NumberFormat.getInstance().format(planet.getPopulation())+" - "+planet.getTechLevel()+"/"+planet.getLawLevel()+" [");
				String	codes = "";
				for (String code : planet.getTradeCodes()) {
					codes += code+" ";
				}
				System.out.println(codes.trim()+"]");
				trade.productionAbility();
				trade.demand();
				
				trade.results();
			}
			
			Sector sector = new Sector(factory, 104);
			System.out.println(sector.getBTN(factory.getPlanet(212031), factory.getPlanet(212304)));
			*/
		} finally {
			factory.close();
			factory = null;
		}
	}
}
