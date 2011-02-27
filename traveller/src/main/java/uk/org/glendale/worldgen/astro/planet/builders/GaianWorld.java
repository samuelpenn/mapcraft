/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Gaian worlds tend to be rich in life.
 * 
 * @author Samuel Penn
 */
public abstract class GaianWorld extends PlanetBuilder {
	protected Tile sea = new Tile("Sea", "#4444aa", true);
	protected Tile land = new Tile("Land", "#aaaa44", false);
	protected Tile mountains = new Tile("Mountains", "#B0B0B0", false);

	public GaianWorld() {
	}

	@Override
	public void generate() {
		if (planet.getRadius() > 5000) {
			planet.setAtmosphere(AtmosphereType.Standard);
			planet.setPressure(AtmospherePressure.Standard);
		}
		generateMap();
		generateResources();
	}

	@Override
	public void generateMap() {
		if (!AppManager.getDrawMap()) {
			return;
		}
		addContinents(sea, land, mountains);
		addEcology();

		// Increase resolution to maximum.
		map = scaleMap(map, TILE_SIZE);

		if (AppManager.getStretchMap()) {
			map = stretchMap(map);
		}
		getImage();
	}

	protected abstract void addEcology();

	/**
	 * Add resources based on the ecology. This is based pretty much on the
	 * LifeType of the world.
	 */
	protected void addEcologicalResources() {
		switch (planet.getLifeType()) {
		case None:
			// No life. Not really a Gaian world then.
			addResource("Base Organics", Die.d10());
			break;
		case Organic:
			// Basic organic compounds. May be actual life.
			if (Die.d2() == 1) {
				addResource("Base Organics", Die.d20(4));
			} else {
				addResource("Simple Organics", Die.d10(5));
			}
			break;
		case Archaean:
			addResource("Algae", 20 + Die.d20(3));
			if (Die.d3() == 1) {
				addResource("Plankton", Die.d12(3));
			}
			break;
		case Aerobic:
			addResource("Algae", Die.d12(4));
			addResource("Plankton", Die.d12(3));
			if (Die.d3() == 1) {
				addResource("Simple Marine", 30 + Die.d20(3));
			} else {
				addResource("Simple Marine", 20 + Die.d20(2));
				addResource("Crustaceans", 30 + Die.d20(3));
			}
			break;
		case ComplexOcean:
			addResource("Simple Marine", 10 + Die.d12(3));
			addResource("Crustaceans", 10 + Die.d12(4));
			addResource("Fish", 20 + Die.d20(3));
			break;
		case SimpleLand:
			addResource("Simple Marine", 10 + Die.d12(3));
			addResource("Crustaceans", 10 + Die.d12(4));
			addResource("Fish", 20 + Die.d20(3));
			if (Die.d3() == 1) {
				addResource("Moss", Die.d8(3));
			} else {
				addResource("Fungi", 5 + Die.d10(3));
			}
			break;
		case ComplexLand:
			addResource("Simple Marine", 10 + Die.d12(3));
			addResource("Crustaceans", 10 + Die.d12(4));
			addResource("Fish", 20 + Die.d20(3));
			addResource("Wood", 10 + Die.d12(3));
			addResource("Mammals", 10 + Die.d12(2));
			break;
		case Extensive:
			addResource("Simple Marine", 10 + Die.d12(3));
			addResource("Crustaceans", 10 + Die.d12(4));
			addResource("Fish", 20 + Die.d20(3));
			addResource("Wood", 25 + Die.d20(3));
			addResource("Mammals", 20 + Die.d20(3));
			break;
		}
	}

}
