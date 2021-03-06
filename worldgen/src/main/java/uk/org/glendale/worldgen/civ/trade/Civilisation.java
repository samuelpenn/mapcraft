/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Installation;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.Resource;
import uk.org.glendale.worldgen.civ.commodity.Commodity;
import uk.org.glendale.worldgen.civ.commodity.CommodityCode;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.commodity.CommodityMap;
import uk.org.glendale.worldgen.civ.facility.Facility;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityType;
import uk.org.glendale.worldgen.civ.facility.ProductionMap;

/**
 * Manages the weekly processes for a planet.
 * 
 * @author Samuel Penn
 */
public class Civilisation {
	private Planet	planet;
	private CommodityFactory	commodityFactory;
	private FacilityFactory		facilityFactory;
	
	private List<Inventory>		inventory;
	
	final void setCommodityFactory(CommodityFactory factory) {
		this.commodityFactory = factory;
	}
	
	final void setFacilityFactory(FacilityFactory factory) {
		this.facilityFactory = factory;
	}
	
	final void setInventory(List<Inventory> inventory) {
		this.inventory = inventory;
	}

	final public List<Inventory> getInventory() {
		return inventory;
	}
	
	
	Civilisation(Planet planet) {
		this.planet = planet;
	}
	
	public void simulate() {
		if (planet.getNextEvent() <= 1) {
			// Planet has never been simulated before.
			processWeek(false);
			// processWeek(false);
			// processWeek(false);
		} else {
			processWeek(true);
		}
	}
	
	private void processWeek(boolean honourRequirements) {
		// Run the End Of Week processing. This updates all the statistics
		// from the previous week.
		for (Inventory item : inventory) {
			item.endOfWeek();
		}
		
		// Manage all the resources for the planet.
		manageResources(honourRequirements);		
	}
	
	/**
	 * For each of the resource management facilities, produce the necessary
	 * resources. Facilities may have requirements in the form of required
	 * goods, and if those goods don't exist, then the output of the facility
	 * will be reduced unless the honourRequirements flag is false.
	 * 
	 * The first time this is run for a planet, requirements are ignored in
	 * order to kick start the economy.
	 * 
	 * @param honourRequirements
	 * 				If false, requirements for a facility are ignored.
	 */
	private void manageResources(boolean honourRequirements) {
		List<Resource>	resources = planet.getResources();

		System.out.println("Resources: " + resources.size());
		
		// Process mining facilities first.
		for (Installation inst : planet.getFacilities()) {
			Facility	facility = inst.getFacility();
			long		capacity = inst.getSize();
			
			capacity = (planet.getPopulation() * capacity) / 100;

			if (facility.getType().equals(FacilityType.Mining)) {
				System.out.println("Mine: "+facility.getName()+" [" + capacity + "]");
				Set<CommodityCode> required = facility.getRequiredGoods();
				int honoured = requiredGoods(required, capacity);
				if (honourRequirements && honoured < 100) {
					// TODO: Work out requirements and their effect on
					// the capacity of this installation.
				}
				processMiAg(facility, capacity);
			}
		}
		
		// Process agricultural facilities second.
		for (Installation inst : planet.getFacilities()) {
			Facility	facility = inst.getFacility();
			long		capacity = inst.getSize();
			
			capacity = (planet.getPopulation() * capacity) / 100;

			if (facility.getType().equals(FacilityType.Agriculture)) {
				System.out.println("Agriculture: "+facility.getName()+" [" + capacity + "]");
				Set<CommodityCode> required = facility.getRequiredGoods();
				int honoured = requiredGoods(required, capacity);
				if (honourRequirements && honoured < 100) {
					// TODO: Work out requirements and their effect on
					// the capacity of this installation.
				}
				processMiAg(facility, capacity);
			}		
		}

		// Process Residential facilities third.
		for (Installation inst : planet.getFacilities()) {
			Facility	facility = inst.getFacility();
			long		capacity = inst.getSize();
			
			capacity = (planet.getPopulation() * capacity) / 100;

			if (facility.getType().equals(FacilityType.Residential)) {
				System.out.println("Residential: "+facility.getName()+" [" + capacity + "]");
				Set<CommodityCode> required = facility.getRequiredGoods();
				int honoured = requiredGoods(required, capacity);
				if (honourRequirements && honoured < 100) {
					// TODO: Work out requirements and their effect on
					// the capacity of this installation.
					System.out.println("  Requirements at " + honoured + "%");
				}
				processResidential(facility, capacity);
				
				Set<CommodityCode> consumed = facility.getConsumedGoods();
				System.out.println("  Consume " + consumed.size() + " things");
				requiredGoods(consumed, capacity);
			}		
		}
	}
	
