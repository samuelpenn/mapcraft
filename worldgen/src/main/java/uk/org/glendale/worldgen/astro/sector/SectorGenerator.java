/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.text.Names;

/**
 * Generates a new sector full of star systems. May be used to re-populate an
 * existing sector (wiping it clean, then re-generating), or to build a complete
 * new sector from scratch.
 * 
 * @author Samuel Penn
 */
@Service
public class SectorGenerator {
	@Autowired
	private SectorFactory	sectorFactory;
	
	@Autowired
	private StarSystemFactory	starSystemFactory;
	
	@Autowired
	private StarSystemGenerator	starSystemGenerator;

	/**
	 * Default constructor for bean.
	 */
	public SectorGenerator() {
	}


	/**
	 * Create an empty sector which has no star systems defined. X and Y
	 * coordinate must be unique, and so must the name.
	 * 
	 * @param name
	 *            Unique name for the sector.
	 * @param x
	 *            X coordinate for the sector.
	 * @param y
	 *            Y coordinate for the sector.
	 * @param codes
	 *            Codes, if any.
	 * @param allegiance
	 *            Allegiance, if any.
	 * @return
	 */
	public Sector createEmptySector(String name, int x, int y, String codes,
			String allegiance) {
		
		if (codes != null && codes.trim().length() > 0) {
			List<SectorCode>	list = new ArrayList<SectorCode>();
			for (String code : codes.split(" ")) {
				try {
					list.add(SectorCode.valueOf(code));
				} catch (Throwable t) {
					Log.warn("Sector code [" + code + "] is invalid, ignoring");
				}
			}
			sectorFactory.createSector(name, x, y, allegiance, 
									list.toArray(new SectorCode[0]));
		} else {
			sectorFactory.createSector(name, x, y, allegiance);
		}

		return sectorFactory.getSector(name);
	}

	/**
	 * Deletes an existing sector. All star systems and planets within the
	 * sector are removed first.
	 * 
	 * @param sector
	 *            Sector to be deleted.
	 */
	public void deleteSector(Sector sector) {
	}

	/**
	 * Delete all star systems and associated data from the sector. Leaves the
	 * sector itself as intact, but empty.
	 * 
	 * @param sector
	 *            Sector to clear.
	 */
	public void clearSector(Sector sector) {
	}

	/**
	 * Fill an empty sector with random star systems. The chance of any parsec
	 * having a star system is determined by the percentage defined.
	 * 
	 * @param sector
	 *            Sector to fill. Should be empty.
	 * @param names
	 *            Name generator to use to create system names.
	 * @param percentChance
	 *            Percentage change each hex has a system.
	 * 
	 * @return Count of number of systems added.
	 */
	public int fillRandomSector(Sector sector, Names names, int percentChance) {
		int count = 0;
		if (sector == null) {
			throw new IllegalArgumentException("Sector must be defined.");
		}
		if (names == null) {
			throw new IllegalArgumentException("Names must be defined");
		}
		if (percentChance < 0 || percentChance > 100) {
			throw new IllegalArgumentException(
					"Percentage chance must be between 0% and 100%");
		}
		if (sector.getId() < 1) {
			throw new IllegalStateException("Sector does not have a valid id");
		}

		StarSystemGenerator systemGenerator = null;

		try {
			for (int x = 1; x <= Sector.WIDTH; x++) {
				System.out.println("X: [" + x + "]");
				for (int y = 1; y <= Sector.HEIGHT; y++) {
					if (Die.d100() <= percentChance) {
						systemGenerator.createStarSystem(sector,
								names.getPlanetName(), x, y);
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return count;
	}
}
