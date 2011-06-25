/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import uk.org.glendale.worldgen.server.AppManager;

@ManagedBean
public class FacilityFactory {
	EntityManager em;

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

	/*
	 * private void createFacilities(File file) throws
	 * ParserConfigurationException, SAXException, IOException { DocumentBuilder
	 * db = DocumentBuilderFactory.newInstance().newDocumentBuilder(); Document
	 * document = db.parse(file); NodeList list =
	 * document.getElementsByTagName("commodity");
	 * 
	 * EntityTransaction transaction = em.getTransaction(); transaction.begin();
	 * 
	 * for (int i=0; i < list.getLength(); i++) { Node node = list.item(i);
	 * Facility facility = new Facility(); String name =
	 * node.getAttributes().getNamedItem("name").getNodeValue();
	 * 
	 * if (getFacility(name) != null) { facility = getFacility(name); }
	 * 
	 * // Defaults facility.setName(name);
	 * facility.setImagePath(name.toLowerCase().replaceAll(" ", "")); NodeList
	 * params = node.getChildNodes(); System.out.println(name); for (int j=0; j
	 * < params.getLength(); j++) { Node p = params.item(j); if (p.getNodeType()
	 * == Node.ELEMENT_NODE) { String pName = p.getNodeName(); String value =
	 * p.getTextContent().trim(); System.out.println("  "+pName+": "+value); if
	 * (pName.equals("source")) { facility.setSource(Source.valueOf(value)); }
	 * else if (pName.equals("image")) { facility.setImagePath(value); } else if
	 * (pName.equals("cost")) { facility.setCost(Integer.parseInt(value)); }
	 * else if (pName.equals("volume")) {
	 * facility.setVolume(Integer.parseInt(value)); } else if
	 * (pName.equals("law")) { facility.setLawLevel(Integer.parseInt(value)); }
	 * else if (pName.equals("tech")) {
	 * facility.setTechLevel(Integer.parseInt(value)); } else if
	 * (pName.equals("pr")) {
	 * facility.setProductionRating(Integer.parseInt(value)); } else if
	 * (pName.equals("cr")) {
	 * facility.setConsumptionRating(Integer.parseInt(value)); } else if
	 * (pName.equals("codes")) { String[] codes = value.split(" "); for (String
	 * c : codes) { System.out.println("["+c+"]");
	 * facility.addCode(CommodityCode.valueOf(c)); } } } } em.persist(facility);
	 * } transaction.commit(); }
	 */

	public static void main(String[] args) throws Exception {
		FacilityFactory factory = new FacilityFactory();
		// factory.createCommodities(new File("docs/commodities.xml"));

		Facility f = factory.getFacility(55);

		System.out.println(f.getName());
	}

}
