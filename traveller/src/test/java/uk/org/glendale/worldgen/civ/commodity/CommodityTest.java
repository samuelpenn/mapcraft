/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import junit.framework.Assert;

import org.junit.Test;

public class CommodityTest {
	@Test
	public void testCommodity() {
		Commodity c = new Commodity();
		Assert.assertEquals(0, c.getId());
		c.setName("Test");
		Assert.assertEquals("Test", c.getName());
		c.setConsumptionRating(1);
		Assert.assertEquals(1, c.getConsumptionRating());
		c.setProductionRating(1);
		Assert.assertEquals(1, c.getProductionRating());
		c.setCost(1);
		Assert.assertEquals(1, c.getCost());
		c.setVolume(1);
		Assert.assertEquals(1, c.getVolume());
		c.setImagePath("/test");
		Assert.assertEquals("/test", c.getImagePath());
		c.setLawLevel(1);
		Assert.assertEquals(1, c.getLawLevel());
		c.setTechLevel(1);
		Assert.assertEquals(1, c.getTechLevel());
		c.setSource(Source.Ag);
		Assert.assertEquals(Source.Ag, c.getSource());

		// Limit testing.
		c.setLawLevel(-5);
		Assert.assertEquals(0, c.getLawLevel());
		c.setLawLevel(10);
		Assert.assertEquals(6, c.getLawLevel());

		c.setTechLevel(-1);
		Assert.assertEquals(0, c.getTechLevel());

		Assert.assertEquals(0, c.getCodes().size());
		c.addCode(CommodityCode.Ag);
		Assert.assertTrue(c.hasCode(CommodityCode.Ag));

		Commodity c2 = new Commodity();
		c.setParent(c2);
		Assert.assertNotNull(c.getParent());
		Assert.assertEquals(c2, c.getParent());
	}

	@Test
	public void testProduction() {
		Commodity c = new Commodity();
		c.setProductionRating(2);
		c.setConsumptionRating(6);

		Assert.assertEquals(100, c.getProduction(1000));
		Assert.assertEquals(100, c.getConsumption(100000));
	}

	@Test
	public void testCommodityCode() {
		for (CommodityCode c : CommodityCode.values()) {
			Assert.assertNotNull(c.getName());
		}
	}

	@Test
	public void testSource() {
		for (Source s : Source.values()) {
			Assert.assertNotNull(s.name());
		}
	}
}
