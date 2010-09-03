package uk.org.glendale.worldgen.astro.planet;

import java.io.File;

import javax.ws.rs.*;

import uk.org.glendale.worldgen.astro.planet.MapImage.Projection;

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
		PlanetFactory	pf = new PlanetFactory();
		Planet			planet = pf.getPlanet(id);
		
		byte[]			data = planet.getFlatImage();
		
		return data;
	}
}
