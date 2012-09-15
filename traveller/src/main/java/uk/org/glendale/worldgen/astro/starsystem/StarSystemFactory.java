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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
	@PersistenceContext
	private EntityManager		em;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
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
		Query query = em.createQuery("SELECT s FROM StarSystem s WHERE id = :id");
		query.setParameter("id", systemId);

		try {
			StarSystem system = (StarSystem) query.getSingleResult();
			system.getId();
			system.getStars();
			return system;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Gets the star system at the specified location. If there is no
	 * star system there, then null is returned.
	 * 
	 * @param sector	Sector to look in.
	 * @param x			X coordinate, 1-32.
	 * @param y			Y coordinate, 1-40.
	 * @return			Star system if found, or null.
	 */
	@Transactional
	public StarSystem getStarSystem(Sector sector, int x, int y) {
		Query	query = em.createQuery("SELECT s FROM StarSystem s WHERE sector = :sector AND x = :x AND y = :y");
		query.setParameter("sector", sector);
		query.setParameter("x", x);
		query.setParameter("y", y);

		try {
			StarSystem system = (StarSystem) query.getSingleResult();
			system.getId();
			system.getStars();
			return system;
		} catch (NoResultException e) {
			return null;
		}
	}

	public void persist(StarSystem system) {
		em.persist(system);
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
		Query query = em.createQuery("from StarSystem where sector = :sector order by name asc");
		query.setParameter("sector", sector);

		return query.getResultList();
	}
}
