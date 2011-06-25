/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;

public class CommodityFactoryTest {
	/**
	 * Tear down the test database and rebuild from scratch. This ensures that
	 * our define schema is fully updated and correct.
	 */
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void commodityFactoryTest() throws ParserConfigurationException,
			SAXException, IOException {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		CommodityFactory factory = new CommodityFactory();
		List<Commodity> list = factory.getChildren(null);

		Assert.assertNotNull(list);
		Assert.assertEquals(0, list.size());

		factory.createAllCommodities(new File("src/main/resources/commodities"));

		list = factory.getChildren(null);
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);

		Commodity c = list.get(0);
		Assert.assertNotNull(c);
		Assert.assertTrue(c.getId() > 0);

		list = factory.getChildren(c);
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
	}
}
