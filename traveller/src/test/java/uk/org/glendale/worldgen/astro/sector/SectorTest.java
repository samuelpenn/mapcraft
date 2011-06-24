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
 * Tests sectors.
 * 
 * @author Samuel Penn
 */
public class SectorTest {
	@Test
	public void testSector() {
		Sector sector = new Sector();

		Assert.assertEquals(0, sector.getId());
		Assert.assertEquals("Unnamed", sector.getName());

		sector = new Sector("Test", 1, 2, "Na", "Im");
		Assert.assertEquals("Test", sector.getName());
		Assert.assertEquals(1, sector.getX());
		Assert.assertEquals(2, sector.getY());
		Assert.assertEquals("Im", sector.getAllegiance());
		Assert.assertEquals("Test", sector.toString());
	}
}
