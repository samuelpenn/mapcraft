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

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
	private SessionFactory		sessionFactory;
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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
		List<Sector>	list = new ArrayList<Sector>();
		
		Iterator<Sector> it = sessionFactory.getCurrentSession().createQuery("from Sector").iterate();
		
		while (it != null && it.hasNext()) {
			Sector s = it.next();
			s.getId(); // Force loading of entity.
			list.add(s);
		}

		return list;
	}

	/**
	 * Gets a sector identified by its unique id.
	 * 
	 * @param id
	 * @return
	 */
	public Sector getSector(int id) {
		Query q = sessionFactory.getCurrentSession().createQuery("from Sector where id = :id");
		q.setParameter("id", id);
		
		return (Sector) q.uniqueResult();		
	}

	/**
	 * Gets a sector identified by its name. If the name is entirely numeric,
	 * then it is assumed to be an id, and an id based search is returned
	 * instead.
	 * 
	 * @param name
	 * @return
	 */
	public Sector getSector(String name) {
		Query q = sessionFactory.getCurrentSession().createQuery("from Sector where name = :name");
		q.setParameter("name", name);
		
		return (Sector) q.uniqueResult();
	}
	
	@Transactional
	public void createSector(String name, int x, int y, String allegiance) {
		Sector sector = new Sector();
		sector.setName(name);
		sector.setX(x);
		sector.setY(y);
		sector.setAllegiance(allegiance);
		sessionFactory.getCurrentSession().persist(sector);
	}
}
