/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import org.junit.Assert;
import org.junit.Test;

import uk.org.glendale.worldgen.astro.planet.MapImage.Projection;

/**
 * Tests a MapImage entity. These store images of a world map.
 * 
 * @author Samuel Penn
 */
public class MapImageTest {
	@Test
	public void mapImageTest() {
		MapImage image = new MapImage();

		image.setType(Projection.Globe);
		Assert.assertEquals(Projection.Globe, image.getType());
		image.setType(Projection.Mercator);
		Assert.assertEquals(Projection.Mercator, image.getType());

		byte[] data = new byte[1024];
		image.setData(data);

		Assert.assertNotNull(image.getData());
		Assert.assertEquals(1024, image.getData().length);
	}
}
