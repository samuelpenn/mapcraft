/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.rpg.traveller.civilisation.trade.CommodityCode;
import uk.org.glendale.rpg.traveller.civilisation.trade.Source;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides access to commodities.
 * 
 * @author Samuel Penn
 */
@ManagedBean
public class CommodityFactory {
	EntityManager em;

	public CommodityFactory(EntityManager hibernateEntityManager) {
		if (em == null) {
			em = AppManager.getInstance().getEntityManager();
		}
		em = hibernateEntityManager;
	}

	public CommodityFactory() {
		em = AppManager.getInstance().getEntityManager();
	}

	/**
	 * Gets a commodity by its unique id.
	 * 
	 * @param id
	 *            Unique id of the commodity.
	 * @return The commodity found, or null.
	 */
	public Commodity getCommodity(int id) {
		return em.find(Commodity.class, id);
	}

	/**
	 * Gets a named commodity.
	 * 
	 * @param name
	 *            Name of the commodity to retrieve.
	 * @return Found commodity, or null.
	 */
	public Commodity getCommodity(String name) {
		Query q = em.createQuery("from Commodity where name = :n");
		q.setParameter("n", name);
		try {
			return (Commodity) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Gets all the children of the specified commodity. Only direct children of
	 * the commodity are returned.
	 * 
	 * @param parent
	 *            Parent to find children of.
	 * @return List of all children. May be empty.
	 */
	public List<Commodity> getChildren(Commodity parent) {
		Query q = em.createQuery("from Commodity where parent = :c");
		q.setParameter("c", parent);
		try {
			return q.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<Commodity>();
		}
	}

	private void createCommodities(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList list = document.getElementsByTagName("commodity");

		EntityTransaction transaction = em.getTransaction();
		transaction.begin();

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			Commodity commodity = new Commodity();
			String name = node.getAttributes().getNamedItem("name")
					.getNodeValue();

			if (getCommodity(name) != null) {
				continue;
			}

			// Defaults
			commodity.setName(name);
			commodity.setImagePath(name.toLowerCase().replaceAll(" ", ""));
			NodeList params = node.getChildNodes();
			System.out.println(name);
			for (int j = 0; j < params.getLength(); j++) {
				Node p = params.item(j);
				if (p.getNodeType() == Node.ELEMENT_NODE) {
					String pName = p.getNodeName();
					String value = p.getTextContent().trim();
					System.out.println("  " + pName + ": " + value);
					if (pName.equals("source")) {
						commodity.setSource(Source.valueOf(value));
					} else if (pName.equals("image")) {
						commodity.setImagePath(value);
					} else if (pName.equals("cost")) {
						commodity.setCost(Integer.parseInt(value));
					} else if (pName.equals("volume")) {
						commodity.setVolume(Integer.parseInt(value));
					} else if (pName.equals("law")) {
						commodity.setLawLevel(Integer.parseInt(value));
					} else if (pName.equals("tech")) {
						commodity.setTechLevel(Integer.parseInt(value));
					} else if (pName.equals("pr")) {
						commodity.setProductionRating(Integer.parseInt(value));
					} else if (pName.equals("cr")) {
						commodity.setConsumptionRating(Integer.parseInt(value));
					} else if (pName.equals("codes")) {
						String[] codes = value.split(" ");
						for (String c : codes) {
							System.out.println("[" + c + "]");
							commodity.addCode(CommodityCode.valueOf(c));
						}
					}
				}
			}
			em.persist(commodity);
		}
		transaction.commit();
	}

	public static void main(String[] args) throws Exception {
		CommodityFactory factory = new CommodityFactory();
		factory.createCommodities(new File(
				"src/main/resources/commodities/organic.xml"));

		Commodity c = factory.getCommodity(1);

		System.out.println(c.getName() + " (" + c.getSource() + ")");
		while (c.getParent() != null) {
			c = c.getParent();
			System.out.println(c.getName());
		}
	}

}
