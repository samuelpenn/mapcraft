/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Provide API for obtaining planet surface images.
 * 
 * @author Samuel Penn
 */
@Path("/planetimage/{id}")
public class PlanetImageAPI {
	@GET
	@Produces("image/jpeg")
	public byte[] getImage(@PathParam("id") int id) {
		PlanetFactory pf = new PlanetFactory();
		Planet planet = pf.getPlanet(id);

		byte[] data = planet.getFlatImage();

		return data;
	}
}
