/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import uk.org.glendale.worldgen.server.AppManager;

@Controller
@RequestMapping("/api/planet")
public class PlanetAPI {
	@Autowired
	PlanetFactory		factory;
	
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
}
