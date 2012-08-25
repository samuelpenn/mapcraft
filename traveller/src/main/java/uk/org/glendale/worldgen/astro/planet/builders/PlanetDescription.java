/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.4 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Builder;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hermian;

/**
 * Create a random description for a planet. Descriptions are made up of phrases
 * which are read from a resource file, allowing easy changing. The phrases
 * support a very simple form of flow control, allowing a phrase to consist of
 * random elements, to reference other elements, and to choose based on planet
 * values.
 * 
 * [a|b|c] Select random one of a, b or c {a} Insert phrase referenced by key a
 * {a|b|c} Select random one of a, b or c and reference that key. (50?phrase)
 * 50% chance of displaying phrase (25?a|b) 25% chance of a, otherwise b
 * (Prop>20?a:b) Get property of planet, compare with value, then a otherwise b.
 * $Prop Value of property for planet.
 * 
 * 
 * @author Samuel Penn
 */
public class PlanetDescription {
	private StringBuffer buffer = new StringBuffer();
	private Builder builder = null;
	private Planet planet = null;

	private Properties phrases = null;

	/**
	 * Get the phrase for the given key from the resource bundle. Some keys will
	 * have a number of possible options (in the form key, key.1, key.2 etc). If
	 * a key has several options, one will be selected randomly.
	 * 
	 * @param key
	 *            Key to use to find a phrase.
	 * @return The selected phrase, or null if none found.
	 */
	private String getPhrase(String key) {
		String text = null;

		text = phrases.getProperty(key);
		if (text != null) {
			int i = 0;
			while (phrases.getProperty(key + "." + (i + 1)) != null)
				i++;
			if (i > 0) {
				int choice = (int) (Math.random() * (i + 1));
				// System.out.println("Going for choice "+choice+" out of "+i);
				if (choice != 0)
					text = phrases.getProperty(key + "." + choice);
			}
		}
		// System.out.println("Got ["+key+"] ["+text+"]");

		return text;
	}

	private void readResources() {
		Class<?> cls = builder.getClass();
		ResourceBundle bundle = null;

		phrases = new Properties();

		while (cls != null) {
			try {
				bundle = ResourceBundle.getBundle(cls.getName());
			} catch (MissingResourceException e) {
				// If a bundle is missing, just skip to the next one.
				cls = cls.getSuperclass();
				continue;
			}
			Enumeration<String> e = bundle.getKeys();
			while (e.hasMoreElements()) {
				String key = e.nextElement();
				String value = bundle.getString(key);
				if (phrases.getProperty(key) == null) {
					phrases.setProperty(key, value);
				}
			}
			if (cls.getSimpleName().equals(PlanetBuilder.class.getSimpleName())) {
				// The PlanetBuilder should be the top level.
				break;
			}
			cls = cls.getSuperclass();
		}
	}

	public PlanetDescription(Builder builder) {
		this.builder = builder;
		this.planet = builder.getPlanet();
		if (this.planet == null || builder.getStar() == null) {
			throw new IllegalStateException(
					"Planet builder has not been correctly initiated");
		}
		readResources();
	}

	/**
	 * Get a random phrase from the text fragment. The text may be bounded by [
	 * ] and will be delimited by '|'. For example, [hello|hi|greetings] will
	 * return one of 'hello', 'hi' or 'greetings'.
	 * 
	 * @param text
	 *            Text to choose from.
	 * @return One of the phrases from the list of phrases.
	 */
	private String random(String text) {
		text = text.replaceAll("^[\\[\\{]", "");
		text = text.replaceAll("[\\}\\]]$", "");
		StringTokenizer tokens = new StringTokenizer(text, "|");
		int count = tokens.countTokens();
		String token = null;
		int choice = (int) (Math.random() * count);

		for (int i = 0; i <= choice; i++) {
			token = tokens.nextToken();
		}

		return token;
	}

	/**
	 * Does a comparison between two strings, trying a numerical comparison
	 * first followed by a alphabetical comparison.
	 * 
	 * @param v1
	 *            String 1
	 * @param v2
	 *            String 2
	 * @return +ve if 1 > 2, -ve is 2 < 1, 0 if equal.
	 */
	private int compare(String v1, String v2) {
		return 0;
	}

