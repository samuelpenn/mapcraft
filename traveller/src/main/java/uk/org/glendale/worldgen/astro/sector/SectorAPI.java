/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides a REST style interface for obtaining information about sectors.
 * 
 * @author Samuel Penn
 */
@RequestMapping("/api/sector/")
@Controller
public class SectorAPI {
	@Autowired
	private SectorFactory	factory;
	
	@Autowired
	private SectorGenerator	generator;
	
	/**
	 * Gets details on the specified sector. The sector can be defined by either
	 * its unique name, or by its coordinates using x,y instead of the name.
	 * 
	 * @param name
	 *            Name of the sector, or its unique id.
	 * @return JSON describing the sector.
	 */
	@ResponseBody
	@RequestMapping(value="/{name}", method=RequestMethod.GET)
	public Sector getSector(@PathVariable("name") String name) {
		System.out.println("Looking for [" + name + "]");
		
		if (name.matches("[0-9]+")) {
			try {
				int	id = Integer.parseInt(name);
				return factory.getSector(id);
			} catch (NumberFormatException e) {
				// Not a number. Fall back to getting by name.
			}
		}

		return factory.getSector(name);
	}
	
	@ResponseBody
	@RequestMapping(value="/", method=RequestMethod.GET)
	public List<Sector> getSectors() {
		System.out.println("List all sectors");
		
		return factory.getAllSectors();
	}
	
	@ResponseBody
	@RequestMapping(value="/{name}", method=RequestMethod.POST)
	public Sector createSector(@PathVariable("name") String name,
			@RequestParam int x, @RequestParam int y,
			@RequestParam(required=false) String allegiance, 
			@RequestParam(required=false) String codes) {

		factory.createSector(name, x, y, allegiance);

		return factory.getSector(name);
	}
}
