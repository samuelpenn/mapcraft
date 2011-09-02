/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

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
public class StarFactory {
	/** Hibernate session factory. */
	@Autowired
	private SessionFactory		sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Empty bean constructor.
	 */
	public StarFactory() {
	}

	/**
	 * Gets a star system identified by its unique id.
	 * 
	 * @param systemId
	 *            Id of the star system to be retrieved.
	 * @return The star system if found,
	 */

	public Star getStar(int id) {
		Session session = sessionFactory.getCurrentSession();
		
		Query query = (Query) session.createQuery("from Star where id = :id");
		query.setParameter("id", id);

		Star star = (Star) query.uniqueResult();
		star.getId();
		return star;
	}

	//@Transactional
	public void persist(Star star) {
		Session session = sessionFactory.getCurrentSession();
		//star = (Star) session.merge(star);

		session.persist(star);
	}
}