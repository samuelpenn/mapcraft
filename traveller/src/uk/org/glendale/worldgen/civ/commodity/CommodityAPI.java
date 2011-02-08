package uk.org.glendale.worldgen.civ.commodity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import uk.org.glendale.rpg.traveller.civilisation.trade.CommodityCode;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.planet.Resource;

/**
 * Logic for handling commodities.
 * 
 * @author Samuel Penn
 */
public class CommodityAPI {
	private CommodityFactory factory = null;
	
	public CommodityAPI() {
		factory = new CommodityFactory();
	}
	
	/**
	 * Produce goods from a planet's resource. Returns a list of goods which
	 * can be added to the planet's goods list. There are no side effects to
	 * this method - it only works out what needs to be done, but doesn't
	 * actually persist any changes itself.
	 * 
	 * @param planet		Planet to produce goods for.
	 * @param resource		Resource to calculate.
	 * @return				List of goods and quantities.
	 */
	public List<TradeGood> produceFromResource(Planet planet, Resource resource) {
		Commodity	base = resource.getCommodity();
		int			density = resource.getDensity();
		
		System.out.println("produceFromResource: ["+base.getName()+"] ["+density+"]");
		
		long		effectivePopulation = (planet.getPopulation() * density * density) / 10000;
		
		long		producedGoods = base.getProduction(effectivePopulation);
		System.out.println("Base production number: "+producedGoods);

		List<Commodity>	children = factory.getChildren(base);
		Hashtable<Commodity,TradeGood> goods = new Hashtable<Commodity,TradeGood>();
		if (base.hasCode(CommodityCode.PR)) {
			System.out.println("Has PR code ("+children.size()+")");
			children.add(base);
			for (Commodity child : children) {
				long	producedChild = 0;
				if (child.hasCode(CommodityCode.P0)) {
					producedChild = child.getProduction(effectivePopulation);
				} else if (child.hasCode(CommodityCode.P1)) {
					producedChild = child.getProduction(effectivePopulation) / 2;
				} else if (child.hasCode(CommodityCode.P2)) {
					producedChild = child.getProduction(effectivePopulation) / 4;
				} else if (child.hasCode(CommodityCode.P3)) {
					producedChild = child.getProduction(effectivePopulation) / 10;
				}
				
				if (producedChild > 0) {
					System.out.println(child.getName()+": "+producedChild);
					goods.put(child, new TradeGood(child, producedChild, child.getCost()));
				}
			}
		}
		if (base.hasCode(CommodityCode.VR)) {
			for (Commodity child : children) {
				double	modifier = 0.0;
				int		level = 0;
				if (child.hasCode(CommodityCode.V0)) {
					level = 0;
				} else if (child.hasCode(CommodityCode.V1) && density >= 15) {
					level = 20;
				} else if (child.hasCode(CommodityCode.V2) && density >= 35) {
					level = 40;
				} else if (child.hasCode(CommodityCode.V3) && density >= 55) {
					level = 60;
				} else if (child.hasCode(CommodityCode.V4) && density >= 75) {
					level = 80;
				} else {
					// Not a variable resource child, so ignored.
					continue;
				}
				long	producedChild = 0;
				if (goods.get(child) != null) {
					producedChild = goods.get(child).getQuantity();
				} else {
					producedChild = child.getProduction(effectivePopulation);
				}
				level = density - level;
				if (level < 0) {
					producedChild /= Math.pow(10, Math.abs(level));
				} else if (level < 2) {
					producedChild /= 3;
				} else if (level < 5) {
					producedChild /= 2;
				} else if (child.hasCode(CommodityCode.VL) && level > 25) {
					level -= 20;
					producedChild /= Math.pow(10, level/10.0);
				}
				goods.put(child, new TradeGood(child, producedChild, child.getCost()));
			}
		}
		if (goods.size() == 0) {
			goods.put(base, new TradeGood(base, producedGoods, base.getCost()));
		}
		
		List<TradeGood> list = new ArrayList<TradeGood>();
		for (TradeGood tg: goods.values()) {
			list.add(tg);
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		CommodityFactory	commodityFactory = new CommodityFactory();
		PlanetFactory		planetFactory = new PlanetFactory();
		
		Planet	planet = planetFactory.getPlanet(601524);
		List<Resource> list = planet.getResources();
		
		CommodityAPI	api = new CommodityAPI();
		
		System.out.println("Planet population: "+planet.getPopulation());
		
		for (Resource r : list) {
			//System.out.println(r.getCommodity().getName());
			if (r.getCommodity().getName().equals("Mammals")) {
				List<TradeGood> goods = api.produceFromResource(planet, r);
				
				for (TradeGood tg : goods) {
					System.out.println(" --> "+tg.getCommodity().getName()+": "+tg.getQuantity());
				}
			}
		}
	}
	
	
}
