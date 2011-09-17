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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining existing Planet objects.
 * 
 * @author Samuel Penn
 */
@Repository
public class PlanetFactory {
	/** Hibernate session factory. */
	@Autowired
	private SessionFactory		sessionFactory;
	
	public PlanetFactory() {
	}
	
	/**
	 * Gets a planet identified by its unique id.
	 * @param id
	 * @return
	 */
	public Planet getPlanet(int id) {
		Session session = sessionFactory.getCurrentSession();
		
		Query query = (Query) session.createQuery("from Planet where id = :id");
		query.setParameter("id", id);

		Planet planet = (Planet) query.uniqueResult();
		planet.getId();
		return planet;
	}
	

	public List<Planet>	getMoons(Planet planet) {
		Session session = sessionFactory.getCurrentSession();
		
		Query query = (Query) session.createQuery("from Planet p where p.parent_id = :planet");
		query.setParameter("planet", planet);

		return query.list();
	}
	
	public byte[] getPlanetImage(int id, MapImage.Projection projection) {
		/*
		Query query = em.createQuery("from MapImage m where m.id = :i and m.type = :p");
		query.setParameter("i", id);
		query.setParameter("p", projection);
		
		MapImage	image = (MapImage) query.getSingleResult();
		
		return image.getData();
		*/
		return null;
	}
	
}
