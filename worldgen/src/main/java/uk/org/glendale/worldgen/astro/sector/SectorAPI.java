/*
 * Copyright (C) 2009, 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemTO;

/**
 * Provides a REST style interface for obtaining information about sectors. A
 * sector is a flat, 2D, region of space 40 parsecs 'high' and 32 parsecs
 * 'wide'. Each parsec is a hexagonal tile which may contain zero or one star
 * systems.
 * 
 * @author Samuel Penn
 */
@RequestMapping("/api/sector/")
@Controller
public class SectorAPI {
	@Autowired
	private SectorFactory		factory;

	@Autowired
	private StarSystemFactory	starSystemFactory;

	// @Autowired
	private SectorGenerator		generator;

	/**
	 * Gets details on the specified sector. The sector can be defined by either
	 * its unique name, or by its coordinates using x,y instead of the name. If
	 * the sector 'name' is a pure number (matching [0-9]+), then the 'name' is
	 * assumed to be a unique id. Otherwise, search for a sector with the given
	 * unique name.
	 * 
	 * @param name
	 *            Name of the sector, or its unique id.
	 * @return JSON describing the sector.
	 */
	@ResponseBody
	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
	public Sector getSector(@PathVariable("name") String name) {
		System.out.println("Looking for [" + name + "]");

		if (name.matches("[0-9]+")) {
			try {
				int id = Integer.parseInt(name);
				return factory.getSector(id);
			} catch (NumberFormatException e) {
				// Not a number. Fall back to getting by name.
			}
		}

		return factory.getSector(name);
	}

	@Transactional
	@ResponseBody
	@RequestMapping(value = "/{name}/systems", method = RequestMethod.GET)
	public List<StarSystemTO> getStarSystems(@PathVariable("name") String name) {
		final Sector sector = getSector(name);

		List<StarSystemTO> list = null;
		if (sector == null) {
			return null;
		}
		list = new ArrayList<StarSystemTO>();
		for (StarSystem system : starSystemFactory
				.getStarSystemsInSector(sector)) {
			System.out.println(system.getName());
			system.getStars();
			for (Planet planet : system.getPlanets()) {
				System.out.println("  " + planet.getName());
			}
			list.add(new StarSystemTO(system));
		}

		return list;
	}

	@Transactional
	@ResponseBody
	@RequestMapping(value = "/{name}/data", method = RequestMethod.GET)
	public SectorTO getSectorData(@PathVariable("name") String name) {
		System.out.println("Looking for [" + name + "]");

		Sector sector = null;
		if (name.matches("[0-9]+")) {
			try {
				int id = Integer.parseInt(name);
				sector = factory.getSector(id);
			} catch (NumberFormatException e) {
				// Not a number. Fall back to getting by name.
				sector = factory.getSector(name);
			}
		}
		if (sector == null) {
			return null;
		}

		List<StarSystemTO> systems = new ArrayList<StarSystemTO>();
		for (StarSystem system : starSystemFactory
				.getStarSystemsInSector(sector)) {
			System.out.println(system.getName());
			system.getStars();
			for (Planet planet : system.getPlanets()) {
				System.out.println("  " + planet.getName());
			}
			systems.add(new StarSystemTO(system));
		}
		SectorTO data = new SectorTO(sector, systems);

		return data;
	}

	// @ResponseBody
	@RequestMapping(value = "/{name}/image", method = RequestMethod.GET)
	public void getSectorThumbnail(@PathVariable("name") String name,
			HttpServletResponse response) {
		response.setContentType("image/jpeg");

		Sector sector = getSector(name);
		SimpleImage image = factory.getThumbnail(sector);
		try {
			ByteArrayOutputStream stream = image.save();
			ServletOutputStream out = response.getOutputStream();

			out.write(stream.toByteArray());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return image;
	}

	/**
	 * Gets a list of all the defined sectors, from top to bottom and left to
	 * right.
	 * 
	 * @return List of all sectors.
	 */
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public List<Sector> getSectors() {
		System.out.println("List all sectors");

		return factory.getAllSectors();
	}

	@ResponseBody
	@RequestMapping(value = "/{name}", method = RequestMethod.POST)
	public Sector createSector(@PathVariable("name") String name,
			@RequestParam int x, @RequestParam int y,
			@RequestParam(required = false) String allegiance,
			@RequestParam(required = false) String codes) {

		factory.createSector(name, x, y, allegiance, null);

		return factory.getSector(name);
	}
}
