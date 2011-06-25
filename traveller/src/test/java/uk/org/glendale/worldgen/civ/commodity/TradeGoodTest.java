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

public class TradeGoodTest {
	@Test
	public void tradeGoodTest() {
		Commodity c = new Commodity();
		TradeGood g = new TradeGood(c, 1, 100);

		Assert.assertEquals(0, g.getId());
		Assert.assertEquals(c, g.getCommodity());
		Assert.assertEquals(100, g.getPrice());
		Assert.assertEquals(1, g.getQuantity());

		g.addQuantity(3);
		Assert.assertEquals(4, g.getQuantity());
		g.setQuantity(10);
		Assert.assertEquals(10, g.getQuantity());
	}
}