	/**
	 * Parse a line of text which has flow control sections. Currently supported
	 * flow control consists of: [a|b|c] - Choose one of a, b or c and display
	 * that. {a|b|c} - Choose one of a, b or c and look it up as a key phrase,
	 * then parse that before displaying it.
	 * 
	 * @param line
	 *            Line to be parsed.
	 * @return Result instance of the parsed line.
	 */
	private String parse(String line) {
		if (line == null)
			return "";

		StringBuffer buffer = new StringBuffer();

		try {
			// Replace any property variables.
			while (line.indexOf("$") >= 0) {
				String prop = line.substring(line.indexOf("$") + 1);
				String value = "";

				prop = prop.replaceAll("[^A-Za-z0-9].*", "");
				// System.out.println(prop);

				value = getProperty(prop);
				line = line.replaceFirst("\\$" + prop, value);
			}

			// Switch statement.
			// (VARIABLE|VALUE=a|VALUE=b|VALUE=c|DEFAULT)
			// If the VARIABLE is equal to a VALUE, display the option for that
			// value.
			// If VALUE> or VALUE< is used (instead of VALUE=), select the
			// option if the
			// VALUE is greater than or less than the VARIABLE.
			while (line.indexOf("(") >= 0 && line.indexOf(")") >= 0) {
				String options = line.substring(line.indexOf("(") + 1,
						line.indexOf(")") + 1);
				String[] tokens = options.split("\\|");
				String option = "";
				String value = tokens[0];

				for (int i = 1; i < tokens.length; i++) {
					String test = tokens[i].replaceAll("[=<>].*", "");
					option = tokens[i].replaceAll(".*[=<>]", "");

					if (tokens[i].indexOf("=") > -1) {
						if (test.equals(value)) {
							break;
						}
					} else if (tokens[i].indexOf("<") > -1) {
						try {
							Long v = Long.parseLong(value);
							Long t = Long.parseLong(test);
							if (t < v) {
								break;
							}
						} catch (NumberFormatException e) {
							if (value.compareToIgnoreCase(test) < 0)
								break;
						}

					} else if (tokens[i].indexOf(">") > -1) {
						// Is the tested value greater than this case?
						try {
							Long v = Long.parseLong(value);
							Long t = Long.parseLong(test);
							if (t > v) {
								break;
							}
						} catch (NumberFormatException e) {
							if (value.compareToIgnoreCase(test) > 0)
								break;
						}
					} else {
						// Default value.
						break;
					}
				}

				line = line.replaceFirst("\\(.*?\\)", option);
				line = line.replaceAll("\\)", "");
			}

			// Choosen a random option.
			while (line.indexOf("[") >= 0 && line.indexOf("]") >= 0) {
				String options = line.substring(line.indexOf("["),
						line.indexOf("]") + 1);
				String option = random(options);

				line = line.replaceFirst("\\[.*?\\]", option);
			}

			// Replace a random option with the substituted phrase.
			while (line.indexOf("{") >= 0 && line.indexOf("}") >= 0) {
				String options = line.substring(line.indexOf("{"),
						line.indexOf("}") + 1);
				String option = random(options);
				// System.out.println("Replacing ["+option+"] in ["+line+"]...");
				option = parse(getPhrase(option));
				// System.out.println("...with ["+option+"]");

				line = line.replaceFirst("\\{.*?\\}", option);
			}
		} catch (Throwable e) {
			System.out.println("Unable to parse [" + line + "] ("
					+ e.getMessage() + ")");
			e.printStackTrace();
		}
		return line;
	}

