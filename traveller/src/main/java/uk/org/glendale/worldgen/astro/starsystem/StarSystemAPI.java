/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.starsystem;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.text.Names;

/**
 * Provides REST API for accessing star systems.
 * 
 * @author Samuel Penn
 */
@Controller
@RequestMapping("/api/system")
public class StarSystemAPI {
	@Autowired
	private StarSystemFactory	factory;
	
	@Autowired
	private StarSystemGenerator	generator;
	
	@Autowired
	private SectorFactory sectorFactory;

	@RequestMapping(value="/system/{systemId}", method=RequestMethod.GET)
	@ResponseBody
	public StarSystem getSystem(@PathVariable("systemId") int systemId) {
		StarSystem		system = null;
		
		system = factory.getStarSystem(systemId);

		return system;
	}
	
	/**
	 * Creates an empty star system, with no stars or planets.
	 * @param sectorId
	 * @param x
	 * @param y
	 * @param name
	 * @return
	 */
	@RequestMapping("empty")
	@ResponseBody
	public int createEmptySystem(@RequestParam int sectorId, 
			@RequestParam(defaultValue="0") int x, 
			@RequestParam(defaultValue="0") int y, 
			@RequestParam(required=false) String name) {
		
		Sector sector = sectorFactory.getSector(sectorId);
		if (sector == null) {
			throw new IllegalArgumentException("Invalid sector id [" + sectorId + "]");
		}
		
		generator.createEmptySystem(sector, name, x, y);
		
		return 0;
	}
	
	/**
	 * Creates a new simple star system. Simple star systems are very
	 * basic, with a single star and a small number of planets, one of
	 * which will be habitable.
	 * 
	 * @param sectorId	Sector to create system in.
	 * @param x			X coordinate of system (1-32)
	 * @param y			Y coordinate of system (1-40)
	 * @param name		Name of star system.
	 * 
	 * @return			Id of newly created star system.
	 */
	@RequestMapping("simple")
	@ResponseBody
	public int createSimpleSystem(@RequestParam int sectorId, 
			@RequestParam(defaultValue="0") int x, 
			@RequestParam(defaultValue="0") int y, 
			@RequestParam(required=false) String name) {
		
		Sector sector = sectorFactory.getSector(sectorId);
		if (sector == null) {
			throw new IllegalArgumentException("Invalid sector id [" + sectorId + "]");
		}
		
		generator.createSimpleSystem(sector, name, x, y);
		
		return 0;
	}
}
