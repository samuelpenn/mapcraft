/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test a SubSector enumeration.
 * 
 * @author Samuel Penn
 */
public class SubSectorTest {
	/**
	 * Make sure that all coordinates are sane.
	 */
	@Test
	public void subSectorTest() {
		for (SubSector ss : SubSector.values()) {
			Assert.assertTrue(ss.getX() >= 0 && ss.getX() <= 3);
			Assert.assertTrue(ss.getY() >= 0 && ss.getY() <= 3);
		}
	}

	/**
	 * A few basic coordinate tests.
	 */
	@Test
	public void coordinateTest() {
		// Should not be valid, should return null;
		Assert.assertNull(SubSector.getSubSector(50, 50));

		Assert.assertEquals(SubSector.A, SubSector.getSubSector(5, 5));
		Assert.assertEquals(SubSector.D, SubSector.getSubSector(30, 8));
		Assert.assertEquals(SubSector.E, SubSector.getSubSector(7, 15));
		Assert.assertEquals(SubSector.M, SubSector.getSubSector(5, 35));
	}
}
