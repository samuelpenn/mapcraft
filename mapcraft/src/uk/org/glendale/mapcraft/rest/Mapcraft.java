package uk.org.glendale.mapcraft.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import uk.org.glendale.mapcraft.map.Map;


/**
 * JAX-RS management application.
 * 
 * @author Samuel Penn
 */
public class Mapcraft extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(MapInfo.class);
		s.add(MapImage.class);
		s.add(Map.class);
		s.add(Test.class);
		return s;
	}
}
