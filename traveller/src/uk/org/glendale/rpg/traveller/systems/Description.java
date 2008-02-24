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
package uk.org.glendale.rpg.traveller.systems;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetType;
import uk.org.glendale.rpg.utils.Die;

/**
 * Create a random description for a planet. Descriptions are made up of phrases
 * which are read from a resource file, allowing easy changing. The phrases support
 * a very simple form of flow control, allowing a phrase to consist of random
 * elements, to reference other elements, and to choose based on planet values. 
 * 
 * [a|b|c]			Select random one of a, b or c
 * {a}				Insert phrase referenced by key a
 * {a|b|c}			Select random one of a, b or c and reference that key.
 * (50?phrase)		50% chance of displaying phrase
 * (25?a|b)			25% chance of a, otherwise b
 * (Prop>20?a:b)	Get property of planet, compare with value, then a otherwise b.
 * $Prop			Value of property for planet.
 *  
 * 
 * @author Samuel Penn
 */
public class Description {
	private StringBuffer		buffer = new StringBuffer();
	private Planet				planet = null;
	
	private static final String	BUNDLE = "uk.org.glendale.rpg.traveller.systems.descriptions";
	private static Properties	phrases = null;
	
	/**
	 * Automatically load the list of random phrases from the resource bundle.
	 */
    static {
        ResourceBundle      bundle = ResourceBundle.getBundle(BUNDLE);
        phrases = new Properties();

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            phrases.setProperty(key, bundle.getString(key));
        }
    }
    
    /**
     * Get the phrase for the given key from the resource bundle. Some keys will
     * have a number of possible options (in the form key, key.1, key.2 etc). If
     * a key has several options, one will be selected randomly.
     * 
     * @param key		Key to use to find a phrase.
     * @return			The selected phrase, or null if none found.
     */
    private static String getPhrase(String key) {
    	String		text = null;
    	
    	text = phrases.getProperty(key);
    	if (text != null) {
    		int		i = 0;
    		while (phrases.getProperty(key+"."+(i+1)) != null) i++;
    		if (i > 0) {
    			int		choice = (int)(Math.random() * (i+1));
    			//System.out.println("Going for choice "+choice+" out of "+i);
    			if (choice != 0) text = phrases.getProperty(key+"."+choice);
    		}
    	}
    	//System.out.println("Got ["+key+"] ["+text+"]");
    	
    	return text;
    }
    
		
	public Description(Planet planet) {
		this.planet = planet;
		
	}
	
	private Description() {
		this.planet = null;
	}
	
	/**
	 * Get a random phrase from the text fragment. The text may be bounded by [ ]
	 * and will be delimited by '|'. For example, [hello|hi|greetings] will return
	 * one of 'hello', 'hi' or 'greetings'. 
	 * 
	 * @param text		Text to choose from.
	 * @return			One of the phrases from the list of phrases.
	 */
	private String random(String text) {
		text = text.replaceAll("^[\\[\\{]", "");
		text = text.replaceAll("[\\}\\]]$", "");
		StringTokenizer		tokens = new StringTokenizer(text, "|");
		int					count = tokens.countTokens();
		String				token = null;
		int					choice = (int)(Math.random() * count);

		for (int i=0; i <= choice; i++) {
			token = tokens.nextToken();
		}
		
		return token;
	}
	
	/**
	 * Does a comparison between two strings, trying a numerical
	 * comparison first followed by a alphabetical comparison.
	 * 
	 * @param v1		String 1
	 * @param v2		String 2
	 * @return			+ve if 1 > 2, -ve is 2 < 1, 0 if equal.
	 */
	private int compare(String v1, String v2) {
		return 0;
	}

	/**
	 * Parse a line of text which has flow control sections. Currently supported
	 * flow control consists of:
	 * [a|b|c]  -  Choose one of a, b or c and display that.
	 * {a|b|c}  -  Choose one of a, b or c and look it up as a key phrase, then
	 *             parse that before displaying it.
	 *             
	 * @param line 	Line to be parsed.
	 * @return			Result instance of the parsed line.
	 */
	private String parse(String line) {
		if (line == null) return "";

		StringBuffer		buffer = new StringBuffer();

		try {
			// Replace any property variables.
			while (line.indexOf("$") >= 0) {
				String		prop = line.substring(line.indexOf("$")+1);
				String		value = "";
				
				prop = prop.replaceAll("[^A-Za-z].*", "");
				//System.out.println(prop);
				
				value = getProperty(prop);
				line = line.replaceFirst("\\$"+prop, value);
			}

			// Switch statement.
			// (VARIABLE|VALUE=a|VALUE=b|VALUE=c|DEFAULT)
			// If the VARIABLE is equal to a VALUE, display the option for that value.
			// If VALUE> or VALUE< is used (instead of VALUE=), select the option if the
			// VALUE is greater than or less than the VARIABLE.
			while (line.indexOf("(") >= 0 && line.indexOf(")") >= 0) {
				String				options = line.substring(line.indexOf("(")+1, line.indexOf(")")+1);
				String[]			tokens = options.split("\\|");
				String				option = "";
				String				value = tokens[0];
				
				for (int i=1; i < tokens.length; i++) {
					String		test = tokens[i].replaceAll("[=<>].*", "");
					option = tokens[i].replaceAll(".*[=<>]", "");
					
					if (tokens[i].indexOf("=") > -1) {
						if (test.equals(value)) {
							break; 
						}
					} else if (tokens[i].indexOf("<") > -1) {
						try {
							Long	v = Long.parseLong(value);
							Long	t = Long.parseLong(test);
							if (t < v) {
								break;
							}
						} catch (NumberFormatException e) {
							if (value.compareToIgnoreCase(test) < 0) break;
						}
						
					} else if (tokens[i].indexOf(">") > -1) {
						// Is the tested value greater than this case?
						try {
							Long	v = Long.parseLong(value);
							Long	t = Long.parseLong(test);
							if (t > v) {
								break;
							}
						} catch (NumberFormatException e) {
							if (value.compareToIgnoreCase(test) > 0) break;
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
				String		options = line.substring(line.indexOf("["), line.indexOf("]")+1);
				String		option = random(options);
	
				line = line.replaceFirst("\\[.*?\\]", option);
			}
			
			// Replace a random option with the substituted phrase.
			while (line.indexOf("{") >= 0 && line.indexOf("}") >= 0) {
				String		options = line.substring(line.indexOf("{"), line.indexOf("}")+1);
				String		option = random(options);
				//System.out.println("Replacing ["+option+"] in ["+line+"]...");
				option = parse(getPhrase(option));
				//System.out.println("...with ["+option+"]");
	
				line = line.replaceFirst("\\{.*?\\}", option);
			}			
		} catch (Throwable e) {
			System.out.println("Unable to parse ["+line+"] ("+e.getMessage()+")");
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
	 * @param name		Name of property to fetch.
	 * @return			Value of the property, or empty string.
	 */
	private String getProperty(String name) {
		String		value = "";
		if (planet == null) return value;
		
		try {
			Method		method = planet.getClass().getMethod("get"+name);
			Object		result = method.invoke(planet);
			
			value = ""+result;
			
			try {
				double i = Double.parseDouble(value);
				DecimalFormat	format = new DecimalFormat();
				format.setMaximumFractionDigits(1);
				value = format.format(i);
			} catch (NumberFormatException e) {
				// Do nothing.
			}
		} catch (Throwable e) {
			e.printStackTrace();
			value = "";
		}
		
		return value;
	}
	
	private void addText(StringBuffer buffer, String key, int percentChance) {
		if (Die.d100() > percentChance) return;
		
		String		text = Description.getPhrase(key);
		if (text == null) return;

		if (buffer.length() > 0) {
			buffer.append(" ");
		}
		buffer.append(parse(text));
	}

	private void generate() {
		buffer = new StringBuffer();

		// Get a comment on the world's temperature.
		addText(buffer, "planet."+planet.getType(), 100);
		addText(buffer, "temperature."+planet.getTemperature(), 75);
		addText(buffer, "government."+planet.getGovernment(), 75);
	}
	
	/**
	 * Get a textual description of this world, fully defined by the
	 * resources file. If there is no base description for the given
	 * world type, then null is returned.
	 */
	public static void setDescription(Planet planet) {
		String		rootText = "planet."+planet.getType();
		
		Description		description = new Description(planet);
		planet.setDescription(description.getFullDescription(rootText));
	}
	
	public String getDescription(String key) {
		buffer = new StringBuffer();
		addText(buffer, key, 100);
		return buffer.toString();
	}
	
	public String getFullDescription() {
		return getFullDescription("planet."+planet.getType());
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
		addText(buffer, rootKey+".temperature."+planet.getTemperature(), 100);
		addText(buffer, rootKey+".atmosphere."+planet.getAtmosphereType(), 100);
		addText(buffer, rootKey+".atmosphere."+planet.getAtmospherePressure(), 100);
		addText(buffer, rootKey+".biosphere."+planet.getLifeLevel(), 100);
		
		// Add description for any physical features.
		Iterator<PlanetFeature>	features = planet.getFeatures();
		while (features.hasNext()) {
			PlanetFeature	feature = features.next();
			String key = rootKey+".feature."+feature;
			if (phrases.getProperty(key) != null) {
				addText(buffer, key, 100);
			} else {
				key = "feature."+feature;
				if (phrases.getProperty(key) != null) {
					addText(buffer, key, 100);					
				}
			}
		}
		
		// Add description for any trade codes.
		/*
		for (String code : planet.getTradeCodes()) {
			String	key = rootKey+".trade."+code;
			if (phrases.getProperty(key) != null) {
				addText(buffer, key, 100);
			} else {
				key = "trade."+code;
				if (phrases.getProperty(key) != null) {
					addText(buffer, key, 100);
				}
			}
		}
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
	
	/**
	 * Test things.
	 */
	public static void main(String[] args) throws Exception {
		ObjectFactory	factory = new ObjectFactory();
		Planet		p = factory.getPlanet(171244);
		Description d = new Description(p);
		System.out.println(d.getDescription("planet.AreanLacustric"));
		factory.close();
		System.exit(0);
		
		
		/*
		Planet p = factory.getPlanet(7631);
		Description d = new Description(p);
		System.out.println(d.parse("{LithicGelidian.extra.1}"));
		System.exit(0);
		*/
		Vector<Planet> planets = factory.getPlanetsBySystem(366);

		for (Planet planet : planets) {
			System.out.println(planet.getName()+" ("+planet.getType()+")");
			Description.setDescription(planet);
			planet.persist();
		}
		//System.out.println(description.parse("This is a really [nice|nasty|beautiful] thing."));
		//System.out.println(description.parse("The [chaotic|anarchic|fractured|unstable] [societies|gangs|groups|governments|states] of this [world|planet] [make it|means that it is] a [dangerous|treacherous] place."));
	}
}
