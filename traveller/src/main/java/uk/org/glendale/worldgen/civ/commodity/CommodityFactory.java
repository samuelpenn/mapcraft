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
	@SuppressWarnings("unchecked")
	public List<Commodity> getChildren(Commodity parent) {
		Query q = em.createQuery("from Commodity where parent = :c");
		q.setParameter("c", parent);
		try {
			return q.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<Commodity>();
		}
	}

	@SuppressWarnings("unchecked")
	public List<CommodityMap> getMappings(Commodity commodity) {
		Query q = em.createQuery("from CommodityMap where commodity = :c");
		q.setParameter("c", commodity);
		try {
			return q.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<CommodityMap>();
		}
	}

	/**
	 * Gets the value of the named attribute on this node. If the node has no
	 * such attribute, or it is empty, then null is returned.
	 * 
	 * @param node
	 *            Node to get attribute from.
	 * @param name
	 *            Name of attribute to read.
	 * @return Value of attribute, or null.
	 */
	private String getAttribute(Node node, String name) {
		String value = null;

		if (node.getAttributes() != null) {
			Node n = node.getAttributes().getNamedItem(name);
			if (n != null) {
				value = n.getNodeValue();
				if (value != null && value.length() == 0) {
					value = null;
				}
			}
		}
		return value;
	}

	private int getInteger(Node node, String name) {
		int value = 0;
		String v = getAttribute(node, name);
		if (v != null) {
			try {
				value = Integer.parseInt(v);
			} catch (NumberFormatException e) {
				// Ignore.
			}
		}
		return value;
	}

	/**
	 * Create a new commodity if it doesn't already exist. Writes commodity into
	 * the database.
	 * 
	 * @param node
	 *            XML node that defines the commodity.
	 * @param baseDir
	 *            Base directory where images are located.
	 */
	private void createCommodity(Node node, String baseDir) {
		Commodity commodity = new Commodity();
		String name = getAttribute(node, "name");
		String parent = getAttribute(node, "parent");

		if (name == null || name.length() == 0 || getCommodity(name) != null) {
			return;
		}
		String image = name.toLowerCase().replaceAll(" ", "_");
		if (parent != null) {
			commodity.setParent(getCommodity(parent));
		} else {
			commodity.setParent(null);
		}

		// Defaults
		commodity.setName(name);
		commodity.setImagePath(baseDir + image);
		NodeList params = node.getChildNodes();
		System.out.println(name);
		for (int j = 0; j < params.getLength(); j++) {
			Node p = params.item(j);
			if (p.getNodeType() == Node.ELEMENT_NODE) {
				String pName = p.getNodeName();
				String value = p.getTextContent().trim();

				if (pName.equals("cost")) {
					commodity.setCost(Integer.parseInt(value));
					commodity.setVolume(getInteger(p, "volume"));
				} else if (pName.equals("production")) {
					commodity.setLawLevel(getInteger(p, "law"));
					commodity.setTechLevel(getInteger(p, "tech"));
					commodity.setProductionRating(getInteger(p, "pr"));
					commodity.setConsumptionRating(getInteger(p, "cr"));
				} else if (pName.equals("codes")) {
					commodity.setSource(Source
							.valueOf(getAttribute(p, "source")));
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

	/**
	 * Create commodities from the XML file provided.
	 * 
	 * @param file
	 *            File containing commodity definitions.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void createCommodities(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		EntityTransaction transaction = em.getTransaction();
		transaction.begin();

		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			String baseDir = getAttribute(group, "base");
			if (baseDir == null) {
				baseDir = "";
			} else if (!baseDir.endsWith("/")) {
				baseDir += "/";
			}
			NodeList list = group.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				Node node = list.item(j);
				createCommodity(node, baseDir);
			}
		}
		transaction.commit();
	}

	private void createMappings(Node node) {
		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeName().equals("production")) {
				NodeList outputs = n.getChildNodes();
				if (outputs != null && outputs.getLength() > 0) {
					for (int j = 0; j < outputs.getLength(); j++) {
						Node o = outputs.item(j);
						if (o.getNodeName().equals("output")) {
							String value = o.getTextContent().trim();
							System.out.println(getAttribute(node, "name")
									+ ": " + value);
							if (getCommodity(value) == null) {
								continue;
							}
							String mode = getAttribute(o, "mode");
							int efficiency = getInteger(o, "efficiency");
							if (mode == null) {
								continue;
							}
							if (efficiency < 1) {
								efficiency = 100;
							}
							CommodityMap map = new CommodityMap(
									getCommodity(getAttribute(node, "name")),
									getCommodity(value), mode);
							em.persist(map);
						}
					}
				}
			}
		}
	}

	private void createMappings(File file) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		EntityTransaction transaction = em.getTransaction();

		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			NodeList list = group.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				transaction.begin();
				Node node = list.item(j);
				createMappings(node);
				transaction.commit();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		CommodityFactory factory = new CommodityFactory();
		String base = "src/main/resources/commodities/";

		String[] files = { "minerals.xml", "organic.xml" };

		// Requires two passes to build everything. This allows mappings
		// to refer to future commodities.
		for (String file : files) {
			factory.createCommodities(new File(base + file));
		}
		for (String file : files) {
			factory.createMappings(new File(base + file));
		}
		Commodity c = factory.getCommodity("Minerals");

		System.out.println(c.getName() + " (" + c.getSource() + ")");
		while (c.getParent() != null) {
			c = c.getParent();
			System.out.println(c.getName());
		}
	}

}
