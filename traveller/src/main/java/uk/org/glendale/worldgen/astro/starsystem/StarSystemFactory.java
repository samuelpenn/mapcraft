/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.starsystem;

import java.util.List;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining star systems from the database.
 * 
 * @author Samuel Penn
 */
@Repository
public class StarSystemFactory {
	/** Hibernate session factory. */
	@Autowired
	private SessionFactory		sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Empty bean constructor.
	 */
	public StarSystemFactory() {
	}

	/**
	 * Gets a star system identified by its unique id.
	 * 
	 * @param systemId
	 *            Id of the star system to be retrieved.
	 * @return The star system if found,
	 */
	@Transactional
	public StarSystem getStarSystem(int systemId) {
		Session session = sessionFactory.getCurrentSession();
		
		Query query = (Query) session.createQuery("from StarSystem where id = :id");
		query.setParameter("id", systemId);

		StarSystem system = (StarSystem) query.uniqueResult();
		system.getId();
		system.getStars();
		return system;
	}

	public void persist(StarSystem system) {
		Session session = sessionFactory.getCurrentSession();

		session.persist(system);
	}

	/**
	 * Gets all the star systems in the given sector.
	 * 
	 * @param sector
	 *            Sector to get star systems for.
	 * @return List of star systems found.
	 */
	@SuppressWarnings("unchecked")
	public List<StarSystem> getStarSystemsInSector(Sector sector) {
		Session session = sessionFactory.getCurrentSession();
		
		Query query = (Query) session.createQuery("from StarSystem where sector = :sector order by name asc");
		query.setParameter("sector", sector);

		return query.list();
	}
}
