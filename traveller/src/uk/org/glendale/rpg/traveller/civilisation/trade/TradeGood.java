/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

/**
 * Keeps track of the amount of a particular commodity. Includes the
 * actual price, the planet it is at and the amount consumed in the
 * previous week (for calculating demands). 
 * 
 * @author Samuel Penn
 *
 */
public class TradeGood {
	int		id = 0;
	int		commodityId=0;
	long	amount=0;
	int		price=0;
	int		planetId=0;
	long	consumed = 0;
	
	public TradeGood(int commodityId, long amount, long consumed, int price) {
		this.commodityId = commodityId;
		this.amount = amount;
		this.consumed = consumed;
		this.price = price;
		this.planetId = 0;
	}

	public TradeGood(int id, int commodityId, long amount, long consumed, int price, int planetId) {
		this.id = id;
		this.commodityId = commodityId;
		this.amount = amount;
		this.consumed = consumed;
		this.price = price;
		this.planetId = planetId;
	}

	public TradeGood(int commodityId, long amount, long consumed, int price, int planetId) {
		this.id = 0;
		this.commodityId = commodityId;
		this.amount = amount;
		this.consumed = consumed;
		this.price = price;
		this.planetId = planetId;
	}
	
	public int getId() {
		return id;
	}
	
	public int getCommodityId() {
		return commodityId;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public void setAmount(long amount) {
		if (amount < 0) {
			amount = 0;
		}
		this.amount = amount;
	}
	
	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public void setPrice(double price) {
		this.price = (int)price;
	}
	
	public int getPlanetId() {
		return planetId;
	}

	/**
	 * The amount of this commodity consumed in the week on the
	 * given planet.
	 * 
	 * @return		Number of units consumed.
	 */
	public long getConsumed() {
		return consumed;
	}
	
	public void setConsumed(long consumed) {
		this.consumed = consumed;
	}
	
	/**
	 * Add the amount to the total consumed, and return the total.
	 * 
	 * @param consumed		Number of extra units consumed.
	 * @return				Total consumed so far.
	 */
	public long addConsumed(long consumed) {
		this.consumed += consumed;
		
		return this.consumed;
	}	
}
