/*
 * Copyright (C) 2011, 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.TechnologyLevel;
import uk.org.glendale.worldgen.astro.planet.builders.ice.Europan;
import uk.org.glendale.worldgen.astro.planet.maps.Tile;
import uk.org.glendale.worldgen.astro.planet.maps.WorldBuilder;
import uk.org.glendale.worldgen.civ.facility.builders.FacilityBuilder;

/**
 * Abstract class for creating Jovian worlds such as Jupiter or Saturn. There
 * are several sub types, including EuJovian, SubJovian and CryoJovian. World
 * maps are simply the outer cloud layers, no surface maps are generated.
 * 
 * Currently, cloud maps consist of simple stripes with no weather patterns.
 * 
 * @author Samuel Penn
 */
public abstract class JovianWorld extends WorldBuilder {
	protected List<Tile> tiles;

	public JovianWorld() {
		map = new Tile[model.getTotalHeight()][];
		for (int y=0; y < model.getTotalHeight(); y++) {
			map[y] = new Tile[model.getWidthAtY(y)];
			for (int x=0; x < model.getWidthAtY(y); x++) {
				map[y][x] = BLANK;
			}
		}
	}

	@Override
	public void generate() {
		tiles = new ArrayList<Tile>();
		tiles.add(new Tile("Dark", "#999977", false));
		tiles.add(new Tile("Light", "#cccc99", false));

		generateMap();
		generateResources();
	}

	protected Tile getBand(int y) {
		return tiles.get((y / 2) % tiles.size());
	}
	
	private void drawBandedJovianWorld() {
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[model.getWidthAtY(tileY)];
			for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
				map[tileY][tileX] = tiles.get(tileY % tiles.size());
			}
		}
	}
	
	/**
	 * Add blurring to the cloud bands. This makes the world look a bit more
	 * natural.
	 */
	protected SimpleImage postProcess(SimpleImage image) {
		BufferedImage bimg = image.getBufferedImage();
		
		int num = Die.d8(4);
		for (int i = 0; i < num; i++) {
			blur(bimg);
		}
		
		return new SimpleImage(bimg);
	}

	@Override
	public void generateMap() {
		drawBandedJovianWorld();
		getImage();
	}

	@Override
	public void generateResources() {
		addResource("Hydrogen", 60 + Die.d20(2));
	}

	private WorldBuilder[] moonBuilders = null;

	/**
	 * Gets a list of planet types typically found as moons of a Jovian world.
	 * Jovian worlds can have lots of moons, though generally only the larger
	 * ones will be listed here. It can be assumed that there will also be a
	 * large number of captured asteroids.
	 */
	public WorldBuilder[] getMoonBuilders() {
		if (moonBuilders != null) {
			return moonBuilders;
		}
		int numMoons = Die.d3();

		System.out.println("JovianWorlds: Adding " + numMoons + " moons");		
		moonBuilders = new WorldBuilder[numMoons];

		for (int i = 0; i < numMoons; i++) {
			moonBuilders[i] = new Europan();
		}
		

		return moonBuilders;
	}
	
	/**
	 * By default, worlds of this type have no population.
	 */
	public String getFacilityBuilderName(PopulationSize size, TechnologyLevel level) {
		return null;
	}

}
