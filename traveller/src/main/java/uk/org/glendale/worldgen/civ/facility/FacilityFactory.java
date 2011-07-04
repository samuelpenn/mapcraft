/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import uk.org.glendale.worldgen.server.AppManager;

public class FacilityFactory {
	EntityManager	em;

	public FacilityFactory(EntityManager hibernateEntityManager) {
		if (em == null) {
			em = AppManager.getInstance().getEntityManager();
		}
		em = hibernateEntityManager;
	}

	public FacilityFactory() {
		em = AppManager.getInstance().getEntityManager();
	}

	public Facility getFacility(int id) {
		return em.find(Facility.class, id);
	}

	public Facility getFacility(String name) {
		Query q = em.createQuery("from Facility where name = :n");
		q.setParameter("n", name);
		try {
			return (Facility) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public void createFacility(Facility facility) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		em.persist(facility);
		transaction.commit();
	}

	public void persist(Facility facility) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		em.persist(facility);
		transaction.commit();
	}
}
