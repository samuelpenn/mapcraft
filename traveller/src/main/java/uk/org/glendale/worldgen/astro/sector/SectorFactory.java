/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.rpg.traveller.Log;

/**
 * Factory class for creating and fetching Sector objects. This class is
 * just a simple wrapper to the persistence layer, and has no intelligence
 * associated with it.
 * 
 * @see uk.org.glendale.worldgen.astro.sector.SectorGenerator
 * 
 * @author Samuel Penn
 */
@Repository
@Transactional
public class SectorFactory {
	@PersistenceContext
	private EntityManager		em;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Empty bean constructor used by Spring.
	 */
	public SectorFactory() {
	}

	/**
	 * Gets a list of all the sectors defined in this universe.
	 * 
	 * @return	List of sectors, may be empty, never null.
	 */
	@SuppressWarnings("unchecked")
	public List<Sector> getAllSectors() {
		return em.createQuery("from Sector").getResultList();
	}

	/**
	 * Gets a sector identified by its unique id.
	 * 
	 * @param id	Id of the sector to locate.
	 * @return		Found sector, or null if it doesn't exist.
	 */
	public Sector getSector(int id) {
		Query q = em.createQuery("SELECT s FROM Sector s WHERE s.id = :id");
		q.setParameter("id", id);
		
		try {
			return (Sector) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Gets a sector identified by its name. If the name is entirely numeric,
	 * then it is assumed to be an id, and an id based search is returned
	 * instead.
	 * 
	 * @param name		Name of the sector to locate.
	 * @return			Found sector, or null if it doesn't exist.
	 */
	public Sector getSector(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Sector name cannot be empty");
		}
		if (name.matches("^[0-9]+$")) {
			return getSector(Integer.parseInt(name));
		}
		
		Query q = em.createQuery("SELECT s FROM Sector s WHERE s.name = :name");
		q.setParameter("name", name);
		
		try {
			return (Sector) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public Sector getSector(int x, int y) {
		Query q = em.createQuery("SELECT s FROM Sector s WHERE x = :x and y = :y");
		q.setParameter("x", x);
		q.setParameter("y", y);
		
		try {
			return (Sector) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Create a new sector and persist it.
	 * 
	 * @param name			Name of the sector.
	 * @param x				X coordinate of sector.
	 * @param y				Y coordinate of sector.
	 * @param allegiance	Allegiance, if any.
	 * @param codes			Sector codes, if any.
	 */
	@Transactional
	public Sector createSector(String name, int x, int y, String allegiance, SectorCode... codes) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Sector name must be valid");
		} else if (name.matches("[0-9]+")) {
			throw new IllegalArgumentException("Sector name must not be a number");
		}
		if (getSector(name) != null) {
			throw new IllegalStateException("Sector [" + name + "] already exists.");
		}
		
		Sector sector = new Sector();
		sector.setName(name);
		sector.setX(x);
		sector.setY(y);
		if (allegiance != null && allegiance.trim().length() > 0) {
			sector.setAllegiance(allegiance);
		} else {
			sector.setAllegiance("Un");
		}
		if (codes != null && codes.length > 0) {
			for (SectorCode code : codes) {
				sector.addCode(code);
			}
		}
		em.persist(sector);
		
		return sector;
	}
}
