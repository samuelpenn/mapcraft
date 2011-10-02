/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
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
		
		for (Installation inst : planet.getFacilities()) {
			Facility	facility = inst.getFacility();
			long		capacity = inst.getSize();
			
			capacity = (planet.getPopulation() * capacity) / 100;
			
			if (facility.getType().equals(FacilityType.Mining)) {
				System.out.println("Mine: "+facility.getName()+" [" + capacity + "]");
				Set<CommodityCode> required = facility.getRequiredGoods();
				if (honourRequirements) {
					// TODO: Work out requirements and their effect on
					// the capacity of this installation.
				}
				processMine(facility, capacity);
			}
		}
	}
	
	private long getWeeklyProduction(Commodity commodity, final long capacity) {
		final int	pr = commodity.getProductionRating();
		
		return (long) (capacity / Math.log10( pr / 2.0));
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
	 * Process a mining facility. Mining facilities perform operations on
	 * raw resources. Each resource which supports an operation supported
	 * by the facility will be processed, and converted into trade goods
	 * for that planet.
	 * 
	 * @param facility	Facility being processed.
	 * @param capacity	Effective population capacity for this resource.
	 */
	private void processMine(Facility facility, long capacity) {
		List<Resource>	resources = planet.getResources();

		System.out.println("processMine: [" + facility.getName() + "] [" 
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
				System.out.println("processMine: [" + c.getName() + 
						" -> " + operation + " [" + rate + "%] -> [" 
						+ m.getOutput().getName() + "]");

				// Get capacity of this operation.
				long cap = (capacity * rate * m.getEfficiency()) / 100000;
				long produced = getWeeklyProduction(c, cap);
				
				System.out.println("    + " + produced);
				getInventoryItem(c).addAmount(produced);
			}
		}
	}
	
}
