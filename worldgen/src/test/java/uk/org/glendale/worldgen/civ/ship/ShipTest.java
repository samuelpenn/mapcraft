/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.ship;

import junit.framework.Assert;

import org.junit.Test;

public class ShipTest {
	@Test
	public void shipTest() {
		Ship ship = new Ship();
		ship.setName("Test");
		ship.setDisplacement(100);
		ship.setType("Tester");

		Assert.assertEquals(0, ship.getId());
		Assert.assertEquals("Test", ship.getName());
		Assert.assertEquals(100, ship.getDisplacement());
		Assert.assertEquals("Tester", ship.getType());
		Assert.assertEquals(0, ship.getInServiceDate());
	}

	@Test
	public void shipStatusTest() {
		for (ShipStatus s : ShipStatus.values()) {
			Assert.assertNotNull(s.getDescription());
		}
	}
}
