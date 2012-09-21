/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;

@RequestMapping("/ui/sector")
@Controller
public class SectorController {
	@Autowired
	private SectorFactory	sectorFactory;
	
	@Autowired
	private StarSystemFactory starSystemFactory;
	
	@Autowired
	private StarSystemGenerator	starSystemGenerator;
	
	@RequestMapping("/{id}")
	public final String showSector(Model model, @PathVariable String id) {
		Sector sector = sectorFactory.getSector(id);
		if (sector == null) {
			return null;
		}
		
		model.addAttribute("sectorId", sector.getId());
		model.addAttribute("sector", sector);
		model.addAttribute("sectorName", sector.getName());
		model.addAttribute("sectorX", sector.getY());
		model.addAttribute("sectorY", sector.getX());
		
		return "sector";
	}

}
