/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.civ.commodity.Commodity;

/**
 * Represents a planet's inventory of stock that is available for sale and/or
 * consumption. It contains information on a single type of commodity,
 * including the amount, current price and variation in sales and production.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name = "inventory")
public class Inventory {
	/** Unique identifier for this inventory item. */
	@Id	@GeneratedValue	@Column(name = "id")
	private int			id = 0;
	
	/** The planet that this inventory is for. */
	@ManyToOne @JoinColumn(name = "planet_id", referencedColumnName = "id")
	private Planet		planet;
	
	/** The commodity that this inventory is for. */
	@ManyToOne @JoinColumn(name = "commodity_id", referencedColumnName = "id")
	private Commodity	commodity;

	@Column(name = "amount")
	long	amount=0;
	
	@Column(name = "price")
	int		price=0;
	
	@Column(name = "consumed")
	long	consumed = 0;
	
	@Column(name = "produced")
	long	produced = 0;
	
	@Column(name = "bought")
	long	bought = 0;
	
	@Column(name = "sold")
	long	sold = 0;
	
	@Column(name = "weeklyin")
	long	weeklyIn = 0;
	
	@Column(name = "weeklyout")
	long	weeklyOut = 0;
	
	Inventory() {
		
	}
	
	Inventory(final Planet planet, final Commodity commodity) {
		this.planet = planet;
		this.commodity = commodity;
	}

	/**
	 * Gets the commodity represented by this inventory item.
	 * 
	 * @return	Type of commodity;
	 */
	public Commodity getCommodity() {
		return commodity;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public void setAmount(final long amount) {
		this.amount = amount;
	}
	
	public void addAmount(final long amount) {
		this.amount += amount;
		if (this.amount < 0) {
			this.amount = 0;
		}
	}
	
}
