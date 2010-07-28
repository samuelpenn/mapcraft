package uk.org.glendale.worldgen.astro.sector;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.Foo;

/**
 * Provides a REST style interface for obtaining information about
 * sectors.
 * 
 * @author Samuel Penn
 */
@Path("/sector/{name}")
public class SectorAPI {
	@Resource(name="java:com/env/jdbc/Traveller")
	private DataSource ds;


	private String error="";
	
	/**
	 * Gets details on the specified sector. The sector can be defined by either
	 * its unique name, or by its coordinates using x,y instead of the name.
	 * 
	 * @param name		Name of the sector, or x,y coordinates.
	 * @return			JSON describing the sector.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Sector getSector(@PathParam("name") String name) {
		AppManager		app = AppManager.getInstance();
		
		System.out.println("Looking for ["+name+"]");

		SectorFactory	sf = new SectorFactory(app.getEntityManager());
		
		
		return sf.getSector(name);

	}

	
	@GET @Path("/size")
	@Produces("text/plain")
	public String getSize() {
		return "42";
	}
	
}
