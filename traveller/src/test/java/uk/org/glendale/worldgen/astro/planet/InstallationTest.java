/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */

package uk.org.glendale.worldgen.astro.planet;

import org.junit.Assert;
import org.junit.Test;

import uk.org.glendale.worldgen.civ.facility.Facility;
import uk.org.glendale.worldgen.civ.facility.FacilityType;

/**
 * Tests installations.
 * 
 * @author Samuel Penn
 */
public class InstallationTest {
	@Test
	public void testInstallation() {
		Facility f = new Facility("Test", FacilityType.Industry, "test");
		Installation i = new Installation(f, 100);

		Assert.assertEquals(f, i.getFacility());
		Assert.assertEquals(100, i.getSize());

		Assert.assertTrue(i.toString().indexOf("Test") > -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullFacility() {
		Installation i = new Installation(null, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize() {
		Facility f = new Facility("Test", FacilityType.Industry, "test");
		Installation i = new Installation(f, 0);
	}
}
