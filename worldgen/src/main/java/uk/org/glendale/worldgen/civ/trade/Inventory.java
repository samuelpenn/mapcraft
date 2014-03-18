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
import uk.org.glendale.worldgen.civ.commodity.CommodityCode;

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
		this.price = commodity.getCost();
	}
	
	/**
	 * Gets the unique internal id for this inventory item.
	 * 
	 * @return	Unique database id.
	 */
	int getId() {
		return id;
	}
	
	Planet getPlanet() {
		return planet;
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
	
	public void setAmount(final long units) {
		this.amount = units;
	}
	
	public void addAmount(final long units) {
		this.amount += units;
		if (this.amount < 0) {
			this.amount = 0;
		}
	}
	
	/**
	 * Consume the given amount of the item. Consuming the item reduces the
	 * amount stored, and adds to the amount consumed. If the amount to
	 * consume is greater than that available, then only that which is
	 * available is consumed.
	 * 
	 * @param units		Amount of the item to consume.
	 * @return			The amount actually consumed.
	 */
	public long consume(long units) {
		if (units < 0) {
			throw new IllegalArgumentException("Cannot consume negative amounts.");
		} else if (units > amount) {
			units = amount;
		}
		this.amount -= units;
		this.consumed += units;
		this.weeklyOut += units;
		
		return units;
	}
	
	/**
	 * Produce the given number of units of this item. Amount is added to the
	 * amount stored, the number produced this week, and the total weekly
	 * input. Cannot produce a negative number of units.
	 * 
	 * @param units		Number of units of this item to produce.
	 */
	public void produce(long units) {
		if (units < 0) {
			throw new IllegalArgumentException("Cannot produce negative amounts.");
		}
		this.amount += units;
		this.produced += units;
		this.weeklyIn += units;
	}
	
	/**
	 * Sell the given number of units of this item. Stored amount is reduced
	 * by units sold, count of sold and weeklyOut is incremented. Exception
	 * is thrown if units are negative. If try to sell more than we have, then
	 * the amount sold is capped at current stocks.
	 * 
	 * @param units		Number of units to sell.
	 * @return			Actual number sold.
	 */
	public long sell(long units) {
		if (units < 0) {
			throw new IllegalArgumentException("Cannot sell a negative amount.");
		} else if (units > amount) {
			units = amount;
		}
		this.amount -= units;
		this.sold += units;
		this.weeklyOut += units;
		return units;
	}
	
	/**
	 * Buy the given number of units of this item. Stored amount is incremented
	 * by units bought. Count of bought and weeklyIn is incremented. Cannot
	 * buy a negative number of units.
	 * 
	 * @param units		Number of units to buy.
	 */
	public void buy(long units) {
		if (units < 0) {
			throw new IllegalArgumentException("Cannot buy a negative amount.");
		}
		this.amount += units;
		this.bought += units;
		this.weeklyIn += units;
	}
	
	public long getConsumed() {
		return consumed;
	}
	
	public long getProduced() {
		return produced;
	}
	
	public long getSold() {
		return sold;
	}
	
	public long getBought() {
		return bought;
	}
	
	public long getWeeklyIn() {
		return weeklyIn;
	}
	
	public long getWeeklyOut() {
		return weeklyOut;
	}
	
	/**
	 * Get the current price for this item. This is affected by current stock
	 * levels, and whether more are being produced than consumed.
	 * 
	 * @return	Current unit price of this commodity.
	 */
	public int getPrice() {
		return price;
	}
	
	/**
	 * At the end of each week, the inventory is processed. Stock levels will
	 * decay (Perishable goods decay faster), and other statistics are halved.
	 * Statistics aren't reset to zero, so the previous week affects the
	 * current week's statistics.
	 */
	public void endOfWeek() {
		if (commodity.hasCode(CommodityCode.Pe)) {
			this.amount *= 0.9;
		} else {
			this.amount *= 0.99;
		}
		this.weeklyIn *= 0.5;
		this.weeklyOut *= 0.5;
		this.bought *= 0.5;
		this.sold *= 0.5;
		this.consumed *= 0.5;
		this.produced *= 0.5;
		
		if (weeklyIn > 0) {
			price = (int)(commodity.getCost() * weeklyOut / weeklyIn);
		} else {
			price = commodity.getCost();
		}
	}
}
