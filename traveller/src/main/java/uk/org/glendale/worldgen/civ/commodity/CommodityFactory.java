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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.XMLHelper;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides access to commodities.
 * 
 * @author Samuel Penn
 */
@Repository
@Transactional
public class CommodityFactory {
	@PersistenceContext
	EntityManager	em;

	public CommodityFactory() {
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
		Query q = em.createQuery("SELECT c FROM Commodity c WHERE c.name = :n");
		q.setParameter("n", name);
		try {
			return (Commodity) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Gets all the children of the specified commodity. Only direct children of
	 * the commodity are returned. If the parent is null, then all root items
	 * will be returned.
	 * 
	 * @param parent
	 *            Parent to find children of.
	 * @return List of all children. May be empty.
	 */
	@SuppressWarnings("unchecked")
	public List<Commodity> getChildren(Commodity parent) {
		Query q;

		if (parent == null) {
			q = em.createQuery("from Commodity where parent is null");
		} else {
			q = em.createQuery("from Commodity where parent = :c");
			q.setParameter("c", parent);
		}

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
		String name = XMLHelper.getAttribute(node, "name");
		String parent = XMLHelper.getAttribute(node, "parent");

		if (name == null || name.length() == 0 || getCommodity(name) != null) {
			return;
		}
		System.out.println("    "+name);
		String image = name.toLowerCase().replaceAll(" ", "_");
		if (parent != null) {
			if (getCommodity(parent) == null) {
				throw new IllegalArgumentException("Commodity [" + name
						+ "] has unknown parent [" + parent + "]");
			}
			commodity.setParent(getCommodity(parent));
		} else {
			commodity.setParent(null);
		}

		// Defaults
		commodity.setName(name);
		commodity.setImagePath(baseDir + image);
		NodeList params = node.getChildNodes();
		for (int j = 0; j < params.getLength(); j++) {
			Node p = params.item(j);
			if (p.getNodeType() == Node.ELEMENT_NODE) {
				String pName = p.getNodeName();
				String value = p.getTextContent().trim();

				if (pName.equals("cost")) {
					commodity.setCost(Integer.parseInt(value));
					commodity.setVolume(XMLHelper.getInteger(p, "volume"));
				} else if (pName.equals("production")) {
					commodity.setLawLevel(XMLHelper.getInteger(p, "law"));
					commodity.setTechLevel(XMLHelper.getInteger(p, "tech"));
					commodity
							.setProductionRating(XMLHelper.getInteger(p, "pr"));
					commodity.setConsumptionRating(XMLHelper
							.getInteger(p, "cr"));
				} else if (pName.equals("codes")) {
					commodity.setSource(Source.valueOf(XMLHelper.getAttribute(
							p, "source")));
					String[] codes = value.split(" ");
					for (String c : codes) {
						commodity.addCode(CommodityCode.valueOf(c));
					}
				}
			}
		}
		System.out.println("Persist "+name);
		persist(commodity);
	}
	
	public void createCommodity(String name) {
		Commodity c = new Commodity();
		c.setName(name);
		
		persist(c);
		
		System.out.println(c.getId());
	}
	
	@Transactional
	public void persist(Commodity commodity) {
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
	public void createCommodities(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			System.out.println(XMLHelper.getAttribute(group, "name"));
			String baseDir = XMLHelper.getAttribute(group, "base");
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
							if (getCommodity(value) == null) {
								continue;
							}
							String mode = XMLHelper.getAttribute(o, "mode");
							int efficiency = XMLHelper.getInteger(o,
									"efficiency");
							if (mode == null) {
								continue;
							}
							if (efficiency < 1) {
								efficiency = 100;
							}
							CommodityMap map = new CommodityMap(
									getCommodity(XMLHelper.getAttribute(node,
											"name")), getCommodity(value), mode, 
											efficiency);
							em.persist(map);
						}
					}
				}
			}
		}
	}

	public void createMappings(File file) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			NodeList list = group.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				Node node = list.item(j);
				createMappings(node);
			}
		}
	}

	public void createAllCommodities(final File base)
			throws ParserConfigurationException, SAXException, IOException {
		if (base.isDirectory()) {
			// Add all files in the directory.
			File[] files = base.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});

			// Requires two passes to build everything. This allows mappings
			// to refer to future commodities.
			for (File file : files) {
				createCommodities(file);
			}
			for (File file : files) {
				createMappings(file);
			}
		} else {
			// Just add a single file.
			createCommodities(base);
			createMappings(base);
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
