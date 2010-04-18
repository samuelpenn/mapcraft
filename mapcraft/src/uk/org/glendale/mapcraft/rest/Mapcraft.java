package uk.org.glendale.mapcraft.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


/**
 * JAX-RS management application.
 * 
 * @author Samuel Penn
 */
public class Mapcraft extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(MapInfo.class);
		s.add(Test.class);
		return s;
	}
}
