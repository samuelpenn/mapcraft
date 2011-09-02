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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.server.SQLReader;

/**
 * Strictly speaking, these are integration tests.
 * 
 * @author Samuel Penn
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/servlet-context.xml" })
public class StarGeneratorTest {
	
	@Autowired
	private SectorGenerator		sectorGenerator;
	
	@Autowired
	private SectorFactory		sectorFactory;
	
	@Autowired
	private StarSystemGenerator	systemGenerator;
	
	@Autowired
	private StarSystemFactory	systemFactory;
	
	@BeforeClass
	public static void setup() {
		SQLReader.setupTestDatabase();
	}

	//@Test
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
	@Transactional
	public void doubleTest() {
		Sector sector = sectorGenerator.createEmptySector("Test", 0, 0, "", "");
		sector = sectorFactory.getSector("Test");

		StarSystem system = systemGenerator.createEmptySystem(sector, "Test", 1, 1);

		StarGenerator sg = new StarGenerator(system, true);

		Star star = sg.generatePrimary();
		Assert.assertNotNull(star);
		Assert.assertEquals("Test Alpha", star.getName());
		Assert.assertNotNull(star.getClassification());
		Assert.assertNotNull(star.getSpectralType());
		Assert.assertNotNull(star.getForm());

		Assert.assertTrue(star.getDistance() == 0);
		system = systemFactory.getStarSystem(1);
		system.addStar(star);
		systemFactory.persist(system);

		star = sg.generateSecondary();
		Assert.assertEquals("Test Beta", star.getName());
		Assert.assertTrue(star.getDistance() > 0);
		system.addStar(star);
		systemFactory.persist(system);

		star = sg.generateTertiary();
		Assert.assertEquals("Test Gamma", star.getName());
		Assert.assertTrue(star.getDistance() > 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalid() {
		Sector sector = new Sector();
		StarSystem system = new StarSystem(sector, "Test", 1, 1);

		StarGenerator sg = new StarGenerator(system, false);
		sg.generateSecondary();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalid2() {
		Sector sector = new Sector();
		StarSystem system = new StarSystem(sector, "Test", 1, 1);

		StarGenerator sg = new StarGenerator(system, false);
		sg.generateTertiary();
	}

	//@Test(expected = IllegalStateException.class)
	public void testInvalid3() {
		SectorFactory secFac = new SectorFactory();
		Sector sector = secFac.getSector("Test");

		StarSystemGenerator sysGen = new StarSystemGenerator();
		StarSystemFactory sysFac = new StarSystemFactory();

		StarSystem system = sysGen.createEmptySystem(sector, "Test2", 2, 2);

		StarGenerator sg = new StarGenerator(system, true);

		Star star = sg.generatePrimary();

		system = sysFac.getStarSystem(2);
		system.addStar(star);
		sysFac.persist(system);

		star = sg.generateTertiary();
	}
}
