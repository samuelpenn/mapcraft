/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining existing Sector objects.
 * 
 * @author Samuel Penn
 */
@Repository
public class SectorFactory {
	private HibernateTemplate	template;
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		template = new HibernateTemplate(sessionFactory);
	}
	
	//private EntityManager entityManager;

	public SectorFactory(EntityManager hibernateEntityManager) {
		//entityManager = hibernateEntityManager;
	}

	public SectorFactory() {
		//em = AppManager.getInstance().getEntityManager();
	}

	@SuppressWarnings("unchecked")
	public List<Sector> getAllSectors() {
		//Session	session = sessionFactory.getCurrentSession();
		
		ListIterator<Sector> it = template.find("from Sector").listIterator();
		
		while (it.hasNext()) {
			Sector s = it.next();
			System.out.println(s.getName());
		}
		
		/*
		List<Sector> sectors = session.createQuery(
				"from Sector s order by s.name asc").
				getResultList();
		*/
		/*
		System.out.println(sectors.size() + " sectors found:");
		for (Iterator iter = sectors.iterator(); iter.hasNext();) {
			Sector loadedMsg = (Sector) iter.next();
			System.out.println(loadedMsg.getName());
		}
		*/

		return null;
		//return sectors;
	}

	/**
	 * Gets a sector identified by its unique id.
	 * 
	 * @param id
	 * @return
	 */
	public Sector getSector(int id) {
		return null;//entityManager.find(Sector.class, id);
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
		/*
		if (name.matches("[0-9]+")) {
			return getSector(Integer.parseInt(name));
		} else {
			Query q = entityManager.createQuery("from Sector where name = :n");
			q.setParameter("n", name);
			return (Sector) q.getSingleResult();
		}
		*/
		return null;
	}

	public void close() {
		//entityManager.close();
	}
}