	/**
	 * Gets the amount of a resource that is produced each week given the
	 * production capacity available to produce it. The capacity is based
	 * on the size of the facility and the population. The amount actually
	 * produced depends on the production rating of the commodity.
	 * 
	 * The actual capacity is randomly modified according to the government
	 * type. If the output is going to be zero, then there is a random
	 * probability of producing 1 unit.
	 * 
	 * @param commodity		Commodity to be produced.
	 * @param capacity		Facility capacity available.
	 * @return				Units of commodity actually produced.
	 */
	private long getWeeklyProduction(Commodity commodity, final long capacity) {
		final int	pr = commodity.getProductionRating();
		final int	var = planet.getGovernment().getVariability();
		double modifier = 1.00 + (Die.die(var) - Die.die(var)) / 100.0;
		
		long 	available = (long) (capacity * modifier);
		double 	output = available / Math.pow(10, pr / 2.0);
		
		if (planet.getTechLevel() < commodity.getTechLevel()) {
			// Unable to produce this good.
			return 0;
		} else {
			// If tech level is higher, then can produce the good at a
			// faster rate than normal, normally 5% per level higher.
			int	 techDiff = planet.getTechLevel() - commodity.getTechLevel();
			double techMod = 1.0 + techDiff * 0.05;
			if (commodity.hasCode(CommodityCode.Tl)) {
				techMod = 1.0 + techDiff * 0.10;
			} else if (commodity.hasCode(CommodityCode.TL)) {
				techMod = 1.0 + techDiff * 0.20;
			}
			output *= techMod;
		}
		
		// We use the square root of the fraction, rather than the raw
		// fraction, to increase the likelihood of producing something.
		if (output < 1 && Math.random() <= Math.sqrt(output - ((int)output))) {
			output += 1;
		}
		
		return (long) output;
	}
	
	/**
	 * Gets the inventory item for the given commodity. If there is currently
	 * no inventory item, then one is created and added to the planet's
	 * current inventory with zero statistics.
	 * 
	 * @param commodity		Commodity to retrieve inventory for.
	 * @return				Existing or new inventory item.
	 */
	private Inventory getInventoryItem(Commodity commodity) {
		Inventory	item = null;
		
		for (Inventory i : inventory) {
			if (i.getCommodity().equals(commodity)) {
				item = i;
				break;
			}
		}
		if (item == null) {
			item = new Inventory(planet, commodity);
			inventory.add(item);
		}
		
		return item;
		
	}
	
	/**
	 * Work out what inventory stock is consumed based on the requirements
	 * list provided. 
	 * 
	 * @param required	List of codes that must be satisfied.
	 * @param capacity	Amount of satisfaction required.
	 * 
	 * @return			Percentage of requirements which were met.
	 */
	private int requiredGoods(Set<CommodityCode> required, long capacity) {
		int	percentageMet = 100;
		
		for (CommodityCode code : required) {
			Set<Inventory>	hq = new HashSet<Inventory>();
			Set<Inventory>	std = new HashSet<Inventory>();
			Set<Inventory>	lq = new HashSet<Inventory>();
			Set<Inventory>	lu = new HashSet<Inventory>();
			Set<Inventory>	vi = new HashSet<Inventory>();

			for (Inventory item : inventory) {
				Commodity	c = item.getCommodity();
				if (c.hasCode(code)) {
					if (c.hasCode(CommodityCode.Lu)) {
						lu.add(item);
					} else if (c.hasCode(CommodityCode.Vi)) {
						vi.add(item);
					} else if (c.hasCode(CommodityCode.Hq)) {
						hq.add(item);
					} else if (c.hasCode(CommodityCode.Lq)) {
						lq.add(item);
					} else {
						std.add(item);
					}
				}
			}
			
			long	needed = capacity;
			System.out.println(code+": "+needed);
			while (needed > 0) {
				if (hq.size() + std.size() + lq.size() + lu.size() + vi.size() == 0) {
					break;
				}
				long	needLu = (int)(needed * 0.05);
				long	needHq = (int)(needed * 0.15);
				long	needLq = (int)(needed * 0.20);
				long	needVi = (int)(needed * 0.20);
				long	needStd = needed - (needLu + needHq + needLq + needVi);
				
				long	consumed = 0;
				consumed += consumeResources(std, needStd);
				consumed += consumeResources(vi, needVi);
				consumed += consumeResources(lu, needLu);
				consumed += consumeResources(hq, needHq);
				consumed += consumeResources(lq, needLq);
				needed -= consumed;
				System.out.println("Needed: "+needed);
			}	
		}
		return percentageMet;
	}
	
