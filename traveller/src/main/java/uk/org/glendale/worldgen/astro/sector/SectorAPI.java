package uk.org.glendale.worldgen.astro.sector;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides a REST style interface for obtaining information about sectors.
 * 
 * @author Samuel Penn
 */
@Path("/sector/{name}")
public class SectorAPI {
	@Resource(name = "java:com/env/jdbc/Traveller")
	private DataSource ds;

	private String error = "";

	/**
	 * Gets details on the specified sector. The sector can be defined by either
	 * its unique name, or by its coordinates using x,y instead of the name.
	 * 
	 * @param name
	 *            Name of the sector, or x,y coordinates.
	 * @return JSON describing the sector.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Sector getSector(@PathParam("name") String name) {
		AppManager app = AppManager.getInstance();

		System.out.println("Looking for [" + name + "]");

		SectorFactory sf = new SectorFactory(app.getEntityManager());

		return sf.getSector(name);
	}

	@GET
	@Path("/size")
	@Produces("text/plain")
	public String getSize() {
		return "42";
	}

}
