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
import javax.ws.rs.core.MediaType;

import uk.org.glendale.worldgen.server.AppManager;

@Path("/planet/{id}")
public class PlanetAPI {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Planet getPlanet(@PathParam("id") int id) {
		AppManager app = AppManager.getInstance();

		PlanetFactory pf = new PlanetFactory();

		return pf.getPlanet(id);

	}
}
