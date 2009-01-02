/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

public class TradeGood {
	int		id = 0;
	int		commodityId=0;
	int		amount=0;
	int		price=0;
	int		planetId=0;
	
	public TradeGood(int commodityId, int amount, int price) {
		this.commodityId = commodityId;
		this.amount = amount;
		this.price = price;
		this.planetId = 0;
	}

	public TradeGood(int id, int commodityId, int amount, int price, int planetId) {
		this.id = id;
		this.commodityId = commodityId;
		this.amount = amount;
		this.price = price;
		this.planetId = planetId;
	}

	public TradeGood(int commodityId, int amount, int price, int planetId) {
		this.id = 0;
		this.commodityId = commodityId;
		this.amount = amount;
		this.price = price;
		this.planetId = planetId;
	}
	
	public int getId() {
		return id;
	}
	
	public int getCommodityId() {
		return commodityId;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public int getPrice() {
		return price;
	}
	
	public int getPlanetId() {
		return planetId;
	}

}
