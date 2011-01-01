package uk.org.glendale.mapcraft.rest;

import java.util.ArrayList;
import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import uk.org.glendale.mapcraft.map.NamedArea;

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
	
	public static void main(String[] args) {
		ArrayList<NamedArea>	list = new ArrayList<NamedArea>();
		
		list.add(new NamedArea(1, "fred", "Frederick",0));
		list.add(new NamedArea(1, "bob", "Robert",0));
		list.add(new NamedArea(1, "sarah", "Sarah",0));
		list.add(new NamedArea(1, "alice", "Alice",0));
		
		for (NamedArea a : list) {
			System.out.println(a.getTitle());
		}
		Collections.sort(list);
		for (NamedArea a : list) {
			System.out.println(a.getTitle());
		}
	}
}
