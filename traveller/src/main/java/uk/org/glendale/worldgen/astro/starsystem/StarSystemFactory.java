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
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining star systems from the database.
 * 
 * @author Samuel Penn
 */
public class StarSystemFactory {
	EntityManager em;

	public StarSystemFactory(EntityManager hibernateEntityManager) {
		em = hibernateEntityManager;
	}

	public StarSystemFactory() {
		em = AppManager.getInstance().getEntityManager();
	}

	/**
	 * Gets a star system identified by its unique id.
	 * 
	 * @param systemId
	 *            Id of the star system to be retrieved.
	 * @return The star system if found,
	 */
	public StarSystem getStarSystem(int systemId) {
		return em.find(StarSystem.class, systemId);
	}

	public void persist(StarSystem system) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		em.persist(system);
		transaction.commit();
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
		Query q = em
				.createQuery("from StarSystem s where s.sector = :sector order by s.name asc");
		q.setParameter("sector", sector);

		List<StarSystem> systems = q.getResultList();

		return systems;
	}

	public void close() {
		em.close();
	}

}
