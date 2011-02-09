package uk.org.glendale.worldgen.astro.starsystem;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides REST API for accessing star systems.
 * 
 * @author Samuel Penn
 */
@Path("/system/{systemId}")
public class StarSystemAPI {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StarSystem getSystem(@PathParam("systemId") int systemId) {
		StarSystem		system = null;
		
		AppManager		app = AppManager.getInstance();
		
		StarSystemFactory	sf = new StarSystemFactory(app.getEntityManager());
		
		system = sf.getStarSystem(systemId);

		sf.close();
		
		return system;
	}
}
