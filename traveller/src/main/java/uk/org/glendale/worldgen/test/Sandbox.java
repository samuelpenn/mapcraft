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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Create a sandbox universe for testing purposes.
 * 
 * @author Samuel Penn
 */
public class Sandbox {
	//@Autowired
	private SectorFactory		sectorFactory;
	
	private static void sandbox() {

	}

	public static void main(String[] args) throws Exception {
		ApplicationContext context;
		
		
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		System.out.println(context.containsBean("sectorFactory"));
		SectorFactory sf = (SectorFactory)context.getBean("sectorFactory");
		sf.getAllSectors();

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
