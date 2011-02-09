package uk.org.glendale.worldgen.astro.planet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import uk.org.glendale.worldgen.server.AppManager;

@Path("/planet/{id}")
public class PlanetAPI {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Planet getPlanet(@PathParam("id") int id) {
		AppManager		app = AppManager.getInstance();

		PlanetFactory	pf = new PlanetFactory();
		
		return pf.getPlanet(id);
		
	}
}