	/**
	 * Get the named property from the Planet object. Uses reflection to call
	 * the right getter on the Planet. If no such property is found, then the
	 * empty string is returned. Result is always a string. If the contents
	 * looks like a number, then it will be formatted and truncated to 1dp if
	 * necessary.
	 * 
	 * If the property name is of the form xDy, then a dice roll is actually
	 * done instead, rolling x dice of size y. e.g., $3D6 translates as roll 3
	 * six sided dice, and return the result.
	 * 
	 * @param name
	 *            Name of property to fetch.
	 * @return Value of the property, or empty string.
	 */
	private String getProperty(String name) {
		String value = "";
		if (planet == null)
			return value;

		// Possible to make die rolls.
		if (name.matches("[0-9]D[0-9]")) {
			int number = Integer.parseInt(name.replaceAll("([0-9]+)D[0-9]+",
					"$1"));
			int type = Integer.parseInt(name
					.replaceAll("[0-9]+D([0-9])+", "$1"));

			return "" + Die.die(type, number);
		}

		try {
			Method method = planet.getClass().getMethod("get" + name);
			Object result = method.invoke(planet);

			value = "" + result;

			try {
				double i = Double.parseDouble(value);
				DecimalFormat format = new DecimalFormat();
				format.setMaximumFractionDigits(1);
				value = format.format(i);
			} catch (NumberFormatException e) {
				// Do nothing.
			}
		} catch (Throwable e) {
			System.out.println("getProperty: Cannot find method for [" + name
					+ "]");
			value = "";
		}

		return value;
	}

	private void addText(StringBuffer buffer, String key, int percentChance) {
		if (Die.d100() > percentChance)
			return;

		String text = getPhrase(key);
		if (text == null)
			return;

		if (buffer.length() > 0) {
			buffer.append(" ");
		}
		buffer.append(parse(text));
	}

	private void generate() {
		buffer = new StringBuffer();

		// Get a comment on the world's temperature.
		addText(buffer, "planet." + planet.getType(), 100);
		addText(buffer, "temperature." + planet.getTemperature(), 75);
		addText(buffer, "government." + planet.getGovernment(), 75);
	}

	/**
	 * Get a textual description of this world, fully defined by the resources
	 * file. If there is no base description for the given world type, then null
	 * is returned.
	 */
	public static void setDescription(Planet planet) {
		String rootText = "planet." + planet.getType();

		// PlanetDescription description = new PlanetDescription(planet);
		// planet.setDescription(description.getFullDescription(rootText));
	}

	public String getDescription(String key) {
		buffer = new StringBuffer();
		addText(buffer, key, 100);
		return buffer.toString();
	}

	public String getFullDescription() {
		return getFullDescription("planet." + planet.getType());
	}

	/**
	 * Get the full description for this planet.
	 * 
	 * @param rootKey
	 * @return
	 */
	private String getFullDescription(String rootKey) {
		buffer = new StringBuffer();
		addText(buffer, rootKey, 100);
		addText(buffer, rootKey + ".temperature." + planet.getTemperature(),
				100);
		addText(buffer, rootKey + ".atmosphere." + planet.getAtmosphere(), 100);
		addText(buffer, rootKey + ".atmosphere." + planet.getPressure(), 100);
		addText(buffer, rootKey + ".biosphere." + planet.getLifeType(), 100);

		// Add description for any physical features.
		Set<PlanetFeature> features = planet.getFeatureCodes();
		for (PlanetFeature feature : features.toArray(new PlanetFeature[0])) {
			String key = rootKey + ".feature." + feature;
			if (phrases.getProperty(key) != null) {
				addText(buffer, key, 100);
			} else {
				key = "feature." + feature;
				if (phrases.getProperty(key) != null) {
					addText(buffer, key, 100);
				}
			}
		}

		// Add description for any trade codes.
		/*
		 * for (String code : planet.getTradeCodes()) { String key =
		 * rootKey+".trade."+code; if (phrases.getProperty(key) != null) {
		 * addText(buffer, key, 100); } else { key = "trade."+code; if
		 * (phrases.getProperty(key) != null) { addText(buffer, key, 100); } } }
		 */
		return buffer.toString();
	}

	/**
	 * Get the full description for this planet.
	 */
	public String getText() {
		generate();
		return buffer.toString();
	}

	private static void wrap(String text) {
		int col = 0;
		for (char c : text.toCharArray()) {
			System.out.print(c);
			if (col++ > 70 && c == ' ') {
				System.out.println("");
				col = 0;
			}
		}
	}

	/**
	 * Test things.
	 */
	public static void main(String[] args) throws Exception {
		/*
		PlanetBuilder b = new Hermian();
		b.setPlanet(new Planet());
		PlanetDescription d = new PlanetDescription(b);
		*/
	}
}
