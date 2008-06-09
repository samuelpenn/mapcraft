package uk.org.glendale.rpg.traveller.civilisation.trade;

import java.text.NumberFormat;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;

/**
 * Calculates trade requirements for a world.
 * 
 * @author Samuel Penn
 */
public class Trade {
	private ObjectFactory		factory = null;
	private Planet				planet = null;
	private Hashtable<Integer,Commodity>	commodities = null;
	
	private static NumberFormat		format = NumberFormat.getInstance();
	
	public Trade(ObjectFactory factory, Planet planet) {
		this.factory = factory;
		this.planet = planet;
		
		commodities = factory.getAllCommodities();
		
		Hashtable<Integer,Long>	amounts = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int key : amounts.keySet()) {
			long		amount = amounts.get(key);
			Commodity	c = commodities.get(key);
			c.setAmount(amount);
		}
	}
		
	public Planet getPlanet() {
		return planet;
	}
	
	private long getWorkersRequired(Commodity c) {
		return getWorkersRequired(c, 10);
	}
	
	private long getWorkersRequired(Commodity c, int density) {
		if (density < 1) {
			return Long.MAX_VALUE;
		} else if (density > 10) {
			density = 10;
		}
		
		long		workersRequired = c.getProductionRate() * 1000 / (int)Math.pow(density, 3);
		
		if (planet.getTechLevel() < c.getTechLevel()-1) {
			// Tech level is way too low, return infinite number of workers.
			return Long.MAX_VALUE;
		} else if (planet.getTechLevel() == c.getTechLevel()-1) {
			// May be able to make something, unless commodity is heavily TL dependant.
			if (c.hasCode(CommodityCode.Tl)) {
				return Long.MAX_VALUE;
			}
			workersRequired *= 5;
		} else if (planet.getTechLevel() > c.getTechLevel()) {
			if (c.hasCode(CommodityCode.Tl)) {
				workersRequired /= (1 + planet.getTechLevel() - c.getTechLevel());
			} else {
				workersRequired /= Math.sqrt(1 + planet.getTechLevel() - c.getTechLevel());
			}
		}
		
		if (c.getSource() == Source.Ag) {
			if (planet.hasTradeCode(TradeCode.Ag)) {
				workersRequired /= 2;
			} else if (planet.hasTradeCode(TradeCode.Na)) {
				workersRequired *= 3;
			}
		} else if (c.getSource() == Source.In) {
			if (planet.hasTradeCode(TradeCode.In)) {
				workersRequired /= 2;
			} else if (planet.hasTradeCode(TradeCode.Ni)) {
				workersRequired *= 10;
			} else {
				workersRequired *= 3;
			}
		} else if (c.getSource() == Source.Mi) {
			if (planet.hasTradeCode(TradeCode.Mi)) {
				// This is assumed to be a dedicated mining colony, where
				// everyone is focused on mining, so less support people
				// are needed.
				workersRequired /= 100;
			} else if (planet.hasTradeCode(TradeCode.In)) {
				// No change.
			} else if (planet.hasTradeCode(TradeCode.Ni)) {
				workersRequired *= 4;
			} else if (planet.hasTradeCode(TradeCode.Ag)) {
				workersRequired *= 2;
			}
		}
		
		if (planet.getLawLevel() > c.getLegality()) {
			workersRequired *= Math.pow(10, planet.getLawLevel() - c.getLegality());
		}
		
		return workersRequired;
	}
	
	/**
	 * Work out what resources are gathered this week from the planet's
	 * stock of natural resources.
	 */
	public void gatherResources() {
		Hashtable<Integer,Integer> list = factory.getResources(planet.getId());
		
		// For each commodity, work out how much is gathered.
		System.out.println("Production rates");
		for (int i : list.keySet()) {
			Commodity	c = commodities.get(i); 
			
			if (c == null) {
				c = factory.getCommodity(i);
			}
			
			int			density = list.get(i);
			long		workersRequired = getWorkersRequired(c, density);
			long		produced = planet.getPopulation() / workersRequired;
			
			System.out.println("  "+c.getName() + "("+c.getAmount()+") - "+produced);
			c.setAmount(c.getAmount() + produced);
			factory.setCommodity(planet.getId(), c.getId(), c.getAmount());
		}
	}
	
	public void consumeResources() {
		for (int i : commodities.keySet()) {
			Commodity	c = commodities.get(i); 
		}
	}

	public void productionAbility() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			effortRequired = c.getProductionRate();
			
			if (effortRequired == 0 || planet.getPopulation() == 0) {
				System.out.println("  "+c.getName()+" not wanted");
				continue;
			}
			
			if (planet.hasTradeCode(TradeCode.Ri)) {
				effortRequired *= 0.5;
			} else if (planet.hasTradeCode(TradeCode.Po)) {
				effortRequired *= 1.2;
			}
			
			// Tech level requirements
			int techGap = planet.getTechLevel() - c.getTechLevel();
			if (techGap < -1) {
				continue;
			} else if (techGap == -1) {
				effortRequired *= 10;
			} else if (techGap == 0) {
				effortRequired *= 2;
			} else {
				if (c.hasCode(CommodityCode.Tl)) {
					effortRequired /= techGap+1;
				} else if (c.hasCode(CommodityCode.TL)) {
					effortRequired /= ((techGap+1) * (techGap+1));
				}
			}
			
			// Legal requirements
			int legality = planet.getLawLevel() - c.getLegality();
			if (legality < -2) {
				continue;
			} else if (legality == -2) {
				effortRequired *= 100;
			} else if (legality == -1) {
				effortRequired *= 10;
			}
			if (c.hasCode(CommodityCode.Il)) {
				effortRequired *= 10;
			}
			
			if (c.getSource() == Source.Ag) {
				if (planet.hasTradeCode(TradeCode.Na)) {
					effortRequired *= 25;
				} else if (!planet.hasTradeCode(TradeCode.Ag)) {
					effortRequired *= 5;
				}
			}
			if (c.getSource() == Source.In) {
				if (planet.hasTradeCode(TradeCode.Ni)) {
					effortRequired *= 100;
				} else if (!planet.hasTradeCode(TradeCode.In)) {
					effortRequired *= 10;
				}
			}
			if (c.getSource() == Source.Mi) {
				if (planet.hasTradeCode(TradeCode.Ag)) {
					effortRequired *= 3;
				}
				if (planet.hasTradeCode(TradeCode.Ni)) {
					effortRequired *= 3;
				}
			}
			
			long		production = planet.getPopulation() / effortRequired;
			c.setProduction(production);
			//System.out.println("  "+c.getName()+" : "+format.format(production));
		}
	}
	
	public void demand() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			demand = c.getConsumptionRate();
			
			if (demand == 0 || planet.getPopulation() == 0) {
				System.out.println("  "+c.getName()+" not wanted");
				continue;
			}
			
			if (planet.hasTradeCode(TradeCode.Ri)) {
				if (c.hasCode(CommodityCode.Lu)) {
					demand *= 0.9;
				}
			} else if (planet.hasTradeCode(TradeCode.Po)) {
				if (c.hasCode(CommodityCode.Lu)) {
					demand *= 5;
				}
			}
			
			// Tech level requirements
			if (c.hasCode(CommodityCode.Lo)) {
				// Ultra-tech
				if (planet.getTechLevel() > 6) {
					demand *= Math.pow(10, (planet.getTechLevel()-6));
				}
			} else if (c.hasCode(CommodityCode.Mi)) {
				if (planet.getTechLevel() < 4) {
					demand = 0;
				} else if (planet.getTechLevel() < 6) {
					demand *= 100;
				} else if (planet.getTechLevel() == 6) {
					demand *= 3;
				} else if (planet.getTechLevel() == 9) {
					demand *= 3;
				} else if (planet.getTechLevel() > 9) {
					demand *= 10;
				}
			} else if (c.hasCode(CommodityCode.Hi)) {
				if (planet.getTechLevel() < 7) {
					demand = 0;
				} else if (planet.getTechLevel() == 7) {
					demand *= 30;
				} else if (planet.getTechLevel() == 8) {
					demand *= 3;
				} else if (planet.getTechLevel() == 11) {
					demand *= 3;
				} else if (planet.getTechLevel() > 11) {
					demand *= 10;
				}
			} else if (c.hasCode(CommodityCode.Ul)) {
				if (planet.getTechLevel() < 10) {
					demand = 0;
				} else if (planet.getTechLevel() == 10) {
					demand *= 10;
				}
			}
			
			if (c.hasCode(CommodityCode.In)) {
				if (planet.hasTradeCode(TradeCode.Ni)) {
					demand *= 1000;
				} else if (!planet.hasTradeCode(TradeCode.In)) {
					demand *= 10;
				}
			}
			if (c.hasCode(CommodityCode.Ag)) {
				if (planet.hasTradeCode(TradeCode.Na)) {
					demand *= 1000;
				} else if (!planet.hasTradeCode(TradeCode.Ag)) {
					demand *= 10;
				}
			}
			
			long		desired = 0;
			if (demand > 0) desired = planet.getPopulation() / demand;
			c.setDesired(desired);
			//System.out.println("  "+c.getName()+" : "+format.format(desired));
		}
	}
	
	public void results() {
		for (int id : commodities.keySet()) {
			Commodity		c = commodities.get(id);
			long			desired = c.getDesired();
			long			production = c.getProduction();
			
			System.out.println("  "+c.getName()+" : "+format.format(production - desired));
		}
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		try {
			Planet			planet = factory.getPlanet(214841);
			Trade			trade = new Trade(factory, planet);
			trade.gatherResources();
			/*
			for (int id : new int[] { 212031, 212304} ) {
				Planet			planet = factory.getPlanet(id);
				Trade			trade = new Trade(factory, planet);

				System.out.print(planet.getName()+" ("+planet.getType()+")  "+NumberFormat.getInstance().format(planet.getPopulation())+" - "+planet.getTechLevel()+"/"+planet.getLawLevel()+" [");
				String	codes = "";
				for (String code : planet.getTradeCodes()) {
					codes += code+" ";
				}
				System.out.println(codes.trim()+"]");
				trade.productionAbility();
				trade.demand();
				
				trade.results();
			}
			
			Sector sector = new Sector(factory, 104);
			System.out.println(sector.getBTN(factory.getPlanet(212031), factory.getPlanet(212304)));
			*/
		} finally {
			factory.close();
			factory = null;
		}
	}
}
