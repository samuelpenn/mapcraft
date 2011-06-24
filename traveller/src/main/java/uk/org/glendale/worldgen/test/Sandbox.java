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
	private static void sandbox() {

	}

	public static void main(String[] args) throws Exception {
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
	}

}
