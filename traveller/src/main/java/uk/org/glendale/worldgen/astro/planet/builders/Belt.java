/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.io.File;
import java.io.IOException;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.MapImage;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Asteroid belts are quite common. Can be rocky or icy.
 * 
 * @author Samuel Penn
 */
public abstract class Belt extends WorldBuilder {
	public Belt() {
	}

	@Override
	public void generate() {
		planet.addTradeCode(TradeCode.As);
		planet.setAtmosphere(AtmosphereType.Vacuum);
		planet.setPressure(AtmospherePressure.None);

		generateMap();
		generateResources();
	}

	@Override
	public void generateMap() {
		getImage();
	}
	
	/**
	 * Override the normal getImage. Asteroid belts have their own way of
	 * drawing an image. We draw a map of the belt, rather than drawing
	 * a particular rock in the belt.
	 */
	public SimpleImage getImage() {
		SimpleImage		image = new SimpleImage(1024, 1024, "#FFFFFF");
		
		int		distance = planet.getDistance();
		int		thickness = planet.getRadius();
		
		double  scale = 800.0 / distance;
		
		image.circle(512, 512, (int) ((distance + thickness*3) * scale), "#AAAAAA");
		image.circle(512, 512, (int) ((distance + thickness) * scale), "#777777");
		image.circle(512, 512, (int) ((distance - thickness) * scale), "#AAAAAA");
		image.circle(512, 512, (int) ((distance - thickness*3) * scale), "#FFFFFF");
		
		// Show 1 AU to scale.		
		image.circleOutline(512, 512, (int)(150 * scale), "#999999");

		// Show the star.
		image.circle(512, 512, 10, "#997700");

		try {
			MapImage orbitMap = new MapImage();
			orbitMap.setType(MapImage.Projection.Orbital);
			orbitMap.setData(image.save().toByteArray());
			planet.addImage(orbitMap);

			image.save(new File("/tmp/maps/" + planet.getId() + "_" + 
					planet.getType() + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
}
