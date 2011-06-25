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
import uk.org.glendale.rpg.traveller.database.Constants;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.worldgen.civ.commodity.CommodityCode;
import uk.org.glendale.worldgen.civ.commodity.Source;
import uk.org.glendale.worldgen.civ.facility.FacilityType;

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
 * The method version tags are quite important, since the trade system
 * has got rather confusing and has been rewritten completely several times.
 * Anything prior to version 0.3 (i.e., unversioned) is obselete, though
 * may still be being used by something.
 * 
 * Version 0.3 added in facilities and worked a lot better.
 * 
 * Version 0.4 is a refactoring of the 0.3 code after figuring out how
 * facilities should really work. Also adds better stat tracking for
 * trade figures.
 * 
 * @author Samuel Penn
 */
public class Trade {
	private Logger				logger = Logger.getLogger(Trade.class.toString());

	private ObjectFactory		factory = null;
	private Planet				planet = null;
	
	// This is the complete list of all commodities.
	private Hashtable<Integer,Commodity>	commodities = null;
	// These are all the resources on the planet.
	private Hashtable<Integer,Integer>		resources = null;
	// These are all the trade goods on the planet.
	private Hashtable<Integer,TradeGood>	goods = null;
		
	private static NumberFormat		format = NumberFormat.getInstance();
	
