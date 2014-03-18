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
import org.springframework.transaction.annotation.Transactional;
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

	@RequestMapping(value="/{systemId}", method=RequestMethod.GET)
	@ResponseBody
	@Transactional
	public StarSystemTO getSystem(@PathVariable("systemId") int systemId) {
		StarSystem		system = null;
		
		system = factory.getStarSystem(systemId);
		system.getSector();

		return new StarSystemTO(system, true);
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
	
	/**
	 * Gets the Roman Numeral suffix for the given orbital position.
	 * 
	 * @param position	1 is the first orbit.
	 * @return			Roman numeral.
	 */
	public String getOrbitNumeral(int position) {
		String numeral = "";
		
		while (position > 10) {
			numeral += "X";
			position -= 10;
		}
		
		switch (position) {
		case 1:
			return numeral + "I";
		case 2:
			return numeral + "II";
		case 3:
			return numeral + "III";
		case 4:
			return numeral + "IV";
		case 5:
			return numeral + "V";
		case 6:
			return numeral + "VI";
		case 7:
			return numeral + "VII";
		case 8: 
			return numeral + "VIII";
		case 9:
			return numeral + "IX";
		case 10:
			return numeral + "X";
		}
		return numeral;
	}
}
