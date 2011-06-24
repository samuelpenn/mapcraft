/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.server;

import javax.persistence.EntityManager;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the application manager.
 * 
 * @author Samuel Penn
 */
public class AppManagerTest {
	@BeforeClass
	public static void setupDatabase() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void testDatabase() {
		Assert.assertNotNull(AppManager.getInstance());
		Assert.assertEquals("test", AppManager.getUniverse());

		EntityManager em = AppManager.getInstance().getEntityManager();
		Assert.assertNotNull(em);

		AppManager.getDrawGlobe();
		AppManager.getDrawMap();
		AppManager.getStretchMap();
		AppManager.getRootPath();

	}
}
