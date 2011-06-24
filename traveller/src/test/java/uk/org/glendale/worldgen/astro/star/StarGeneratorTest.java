/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.server.SQLReader;

public class StarGeneratorTest {
	@BeforeClass
	public static void setup() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void singleTest() {
		Sector sector = new Sector();
		StarSystem system = new StarSystem(sector, "Test", 1, 1);

		StarGenerator sg = new StarGenerator(system, false);

		Star star = sg.generatePrimary();
		Assert.assertNotNull(star);
		Assert.assertEquals("Test", star.getName());
		Assert.assertNotNull(star.getClassification());
		Assert.assertNotNull(star.getSpectralType());
		Assert.assertNotNull(star.getForm());

		Assert.assertTrue(star.getDistance() == 0);
	}

	@Test
	public void doubleTest() {
		SectorGenerator secGen = new SectorGenerator();
		Sector sector = secGen.createEmptySector("Test", 0, 0, "", "");

		StarSystemGenerator sysGen = new StarSystemGenerator();
		StarSystemFactory sysFac = new StarSystemFactory();

		StarSystem system = sysGen.createEmptySystem(sector, "Test", 1, 1);

		StarGenerator sg = new StarGenerator(system, true);

		Star star = sg.generatePrimary();
		Assert.assertNotNull(star);
		Assert.assertEquals("Test Alpha", star.getName());
		Assert.assertNotNull(star.getClassification());
		Assert.assertNotNull(star.getSpectralType());
		Assert.assertNotNull(star.getForm());

		Assert.assertTrue(star.getDistance() == 0);
		system = sysFac.getStarSystem(1);
		system.addStar(star);
		sysFac.persist(system);

		star = sg.generateSecondary();
		Assert.assertEquals("Test Beta", star.getName());
		Assert.assertTrue(star.getDistance() > 0);
		system.addStar(star);
		sysFac.persist(system);

		star = sg.generateTertiary();
		Assert.assertEquals("Test Gamma", star.getName());
		Assert.assertTrue(star.getDistance() > 0);
	}
}
