/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.starsystem;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides REST API for accessing star systems.
 * 
 * @author Samuel Penn
 */
@Controller
public class StarSystemAPI {
	@RequestMapping(value="/system/{systemId}", method=RequestMethod.GET)
	@ResponseBody
	public StarSystem getSystem(@PathVariable("systemId") int systemId) {
		StarSystem		system = null;
		
		AppManager		app = AppManager.getInstance();
		
		StarSystemFactory	sf = new StarSystemFactory(app.getEntityManager());
		
		system = sf.getStarSystem(systemId);

		sf.close();
		
		return system;
	}
}
