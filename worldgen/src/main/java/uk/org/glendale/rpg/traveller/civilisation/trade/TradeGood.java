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
 * actual price, the planet it is at and statistics concerning how
 * much of it has been produced/consumed.
 * 
 * @author Samuel Penn
 */
public class TradeGood {
	int		id = 0;
	int		commodityId=0;
	long	amount=0;
	int		price=0;
	int		planetId=0;
	long	consumed = 0;
	long	produced = 0;
	long	bought = 0;
	long	sold = 0;
	
	long	weeklyIn = 0;
	long	weeklyOut = 0;
	
	public TradeGood(Commodity commodity) {
		this.commodityId = commodity.getId();
		this.price = commodity.getUnitCost();
	}
	
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
		this.amount -= consumed;
		if (this.amount < 0) this.amount = 0;
		
		return this.consumed;
	}
	
	public long getProduced() {
		return produced;
	}
	
	public void setProduced(long produced) {
		this.produced = produced;
	}
	
	public long addProduced(long produced) {
		this.produced += produced;
		this.amount += produced;
		return this.produced;
	}
	
	public long getBought() {
		return bought;
	}
	
	public void setBought(long bought) {
		this.bought = bought;
	}
	
	public long addBought(long bought) {
		this.bought += bought;
		this.amount -= bought;
		if (this.bought < 0) this.bought =  0;
		if (amount < 0) amount = 0;
		return this.bought;
	}
	
	public long getSold() {
		return sold;
	}
	
	public void setSold(long sold) {
		this.sold = sold;
	}
	
	public long addSold(long sold) {
		this.sold += sold;
		this.amount -= sold;
		if (this.sold < 0) this.sold = 0;
		if (this.amount < 0) this.amount = 0;
		return this.sold;
	}
	
	public long getWeeklyIn() {
		return weeklyIn;
	}
	
	public void setWeeklyIn(long in) {
		this.weeklyIn = in;
	}
	
	public long getWeeklyOut() {
		return weeklyOut;
	}
	
	public void setWeeklyOut(long out) {
		this.weeklyOut = out;
	}
	
	/**
	 * True if there is none of this good left and there has been no
	 * recorded activity on it. Otherwise false.
	 */
	public boolean isUnused() {
		if (weeklyIn == 0 && weeklyOut == 0 && bought == 0 && sold == 0 && consumed == 0 && produced == 0) {
			return true;
		}
		return false;
	}

	public void clear() {
		weeklyIn = weeklyOut = consumed = produced = bought = sold = 0;
	}
}
