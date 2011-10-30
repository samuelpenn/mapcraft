/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.XMLHelper;
import uk.org.glendale.worldgen.astro.planet.Habitability;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.civ.commodity.Commodity;
import uk.org.glendale.worldgen.civ.commodity.CommodityCode;
import uk.org.glendale.worldgen.civ.commodity.CommodityFactory;

/**
 * Generates facilities from XML configuration files.
 * 
 * @author Samuel Penn
 * 
 */
@Service
public class FacilityGenerator {
	@Autowired
	private FacilityFactory	factory;
	@Autowired
	private CommodityFactory	commodityFactory;

	public FacilityGenerator() {
	}

	/**
	 * Import list of facilities into database from XML file. If the given file
	 * is a directory, all XML files in that directory are processed.
	 * 
	 * @param base	XML file to import, or directory of XML files.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void createAllFacilities(final File base)
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
				createFacilities(file);
			}
		} else {
			// Just add a single file.
			createFacilities(base);
		}
	}
	
	private String getTextChild(Node node, String childName) {
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			if (child != null && child.getNodeName().equals(childName)) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Creates and imports a new facility into the database from the XML
	 * document.
	 * 
	 * @param node			XML node to parse.
	 * @param baseDir		Base directory for resources.
	 */
	private void createFacility(Node node, String baseDir) {
		String name = XMLHelper.getAttribute(node, "name");

		if (name == null || name.trim().length() == 0) {
			return;
		}
		System.out.println("  "+name);

		NodeList nodes = node.getChildNodes();
		String image = name.toLowerCase().replaceAll(" ", "_");
		FacilityType type = FacilityType.Residential;
		type = FacilityType.valueOf(XMLHelper.getAttribute(node, "type"));
		
		String title = getTextChild(node, "title");
		if (title == null || title.trim().length() == 0) {
			title = name;
		}
		
		Facility facility = new Facility(name, title, type, baseDir + image);
		List<ProductionMap>	map = new ArrayList<ProductionMap>();
		
		if (factory.getFacility(name) != null) {
			// Duplicate, so skip this one.
			return;
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			if (n.getNodeName().equals("codes")) {
				String codes = n.getTextContent();
				for (String code : codes.split(" ")) {
					facility.addCode(FacilityCode.valueOf(code));
				}
			} else if (n.getNodeName().equals("operation")) {
				String ops = n.getTextContent();
				int level = XMLHelper.getInteger(n, "level");
				if (level == 0) {
					level = 100;
				}
				for (String op : ops.split(" ")) {
					facility.addOperation(op, level);
				}
			} else if (n.getNodeName().equals("require")) {
				String	required = n.getTextContent();
				for (String r : required.split(" ")) {
					facility.addRequired(CommodityCode.valueOf(r));
				}
			} else if (n.getNodeName().equals("consume")) {
			} else if (n.getNodeName().equals("map")) {
				String fromName = XMLHelper.getAttribute(n, "from");
				String toName = XMLHelper.getAttribute(n, "to");
				int		level = XMLHelper.getInteger(n, "level");
				if (level < 1) {
					// Default value if no attribute set.
					level = 100;
				}
				
				Commodity from = commodityFactory.getCommodity(fromName);
				Commodity to = commodityFactory.getCommodity(toName);
				if ((fromName == null || from != null) && (toName == null || to != null)) {
					map.add(new ProductionMap(facility, from, to, level));
				} else if (from == null) {
					System.out.println("Facility [" + name + "] maps unknown commodity [" + fromName + "]");
				} else {
					System.out.println("Facility [" + name + "] maps unknown commodity [" + toName + "]");					
				}
				
			}
		}

		factory.persist(facility);
		factory.persist(map);
		System.out.println(facility.getId());
	}

	private void createFacilities(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		System.out.println("Adding " + groups.getLength() + " groups from " + file.getName());
		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			System.out.println(XMLHelper.getAttribute(group, "name") + ": ");
			String baseDir = XMLHelper.getAttribute(group, "base");
			if (baseDir == null) {
				baseDir = "";
			} else if (!baseDir.endsWith("/")) {
				baseDir += "/";
			}
			NodeList list = group.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				Node node = list.item(j);
				if (node.getNodeName() == "facility") {
					createFacility(node, baseDir);
				}
			}
		}
	}
	
	private Properties	config;
	
	/**
	 * Read all the configuration properties from the resource file.
	 * If the file has already been read, then don't read it again.
	 */
	private void readConfig() {
		if (config == null) {
			ResourceBundle	bundle = ResourceBundle.getBundle("uk.org.glendale.worldgen.civ.facility.facilities");
			Enumeration<String>		e = bundle.getKeys();
			while (e.hasMoreElements()) {
				String	key = e.nextElement();
				config.setProperty(key, bundle.getString(key));
			}
		}
	}
	
	/**
	 * Get one option from the configuration. A configuration value may consist
	 * of multiple space separated options. If there is more than one, chose one
	 * at random and return that. If the key is unset, returns null.
	 * 
	 * @param key		Key to retrieve from the configuration.
	 * @return			One of the listed options from the value.
	 */
	private String getOneOption(String key) {
		String value = config.getProperty(key, "");
		if (value.trim().length() > 0) {
			String[] options = value.split(" ");
			if (options.length > 1) {
				value = options[Die.rollZero(options.length)];
			}
		}
		return value.trim();
	}
	
	/**
	 * Get the value of the key key"."subKey. If a value consists of
	 * multiple options, one is chosen randomly.
	 * 
	 * @param key		First part of the key.
	 * @param subKey	Second part of the key.
	 * @return			One value chosen from the list of options.
	 */
	private String getOneOption(Object key, Object subKey) {
		return getOneOption(key + "." + subKey);
	}
	
	/**
	 * Generate facilities for the given planet. Uses a properties file to
	 * determine the type of society to build based on the population size
	 * and the world type.
	 * 
	 * @param planet
	 */
	public void generateFacilities(Planet planet, PopulationSize size) {
		readConfig();
		
		Habitability	h = Habitability.getHabitability(planet);
		
		String culture = getOneOption(size, h);
		
		String	residential = getOneOption(culture, "residential");
	}
}
