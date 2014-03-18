/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;

public class FacilityFactoryTest {
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void facilityFactoryTest() throws ParserConfigurationException,
			SAXException, IOException {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		FacilityFactory factory = new FacilityFactory();

		Facility f = new Facility("test", "Test", FacilityType.Residential, "test");
		f.addCode(FacilityCode.H0);
		f.addCode(FacilityCode.T0);
		f.addOperation("Ag", 100);

		factory.persist(f);

		f = factory.getFacility("Test");
		Assert.assertEquals("Test", f.getName());
		Assert.assertEquals(FacilityType.Residential, f.getType());
		Assert.assertTrue(f.hasCode(FacilityCode.T0));
		Assert.assertEquals(100, f.getOperation("Ag"));
	}

}
