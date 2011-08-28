/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.worldgen.server.AppManager;

//@Controller
public class PlanetAPI {
	@RequestMapping(value="/planet/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Planet getPlanet(@PathVariable("id") int id) {
		AppManager app = AppManager.getInstance();

		PlanetFactory pf = new PlanetFactory();

		return pf.getPlanet(id);
	}
}
