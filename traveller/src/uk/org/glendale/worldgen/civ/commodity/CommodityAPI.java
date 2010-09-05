package uk.org.glendale.worldgen.civ.commodity;

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
	
	public void produceFromResource(Planet planet, Resource resource) {
		Commodity	base = resource.getCommodity();
		int			density = resource.getDensity();
		
		System.out.println("produceFromResource: ["+base.getName()+"] ["+density+"]");
		
		long		effectivePopulation = (planet.getPopulation() * density * density) / 10000;
		
		long		producedGoods = base.getProduction(effectivePopulation);
		System.out.println("Base production number: "+producedGoods);

		List<Commodity>	children = factory.getChildren(base);
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
				}
			}
		}
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
				api.produceFromResource(planet, r);
			}
		}
	}
	
	
}
