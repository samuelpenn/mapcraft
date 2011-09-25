/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.astro.planet.builders.gaian.Gaian;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.astro.star.SpectralType;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.star.StarClass;
import uk.org.glendale.worldgen.astro.star.StarForm;
import uk.org.glendale.worldgen.astro.star.StarGenerator;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;

/**
 * Test planet generation. This is a complex set of tests.
 * 
 * @author Samuel Penn
 */
public class PlanetGeneratorTest {
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void testPlanetGenerator() throws ParserConfigurationException,
			SAXException, IOException {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		// First, make sure we have commodities.
		CommodityFactory comFac = new CommodityFactory();
		comFac.createAllCommodities(new File("src/main/resources/commodities"));

		// Generate a sector and star system.
		SectorGenerator sg = new SectorGenerator();
		StarSystemGenerator ssg = new StarSystemGenerator();
		StarSystemFactory ssf = new StarSystemFactory();

		Sector sector = sg.createEmptySector("Test", 0, 0, "", "Un");
		StarSystem system = ssg.createEmptySystem(sector, "Testing", 1, 1);

		StarGenerator starGen = new StarGenerator(system, false);
		Star star = starGen.generatePrimary(StarForm.Star, StarClass.V,
				SpectralType.G2);

		system = ssf.getStarSystem(1);
		system.addStar(star);
		ssf.persist(system);

		PlanetGenerator pg = new PlanetGenerator(new PlanetFactory(), system, star);
		Planet planet = pg.generatePlanet("Test I", 1, 150, new Gaian());

		system.addPlanet(planet);
		ssf.persist(system);
	}
}
