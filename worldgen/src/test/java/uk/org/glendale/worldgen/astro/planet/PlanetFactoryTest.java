/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.SQLReader;

/**
 * Tests the factory class for planets.
 * 
 * @author Samuel Penn
 */
public class PlanetFactoryTest {

	/**
	 * Run once before any of the tests.
	 */
	@BeforeClass
	public static void setupTests() {
		SQLReader.setupTestDatabase();
	}

	@Test
	public void dbFactoryTest() {
	}

	@Test
	public void planetFactoryTest() {
	}
}
