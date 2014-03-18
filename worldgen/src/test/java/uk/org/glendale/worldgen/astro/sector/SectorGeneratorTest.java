/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;
import uk.org.glendale.worldgen.text.Names;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/servlet-context.xml" })
public class SectorGeneratorTest {
	@Autowired
	private SectorGenerator	generator;
	
	@Autowired
	private SectorFactory	factory;
	
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void testSectorGenerator() {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		Sector s = generator.createEmptySector("Test", 3, 4, "Sp Ba Lo", "Un");

		Assert.assertNotNull(s);
		Assert.assertTrue(s.getId() != 0);

		s = factory.getSector("Test");
		Assert.assertEquals("Sector name is wrong.", "Test", s.getName());
		Assert.assertEquals("Allegiance is wrong.", "Un", s.getAllegiance());
		Assert.assertEquals("X is wrong.", 3, s.getX());
		Assert.assertEquals("Y is wrong.", 4, s.getY());
		Assert.assertTrue("Does not have code Sp", s.hasCode(SectorCode.Sp));
		Assert.assertTrue("Does not have code Ba", s.hasCode(SectorCode.Ba));
		Assert.assertTrue("Does not have code Lo", s.hasCode(SectorCode.Lo));
	}

	@Test
	public void testWithEntityManager() {
		Assert.assertNotNull(generator.createEmptySector("Test 2", 5, 5, "Sp",
				"Un"));
	}

	/**
	 * Test generation of a whole sector. This is a very broad test, which will
	 * test random parts of the code (depending on which worlds get created).
	 * The sector created is about as sparse as we can make it, so that the test
	 * runs quickly.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testGeneration() throws ParserConfigurationException,
			SAXException, IOException {
		// First, make sure we have commodities.
		CommodityFactory comFac = new CommodityFactory();
		comFac.createAllCommodities(new File("src/main/resources/commodities"));

		// Now, build the star systems.
		Sector s = factory.getSector("Test");

		generator.fillRandomSector(s, new Names("names"), 1);
	}

}
