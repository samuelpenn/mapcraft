/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

/**
 * A TradeGood is a physical manifestation of a commodity. It has a quantity and
 * a price, which may differ from the standard price for the commodity. If the
 * TradeGood is available for sale on a planet, then the price is the current
 * sale price. If it has been bought and is in a ship, then it is the price that
 * it was purchased at.
 * 
 * @author Samuel Penn
 */
public class TradeGood {
	private int id;
	private Commodity commodity;
	private long quantity;
	private int price;

	private TradeGood() {
	}

	public TradeGood(Commodity commodity, long quantity, int price) {
		this.commodity = commodity;
		this.quantity = quantity;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public Commodity getCommodity() {
		return commodity;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public void addQuantity(long amount) {
		this.quantity += amount;
	}

	public int getPrice() {
		return price;
	}
}
