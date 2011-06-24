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

import uk.org.glendale.rpg.traveller.systems.codes.SpectralType;
import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.StarForm;

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
}
