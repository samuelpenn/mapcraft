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


/**
 * Provide API for obtaining planet surface images.
 * 
 * @author Samuel Penn
 */
//@Controller
public class PlanetImageAPI {
	//@GET	@Produces("image/jpeg")
	@RequestMapping(value="/planetimage/{id}", method=RequestMethod.GET)
	@ResponseBody
	public byte[] getImage(@PathVariable("id") int id) {
		PlanetFactory pf = new PlanetFactory();
		Planet planet = pf.getPlanet(id);

		byte[] data = planet.getFlatImage();

		return data;
	}
}
