package uk.org.glendale.worldgen.server;

import java.util.HashSet;
import java.util.Set;

//import javax.ws.rs.core.Application;

import uk.org.glendale.worldgen.astro.planet.PlanetAPI;
import uk.org.glendale.worldgen.astro.planet.PlanetImageAPI;
import uk.org.glendale.worldgen.astro.sector.SectorAPI;
import uk.org.glendale.worldgen.astro.sector.SubSectorAPI;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemAPI;
import uk.org.glendale.worldgen.dashboard.Dashboard;


/**
 * JAX-RS management application.
 * 
 * @author Samuel Penn
 */
public class WebApplication {// extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(SectorAPI.class);
		s.add(SubSectorAPI.class);
		s.add(StarSystemAPI.class);
		s.add(PlanetAPI.class);
		s.add(PlanetImageAPI.class);
		return s;
	}
}
