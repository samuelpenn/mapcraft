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

import uk.org.glendale.worldgen.civ.commodity.Commodity;

public class ResourceTest {
	@Test(expected = IllegalArgumentException.class)
	public void illegalResourceTest() {
		Resource resource = new Resource(null, 50);
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegalDensityTest() {
		Commodity c = new Commodity() {
		};

		Resource resource = new Resource(c, 0);
	}

	@Test
	public void resourceTest() {
		Commodity c = new Commodity() {
		};

		Resource resource = new Resource(c, 50);

		Assert.assertEquals(c, resource.getCommodity());
		Assert.assertEquals(50, resource.getDensity());

		// Any density higher than 100 should be reset to be 100.
		resource = new Resource(c, 150);
		Assert.assertEquals(100, resource.getDensity());
	}
}
