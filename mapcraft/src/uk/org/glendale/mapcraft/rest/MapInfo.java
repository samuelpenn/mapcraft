package uk.org.glendale.mapcraft.rest;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;

import uk.org.glendale.mapcraft.server.AppManager;

@Path("/info/{mapname}")
public class MapInfo {
	@Resource(name="java:com/env/jdbc/Mapcraft")
	private DataSource ds;

	
	private Connection getConnection() throws NamingException, SQLException {
		Context ctx = new InitialContext();
		
		ds = (DataSource)ctx.lookup("java:com/env/jdbc/Mapcraft");
		//ds = (DataSource)ctx.lookup("Mapcraft");
		
		return ds.getConnection();
	}
	
	private String error="";
	
	@GET
	@Produces("text/plain")
	public String getInfo(@PathParam("mapname") String name,
			              @DefaultValue("0") @QueryParam("x") int x,
			              @DefaultValue("0") @QueryParam("y") int y) {
		
		String	message = name+"("+x+","+y+"): ";
		
		
		AppManager		app = AppManager.getInstance();
		Connection cx = null;
		
		cx = app.getDatabaseConnection();

		
		if (cx == null) {
			message += error;
		} else {
			message += "Have datasource";
		}
		
		return message;
	}
	
	@GET @Path("/size")
	@Produces("text/plain")
	public String getSize() {
		return "42";
	}
}
