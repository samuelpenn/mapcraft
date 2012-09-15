/*
 * Copyright (C) 2011, 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.worldgen.astro.planet.MapImage.Projection;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.civ.commodity.CommodityTO;
import uk.org.glendale.worldgen.civ.trade.CivilisationFactory;
import uk.org.glendale.worldgen.civ.trade.Inventory;
import uk.org.glendale.worldgen.civ.trade.InventoryTO;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides a REST API onto data services for planets. Most return JSON
 * objects, though some return images.
 * 
 * Planetary data is generally returned as a PlanetTO rather than a Planet,
 * since the former restricts the amount of data returned for better
 * performance over HTTP (and to get around problems with lazily
 * instantiated persistence fields).
 * 
 * @author Samuel Penn
 */
@Controller
@RequestMapping("/api/planet")
public class PlanetAPI {
	@Autowired
	PlanetFactory		factory;
	
	@Autowired
	CivilisationFactory	civilisationFactory;
	
	/**
	 * Gets basic information about a single planet.
	 * 
	 * @param id	Unique id of the planet.
	 * @return		Data object describing the planet.
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	@ResponseBody
	@Transactional
	public PlanetTO getPlanet(@PathVariable("id") int id) {
		return new PlanetTO(factory.getPlanet(id));
	}
	
	@RequestMapping(value="/{id}/map", method=RequestMethod.GET)
	@Transactional
	public void getPlanetMap(@PathVariable("id") int id, HttpServletResponse response) {
		
		byte[] data = factory.getPlanetImage(id, Projection.Icosohedron);
		response.setContentType("image/jpeg");
		response.setContentLength(data.length);
		
		try {
			ServletOutputStream 	out =  response.getOutputStream();
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value="/{id}/projection.jpg", method=RequestMethod.GET)
	@Transactional
	public void getPlanetProjected(@PathVariable("id") int id, HttpServletResponse response) {
		
		byte[] data = factory.getPlanetImage(id, Projection.Mercator);

		response.setContentType("image/jpeg");
		response.setContentLength(data.length);
		try {
			ServletOutputStream 	out =  response.getOutputStream();
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value="/{id}/orbit", method=RequestMethod.GET)
	@Transactional
	public void getOrbitalMap(@PathVariable("id") int id, HttpServletResponse response) {
		
		byte[] data = factory.getPlanetImage(id, Projection.Orbital);

		response.setContentType("image/jpeg");
		response.setContentLength(data.length);
		try {
			ServletOutputStream 	out =  response.getOutputStream();
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value="/{id}/resources", method=RequestMethod.GET)
	@ResponseBody
	@Transactional
	public List<CommodityTO> getResources(@PathVariable("id") int id) {
		Planet planet = factory.getPlanet(id);
		
		ArrayList<CommodityTO> list = new ArrayList<CommodityTO>();
		
		for (Resource r : planet.getResources()) {
			list.add(new CommodityTO(r.getCommodity(), r.getDensity()));
		}

		return list;
	}
	
	/**
	 * Gets a list of all the moons of the specified planet. If the planet
	 * does not exist, a null list will be returned. If the planet has no
	 * moons, then the list will be empty.
	 * 
	 * @param id	Id of planet to get moons for.
	 * @return		List of moons. May be empty or null.
	 */
	@RequestMapping(value="/{id}/moons", method=RequestMethod.GET)
	@ResponseBody
	@Transactional
	public List<PlanetTO> getMoons(@PathVariable("id") int id) {
		Planet		 planet = factory.getPlanet(id);
		if (planet == null) {
			return null;
		}
		List<PlanetTO> list = new ArrayList<PlanetTO>();
		
		for (Planet moon : factory.getMoons(planet)) {
			list.add(new PlanetTO(moon));
		}		
		return list;
	}
	
	/**
	 * Gets a list of the inventory items for this planet. This is a record
	 * of everything that has been bought and sold on the planet. It includes
	 * the current price, availability and some historical information on
	 * each type of commodity.
	 * 
	 * @param id	Id of the planet to get the inventory of.
	 * @return		List of inventory items.
	 */
	@RequestMapping(value="/{id}/inventory", method=RequestMethod.GET)
	@ResponseBody
	@Transactional
	public List<InventoryTO> getInventory(@PathVariable("id") int id) {
		Planet		planet = factory.getPlanet(id);
		if (planet == null) {
			return null;
		}
		List<InventoryTO>	list = new ArrayList<InventoryTO>();
		
		for (Inventory item : civilisationFactory.getPlanetInventory(planet)) {
			list.add(new InventoryTO(item));
		}
		return list;
	}
}
