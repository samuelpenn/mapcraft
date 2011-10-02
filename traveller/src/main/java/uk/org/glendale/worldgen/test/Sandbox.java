/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorCode;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityGenerator;
import uk.org.glendale.worldgen.civ.trade.Civilisation;
import uk.org.glendale.worldgen.civ.trade.CivilisationAPI;
import uk.org.glendale.worldgen.civ.trade.CivilisationFactory;

/**
 * Create a sandbox universe for testing purposes.
 * Initiates the spring context and wires everything as required.
 * 
 * @author Samuel Penn
 */
public class Sandbox {
	private static final String CONFIG = "/WEB-INF/spring/servlet-context.xml";
	
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
		starSystemFactory = (StarSystemFactory) context.getBean("starSystemFactory");
		starSystemGenerator = (StarSystemGenerator) context.getBean("starSystemGenerator");
		commodityFactory = (CommodityFactory) context.getBean("commodityFactory");
		facilityFactory = (FacilityFactory) context.getBean("facilityFactory");
		facilityGenerator = (FacilityGenerator) context.getBean("facilityGenerator");
		planetFactory = (PlanetFactory) context.getBean("planetFactory");
		civilisationFactory = (CivilisationFactory) context.getBean("civilisationFactory");
		civilisationAPI = (CivilisationAPI) context.getBean("civilisationAPI");
	}
	
	public void importCommodities() {
		String base = "src/main/resources/commodities/";
		/*
		commodityFactory.createCommodity("Foo");
		if (1 < 2) {
			return;
		}
		*/

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
	
	public void addToSandbox() {
		Sector	sandbox = sectorFactory.getSector("Sandbox");
		if (sandbox == null) {
			sandbox = sectorFactory.createSector("Sandbox", 0, 0, "Un", SectorCode.Fe);
		}
		starSystemGenerator.createSimpleSystem(sandbox, null, 0, 0);
	}
	
	public void testSimulation(int planetId) {
		civilisationAPI.simulate(planetId);
//		Planet planet = planetFactory.getPlanet(planetId);
//		Civilisation civ = civilisationFactory.getCivilisation(planet);
		
//		civ.simulate();
	}
	
	private static void sandbox() {
		/*
		HttpClient	client = new HttpClient();
		
		GetMethod get = new GetMethod("http://localhost:8080/Traveller/api/sector/Test");
		get.setRequestHeader("Accept", "application/json;");
		int status = client.executeMethod(get);
		System.out.println(status);
		*/
		
	}

	/**
	 * Run the application and initiates the spring context.
	 * 
	 * @param args			None used.
	 * @throws Exception	If anything goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext 	context = new ClassPathXmlApplicationContext(CONFIG);
		
		Sandbox sb = new Sandbox(context);
		
		//sb.importCommodities();
		//sb.importFacilities();
		//sb.addToSandbox();
		
		sb.testSimulation(87);
	}


}
