/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides a REST style interface for obtaining information about sectors.
 * 
 * @author Samuel Penn
 */
//@Path("/sector/{name}")
//@Controller
public class SectorAPI {
	@Autowired
	private SectorFactory	factory;
	
	/**
	 * Gets details on the specified sector. The sector can be defined by either
	 * its unique name, or by its coordinates using x,y instead of the name.
	 * 
	 * @param name
	 *            Name of the sector, or x,y coordinates.
	 * @return JSON describing the sector.
	 */
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	@ResponseBody
	@RequestMapping(value="/sector/{name}", method=RequestMethod.GET)
	public Sector getSector(@PathVariable("name") String name) {
		System.out.println("Looking for [" + name + "]");

		return factory.getSector(name);
	}

//	@GET @Path("/size")
//  @Produces("text/plain")
	public String getSize() {
		return "42";
	}

}