	public Trade(ObjectFactory factory, Planet planet) {
		this.factory = factory;
		this.planet = planet;
		
		commodities = factory.getAllCommodities();
		resources = factory.getResources(planet.getId());
		goods = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int key : goods.keySet()) {
			TradeGood	good = goods.get(key);
			
			Commodity	c = commodities.get(key);
			if (c != null) {
				c.setAmount(good.amount);
				c.setActualPrice(good.price);
			}
		}
		
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
		case Organic:
		case Archaean:
		case Aerobic:
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
		if (c.hasCode(CommodityCode.Tp) || c.hasCode(CommodityCode.Tm) || c.hasCode(CommodityCode.Ti) || c.hasCode(CommodityCode.Tt) || c.hasCode(CommodityCode.Ta) || c.hasCode(CommodityCode.Tu)) {
			int		min = 15, max = 0;
			if (c.hasCode(CommodityCode.Tp)) {
				min = 0; max = 1;
			}
			if (c.hasCode(CommodityCode.Tm)) {
				min = Math.min(min, 2);
				max = Math.max(max, 4);
			}
			if (c.hasCode(CommodityCode.Ti)) {
				min = Math.min(min, 5);
				max = Math.max(max, 6);
			}
			if (c.hasCode(CommodityCode.Tt)) {
				min = Math.min(min, 7);
				max = Math.max(max, 8);
			}
			if (c.hasCode(CommodityCode.Ta)) {
				min = Math.min(min, 9);
				max = Math.max(max, 10);
			}
			if (c.hasCode(CommodityCode.Tu)) {
				min = Math.min(min, 11);
				max = Math.max(max, 15);
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
		if (c.hasCode(CommodityCode.Mi)) {
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
	 * Is the potential parent commodity actually the parent of
	 * the second commodity?
	 * @param parentId
	 * @param commodityId
	 * @return
	 */
	private boolean isParentOf(int parentId, int commodityId) {
		if (parentId == 0 || commodityId == 0) return false;
		if (parentId == commodityId) return true;
		
		Commodity	c = commodities.get(commodityId);
		if (c != null) return isParentOf(parentId, c.getParentId());
		
		return false;
	}
	
	private long getProduction(Facility facility, int facilitySize, Commodity commodity, int density, Hashtable<Integer,TradeGood> goods) {
		long	productionRate = planet.getPopulation() / commodity.getProductionRate();
		System.out.println("         Base production "+productionRate);
		productionRate = (productionRate * density * density) / 10000;
		productionRate = commodity.getAmountModifiedByTech(planet.getTechLevel(), productionRate);
		productionRate = (productionRate * facilitySize) / 100;
		
		System.out.println("         Produce 1/"+commodity.getProductionRate()+" ("+productionRate+")");
		if (goods.containsKey(commodity.getId())) {
			long	amount = goods.get(commodity.getId()).getAmount();
			System.out.println("         Already have "+goods.get(commodity.getId()).getAmount());
			
			if (amount > productionRate * 5) {
				productionRate = 0;
			} else if (amount > productionRate *2) {
				productionRate *= 0.1;
			} else if (amount > productionRate) {
				productionRate *= 0.5;
			} else if (amount*2 > productionRate) {
				productionRate *= 0.75;
			}
			
			// We have zero of the good, which means it's sold out.
			// Try and make more of it than normal.
			if (amount == 0) {
				if (commodity.getSource() == Source.Ag) {
					if (planet.hasTradeCode(TradeCode.Ag)) {
						productionRate *= 1.25;
					} else if (planet.hasTradeCode(TradeCode.Na)) {
						// Can't do anything.
					} else {
						productionRate *= 1.1;
					}
				} else if (commodity.getSource() == Source.Mi) {
					if (planet.hasTradeCode(TradeCode.Mi)) {
						productionRate *= 1.25;
					} else if (planet.hasTradeCode(TradeCode.In)) {
						productionRate *= 1.15;
					} else if (planet.hasTradeCode(TradeCode.Ni)) {
						// Can't do anything.
					} else {
						productionRate *= 1.1;
					}
				}
			}
		}
		return productionRate;
	}
	
	private void produce(Commodity commodity, long amount) {
		TradeGood	good = goods.get(commodity.getId());
		
		if (good != null) {
			good.addProduced(amount);
		} else {
			good = new TradeGood(commodity);
			good.setProduced(amount);
			goods.put(commodity.getId(), good);
		}
	}
	
	private void consume(Commodity commodity, long amount) {
		TradeGood	good = goods.get(commodity.getId());
		
		if (good != null) {
			good.addConsumed(amount);
		} else {
			good = new TradeGood(commodity);
			good.setConsumed(amount);
			goods.put(commodity.getId(), good);
		}		
	}
	
	/**
	 * Generate trade goods from natural resources. For each resource,
	 * find a facility (if any) which can manage it, and use that to
	 * produce trade goods. The only facility type which processes
	 * raw resources are Mines and Agriculture.
	 * 
	 * @version 0.3
	 */
	private void manageResources() {
		Hashtable<Integer,Facility>		facilities = Constants.getFacilities();
		Hashtable<Integer,Commodity>	commodities = Constants.getCommodities();
		
		Hashtable<Integer,Long>			planetFacilities = planet.getFacilities();
		//Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int rId : resources.keySet()) {
			Commodity	c = commodities.get(rId);
			int			density = resources.get(rId);

			if (c == null) {
				// Until foreign keys are properly being used, it is possible to have
				// illegal goods left over from previous data sets. Just quietly remove.
				factory.setCommodity(planet.getId(), rId, 0, 0, 0);
				continue;
			}
			
			System.out.println("Resource ["+c.getName()+"] density "+density+"%");
			for (int fId : planetFacilities.keySet()) {
				Facility	facility = facilities.get(fId);
				long		facilitySize = planetFacilities.get(fId);

				// We only deal with Agriculture and Mining.
				switch (facility.getType()) {
				case Agriculture:
				case Mining:
					break;
				default:
					continue;
				}

				// Does this facility manage this resource?
				Hashtable<CommodityCode,Double>	productionCodes = facility.getProductionCodes();
				for (CommodityCode code : productionCodes.keySet()) {
					if (c.hasCode(code)) {
						System.out.println("     --> ["+facility.getName()+"]/"+code);
						int		modifiedSize = (int)(facilitySize * productionCodes.get(code));

						long	productionRate = getProduction(facility, modifiedSize, c, density, goods);
						System.out.println("         Production rate "+n(productionRate)+" dt/wk");
						produce(c, productionRate);
					}
				}
/*
				// Any other outputs?
				for (Facility.Mapping map : facility.getOutputs()) {
					if (isParentOf(map.getSourceId(), rId)) {
						if (map.getSecondaryId() == 0) {
							// We just output this resource as per the primary one.
							System.out.println("         Required input ["+map.getSourceId()+"]");
							long	productionRate = getProduction(facility, (int)facilitySize, c, density, goods);
							factory.addCommodity(planet.getId(), c.getId(), productionRate, 0, c.getUnitCost());
						} else if (map.getSecondaryId() > 0) {
							// In this case, we turn the resource into a different type of commodity.
							System.out.println("         Required input ["+map.getSourceId()+"] to ["+map.getSecondaryId()+"]");
							Commodity	sec = commodities.get(map.getSecondaryId());
							if (sec != null) {
								long	productionRate = getProduction(facility, (int)facilitySize, sec, density, goods);
								factory.addCommodity(planet.getId(), sec.getId(), productionRate, 0, sec.getUnitCost());
							}
						}
					}
				}
*/
			}
		}
	}
	
	private void dumpFacilityStats() {
		Hashtable<Integer,Facility>		facilities = Constants.getFacilities();
		Hashtable<Integer,Commodity>	commodities = Constants.getCommodities();
		
		Hashtable<Integer,Long>			planetFacilities = planet.getFacilities();

		for (int fId : planetFacilities.keySet()) {
			Facility	facility = facilities.get(fId);
			long		facilitySize = planetFacilities.get(fId);
			
			System.out.println("["+facility.getName()+"] ("+facility.getType()+") ["+facilitySize+"%]");

			Hashtable<CommodityCode,Double>	productionCodes = facility.getProductionCodes();
			Hashtable<CommodityCode,Double>	requirementsCodes = facility.getRequirementCodes();

			switch (facility.getType()) {
			case Agriculture:
			case Mining:
				for (CommodityCode code : productionCodes.keySet()) {
					int		modifiedSize = (int)(facilitySize * productionCodes.get(code));
					System.out.print("    "+code+" ("+modifiedSize+"%): ");
					for (int rId : resources.keySet()) {
						Commodity	commodity = commodities.get(rId);
						if (commodity != null && commodity.hasCode(code)) {
							System.out.print("*"+commodity.getName()+", ");
						}
					}
					for (TradeGood good : goods.values()) {
						Commodity	commodity = commodities.get(good.getCommodityId());
						if (commodity != null && commodity.hasCode(code) && resources.get(commodity.getId())==null) {
							System.out.print(commodity.getName()+", ");
						}
					}
					System.out.println("");
				}
				break;
			case Industry:
				for (CommodityCode code : requirementsCodes.keySet()) {
					int		modifiedSize = (int)(facilitySize * requirementsCodes.get(code));
					System.out.print("  --"+code+" ("+modifiedSize+"%): ");
					for (TradeGood good : goods.values()) {
						Commodity	commodity = commodities.get(good.getCommodityId());
						if (commodity != null && commodity.hasCode(code)) {
							System.out.print(commodity.getName()+", ");
						}
					}
					System.out.println("");
				}
				for (CommodityCode code : productionCodes.keySet()) {
					int		modifiedSize = (int)(facilitySize * productionCodes.get(code));
					System.out.print("  ++"+code+" ("+modifiedSize+"%): ");
					for (Commodity commodity : commodities.values()) {
						if (commodity.hasCode(code) && canMake(commodity)) {
							System.out.print(commodity.getName()+", ");
						}
					}
					System.out.println("");
				}
				break;
			}
		}		
	}
	
	/**
	 * Check to see if this planet is capable of producing a commodity.
	 * Assumes that a suitable facility is available, but checks
	 * requirements such as technology level etc.
	 * 
	 * @param commodity		Commodity to check.
	 * @return				True if planet has the right technology base, false otherwise.
	 */
	private boolean canMake(Commodity commodity) {
		if (commodity.getTechLevel() > planet.getTechLevel()) {
			return false;
		}
		if (commodity.hasCode(CommodityCode.Tp, CommodityCode.Tm, CommodityCode.Ti, CommodityCode.Tt, CommodityCode.Ta, CommodityCode.Tu)) {
			boolean		able = false;
			int			tl = planet.getTechLevel();
			
			if (commodity.hasCode(CommodityCode.Tp) && (tl == 0 || tl == 1)) able = true;
			if (commodity.hasCode(CommodityCode.Tm) && (tl == 2 || tl == 3 || tl == 4)) able = true;
			if (commodity.hasCode(CommodityCode.Ti) && (tl == 5 || tl == 6)) able = true;
			if (commodity.hasCode(CommodityCode.Tt) && (tl == 7 || tl == 8)) able = true;
			if (commodity.hasCode(CommodityCode.Ta) && (tl == 9 || tl == 10)) able = true;
			if (commodity.hasCode(CommodityCode.Tu) && (tl > 10)) able = true;
			
			if (!able) return false;
		}
		return true;
	}

	private void manageResources3() {
		Hashtable<Integer,Facility>		facilities = factory.getFacilities();
		Hashtable<Integer,Long>			planetFacilities = planet.getFacilities();
		Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());

		for (int rId : resources.keySet()) {
			Commodity	c = commodities.get(rId);
			int			density = resources.get(rId);

			if (c == null) {
				factory.setCommodity(planet.getId(), rId, 0, 0, 0);
				continue;
			}
			
			System.out.println("Resource ["+c.getName()+"] density "+density+"%");
			for (int fId : planetFacilities.keySet()) {
				Facility	facility = facilities.get(fId);
				long		facilitySize = planetFacilities.get(fId);

				// We only deal with Agriculture and Mining.
				switch (facility.getType()) {
				case Agriculture:
				case Mining:
					break;
				default:
					continue;
				}
				
				// Does this facility manage this resource?
				if (isParentOf(facility.getResourceId(), rId)) {
					System.out.println("     --> ["+facility.getName()+"]/"+commodities.get(facility.getResourceId()).getName());
					
					long	productionRate = getProduction(facility, (int)facilitySize, c, density, goods);
					System.out.println("         Production rate "+n(productionRate)+" dt/wk");
					factory.addCommodity(planet.getId(), c.getId(), productionRate, 0, c.getUnitCost());
				}
				
				// Any other outputs?
				for (Facility.Mapping map : facility.getOutputs()) {
					if (isParentOf(map.getSourceId(), rId)) {
						if (map.getSecondaryId() == 0) {
							// We just output this resource as per the primary one.
							System.out.println("         Required input ["+map.getSourceId()+"]");
							long	productionRate = getProduction(facility, (int)facilitySize, c, density, goods);
							factory.addCommodity(planet.getId(), c.getId(), productionRate, 0, c.getUnitCost());
						} else if (map.getSecondaryId() > 0) {
							// In this case, we turn the resource into a different type of commodity.
							System.out.println("         Required input ["+map.getSourceId()+"] to ["+map.getSecondaryId()+"]");
							Commodity	sec = commodities.get(map.getSecondaryId());
							if (sec != null) {
								long	productionRate = getProduction(facility, (int)facilitySize, sec, density, goods);
								factory.addCommodity(planet.getId(), sec.getId(), productionRate, 0, sec.getUnitCost());
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Simulate the economy for the planet for one week. Manages
	 * mining/farming of resources, consumption and production of
	 * goods and sets the prices. At the end, any left over stocks
	 * decay depending on their type.
	 * 
	 * @version 0.4
	 */
	public void manageEconomy() {
		// Reset all consumption values.
		for (TradeGood g : goods.values()) {
			long	in = g.getWeeklyIn()/2 + g.getProduced() + g.getBought();
			long	out = g.getWeeklyOut()/2 + g.getConsumed() + g.getSold();
			g.setWeeklyIn(in);
			g.setWeeklyOut(out);
			g.setConsumed(0);
			g.setProduced(0);
			g.setBought(0);
			g.setSold(0);
			factory.setCommodity(planet.getId(), g);
		}

		manageResources();
		manageResidential();
		setPrices();
		decayGoods();
		
		for (TradeGood g : goods.values()) {
			factory.setCommodity(planet.getId(), g);
		}
	}

	/**
	 * For each residential facility, manage its resource demands based on
	 * population.
	 * 
	 * @version 0.3
	 */
	private void manageResidential() {
		Hashtable<Integer,Facility>		facilities = factory.getFacilities();
		Hashtable<Integer,Long>			planetFacilities = planet.getFacilities();

		for (int facilityId : planetFacilities.keySet()) {
			Facility	f = facilities.get(facilityId);

			if (f.getType() == FacilityType.Residential) {
				// How many people do we need to feed?
				long		size = planetFacilities.get(facilityId);
				long		people = (planet.getPopulation() * size) / 100;
				int			capability = 100;
				int			numberResources = 1;
				long		shortfall = 0;

				System.out.println("Residential ["+f.getName()+"] size "+size+"% ("+n(people)+")");
				// Eat food.
				shortfall = consumeRequirements(f.getResourceId(), people);
				if (shortfall > 0) {
					capability = (int)(100 - (100 * shortfall) / people);
				}
				
				
				// Consume any other requirements.
				for (Facility.Mapping map : f.getInputs()) {
					if (map.getSecondaryId() == 0) {
						Commodity	c = commodities.get(map.getSourceId());
						if (c == null) {
							continue;
						}
						System.out.println("         Required input ["+map.getSourceId()+"/"+c.getName()+"]");
						shortfall = consumeRequirements(map.getSourceId(), people);
						// TODO: Work out how much demand is unsatisfied, and use that to
						// modify outputs. If only 50% demand is met, output is at 50% etc.
						if (shortfall > 0) {
							capability *= numberResources++;
							capability += (int)(100 - (100 * shortfall) / people);
							capability /= numberResources;
						} else {
							capability *= numberResources++;
							capability += 100;
							capability /= numberResources;
						}
					}
				}
				System.out.println("Capability: "+capability);
				// Now produce outputs
				for (Facility.Mapping map : f.getOutputs()) {
					Commodity	c = commodities.get(map.getSourceId());
					if (c == null) {
						continue;
					}
					if (map.getSecondaryId() == 0) {
						long	productionRate = getProduction(f, (int)f.getCapacity(), c, capability, goods);
						factory.addCommodity(planet.getId(), c.getId(), productionRate, 0, c.getUnitCost());
					}
				}
			}
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
			// TODO: These codes and associated levels are WRONG!
			if (c.hasCode(CommodityCode.Tp)) {
				// Ultra-tech
				if (planet.getTechLevel() > 6) {
					demand *= Math.pow(10, (planet.getTechLevel()-6));
				}
			} else if (c.hasCode(CommodityCode.Tm)) {
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
			} else if (c.hasCode(CommodityCode.Ti)) {
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
			} else if (c.hasCode(CommodityCode.Tt)) {
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

	/**
	 * Given a list of goods, try and consume them equally in turn.
	 * 
	 * @version 0.3
	 * 
	 * @param goods
	 * @param list
	 * @param demand
	 * @param usage
	 * @return  The unsatisfied level of demand.
	 */
	private long consumeGoods(Vector<Commodity> list, long demand, double usage) {
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
					//System.out.println("Eaten "+n(consume)+"dt of "+c.getName()+" satisfies "+n(consume*rate));
					demand -= consume * rate;
					consume(c, consume);
				} else {
					//System.out.println("Eaten "+n(good.getAmount())+"dt of "+c.getName()+" satisfies "+n(good.getAmount()*rate));
					demand -= good.getAmount() * rate;
					consume(c, good.getAmount());
				}
			}
			for (int i=0; i < list.size(); i++) {
				if (goods.get(list.elementAt(i).getId()).getAmount() == 0) {
					list.remove(i);
					i=0;
				}
			}
		}
		if (demand < 0) demand = 0;
		
		return demand;
	}
	
	/**
	 * Generic consumption of trade goods. Given a base commodity id,
	 * consume it and any child commodities that are available up to
	 * the level of demand. Each dt of trade good satisfies a demand
	 * based on its consumption rate.
	 * 
	 * @version 0.3
	 * 
	 * @param requiredId
	 * @param demand
	 * @return
	 */
	private long consumeRequirements(int requiredId, long demand) {
		// Trivial case.
		if (demand < 1) return 0;
		
		Commodity		baseCommodity = commodities.get(requiredId);
		
		System.out.println("Consume ["+baseCommodity.getName()+"] for demand "+n(demand));

		// Work out what the different good types are. How quickly they
		// are consumed depends on the type. Vital goods are consumed
		// quicker than standard goods. Luxury and poor goods are consumed
		// at the slowest rate.
		Vector<Commodity>	vital = new Vector<Commodity>();
		Vector<Commodity>	standard= new Vector<Commodity>();
		Vector<Commodity>	luxury = new Vector<Commodity>();
		Vector<Commodity>	poor = new Vector<Commodity>();
		
		for (TradeGood good : goods.values()) {
			Commodity c = commodities.get(good.getCommodityId());
			if (c == null) {
				System.out.println("Good ["+good.getCommodityId()+"] doesn't exist");
				good.clear();
				continue;
			}
			if (isParentOf(requiredId, c.getId())) {
				long		amount = good.getAmount();
				
				System.out.println("    "+c.getName()+" ("+n(amount)+") @ "+c.getCost()+"Cr");
				if (c.hasCode(CommodityCode.Vi)) {
					vital.add(c);
				} else if (c.hasCode(CommodityCode.Lq)) {
					poor.add(c);
				} else if (c.hasCode(CommodityCode.Lu)) {
					luxury.add(c);
				} else {
					standard.add(c);
				}
			}
		}

		// The split of demand is 70% vital goods, 21% standard and 9% luxury and poor.
		// Poor goods will only be consumed at the end when everything else has
		// been consumed.
		double		vitalDemand = 0.70;
		double		standardDemand = 0.21;
		double		luxuryDemand = 0.06;
		double		poorDemand = 0.03;
		while (demand > 0) {
			//System.out.println("Demand is "+n(demand)+"dt");
			demand = consumeGoods(vital, demand, vitalDemand);
			demand = consumeGoods(standard, demand, standardDemand);
			demand = consumeGoods(luxury, demand, luxuryDemand);
			demand = consumeGoods(poor, demand, poorDemand);
			
			if (vital.size() + standard.size() + luxury.size() + poor.size() == 0) {
				// Completely out of goods.
				break;
			} else if (vital.size() + standard.size() + luxury.size() == 0) {
				// Nothing to use but poor quality goods.
				poorDemand = 1.00;
			} else if (vital.size() + standard.size() == 0) {
				// Just use up luxury and poor goods. Poor goods will go
				// quicker at this point, since they're more affordable.
				luxuryDemand = 0.40;
				poorDemand = 0.60;
			} else if (vital.size() == 0) {
				standardDemand = 0.70;
				luxuryDemand = 0.15;
				poorDemand = 0.15;
			}
		}
		System.out.println("  Shortfall: "+n(demand));
		
		return demand;
	}
	
	/**
	 * Stockpiles of goods will decay over time. Rate of decay can be
	 * affected by trade codes on the goods (e.g. Perishable goods).
	 * This check is run after all other production/consumption has
	 * been calculated.
	 * 
	 * @version 0.3
	 */
	private void decayGoods() {
		for (int i : goods.keySet()) {
			TradeGood		good = goods.get(i);
			
			if (good.getAmount() > 0) {
				Commodity	c = commodities.get(i);
				double		keep = 0.99; // Assume 1% decay rate per week.
				
				if (c.hasCode(CommodityCode.Pe)) {
					keep = 0.95;
				}
				good.setAmount((long)(good.getAmount() * keep));
			}
		}
	}
	
	/**
	 * Set prices for all the goods this planet knows about. Price is based on
	 * how much is currently available, versus how much was consumed in the
	 * last week.
	 * 
	 * @version 0.3
	 */
	private void setPrices() {
		for (int i : goods.keySet()) {
			TradeGood	good = goods.get(i);
			Commodity	commodity = commodities.get(i);
			
			if (good.getConsumed() == 0 && good.getAmount() <= 10) {
				// Not used, so no actual need for it.
				good.setPrice(commodity.getCost() * 0.95);
			} else if (good.getConsumed() == 0) {
				// Don't use it, but some in stock, so price is even cheaper.
				good.setPrice(1.0 * commodity.getCost() / Math.sqrt(Math.log10(good.getAmount())));
			} else if (good.getAmount() <= 10) {
				// We use it, but none in stock.
				System.out.println(commodity.getName());
				good.setPrice(1.0 * commodity.getCost() * Math.sqrt(Math.log10(good.getConsumed())));
			} else {
				double	ratio = 1.0 * good.getConsumed() / good.getAmount();
				good.setPrice(commodity.getCost() * Math.pow(ratio, 0.2));
			}
			System.out.println(commodity.getName()+" : "+good.getPrice()+"Cr");
			
			factory.setCommodity(planet.getId(), good.getCommodityId(), good.getAmount(), 
					             good.getConsumed(), good.getPrice());
		}

		// Do we need to modify production based on these prices?
		Hashtable<Integer,Facility>	facilities = factory.getFacilities();
		Hashtable<Integer,Long>		planetFacilities = planet.getFacilities();
		
		for (int f : planetFacilities.keySet()) {
			Facility	facility = facilities.get(f);
			long		capacity = planetFacilities.get(f);
			
			switch (facility.getType()) {
			case Agriculture:
				break;
			default:
				continue;
			}
			
			int		parentResourceId = facility.getResourceId();
			for (int i : goods.keySet()) {
				TradeGood	good = goods.get(i);
				Commodity	commodity = commodities.get(i);
				
				if (isParentOf(parentResourceId, i)) {
					if (good.getPrice() < (commodity.getCost()*0.8)) {
						capacity--;
					} else if (good.getPrice() > (commodity.getCost()*1.2)) {
						capacity++;
					}
				}
			}
			if (capacity < 1) capacity = 1;
			if (capacity > 100) capacity = 100;
			planetFacilities.put(f, capacity);
		}
		factory.setFacilitiesForPlanet(planet.getId(), planetFacilities);
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
	 * Create any needed residential facilities.
	 * 
	 * @param facilities			Full list of facilities available.
	 * @param listOfFacilities		List of facilities on this planet.
	 */
	private void createResidentialFacilities(Hashtable<Integer,Facility> facilities, Hashtable<Integer,Long> listOfFacilities) {
		long		population = planet.getPopulation();
		Facility	residential = null;
		long		size = 100L;
		
		// Choose the best residential facility. Currently, just choose the
		// highest tech level one the planet can support. The facility capacity
		// is used to filter on the population.
		for (Facility f : Facility.getByType(facilities, FacilityType.Residential).values()) {
			if (f.getTechLevel() > planet.getTechLevel()) continue;
			if (f.getCapacity() > planet.getPopulationLog()) continue;

			if (residential == null) {
				residential = f;
			} else if (f.getTechLevel() > residential.getTechLevel() && f.getTechLevel() <= planet.getTechLevel()
					&& f.getCapacity() > residential.getCapacity()) {
				residential = f;
			}
		}
		if (planet.hasTradeCode(TradeCode.Po)) {
			// Planet isn't fully capable of supporting itself.
			size = 50 + Die.d20(2);
		} else if (planet.hasTradeCode(TradeCode.Ri)) {
			// Planet has more capacity than it actually needs.
			size = 100 + Die.d12(2);
		} else {
			size = 89 + Die.d10(2);
		}
		
		// An unstable or inefficient government limits capability.
		switch (planet.getGovernment()) {
		case Anarchy:
			size *= 0.7;
			break;
		case Balkanization:	case Captive: case FeudalTechnocracy:
			size *= 0.8;
			break;
		case TheocraticDictatorship: case TheocraticOligarchy:
		case CivilService: case ImpersonalBureaucracy:
			size *= 0.9;
			break;
		case Corporation: case NonCharismaticLeader: case TotalitarianOligarchy:
			size *= 1.1;
			break;
		}
		
		// Extreme law levels can reduce production.
		switch (planet.getLawLevel()) {
		case 0:	size *= 0.85; break;
		case 4: size *= 0.90; break;
		case 5: size *= 0.80; break;
		case 6: size *= 0.70; break;
		}
		
		switch (planet.getStarport()) {
		case A: size *= 1.15; break;
		case B: size *= 1.05; break;
		}

		listOfFacilities.put(residential.getId(), size);
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
		long		size = 0;
		
		logger.info("Create facilities for ["+planet.getName()+"]; population "+population);
		
		// Residential
		createResidentialFacilities(facilities, listOfFacilities);


		// Mines
		Facility	mine = null;
		for (Facility f : Facility.getByType(facilities, FacilityType.Mining).values()) {
			if (f.getTechLevel() > planet.getTechLevel()) continue;
			if (mine == null) {
				mine = f;
			} else if (f.getTechLevel() > mine.getTechLevel() && f.getTechLevel() <= planet.getTechLevel()) {
				mine = f;
			}
		}
		if (planet.hasTradeCode(TradeCode.Mi)) {
			size = 150;
		} else if (planet.hasTradeCode(TradeCode.In)) {
			size = 100;
		} else if (planet.hasTradeCode(TradeCode.Ag) || planet.hasTradeCode(TradeCode.Ni)) {
			size = 50;
		} else {
			size = 75;
		}
		listOfFacilities.put(mine.getId(), size);

		// Agriculture
		// This is a bit more complex, since there can be multiple agriculture types.
		// For example, land based and sea based. Facilities which may conflict should
		// (not enforced!) have a resource id at the same level, i.e. one facility won't
		// manage a resource which is the child of a resource managed by another facility.
		Hashtable<Integer,Facility>		agriculture = new Hashtable<Integer,Facility>();
		for (Facility f : Facility.getByType(facilities, FacilityType.Agriculture).values()) {
			if (f.getTechLevel() > planet.getTechLevel()) continue;
			int		cId = f.getResourceId();
			
			for (int rId : resources.keySet()) {
				// If this facility is useful to us (handles the given resource), then
				// see if we want to add it to our list. List is keyed on the resource id.
				if (isParentOf(cId, rId)) {
					if (agriculture.get(cId) == null) {
						agriculture.put(cId, f);
					} else if (agriculture.get(cId).getTechLevel() < f.getTechLevel()) {
						agriculture.put(cId, f);
					}
				}
			}
		}
		if (planet.hasTradeCode(TradeCode.Ag)) {
			size = 150;
		} else if (planet.hasTradeCode(TradeCode.Na)) {
			size = 50;
		} else if (planet.hasTradeCode(TradeCode.In) || planet.hasTradeCode(TradeCode.Mi)) {
			size = 75;
		} else {
			size = 100;
		}
		for (Facility f : agriculture.values()) {
			listOfFacilities.put(f.getId(), size);
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
				
				//Planet		planet = factory.getPlanet(223065);
				Planet		planet = factory.getPlanet(778093);
				
				Trade		trade = new Trade(factory, planet);
				//trade.createFacilities();
				trade.dumpFacilityStats();
				//trade.manageEconomy();
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
