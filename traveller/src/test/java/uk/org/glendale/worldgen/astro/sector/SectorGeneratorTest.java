/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;
import uk.org.glendale.worldgen.text.Names;

public class SectorGeneratorTest {
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void testSectorGenerator() {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		SectorGenerator generator = new SectorGenerator();
		Sector s = generator.createEmptySector("Test", 3, 4, "Sp Ba Lo", "Un");

		Assert.assertNotNull(s);
		Assert.assertTrue(s.getId() != 0);

		SectorFactory factory = new SectorFactory();
		s = factory.getSector("Test");
		Assert.assertEquals("Test", s.getName());
		Assert.assertEquals("Un", s.getAllegiance());
		Assert.assertEquals(3, s.getX());
		Assert.assertEquals(4, s.getY());
		Assert.assertTrue(s.hasCode(SectorCode.Sp));
		Assert.assertTrue(s.hasCode(SectorCode.Ba));
		Assert.assertTrue(s.hasCode(SectorCode.Lo));
	}

	@Test
	public void testWithEntityManager() {
		SectorGenerator generator = new SectorGenerator(AppManager
				.getInstance().getEntityManager());
		Assert.assertNotNull(generator.createEmptySector("Test 2", 5, 5, "Sp",
				"Un"));

	}

	@Test
	public void testGeneration() throws MalformedURLException {
		SectorGenerator generator = new SectorGenerator();
		SectorFactory factory = new SectorFactory();
		Sector s = factory.getSector("Test");

		generator.fillRandomSector(s, new Names("names"), 1);
	}

}
