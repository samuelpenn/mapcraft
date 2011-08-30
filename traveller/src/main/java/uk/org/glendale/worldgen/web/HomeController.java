/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;

/**
 * MVC Controller which provides links to the basic top level web pages.
 * 
 * @author Samuel Penn
 */
@Controller
public class HomeController {
	@Autowired
	private SectorFactory	sectorFactory;
	
	@Autowired
	private StarSystemFactory starSystemFactory;
	
	@RequestMapping("/")
	public String homePage(Model model) {
		model.addAttribute("hello", "Hello World");
		
		List<Sector> list = sectorFactory.getAllSectors();
		model.addAttribute("count", list.size());
		model.addAttribute("sectors", list);
		
		int minX = 0, maxX = 0;
		int minY = 0, maxY = 0;
		
		for (Sector s : list) {
			minX = Math.min(minX, s.getX());
			maxX = Math.max(maxX, s.getX());
			minY = Math.min(minY, s.getY());
			maxY = Math.max(maxY, s.getY());
		}
		model.addAttribute("minX", minX);
		model.addAttribute("maxX", maxX);
		model.addAttribute("minY", minY);
		model.addAttribute("maxY", maxY);
		
		return "home";
	}
	
	@RequestMapping("/help")
	public String help() {
		return "help";
	}
	
	@RequestMapping("/sector/{name}/{x}/{y}")
	public final String createSector(final Model model, @PathVariable String name, @PathVariable int x, @PathVariable int y) {
		
		sectorFactory.createSector(name, x, y, "Un", null);
		
		List<Sector> list = sectorFactory.getAllSectors();
		model.addAttribute("count", list.size());
		model.addAttribute("sectors", list);
		
		return "home";		
	}
}
