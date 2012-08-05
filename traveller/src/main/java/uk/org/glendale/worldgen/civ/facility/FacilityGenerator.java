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

import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.XMLHelper;
import uk.org.glendale.worldgen.astro.planet.Habitability;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PopulationSize;
import uk.org.glendale.worldgen.astro.planet.StarportType;
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
				String	required = n.getTextContent();
				for (String r : required.split(" ")) {
					facility.addConsumed(CommodityCode.valueOf(r));
				}
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
			config = new Properties();
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

	private String[] getAllOptions(String key) {
		String value = config.getProperty(key, "");

		if (value.trim().length() > 0) {
			String[] options = value.split(" ");
			if (options.length > 0) {
				return options;
			}
		}
		return new String[0];
	}
	
	private String[] getAllOptions(Object key, Object subKey) {
		return getAllOptions(key + "." + subKey);
	}
	
	/**
	 * Generate facilities for the given planet. Uses a properties file to
	 * determine the type of society to build based on the population size
	 * and the world type.
	 * 
	 * @param plaet		Planet to generate facilities for.
	 * @param size		Size of population for this planet.
	 */
	public void generateFacilities(Planet planet, PopulationSize size) {
		readConfig();
		
		if (size == null || size == PopulationSize.None) {
			return;
		}
		
		Habitability	h = Habitability.getHabitability(planet);
		
		String culture = getOneOption(size, h);
		
		// XXX: Force to be this for testing.
		culture = "neolithicTribes";
		
		System.out.println("  " + size + ", " + culture);
		
		// There will be one of these.
		String		residentialName = getOneOption(culture, "residential");
		String[]	facilityNames = getAllOptions(culture, "facilities");
		
		residentialName = "neolithicTribes";
		
		System.out.println("  " + residentialName+ ", " + facilityNames.length);
		
		planet.setTechLevel(Integer.parseInt(getOneOption(culture, "tech")));
		planet.setLawLevel(Integer.parseInt(getOneOption(culture, "tech")));
		planet.setStarport(StarportType.valueOf(getOneOption(culture, "port")));
		
		// Population is given as a power of 10. Randomly generate a suitable
		// number to 4 significant figures.
		int	p = Integer.parseInt(getOneOption(culture, "population"));
		switch (p) {
		case 0:
			planet.setPopulation(Die.die(9));
			break;
		case 1:
			planet.setPopulation(10 + Die.rollZero(90));
			break;
		case 2:
			planet.setPopulation(100 + Die.rollZero(900));
			break;
		default:
			planet.setPopulation((1000 + Die.rollZero(9000)) * (long)Math.pow(10, p - 3));
			break;
		}
		
		try {
			planet.setGovernment(GovernmentType.valueOf(getOneOption(culture, "government")));
			planet.setLawLevel(planet.getLawLevel() + planet.getGovernment().getLawModifier());
		} catch (Throwable e) {
			planet.setGovernment(GovernmentType.Anarchy);
		}
		
		System.out.println("Generate: "+planet.getName()+", "+planet.getPopulation()+", "+planet.getGovernment());
		
		// Add all listed trade codes to the planet.
		for (String code : getAllOptions(culture, "trade")) {
			try {
				planet.addTradeCode(TradeCode.valueOf(code));
			} catch (Throwable e) {
				// Catch illegal trade codes.
			}
		}
		
		Facility residential = factory.getFacility(residentialName);
		int		 residentialSize = 89 + Die.d10(2);
		if (planet.hasTradeCode(TradeCode.Po)) {
			residentialSize -= Die.d6(3);
		} else if (planet.hasTradeCode(TradeCode.Ri)){
			residentialSize += Die.d6(3);
		}
		planet.addFacility(residential, residentialSize);
		
		int totalSize = 0;
		for (String f : facilityNames) {
			System.out.println("  Adding facility [" + f + "]");
			int		facilitySize = 100;
			if (f.indexOf(";") > -1) {
				try {
					facilitySize = Integer.parseInt(f.replaceAll("[^0-9]", ""));
				} catch (Throwable e) {
					facilitySize = 100;
				}
			}
			totalSize += facilitySize;
		}
		double multiplier = 100.0 / totalSize;
		System.out.println("Facility totalSize ["+totalSize+"] [" + multiplier + "]");
		for (String f : facilityNames) {
			String	facilityName = f;
			int		facilitySize = 100;
			if (f.indexOf(";") > -1) {
				try {
					facilityName = f.replaceAll(";.*", "");
					facilitySize = Integer.parseInt(f.replaceAll("[^0-9]", ""));
				} catch (Throwable e) {
					facilitySize = 100;
				}
			}
			Facility	facility = factory.getFacility(facilityName);
			if (facility == null) {
				System.out.println("Unable to find facility [" + facilityName + "]");
				continue;
			}
			facilitySize *= multiplier;
			facilitySize += Die.d6() - Die.d6();
			if (facilitySize < 2) {
				facilitySize = Die.d4();
			}
			System.out.println("  Add ["+facility.getName()+"] ["+facilitySize+"]");
			planet.addFacility(facility, facilitySize);
		}
	}
}
