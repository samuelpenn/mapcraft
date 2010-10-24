package uk.org.glendale.mapcraft.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/test")
public class Test {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getHello() {
		return "Hello World";
	}
	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public void postHello(String message) {
		System.out.println(message);
		return;
	}
}
