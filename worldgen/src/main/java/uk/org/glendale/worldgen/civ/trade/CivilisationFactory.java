/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;
import uk.org.glendale.worldgen.civ.facility.FacilityFactory;

/**
 * Spring factory for configuring and returning Civilisation POJOs.
 * 
 * @author Samuel Penn
 */
@Service
public class CivilisationFactory {
	@PersistenceContext
	private EntityManager		em;

	@Autowired
	private CommodityFactory	commodityFactory;
	
	@Autowired
	private FacilityFactory		facilityFactory;
	
	/**
	 * Gets a civilisation object for the specified planet. The Civilisation
	 * object is fully configured with factory dependencies before being
	 * returned.
	 * 
	 * @param planet		Planet to get this civilisation for.
	 * @return				Fully configured Civilisation object.
	 */
	public final Civilisation getCivilisation(Planet planet) {
		Civilisation	civ = new Civilisation(planet);
		
		civ.setCommodityFactory(commodityFactory);
		civ.setFacilityFactory(facilityFactory);
		civ.setInventory(getPlanetInventory(planet));

		return civ;
	}
	
	public final List<Inventory> getPlanetInventory(Planet planet) {
		Query query = em.createQuery("SELECT i FROM Inventory i WHERE i.planet = :planet");
		query.setParameter("planet", planet);
		
		List<Inventory> inventory = query.getResultList();
		
		return inventory;
	}
	
	public final void persist(List<Inventory> inventory) {
		for (Inventory item : inventory) {
			em.persist(item);
		}
	}
}
