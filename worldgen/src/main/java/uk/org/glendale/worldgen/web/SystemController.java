/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;

/**
 * MVC Controller which displays a star system.
 * 
 * @author Samuel Penn
 */
@RequestMapping("/ui/system")
@Controller
public class SystemController {
	@Autowired
	private SectorFactory	sectorFactory;
	
	@Autowired
	private StarSystemFactory starSystemFactory;
	
	@Autowired
	private StarSystemGenerator	starSystemGenerator;
	
	@RequestMapping("/{id}")
	public String homePage(Model model, @PathVariable int id) {
		model.addAttribute("systemId", id);

		StarSystem system = starSystemFactory.getStarSystem(id);
		if (system == null) {
			return null;
		}
		Sector	   sector = system.getSector();
		
		model.addAttribute("sector", sector);
				
		return "system";
	}		
}
