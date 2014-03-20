/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.test;

import java.io.File;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorCode;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityGenerator;
import uk.org.glendale.worldgen.civ.trade.CivilisationAPI;
import uk.org.glendale.worldgen.civ.trade.CivilisationFactory;

/**
 * Create a sand box universe for testing purposes. Initiates the spring context
 * and wires everything as required. The src/main/webapp directory must be in
 * the class path.
 * 
 * @author Samuel Penn
 */
public class Sandbox {
	private static final String	CONFIG	= "/WEB-INF/spring/servlet-context.xml";

	private SectorFactory		sectorFactory;
	private StarSystemFactory	starSystemFactory;
	private StarSystemGenerator	starSystemGenerator;
	private CommodityFactory	commodityFactory;
	private FacilityFactory		facilityFactory;
	private FacilityGenerator	facilityGenerator;
	private PlanetFactory		planetFactory;
	private CivilisationFactory	civilisationFactory;
	private CivilisationAPI		civilisationAPI;

	public Sandbox() {

	}

	public Sandbox(ApplicationContext context) {
		sectorFactory = (SectorFactory) context.getBean("sectorFactory");
		starSystemFactory = (StarSystemFactory) context
				.getBean("starSystemFactory");
		starSystemGenerator = (StarSystemGenerator) context
				.getBean("starSystemGenerator");
		commodityFactory = (CommodityFactory) context
				.getBean("commodityFactory");
		facilityFactory = (FacilityFactory) context.getBean("facilityFactory");
		facilityGenerator = (FacilityGenerator) context
				.getBean("facilityGenerator");
		planetFactory = (PlanetFactory) context.getBean("planetFactory");
		civilisationFactory = (CivilisationFactory) context
				.getBean("civilisationFactory");
		civilisationAPI = (CivilisationAPI) context.getBean("civilisationAPI");
	}

	public void importCommodities() {
		String base = "src/main/resources/commodities/";
		String[] files = { "minerals.xml", "organic.xml", "primitive.xml" };

		// Requires two passes to build everything. This allows mappings
		// to refer to future commodities.
		for (String file : files) {
			try {
				commodityFactory.createCommodities(new File(base + file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (String file : files) {
			try {
				commodityFactory.createMappings(new File(base + file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void importFacilities() throws Exception {
		String base = "src/main/resources/facilities/";
		facilityGenerator.createAllFacilities(new File(base));
	}

	private void createSectors() {
		String[] names = { "Aquila", "Serpens", "Virgo", "Aquarius", "Sol",
				"Hydra", "Pisces", "Taurus", "Orion" };

		int x = -1, y = -1;
		for (String name : names) {
			if (sectorFactory.getSector(name) == null) {
				sectorFactory.createSector(name, x, y, "Un", SectorCode.Fe);
			}
			if (++x > 1) {
				x = -1;
				y++;
			}
		}
		/*
		 * create("Aquila", -1, -1, "hF"); create("Serpens", 0, -1, "hG");
		 * create("Virgo", 1, -1, "hH"); create("Aquarius", -1, 0, "iF");
		 * //create("Sol", 0, 0, "iG"); create("Hydra", 1, 0, "iH");
		 * create("Pisces", -1, 1, "jF"); create("Taurus", 0, 1, "jG");
		 * create("Orion", 1, 1, "jH");
		 * 
		 * // Other sectors. create("Rift", -2, -2, "gE"); create("Passage", -1,
		 * -2, "gF"); create("Borders", -2, -1, "hE"); create("Dominion", -2, 0,
		 * "iE");
		 */
	}

	/**
	 * Add the specified number of star systems to each of the available
	 * sectors. Each star system is placed completely randomly in the sector.
	 * 
	 * @param number
	 *            Number of star systems to add to each sector.
	 */
	public void addManyToSandbox(int number) {
		List<Sector> sectors = sectorFactory.getAllSectors();
		for (Sector sector : sectors) {
			for (int i = 0; i < number; i++) {
				starSystemGenerator.createSimpleSystem(sector, null, 0, 0);
			}
		}
	}

	public void addManyToSandbox(int sectorId, int number) {
		Sector sector = sectorFactory.getSector(sectorId);
		for (int i = 0; i < number; i++) {
			starSystemGenerator.createSimpleSystem(sector, null, 0, 0);
		}
	}

	public void addToCore() {
		String sectorName = "Sol";
		Sector sandbox = sectorFactory.getSector(sectorName);
		starSystemGenerator.createSimpleSystem(sandbox, null, Die.d8(),
				Die.d10());
	}

	/**
	 * Add a random star system to the test sector.
	 */
	public void addToSandbox() {
		String sectorName = "Aquila";
		int sectorId = Die.die(9);
		Sector sandbox = sectorFactory.getSector(sectorId);
		if (sandbox == null) {
			sandbox = sectorFactory.createSector(sectorName, 0, 0, "Un",
					SectorCode.Fe);
		}
		starSystemGenerator.createSimpleSystem(sandbox, null, 0, 0);
	}

	public void testSimulation(int planetId) {
		civilisationAPI.simulate(planetId);
		// Planet planet = planetFactory.getPlanet(planetId);
		// Civilisation civ = civilisationFactory.getCivilisation(planet);

		// civ.simulate();
	}

	public void testSimulation() {
		civilisationAPI.simulate();
	}

	/**
	 * Run the application and initiates the spring context. The src/main/webapp
	 * directory must be in the class path.
	 * 
	 * @param args
	 *            None used.
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(CONFIG);

		Sandbox sb = new Sandbox(context);
		// sb.createSectors();
		if (sb.commodityFactory.getCommodity(1) == null) {
			sb.importCommodities();
			sb.importFacilities();
			sb.addManyToSandbox(1);
		}
		sb.addToSandbox();
		// sb.addToCore();
		// sb.addManyToSandbox(1, 20);

		// sb.testSimulation(1098);
		sb.testSimulation();
	}
}
