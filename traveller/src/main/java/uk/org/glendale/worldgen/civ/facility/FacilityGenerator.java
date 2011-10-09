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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.XMLHelper;
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

		Facility facility = new Facility(name, type, baseDir + image);
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
}