	/**
	 * Consume the given list of resources. The total number of resources
	 * to consume is given, and that is split equally across all the
	 * resources in the list. Each unit of resource is worth an amount based
	 * on its consumption rating (ea +2 = x10). Items are removed from the
	 * list once they become exhausted.
	 * 
	 * @param list
	 * @param needed
	 * @return
	 */
	private long consumeResources(Set<Inventory> list, long needed) {
		long	consumed = 0;
		Set<Inventory>	empty = new HashSet<Inventory>();
		
		if (list != null && list.size() > 0) {
			long	need = needed / list.size();
			
			System.out.println("consumeResources: Need [" + need +"/" + needed + "]");
			
			for (Inventory item : list) {
				int cr = item.getCommodity().getConsumptionRating();
				cr = (int) Math.pow(10, cr / 2.0);
				
				System.out.println("  "+item.getCommodity().getName()+": ["
						+ item.getCommodity().getConsumptionRating()+"] ["+cr+"] [" + item.getAmount() + "]");

				consumed += item.consume(need / cr + 1) * cr;
				System.out.println("  Consumed: "+ consumed);
				if (item.getAmount() < 1) {
					empty.add(item);
				}
			}
			
			for (Inventory item : empty) {
				list.remove(item);
			}
		}
		
		return consumed;
	}
	
	
	/**
	 * Process mining and agriculture facilities. Such facilities perform 
	 * operations on raw resources. Each resource which supports an operation 
	 * supported by the facility will be processed, and converted into trade 
	 * goods for that planet.
	 * 
	 * A facility needs to have an <operation/> defined with a mode which
	 * matches an <output/> defined on the resource commodity.
	 * 
	 * @param facility	Facility being processed.
	 * @param capacity	Effective population capacity for this resource.
	 */
	private void processMiAg(Facility facility, long capacity) {
		List<Resource>	resources = planet.getResources();

		System.out.println("processMiAg: [" + facility.getName() + "] [" 
				+ capacity + "]");
		
		// For each resource that this planet has...
		for (Resource resource : resources) {
			Commodity			c = resource.getCommodity();
			List<CommodityMap> 	map = commodityFactory.getMappings(c);
			
			System.out.println(resource.getCommodity().getName() + ": {{" + map.size() + "}}");
			
			// Get all the <output/> mappings for this commodity, and if the
			// facility has a matching <operation/>, then we produce some goods.
			for (CommodityMap m : map) {
				String	operation = m.getOperation();
				int		rate = facility.getOperation(operation);
				if (rate <= 0) {
					continue;
				}
				if (m.getTechLevel() > planet.getTechLevel()) {
					continue;
				}
				System.out.println("  processMiAg: [" + c.getName() + 
						" -> " + operation + " [" + rate + "%] -> [" 
						+ m.getOutput().getName() + "]");

				// Get capacity of this operation.
				long cap = (capacity * resource.getDensity() * resource.getDensity()) / 10000; 
				cap = (cap * rate * m.getEfficiency()) / 10000;
				long produced = getWeeklyProduction(m.getOutput(), cap);
				
				if (produced > 0) {
					Inventory item = getInventoryItem(m.getOutput());
					if (item.getAmount() > item.getWeeklyOut()) {
						// Reduce the production rate if we are creating this
						// commodity faster than it is being used.
						if (item.getWeeklyOut() == 0) {
							produced = 0;
						} else {
							double e = (double)item.getAmount() / (double)item.getWeeklyOut();
							produced /= (e * e * e);
						}
					}
					System.out.println("    + " + produced);
					item.produce(produced);
				}
			}
		}
	}
	
