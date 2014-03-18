/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the commodity mapping class.
 * 
 * @author Samuel Penn
 */
public class CommodityMapTest {
	@Test
	public void commodityMapTest() {
		Commodity c1 = new Commodity();
		Commodity c2 = new Commodity();

		CommodityMap cm = new CommodityMap(c1, c2, "AgFa");
		Assert.assertEquals(c1, cm.getCommodity());
		Assert.assertEquals(c2, cm.getOutput());
		Assert.assertEquals(100, cm.getEfficiency());
		Assert.assertEquals("AgFa", cm.getOperation());

		cm = new CommodityMap(c1, c2, "AgFa", 75, 0);
		Assert.assertEquals(c1, cm.getCommodity());
		Assert.assertEquals(c2, cm.getOutput());
		Assert.assertEquals(75, cm.getEfficiency());
		Assert.assertEquals("AgFa", cm.getOperation());
	}
}
