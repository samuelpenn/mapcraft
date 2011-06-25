/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import uk.org.glendale.rpg.utils.Die;

/**
 * Random name generator.
 * 
 * @author Samuel Penn
 * 
 */
public class Names {
	private StringBuffer buffer = new StringBuffer();

	private Properties names = null;

	public Names(String resource) throws MalformedURLException {
		if (resource.indexOf(":") > -1) {
			getResource(new URL(resource));
		} else {
			if (resource.indexOf(".") == -1) {
				resource = "uk.org.glendale.worldgen.text." + resource;
			}
			getResource(resource);
		}
	}

	private void getResource(String bundleName) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
		names = new Properties();

		Enumeration keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			names.setProperty(key, bundle.getString(key));
		}
	}

	private void getResource(URL url) {
		throw new UnsupportedOperationException("URLs are not yet supported");
	}

	/**
	 * Get the phrase for the given key from the resource bundle. Some keys will
	 * have a number of possible options (in the form key, key.1, key.2 etc). If
	 * a key has several options, one will be selected randomly.
	 * 
	 * @param key
	 *            Key to use to find a phrase.
	 * @return The selected phrase, or null if none found.
	 */
	private String getRules(String key) {
		String text = null;

		text = names.getProperty(key);
		if (text != null) {
			int i = 0;
			while (names.getProperty(key + "." + (i + 1)) != null)
				i++;
			if (i > 0) {
				int choice = (int) (Math.random() * (i + 1));
				// System.out.println("Going for choice "+choice+" out of "+i);
				if (choice != 0)
					text = names.getProperty(key + "." + choice);
			}
		}
		// System.out.println("Got ["+key+"] ["+text+"]");

		return text;
	}

	private String get(String style, String modifier, String key) {
		String list = null;

		if (modifier != null)
			list = getRules(key + "." + modifier);
		if (list == null)
			list = getRules(key);

		// System.out.println("get["+modifier+","+key+"]: ["+list+"]");

		String[] tokens = list.split(" +");
		String rule = tokens[Die.rollZero(tokens.length)];
		String word = "";

		for (int i = 0; i < rule.length(); i++) {
			char c = rule.charAt(i);
			if (Character.isUpperCase(c)) {
				word += get(style, modifier, style + "." + c);
			} else {
				word += c;
			}
		}

		return word;
	}

	private String getName(String style, String modifier) {
		String format = null;

		if (modifier != null)
			format = getRules(style + "." + modifier + ".format");
		if (format == null)
			format = getRules(style + ".format");
		String[] roots = format.split(" ");
		String name = "";

		for (String f : roots) {
			String n = get(style, modifier, f);
			name += n.substring(0, 1).toUpperCase() + n.substring(1) + " ";
		}
		name = name.replaceAll(" '", "'");
		name = name.replaceAll("_", " ");

		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == ' ' && i < name.length() - 2) {
				name = name.substring(0, i + 1)
						+ name.substring(i + 1, i + 2).toUpperCase()
						+ name.substring(i + 2);
			}
		}

		return name.trim();
	}

	public String getPlanetName() {
		return getName("planet", null);
	}

	public static void main(String[] args) throws Exception {
		Names name = new Names("names");

		for (int i = 0; i < 10; i++) {
			System.out.println(name.getPlanetName());
		}
	}
}
