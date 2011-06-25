/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import junit.framework.Assert;

import org.junit.Test;

public class FacilityTest {
	@Test
	public void facilityTest() {
		Facility facility = new Facility();
		facility.setName("Test");
		facility.setType(FacilityType.Agriculture);
		facility.setTechLevel(1);

		Assert.assertEquals(0, facility.getId());
		Assert.assertEquals("Test", facility.getName());
		Assert.assertEquals(FacilityType.Agriculture, facility.getType());
		Assert.assertEquals(1, facility.getTechLevel());
	}

	@Test
	public void facilityTypeTest() {
		for (FacilityType t : FacilityType.values()) {
			Assert.assertNotNull(t.name());
		}
	}
}
