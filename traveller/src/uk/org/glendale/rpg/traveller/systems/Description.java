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
			// Switch statement.
			// <VARIABLE|VALUE=a|VALUE=b|VALUE=c|DEFAULT>
			while (line.indexOf("(") >= 0 && line.indexOf(")") >= 0) {
				String				options = line.substring(line.indexOf("(")+1, line.indexOf(")")+2);
				String[]			tokens = options.split("\\|");
				String				option = "";
				String				value = getProperty(tokens[0].replaceAll("\\$", ""));
				
				System.out.println(value);
				
				for (int i=1; i < tokens.length; i++) {
					String		test = tokens[i].replaceAll("[=<>].*", "");
					option = tokens[i].replaceAll(".*[=<>]", "");
					System.out.println(tokens[i]);
					if (tokens[i].indexOf("=") > -1) {
						System.out.println("Equal to case");
						if (test.equals(value)) {
							break; 
						}
					} else if (tokens[i].indexOf("<") > -1) {
						System.out.println("Less than case");
					} else if (tokens[i].indexOf(">") > -1) {
						// Is the tested value greater than this case?
						System.out.println("Greater than case");					
					} else {
						// Default value.
						System.out.println("Default");
						break;
					}
				}
				
				line = line.replaceFirst("\\(.*?\\)", option);
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
				option = parse(getPhrase(option));
	
				line = line.replaceFirst("\\{.*?\\}", option);
			}
			
			// Replace any property variables.
			while (line.indexOf("$") >= 0) {
				String		prop = line.substring(line.indexOf("$")+1);
				String		value = "";
				
				prop = prop.replaceAll("[^A-Za-z].*", "");
				System.out.println(prop);
				
				value = getProperty(prop);
				line = line.replaceFirst("\\$"+prop, value);
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
	 * empty string is returned. Result is always a string.
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
		} catch (Throwable e) {
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
		String				text = null;

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
	
	private String getFullDescription(String rootKey) {
		buffer = new StringBuffer();
		addText(buffer, rootKey, 100);
		
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
		int[]			samples = { 68290 };
		
		for (int i=0; i < samples.length; i++) {
			Planet			planet = new Planet(factory, samples[i]);
			Description		description = new Description(planet);

			//System.out.println(description.parse("This planet is $Name with a population of $Population"));
			//System.exit(0);
			System.out.println(planet.getName()+" ("+planet.getType()+"): "+description.getText());
		}
		//System.out.println(description.parse("This is a really [nice|nasty|beautiful] thing."));
		//System.out.println(description.parse("The [chaotic|anarchic|fractured|unstable] [societies|gangs|groups|governments|states] of this [world|planet] [make it|means that it is] a [dangerous|treacherous] place."));
	}
}
