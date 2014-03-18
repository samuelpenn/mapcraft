/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

/**
 * Simplified REST transfer object for commodities.
 * 
 * @author Samuel Penn
 */
public class CommodityTO {
	public final int	id;
	public final String name;
	public final Source	source;
	public final int	consumptionRating;
	public final int	productionRating;
	public final int	volume;
	public final int	lawLevel;
	public final int	techLevel;
	public final int	parentId;
	public final int	baseCost;
	public final long	amount;
	public final String imagePath;
	
	public CommodityTO(Commodity commodity) {
		this(commodity, 0L);
	}
	
	public CommodityTO(Commodity commodity, long amount) {
		this.id = commodity.getId();
		this.name = commodity.getName();
		this.source = commodity.getSource();
		this.consumptionRating = commodity.getConsumptionRating();
		this.productionRating = commodity.getProductionRating();
		this.volume = commodity.getVolume();
		this.lawLevel = commodity.getLawLevel();
		this.techLevel = commodity.getTechLevel();
		if (commodity.getParent() != null) {
			this.parentId = commodity.getParent().getId();
		} else {
			this.parentId = 0;
		}
		this.baseCost = commodity.getCost();
		this.imagePath = commodity.getImagePath();
		this.amount = amount;
	}
}
