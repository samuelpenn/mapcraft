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
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.TechnologyLevel;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Asteroid belts are quite common. Can be rocky or icy.
 * 
 * @author Samuel Penn
 */
public abstract class Belt extends WorldBuilder {
	public static final String ARC_SIZE = "ARC_SIZE";
	public static final String DENSITY = "DENSITY";
	public static final String THICKNESS = "THICKNESS";
	
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
		if (properties.containsKey(THICKNESS)) {
			thickness = (Integer) properties.get(THICKNESS);
		}

		int		arc = 360;
		if (properties.containsKey(ARC_SIZE)) {
			arc = (Integer) properties.get(ARC_SIZE);
		}
		
		int		density = 100;
		if (properties.containsKey(DENSITY)) {
			density = (Integer) properties.get(DENSITY);
		}
		density = (density * distance * arc / 2) / 100;

		double  scale = 400.0 / distance;
		
		for (int i=0; i < density; i++) {
			double x = 0;
			double y = distance + Die.die(thickness, 4) - Die.die(thickness, 4);
			double r = 2.0 * Math.PI;

			if (arc == 360) {
				r *= Die.rollZero(arc * 100) / 36000.0;
			} else {
				double a = Die.die(arc * 25, 2) - Die.die(arc * 25, 2);
				// Ensure the thickest part is near the middle.
				y -= distance;
				y *= 1.0 - (Math.abs(a) / (arc * 50));
				y += distance;
				
				a += arc * 100;
				r *= a / 36000.0;
			}
			y *=  scale;
			
			int x1 = (int)(x * Math.cos(r) - y * Math.sin(r)) + 512;
			int y1 = (int)(y * Math.cos(r) + x * Math.sin(r)) + 512;
			
			switch (Die.d6()) {
			case 1: case 2: case 3:
				image.rectangle(x1, y1, 0, 0, "#999999");
				break;
			case 4: case 5:
				image.rectangle(x1, y1, 0, 0, "#555555");
				break;
			case 6:
				image.rectangle(x1, y1, 0, 0, "#000000");
				break;
			}
		}
		
		// Show 1 AU to scale.		
		image.circleOutline(512, 512, (int)(150 * scale), "#FF0000");

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

	/**
	 * By default, worlds of this type have no population.
	 */
	public String getFacilityBuilderName(PopulationSize size, TechnologyLevel level) {
		return null;
	}
}
