/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.test;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorCode;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Create a sandbox universe for testing purposes.
 * Initiates the spring context and wires everything as required.
 * 
 * @author Samuel Penn
 */
public class Sandbox {
	private static final String CONFIG = "/WEB-INF/spring/servlet-context.xml";
	
	private SectorFactory		sectorFactory;
	
	
	public Sandbox(ApplicationContext context) {
		sectorFactory = (SectorFactory) context.getBean("sectorFactory");
	}
	
	public void addToSandbox() {
		Sector	sandbox = sectorFactory.getSector("Sandbox");
		if (sandbox == null) {
			sandbox = sectorFactory.createSector("Sandbox", 0, 0, "Un", SectorCode.Fe);
		}
		
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
		
		System.out.println(context.containsBean("sectorFactory"));
		SectorFactory sf = (SectorFactory)context.getBean("sectorFactory");
		
		sf.createSector("Sandbox", 1, 1, "Un");
		
		List<Sector> list = sf.getAllSectors();
		System.out.println(list.size());
		for (Sector s : list) {
			System.out.println(s.getId()+": "+s.getName() + " (" + s.getAllegiance() + ")");
			System.out.println(sf.getSector(s.getId()).getName());
		}
		
		

		/*
		AppManager app = new AppManager();
		SectorGenerator sg = new SectorGenerator(app.getEntityManager());
		SectorFactory sf = new SectorFactory(app.getEntityManager());

		List<Sector> list = sf.getAllSectors();
		System.out.println(list.size());

		if (list.size() == 0) {
			sg.createEmptySector("Sandbox Core", 0, 0, "", "Un");
		}
		Sector sector = sf.getSector("Sandbox Core");
		// sg.clearSector(sector);
		// sg.fillRandomSector(sector, new Names("names"), 20);
		app.getEntityManager().close();
		System.out.println("Sector completed");
		*/
	}


}
