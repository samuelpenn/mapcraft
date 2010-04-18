package uk.org.glendale.mapcraft.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;

@Path("/test")
public class Test {
	@GET
	@Produces("text/plain")
	public String getHello() {
		return "Hello World";
	}
	
	@POST
	@Consumes("text/plain")
	public void postHello(String message) {
		System.out.println(message);
		return;
	}
}
