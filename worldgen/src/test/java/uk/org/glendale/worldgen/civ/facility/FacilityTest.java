/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class FacilityTest {
	@Test
	public void facilityTest() {
		Facility facility = new Facility();
		facility.setName("Test");
		facility.setType(FacilityType.Agriculture);

		Assert.assertEquals(0, facility.getId());
		Assert.assertEquals("Test", facility.getName());
		Assert.assertEquals(FacilityType.Agriculture, facility.getType());

		facility.addCode(FacilityCode.H0);
		facility.addCode(FacilityCode.T0, FacilityCode.T1);
		Assert.assertTrue(facility.hasCode(FacilityCode.H0));
		Assert.assertTrue(facility.hasCode(FacilityCode.T0));
		Assert.assertTrue(facility.hasCode(FacilityCode.T1));

	}

	@Test
	public void facilityTypeTest() {
		for (FacilityType t : FacilityType.values()) {
			Assert.assertNotNull(t.name());
		}
	}

	@Test
	public void operationTest() {
		Operation o1 = new Operation("Test", 100);
		Assert.assertEquals("Test", o1.getName());
		Assert.assertEquals(100, o1.getLevel());
		Assert.assertTrue(o1.equals(o1));

		Operation o2 = new Operation("Another Test", 100);
		Assert.assertFalse(o1.equals(o2));

		Operation o3 = new Operation("Test", 50);
		Assert.assertTrue(o1.equals(o3));

		List<Operation> list = new ArrayList<Operation>();
		list.add(o1);
		list.add(o2);
		list.add(o3);
		Assert.assertTrue(list.contains(o2));
	}

	@Test
	public void facilityCodeTest() {
		for (FacilityCode t : FacilityCode.values()) {
			Assert.assertNotNull(t.name());
		}
	}
}
