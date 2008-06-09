package uk.org.glendale.rpg.traveller.civilisation.trade;

import java.util.*;

import java.sql.*;

public class Commodity {
	private int 	id;
	private String	name;
	private Source	source;
	private int		cost;
	private int		volume;
	
	private int		legality;
	private int		productionRate;
	private int		consumptionRate;
	private int		techLevel;
	
	private long	amount;
	private long	production;
	private long	desired;
	private EnumSet<CommodityCode>	codes = EnumSet.noneOf(CommodityCode.class);
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getUnitCost() {
		return cost;
	}
	
	public int getUnitVolume() {
		return volume;
	}
	
	public int getLegality() {
		return legality;
	}

	public Source getSource() {
		return source;
	}
	
	// Constants to make some of the lookup tables more readable.
	private static final long BILLION  = 1000000000;
	private static final long MILLION  = 1000000;
	private static final long THOUSAND = 1000;
	
	/**
	 * Get the production rate for this good, in terms of number of people
	 * required to produce it. High numbers means fewer items will be
	 * produced for a given population size.
	 * 
	 * Internally, this is stored as a value from 0 upwards, normally in
	 * the range 1-9, though values up to 15 are possible.
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
}
