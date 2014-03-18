/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Factory class for obtaining star systems from the database.
 * 
 * @author Samuel Penn
 */
@Repository
public class StarFactory {
	@PersistenceContext
	private EntityManager		em;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	/**
	 * Empty bean constructor.
	 */
	public StarFactory() {
	}

	/**
	 * Gets a star identified by its unique id.
	 * 
	 * @param systemId
	 *            Id of the star to be retrieved.
	 * @return The star system if found, null otherwise.
	 */
	public Star getStar(int id) {
		Query query = em.createQuery("SELECT s FROM Star s WHERE s.id = :id");
		query.setParameter("id", id);

		try {
			Star star = (Star) query.getSingleResult();
			star.getId();
			return star;
		} catch (NoResultException e) {
			return null;
		}
	}

	public void persist(Star star) {
		em.persist(star);
	}
}
