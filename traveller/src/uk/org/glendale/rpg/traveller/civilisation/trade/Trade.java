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
import java.util.logging.Logger;

import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;

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
	private Logger				logger = Logger.getLogger(Trade.class.toString());

	private ObjectFactory		factory = null;
	private Planet				planet = null;
	private Hashtable<Integer,Commodity>	commodities = null;
	private Hashtable<Integer,Integer>		resources = null;
	
	private static NumberFormat		format = NumberFormat.getInstance();
	
	private long				productionCapacity = 0;
	
	public Trade(ObjectFactory factory, Planet planet) {
		this.factory = factory;
		this.planet = planet;
		
		commodities = factory.getAllCommodities();
		resources = factory.getResources(planet.getId());
		
		Hashtable<Integer,TradeGood>	amounts = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int key : amounts.keySet()) {
			TradeGood	good = amounts.get(key);
			
			Commodity	c = commodities.get(key);
			if (c != null) {
				c.setAmount(good.amount);
				c.setActualPrice(good.price);
			}
		}
		
		productionCapacity = getProductionCapacity();
	}
		
	public Planet getPlanet() {
		return planet;
	}
	
	/**
	 * The production capacity of a planet is roughly proportional to
	 * the square root of its population. Bigger populations tend to
	 * have more people involved in service industries. The TL and
	 * Government Type also affect things.
	 */
	public long getProductionCapacity() {
		long		effectivePopulation = planet.getPopulation();
		long		production = 0;
		
		if (effectivePopulation == 0) {
			return 0;
		}
		// Base production is based on the square root of the population.
		production = (int)Math.sqrt(effectivePopulation);
		
		if (planet.hasTradeCode(TradeCode.Ag) || planet.hasTradeCode(TradeCode.In)) {
			// Worlds listed as agricultural or industrial have more resources
			// aimed towards these pursuits.
			production *= 2;
		} else if (planet.hasTradeCode(TradeCode.Na) || planet.hasTradeCode(TradeCode.Ni)) {
			production *= 0.75;
		}
		
		// TL 5 is the industrial revolution, so there's a marked
		// difference immediately before and after it.
		if (planet.getTechLevel() < 5) {
			// Halve at 4, third at 3, quarter at 2 etc.
			production /= (6-planet.getTechLevel());
		} else if (planet.getTechLevel() > 5) {
			// Double at 6, triple at 7, quadruple at 8 etc.
			production *= (planet.getTechLevel()-4);
		}
		
		// The government can effect production, normally badly.
		switch (planet.getGovernment()) {
		case Anarchy:
			production *= 0.1;
			break;
		case Balkanization:
		case Captive:
		case FeudalTechnocracy:
			production *= 0.5;
			break;
		case TheocraticDictatorship:
		case TheocraticOligarchy:
			production *= 0.75;
			break;
		case CivilService:
		case ImpersonalBureaucracy:
			production *= 0.8;
			break;
		case Corporation:
		case NonCharismaticLeader:
		case TotalitarianOligarchy:
			production *= 1.5;
			break;
		}
		
		// Strict laws can reduce production.
		switch (planet.getLawLevel()) {
		case 0:
			production *= 0.9;
			break;
		case 1: case 2: case 3:
			// No effect.
			break;
		case 4:
			production *= 0.9;
			break;
		case 5:
			production *= 0.75;
			break;
		case 6:
			production *= 0.5;
			break;
		}
		
		// Finally, poor planetary conditions make life hard.
		switch (planet.getLifeLevel()) {
		case None:
			production *= 0.1;
			break;
		case Proteins:
		case Protozoa:
		case Metazoa:
			production *= 0.25;
			break;
		case ComplexOcean:
			production *= 0.5;
			break;
		case SimpleLand:
			production *= 0.7;
			break;
		case ComplexLand:
			production *= 0.85;
			break;
		}
		
		if (production < 1) {
			production = 1;
		}
		
		return production;
	}
	
	public long getUsedProductionCapacity() {
		long		used = 0;
		
		for (int f: planet.getFacilities().keySet()) {
			used += planet.getFacilities().get(f);
		}
		return used;
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
	 * Manage the output of a generator facility such as agriculture
	 * or mining.
	 * 
	 * @param facility	Facility to manage.
	 * @param size		Size of this facility on this world.
	 */
	private void manageGenerator(Facility facility, long size) {
		for (int r: facility.inputMap.keySet()) {
			long		produce = facility.inputMap.get(r) * size;
			System.out.println("Produce "+produce+" of resource "+commodities.get(r).getName());
			
			// For each commodity, work out how much is produced.
			for (int cId : facility.outputMap.keySet()) {
				long		amount = facility.outputMap.get(cId) * size;
				Commodity	c = commodities.get(cId);
				
				amount = c.getAmountModifiedByTech(planet.getTechLevel(), amount);
				
				factory.addCommodity(planet.getId(), cId, (int)amount, c.getUnitCost());
			}
		}
	}
	
	/**
	 * Top level method for managing all forms of production and consumption.
	 * Everything is performed in the following order:
	 *   Agriculture and Mining
	 *   Factories
	 *   Residential
	 */
	private void manageEconomy() {
		Hashtable<Integer,Facility>	facilities = factory.getFacilities();
		Hashtable<Integer,Long>		planetFacilities = planet.getFacilities();
		
		// First, look for agricultural and mining facilities.
		for (int facilityId : planetFacilities.keySet()) {
			Facility	f = facilities.get(facilityId);
			
			if (f.getType() == FacilityType.Agriculture) {
				manageGenerator(f, planetFacilities.get(facilityId));
			} else if (f.getType() == FacilityType.Mining) {
				manageGenerator(f, planetFacilities.get(facilityId));
			}
		}
	}
	
	/**
	 * Work out what resource gathering facilities produce this week.
	 * We only calculate facilities gathering natural resources.
	 */
	public void gatherResources() {
		Hashtable<Integer,Long>		planetFacilities = planet.getFacilities();
		Hashtable<Integer,Facility>	facilities = factory.getFacilities();
		
		System.out.println("Production rates");
		for (int i : planetFacilities.keySet()) {
			Facility	facility = facilities.get(i);
			long		size = planetFacilities.get(facility.getId());
			int			resourceId = facility.getResourceId();
			
			if (resourceId == 0) {
				// Only handle those facilities which consume natural resources.
				continue;
			}
			
			for (int r: facility.inputMap.keySet()) {
				long		produce = facility.inputMap.get(r) * size;
				System.out.println("Produce "+produce+" of resource "+commodities.get(r).getName());
				
				// For each commodity, work out how much is produced.
				for (int cId : facility.outputMap.keySet()) {
					long		amount = facility.outputMap.get(cId) * size;
					Commodity	c = commodities.get(cId);
					int			tl = planet.getTechLevel() - c.getTechLevel();
					
					amount = c.getAmountModifiedByTech(planet.getTechLevel(), amount);
					
					factory.addCommodity(planet.getId(), cId, (int)amount, c.getUnitCost());
				}
			}
		}
	}
	
	public long getProductionRate(Commodity c) {
		if (c != null && resources != null) {
			return planet.getPopulation() / getWorkersRequired(c, resources.get(c.getId()));
		}
		return 0;
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

		if (c == null) {
			return 0;
		}
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
	
	private String n(long number) {
		return format.format(number);
	}

	private long eatFood(Hashtable<Integer,TradeGood> goods, Vector<Commodity> list, long demand, double usage) {
		if (list.size() > 0) {
			long		vitalDemand = (demand > 10)?(long)(demand * usage):demand;
			long		each = vitalDemand / list.size();
			
			for (Commodity c: list) {
				TradeGood	good = goods.get(c.getId());
				if (good == null) continue;
				
				// How much demand does each dt satisfy?
				long	rate = c.getConsumptionRate();
				long	consume = Math.max(1, each/rate);
				if (consume <= good.getAmount()) {
					System.out.println("Eaten "+n(consume)+"dt of "+c.getName());
					demand -= consume * rate;
					good.setAmount(good.getAmount() - consume);
				} else {
					System.out.println("Eaten "+n(good.getAmount())+"dt of "+c.getName());
					demand -= good.getAmount() * rate;
					good.setAmount(0);
				}
				factory.setCommodity(planet.getId(), good.getCommodityId(), good.getAmount(), good.getPrice());
			}
			for (int i=0; i < list.size(); i++) {
				if (goods.get(list.elementAt(i).getId()).getAmount() == 0) {
					list.remove(i);
					i=0;
				}
			}
		}
		
		return demand;
	}
	
	/**
	 * Work out how much food a planet needs. Final requirement is in dt,
	 * and assume about 1dt = 200 people per week.
	 */
	private void foodRequirements() {
		// Basic amount of required food based on population.
		long		demand = Math.max(1, planet.getPopulation());
		
		// Rich worlds eat more, poor worlds eat less.
		if (planet.hasTradeCode(TradeCode.Ri)) {
			demand *= 1.5;
		} else if (planet.hasTradeCode(TradeCode.Po)) {
			demand *= 0.67;
		}

		// VeryCold climate requires more food.
		if (planet.getTemperature().isColderThan(Temperature.Cold)) {
			demand *= 1.1;
		}
		System.out.println("  foodRequirements: "+n(demand));
		
		// Work out what the different food types are.
		Vector<Commodity>	vitalFoods = new Vector<Commodity>();
		Vector<Commodity>	standardFoods = new Vector<Commodity>();
		Vector<Commodity>	luxuryFoods = new Vector<Commodity>();
		Vector<Commodity>	poorFoods = new Vector<Commodity>();
		
		Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());
		for (TradeGood good : goods.values()) {
			Commodity c = commodities.get(good.getCommodityId());
			if (c.hasCode(CommodityCode.Fo)) {
				long		amount = good.getAmount();
				
				System.out.println("    "+c.getName()+" ("+n(amount)+") @ "+c.getCost()+"Cr");
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
			System.out.println("Demand is "+n(demand)+"dt");
			demand = eatFood(goods, vitalFoods, demand, 0.70);
			demand = eatFood(goods, standardFoods, demand, 0.21);
			demand = eatFood(goods, luxuryFoods, demand, 0.09);
			
			if (vitalFoods.size() + standardFoods.size() + luxuryFoods.size() == 0) {
				break;
			}
		}
		
		if (demand > 0) {
			System.out.println("  Hungry: "+n(demand));
			// Oops, planet is starving. Can we add agriculture?
			long		used = getUsedProductionCapacity();
			if (used < productionCapacity) {
				long		canUse = productionCapacity - used;
				System.out.println("  Remaining capacity: "+n(canUse));
				if (planet.hasTradeCode(TradeCode.Ag)) {
					canUse *= 0.25;
				} else if (planet.hasTradeCode(TradeCode.Na)) {
					canUse *= 0.05;
				} else {
					canUse *= 0.1;
				}
			}
		}
		
	}
	
	private void livingRequirements() {
		long		population = planet.getPopulation();
		
		long		livingSpace = 0;
		
		//Vector<Facility>		f = planet.getFacilities();
	}
	
	/**
	 * Try and work out what a planet needs. If supply doesn't match demand,
	 * then try to expand facilities to make up the short fall. Facilities
	 * expansion is considered 'instantaneous'. In reality, it is assumed to
	 * have happened a long time ago, so expansion should only happen when
	 * the simulation is first run.
	 */
	private void calculatePlanetRequirements() {
		System.out.println("calculatePlanetRequirements: ["+planet.getName()+"]");
		livingRequirements();
		foodRequirements();
	}
	
	/**
	 * Find the best possible facility for the given resource. Best is
	 * considered to be the highest tech level that can be supported
	 * by the planet.
	 * 
	 * @param list			List of facilities to choose from.
	 * @param resourceId	Resource id that is to be matched.
	 * @return				Best facility, or null if none available.
	 */
	private Facility getBestFacilityForResource(Hashtable<Integer,Facility> list, int resourceId) {
		Facility		best = null;
				
		for (Iterator<Facility> i = list.values().iterator(); i.hasNext();) {
			Facility f = i.next();
			
			if (f.getResourceId() == resourceId) {
				if (f.getTechLevel() > planet.getTechLevel()) {
					continue;
				}
				if (best == null) {
					best = f;
				} else if (f.getTechLevel() > best.getTechLevel()) {
					best = f;
				}
			}
		}
		return best;
	}
	
	/**
	 * Create all the required facilities on the planet, based on population,
	 * tech level and resources.
	 */
	public void createFacilities() {
		// Residential.
		Hashtable<Integer,Facility>		facilities = factory.getFacilities();
		Hashtable<Integer,Long>			listOfFacilities = new Hashtable<Integer,Long>();
		
		long		population = planet.getPopulation();
		
		logger.info("Create facilities for ["+planet.getName()+"]; population "+population+"; capacity "+getProductionCapacity());
		
		Facility		residential = null;
		if (planet.getPopulation() > 4000000000L && planet.getTechLevel()>8) {
			residential = Facility.getByName(facilities, "Arcology");
		}
		
		// Arcologies?
		Facility		arcology = Facility.getByName(facilities, "Arcology");
		while (Die.d10() < population/1000000000 && planet.getTechLevel() > 8) {
			long		pop = population / (Die.d4() * 5);
			int			size = (int) (pop / arcology.getCapacity());
			population -= pop;
			//Facility	f = new Facility(arcology, size, 0);
			//list.add(f);
			//logger.info("Added Arcology pop ["+pop+"] size ["+size+"]");
		}

		// Agriculture
		logger.info("AGRICULTURE");
		Hashtable<Integer,Facility>		ag = Facility.getByType(facilities, FacilityType.Agriculture);
		HashSet<Integer>				used = new HashSet<Integer>();
		
		// Go through each of the possible facilities, and look for one
		// which matches the type of resources this planet has. When
		// found, add the facility. These facilities will be farms.
		for (Iterator<Facility> i = ag.values().iterator(); i.hasNext();) {
			Facility f = i.next();
			logger.info(f.getName()+" - "+f.getResourceId());
			
			if (resources.containsKey(f.getResourceId()) && !used.contains(f.getResourceId())) {
				Commodity		resource = commodities.get(f.getResourceId());
				int				density = resources.get(resource.getId());
				if (resource == null) {
					logger.warning("Cannot find resource in commodity list");
					continue;
				}
				
				// Look for the highest TL version of the facility for this resource.
				Facility		facility = getBestFacilityForResource(ag, f.getResourceId());
				if (facility == null) {
					continue;
				}
				used.add(f.getResourceId());
				logger.info("Adding facility for resource ["+resource.getName()+"]/"+resource.getProductionRate());
				
				// Ideally, a farm produces food units each week equal to its
				// capacity. This is modified by the density of the resource.
				// A resource of 100 is ideal.
				long		pr = (long)(resource.getProductionRate() * 10000.0 / density / density);
				if (resource.hasCode(CommodityCode.TL)) {
					pr /= 2 * (planet.getTechLevel() - resource.getTechLevel()) + 1;
				} else if (resource.hasCode(CommodityCode.Tl)) {
					pr /= (planet.getTechLevel() - resource.getTechLevel() + 1);					
				} else {
					pr /= Math.sqrt(planet.getTechLevel() - resource.getTechLevel() + 1);
				}
				long		maxFacilities = planet.getPopulation() / pr;
				long		eachFacilityFeeds = facility.getCapacity() * 300;
				long		neededFacilities = planet.getPopulation() / eachFacilityFeeds;
				
				logger.info("Maximum facilities: "+maxFacilities+" Needs "+neededFacilities);
				
				if (planet.hasTradeCode(TradeCode.Ag)) {
					neededFacilities = (long)Math.min(neededFacilities*1.25, productionCapacity*0.75);
				} else if (planet.hasTradeCode(TradeCode.Na)) {
					neededFacilities = (long)Math.min(neededFacilities*0.75, productionCapacity*0.3);
				} else {
					neededFacilities = (long)Math.min(neededFacilities, productionCapacity*0.5);
				}
				listOfFacilities.put(facility.getId(), neededFacilities);
			}
		}
		factory.setFacilitiesForPlanet(planet.getId(), listOfFacilities);
	}
	
	public static void main(String[] args) throws Exception {
		
		ObjectFactory	factory = new ObjectFactory();
		try {
			//Vector<StarSystem> list = factory.getStarSystemsBySector(103);
			
			//for (int s=0; s < list.size(); s++) {
				//StarSystem	system = factory.getStarSystem(14062);
				//Planet		planet = system.getMainWorld();
				
				Planet		planet = factory.getPlanet(223065);
				
				Trade		trade = new Trade(factory, planet);
				//trade.createFacilities();
				trade.manageEconomy();
				//trade.gatherResources();
				//trade.calculatePlanetRequirements();
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
