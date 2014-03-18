/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import uk.org.glendale.worldgen.civ.commodity.CommodityTO;

/**
 * Transfer Object for an Inventory item.
 */
public class InventoryTO {
	public final int			id;
	public final CommodityTO	commodity;
	public final long			amount;
	public final int			price;
	public final long			consumed;
	public final long			produced;
	public final long			bought;
	public final long			sold;
	public final long			weeklyIn;
	public final long			weeklyOut;
	
	public InventoryTO(Inventory inventory) {
		this.id = inventory.getId();
		this.commodity = new CommodityTO(inventory.getCommodity());
		this.amount = inventory.getAmount();
		this.price = inventory.getPrice();
		this.consumed = inventory.getConsumed();
		this.produced = inventory.getProduced();
		this.bought = inventory.getBought();
		this.sold = inventory.getSold();
		this.weeklyIn = inventory.getWeeklyIn();
		this.weeklyOut = inventory.getWeeklyOut();
	}

}
