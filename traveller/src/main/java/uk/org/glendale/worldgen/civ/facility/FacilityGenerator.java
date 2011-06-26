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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.worldgen.XMLHelper;

/**
 * Generates facilities.
 * 
 * @author Samuel Penn
 * 
 */
public class FacilityGenerator {
	private FacilityFactory	factory;

	public FacilityGenerator() {
		this.factory = new FacilityFactory();
	}

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

	private void createFacility(Node node, String baseDir) {
		String name = XMLHelper.getAttribute(node, "name");

		if (name == null || name.trim().length() == 0) {
			return;
		}

		NodeList nodes = node.getChildNodes();
		String image = name.toLowerCase().replaceAll(" ", "_");
		FacilityType type = FacilityType.Residential;
		type = FacilityType.valueOf(XMLHelper.getAttribute(node, "type"));

		Facility facility = new Facility(name, type, baseDir + image);

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
			}
		}

		factory.createFacility(facility);
	}

	private void createFacilities(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = db.parse(file);
		NodeList groups = document.getElementsByTagName("group");

		for (int i = 0; i < groups.getLength(); i++) {
			Node group = groups.item(i);
			String baseDir = XMLHelper.getAttribute(group, "base");
			if (baseDir == null) {
				baseDir = "";
			} else if (!baseDir.endsWith("/")) {
				baseDir += "/";
			}
			NodeList list = group.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				Node node = list.item(j);
				createFacility(node, baseDir);
			}
		}
	}
}
