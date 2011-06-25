/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test the Star entity, plus related enums.
 * 
 * @author Samuel Penn
 */
public class StarTest {
	@Test
	public void starTest() {
		Star star = new Star();

		Assert.assertEquals(0, star.getId());
		Assert.assertEquals(0, star.getParentId());
		Assert.assertNull(star.getSystem());

		star.setName("Test");
		Assert.assertEquals("Test", star.getName());
		star.setName(" Test    ");
		Assert.assertEquals("Test", star.getName());

		star.setParentId(1);
		Assert.assertEquals(1, star.getParentId());

		star.setDistance(100);
		Assert.assertEquals(100, star.getDistance());

		star.setForm(StarForm.Star);
		Assert.assertEquals(StarForm.Star, star.getForm());

		star.setClassification(StarClass.V);
		Assert.assertEquals(StarClass.V, star.getClassification());

		star.setSpectralType(SpectralType.G2);
		Assert.assertEquals(SpectralType.G2, star.getSpectralType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegalDistanceTest() {
		Star star = new Star();
		star.setDistance(-1);
	}

	@Test
	public void nullSystemTest() {
		Star star = new Star(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegalNameTest() {
		Star star = new Star();
		star.setName("");
	}

	@Test
	public void spectralTypeTest() {
		for (SpectralType t : SpectralType.values()) {
			Assert.assertNotNull(t);
			Assert.assertTrue(t.getLifeTime() > 0);
			Assert.assertTrue(t.getMass() > 0);
			Assert.assertTrue(t.getSurfaceTemperature() > 0);
			Assert.assertNotNull(t.getRGBColour());
			Assert.assertTrue(t.getRGBColour().split(" ").length == 3);
		}
	}

	@Test
	public void starClassTest() {
		for (StarClass c : StarClass.values()) {
			Assert.assertNotNull(c.getSpectralType());
			Assert.assertNotNull(c.getCompanionStar());
			Assert.assertNotNull(c.getDescription());
			Assert.assertTrue(c.getSize() > 0);
			Assert.assertTrue(c.getRadius() > 0);
		}

		Assert.assertTrue(StarClass.VI.isSmallerThan(StarClass.II));
		Assert.assertTrue(StarClass.III.isBiggerThan(StarClass.V));

		Assert.assertFalse(StarClass.Ia.isSmallerThan(StarClass.IV));
		Assert.assertFalse(StarClass.V.isBiggerThan(StarClass.Ib));

		// Since the spectral type is random, try and make sure we
		// follow all paths via brute force.
		for (StarClass c : StarClass.values()) {
			for (int i = 0; i < 1000; i++) {
				Assert.assertNotNull(c.getSpectralType());
			}
		}
	}

	@Test
	public void starFormTest() {
		for (StarForm f : StarForm.values()) {
			Assert.assertNotNull(f.name());
		}
	}

	@Test
	public void temperatureTest() {
		for (Temperature t : Temperature.values()) {
			Assert.assertNotNull(t.getHotter());
			Assert.assertNotNull(t.getColder());
			Assert.assertTrue(t.getBadness() >= 0);
			Assert.assertTrue(t.getSuitability() >= 0);
		}

		Assert.assertTrue(Temperature.Cold.isColderThan(Temperature.Hot));
		Assert.assertTrue(Temperature.VeryHot
				.isHotterThan(Temperature.Standard));

		Assert.assertFalse(Temperature.Cool.isColderThan(Temperature.UltraCold));
		Assert.assertFalse(Temperature.Warm
				.isHotterThan(Temperature.ExtremelyHot));
	}
}
