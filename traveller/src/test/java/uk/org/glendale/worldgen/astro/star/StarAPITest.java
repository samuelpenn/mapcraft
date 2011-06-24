/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import org.junit.Assert;
import org.junit.Test;

import uk.org.glendale.rpg.traveller.systems.codes.SpectralType;
import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.StarForm;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;

public class StarAPITest {
	@Test
	public void starApiTest() {
		Star star = new Star();

		star.setClassification(StarClass.V);
		star.setForm(StarForm.Star);
		star.setSpectralType(SpectralType.G2);

		int cold = StarAPI.getColdPoint(star);
		Assert.assertTrue(cold > 500);
		Assert.assertTrue(cold < 2000);

		int earth = StarAPI.getEarthDistance(star);
		Assert.assertTrue(earth > 140);
		Assert.assertTrue(earth < 160);

		int inner = StarAPI.getInnerLimit(star);
		Assert.assertTrue(inner > 10);
		Assert.assertTrue(inner < 50);

		long period = StarAPI.getOrbitPeriod(star, 150);
		period /= 86400; // Convert to days from seconds.
		Assert.assertTrue(period > 360);
		Assert.assertTrue(period < 370);

		Temperature t = StarAPI.getOrbitTemperature(star, 150);
	}
}
