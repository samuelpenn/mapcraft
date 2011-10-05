/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.util.HashSet;
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
		if (planet.getNextEvent() == 0) {
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
			
			capacity = (planet.getPopulation() * capacity * capacity) / 10000;

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
			
			capacity = (planet.getPopulation() * capacity * capacity) / 10000;

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
			
			capacity = (planet.getPopulation() * capacity * capacity) / 10000;

			if (facility.getType().equals(FacilityType.Residential)) {
				System.out.println("Residential: "+facility.getName()+" [" + capacity + "]");
				Set<CommodityCode> required = facility.getRequiredGoods();
				int honoured = requiredGoods(required, capacity);
				if (honourRequirements && honoured < 100) {
					// TODO: Work out requirements and their effect on
					// the capacity of this installation.
				}
				processResidential(facility, capacity);
			}		
		}
	}
	
	private long getWeeklyProduction(Commodity commodity, final long capacity) {
		final int	pr = commodity.getProductionRating();
		final int	var = planet.getGovernment().getVariability();
		double modifier = 1.00 + (Die.die(var) - Die.die(var)) / 100.0;
		
		return (long) (modifier * capacity / Math.log10( pr / 2.0));
	}
	
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
	 * @return
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
	 * @param facility	Facility being processed.
	 * @param capacity	Effective population capacity for this resource.
	 */
	private void processMiAg(Facility facility, long capacity) {
		List<Resource>	resources = planet.getResources();

		System.out.println("processMiAg: [" + facility.getName() + "] [" 
				+ capacity + "]");
		
		for (Resource resource : resources) {
			Commodity			c = resource.getCommodity();
			List<CommodityMap> 	map = commodityFactory.getMappings(c);
			
			for (CommodityMap m : map) {
				String	operation = m.getOperation();
				int		rate = facility.getOperation(operation);
				if (rate <= 0) {
					continue;
				}
				System.out.println("processMiAg: [" + c.getName() + 
						" -> " + operation + " [" + rate + "%] -> [" 
						+ m.getOutput().getName() + "]");

				// Get capacity of this operation.
				long cap = (capacity * rate * m.getEfficiency()) / 100000;
				long produced = getWeeklyProduction(m.getOutput(), cap);
				
				System.out.println("    + " + produced);
				getInventoryItem(m.getOutput()).produce(produced);
			}
		}
	}
	
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
				long cap = (capacity * rate * m.getEfficiency()) / 100000;
				long produced = getWeeklyProduction(m.getOutput(), cap);
				
				System.out.println("    + " + produced);
				getInventoryItem(m.getOutput()).produce(produced);
			}
		}
		
	}
		
}
