/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining existing Planet objects.
 * 
 * @author Samuel Penn
 */
public class PlanetFactory {
	EntityManager	em;
	
	public PlanetFactory(EntityManager hibernateEntityManager) {
		em = hibernateEntityManager;
	}
	
	public PlanetFactory() {
		em = AppManager.getInstance().getEntityManager();
	}
	
	/**
	 * Gets a planet identified by its unique id.
	 * @param id
	 * @return
	 */
	public Planet getPlanet(int id) {
		return em.find(Planet.class, id);
	}
	
	/**
	 * Gets the list of all planets in the star system. This list includes
	 * moons as well as actual planets.
	 * 
	 * @param system	System to list planets for.
	 * @return			List of all planets and moons.
	 */
	public List<Planet> getPlanets(StarSystem system) {
		Query query = em.createQuery("from Planet p where p.system = :s");
		query.setParameter("s", system);
		
		List<Planet>	list = query.getResultList();
		
		return list;
	}
	
	public List<Planet>	getMoons(Planet planet) {
		Query query = em.createQuery("from Planet p where p.parent_id = :s");
		query.setParameter("s", planet.getId());
		
		List<Planet>	list = query.getResultList();
		
		return list;		
	}
	
	public byte[] getPlanetImage(int id, MapImage.Projection projection) {
		Query query = em.createQuery("from MapImage m where m.id = :i and m.type = :p");
		query.setParameter("i", id);
		query.setParameter("p", projection);
		
		MapImage	image = (MapImage) query.getSingleResult();
		
		return image.getData();
	}
	
	
	public void close() {
		em.close();
	}
}