	/**
	 * Residential facilities represent societies. There will normally be
	 * one residential facility per world, though this isn't a rule.
	 * They process existing commodities (not resources). They may produce
	 * items 'out of nothing' as well if their other requirements are met.
	 * These are often luxury and/or knowledge based goods.
	 * 
	 * @param facility	Facility being processed.
	 * @param capacity	Effective population capacity for this resource.
	 */
	private void processResidential(Facility facility, long capacity) {
		List<Resource>	resources = planet.getResources();

		System.out.println("processResidential: [" + facility.getName() + "] [" 
				+ capacity + "]");
		
		for (Inventory item : inventory) {
			Commodity			c = item.getCommodity();
			List<CommodityMap> 	map = commodityFactory.getMappings(c);
			
			for (CommodityMap m : map) {
				String	operation = m.getOperation();
				int		rate = facility.getOperation(operation);
				if (rate <= 0) {
					continue;
				}
				System.out.println("processResidential: [" + c.getName() + 
						" -> " + operation + " [" + rate + "%] -> [" 
						+ m.getOutput().getName() + "]");
				

				// Get capacity of this operation.
				/*
				long cap = (capacity * rate * m.getEfficiency()) / 100000;
				long produced = getWeeklyProduction(m.getOutput(), cap);
				
				System.out.println("    + " + produced);
				getInventoryItem(m.getOutput()).produce(produced);
				*/
			}

		}

		List<ProductionMap> pmap = facilityFactory.getProductionMap(facility);

		// The ProductionMap comes from the facility <map/> element. It allows
		// a residential facility to turn one type of good into another type
		// of good.
		
		// If multiple mappings work on the same source commodity, then it's
		// possible one may consume it all before the others have a look in.
		// As a simplistic fix, count how many mappings use each source...
		Hashtable<Commodity, Integer>  reqCount = new Hashtable<Commodity, Integer>();
		for (ProductionMap m : pmap) {
			Commodity	from = m.getFrom();
			if (reqCount.get(from) != null) {
				reqCount.put(from, reqCount.get(from) + 1);
			} else {
				reqCount.put(from, 1);
			}
		}
		
		// ...in the first pass, limit each mapping to only using a limited
		// share of each source, by dividing total available by number of
		// mappings using each requirement. This ensures that every mapping
		// gets a bite at the pie...
		List<ProductionMap>	retry = produceFromMap(pmap, reqCount, capacity);
		// ...if any mappings didn't get everything they want, allow them a
		// second pass, and allow early ones to be greedy and starve later
		// mappings.
		if (retry.size() > 0) {
			produceFromMap(retry, null, capacity);
		}
	}
	
	private List<ProductionMap> produceFromMap(List<ProductionMap> pmap, Hashtable<Commodity, Integer> reqCount, long capacity) {
		List<ProductionMap>	retry = new ArrayList<ProductionMap>();
		for (ProductionMap m : pmap) {
			Commodity	from = m.getFrom();
			Commodity	to = m.getTo();
			
			if (from != null) {
				Inventory item = getInventoryItem(from);
				long		available = item.getAmount();
				if (reqCount != null && reqCount.get(from) > 1) {
					available /= reqCount.get(from);
				}
	
				long amount = to.getProduction(capacity);
				amount *= (m.getLevel() / 100.0);
				if (amount > available) {
					amount = available;
					retry.add(m);
				}
				item.consume(amount);
				System.out.println("    " + from.getName() + " -> " + to.getName() + " [" + amount + "]");
				
				item = getInventoryItem(to);
				item.produce(amount);
			} else if (reqCount != null) {
				// Some items may be produced out of nothing. These are normally
				// knowledge goods.
				long amount = to.getProduction(capacity);
				amount *= (m.getLevel() / 100.0);
				System.out.println("    * -> " + to.getName() + " [" + amount + "]");
				Inventory item = getInventoryItem(to);
				item.produce(amount);
			}
		}
		return retry;
	}
	
	public static void main(String[] args) {
		for (int pr = 0; pr < 21; pr++) {
			DecimalFormat df = new DecimalFormat("#,###");
			String msg = String.format("%-2d: %s", pr, df.format((long)Math.pow(10, pr / 2.0)));
			System.out.println(msg);
		}
	}
		
}
