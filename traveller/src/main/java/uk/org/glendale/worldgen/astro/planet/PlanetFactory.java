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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.worldgen.astro.star.Star;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityGenerator;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining existing Planet objects.
 * 
 * @author Samuel Penn
 */
@Repository
public class PlanetFactory {
	@PersistenceContext
	private EntityManager		em;
	
	@Autowired
	private CommodityFactory		commodityFactory;
	
	@Autowired
	private FacilityFactory			facilityFactory;
	
	@Autowired
	private FacilityGenerator		facilityGenerator;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	public CommodityFactory getCommodityFactory() {
		return commodityFactory;
	}
	
	public FacilityFactory getFacilityFactory() {
		return facilityFactory;
	}
	
	public FacilityGenerator getFacilityGenerator() {
		return facilityGenerator;
	}
	
	public PlanetFactory() {
	}
	
	/**
	 * Gets a planet identified by its unique id.
	 * @param id
	 * @return
	 */
	public Planet getPlanet(int id) {
		Query query = em.createQuery("SELECT p FROM Planet p WHERE p.id = :id");
		query.setParameter("id", id);

		try {
			Planet planet = (Planet) query.getSingleResult();
			planet.getId();
			return planet;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Gets the moons for the specified planet. A planet will have zero or more
	 * moons, so the returned list may be empty. It will never be null. A moon
	 * is simply a Planet where the isMoon() property is true.
	 * 
	 * @param planet	Planet to get the moons of.
	 * @return			List containing zero or more moons (Planets).
	 */
	@SuppressWarnings("unchecked")
	public List<Planet>	getMoons(Planet planet) {
		Query query = em.createQuery("SELECT p FROM Planet p WHERE p.parentId = :planet AND isMoon = true");
		query.setParameter("planet", planet.getId());

		return query.getResultList();
	}
	
	public byte[] getPlanetImage(int id, MapImage.Projection projection) {
		/*
		Query query = em.createQuery("from MapImage m where m.id = :i and m.type = :p");
		query.setParameter("i", id);
		query.setParameter("p", projection);
		
		MapImage	image = (MapImage) query.getSingleResult();
		
		return image.getData();
		*/
		Planet planet = getPlanet(id);

		return planet.getImage(projection);
	}
	
	public void persist(Planet planet) {
		em.persist(planet);
	}
	
	@SuppressWarnings("unchecked")
	public List<Planet> getPlanetsWithEvent(long eventTime, int maxNumber) {
		Query  query = em.createQuery("SELECT p FROM Planet p WHERE p.nextEvent > 0 AND p.nextEvent < :t");
		query.setParameter("t", eventTime);
		if (maxNumber > 0) {
			query.setMaxResults(maxNumber);
		}
		return query.getResultList();
	}
	
}
