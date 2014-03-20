/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen;

import org.w3c.dom.Node;

public class XMLHelper {
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
	public static String getAttribute(Node node, String name) {
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

	public static int getInteger(Node node, String name) {
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
	 * Gets the text content of the given XML node.
	 * 
	 * @param node
	 *            Node to get text content for.
	 * @return Trimmed node content, or null if node is null.
	 */
	public static String getText(Node node) {
		if (node == null || node.getFirstChild() == null) {
			return null;
		} else if (node.getFirstChild().getNodeValue() == null) {
			return "";
		} else {
			return node.getFirstChild().getNodeValue().trim();
		}
	}

}
