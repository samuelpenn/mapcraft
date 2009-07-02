/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

import java.util.*;

import java.sql.*;

/**
 * Defines a commodity within the trade system.
 *
 * @author Samuel Penn
 */
public class Commodity {
	private int 	id;
	private String	name;
	private String	image;
	private Source	source;
	private int		cost;
	private int		actualPrice;
	private int		volume;
	
	private int		legality;
	private int		productionRate;
	private int		consumptionRate;
	private int		techLevel;
	
	private long	amount;
	private long	production;
	private long	desired;
	private EnumSet<CommodityCode>	codes = EnumSet.noneOf(CommodityCode.class);
	
	/**
	 * Unique id of this commodity.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Descriptive name of this commodity.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Standard base price per unit of this commodity.
	 */
	public int getUnitCost() {
		return cost;
	}
	
	/**
	 * How many displacement tonnes a unit of this commodity fills in
	 * the cargo hold. This is currently expected to be 1 for all items,
	 * though other values are supported in case of future changes.
	 */
	public int getUnitVolume() {
		return volume;
	}
	
	/**
	 * The law level at which this commodity can be bought/sold. Normally
	 * this will be '6' for standard goods (completely unrestricted).
	 * Lower legality codes will be restricted at worlds with a high
	 * law level.
	 */
	public int getLegality() {
		return legality;
	}

	/**
	 * The type of good, in terms of where it is sourced from. May
	 * be 'Ag' for agricultural goods, 'Mi' for mined resources,
	 * 'In' for industrial goods etc.
	 */
	public Source getSource() {
		return source;
	}
	
	// Constants to make some of the lookup tables more readable.
	private static final long BILLION  = 1000000000;
	private static final long MILLION  = 1000000;
	private static final long THOUSAND = 1000;
	
	/**
	 * Get the production rate for this good, in terms of number of people
	 * required to produce it. The higher the number, the bigger the
	 * population is needed to support a given level of production.
	 * A value of X equates to 1 unit of commodity being produced each
	 * week per X population.
	 * 
	 * Internally, this is stored as a value from 0 upwards, normally in
	 * the range 1-9, though values up to 15 are possible. A low rating
	 * means more workers required, so fewer goods produced.
	 * 
	 * A high production rate does not necessarily mean that making a
	 * single unit requires lots of people to work on it, but just that
	 * a large supporting infrastructure is required.
	 */
	public long getProductionRate() {
		switch (productionRate) {
		case 0:  return 1 * BILLION;
		case 1:  return 100 * MILLION;
		case 2:  return 10 * MILLION;
		case 3:  return 1 * MILLION;
		case 4:  return 200 * THOUSAND;
		case 5:  return 40 * THOUSAND;
		case 6:  return 10 * THOUSAND;
		case 7:  return 3 * THOUSAND;
		case 8:  return 1 * THOUSAND;
		case 9:  return 500;
		case 10: return 250;
		case 11: return 150;
		case 12: return 100;
		case 13: return 75;
		case 14: return 50;
		case 15: return 25;
		}
		return 1;
	}
	
	/**
	 * Get the level of demand for this good, based on its defined codes.
	 * This is the proportion of people who require the good, so a value
	 * of 1000, means 1 in 1000 people want it. Low numbers are more in
	 * demand than high numbers.
	 * 
	 * A good like food, which everybody needs, is rated at 10. Most goods
	 * will be lower than this.
	 */
	public long getConsumptionRate() {
		switch (consumptionRate) {
		case 0:  return 1 * BILLION;
		case 1:  return 100 * MILLION;
		case 2:  return 10 * MILLION;
		case 3:  return 1 * MILLION;
		case 4:  return 200 * THOUSAND;
		case 5:  return 40 * THOUSAND;
		case 6:  return 10 * THOUSAND;
		case 7:  return 3 * THOUSAND;
		case 8:  return 1 * THOUSAND;
		case 9:  return 500;
		case 10: return 250;
		case 11: return 150;
		case 12: return 100;
		case 13: return 75;
		case 14: return 50;
		case 15: return 25;
		}
		return 1;
	}
	
	public int getTechLevel() {
		return techLevel;
	}
	
	public boolean hasCode(CommodityCode code) {
		return codes.contains(code);
	}
	
	public Commodity(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		cost = rs.getInt("cost");
		volume = rs.getInt("dt");
		productionRate = rs.getInt("production");
		consumptionRate = rs.getInt("consumption");
		techLevel = rs.getInt("tech");
		legality = rs.getInt("law");
		source = Source.valueOf(rs.getString("source"));
		image = rs.getString("image");
		
		String	c = rs.getString("codes");
		
		for (String code : c.split(" ")) {
			if (code.trim().length() == 0) continue;
			try {
				codes.add(CommodityCode.valueOf(code));
			} catch (Throwable e) {
				System.out.println("Commodity ["+name+"] has unrecognised code ["+code+"]");
			}
		}
	}
	
	public long getAmount() {
		return amount;
	}
	
	public void setAmount(long amount) {
		this.amount = amount;
	}
	
	public long getProduction() {
		return production;
	}
	
	public void setProduction(long production) {
		this.production = production;
	}
	
	public long getDesired() {
		return desired;
	}
	
	public void setDesired(long desired) {
		this.desired = desired;
	}
	
	public int getCost() {
		return cost;
	}
	
	/**
	 * The actual price is the price a particular planet is buying/selling
	 * at. This is not stored in the database against the commodity, but
	 * is used as a temporary store when working out trade results for a
	 * planet.
	 */
	public int getActualPrice() {
		return actualPrice;
	}
	
	public void setActualPrice(int price) {
		this.actualPrice = price;
	}
	
	public String getImage() {
		return image;
	}
	
	/**
	 * Amount produced may be modified by the planet's tech level.
	 * @param planetTechLevel
	 * @param amount
	 * @return
	 */
	public long getAmountModifiedByTech(int planetTechLevel, long amount) {
		int tl = planetTechLevel - techLevel;
		if (hasCode(CommodityCode.TL)) {
			if (tl > 0) amount *= (tl + 1) * 2;
			if (tl < 0) amount = 0;
		} else if (hasCode(CommodityCode.Tl)) {
			if (tl > 0) amount *= (tl + 1);
			if (tl < 0) amount /= Math.abs(tl + 1);
		} else {
			if (tl > 0) amount *= Math.sqrt(tl + 1);
		}
		return amount;
	}
}
