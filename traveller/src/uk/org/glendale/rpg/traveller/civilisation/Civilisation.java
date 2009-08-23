/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.4 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.civilisation;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import uk.org.glendale.rpg.traveller.civilisation.trade.Facility;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Select a type of civilisation.
 * 
 * @author Samuel Penn
 */
public class Civilisation {
	private StringBuffer		buffer = new StringBuffer();
	
	private static final String	BUNDLE_BASE = "uk.org.glendale.rpg.traveller.civilisation.";
	private static Hashtable<String,Properties>		configuration = new Hashtable<String,Properties>();
	    
    private static void getConfiguration(String name) {
        ResourceBundle      bundle = ResourceBundle.getBundle(BUNDLE_BASE+name);
        Properties			c = new Properties();

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            c.setProperty(key, bundle.getString(key));
        }
        configuration.put(name, c);
    }
    
    
    
    /**
     * Get the phrase for the given key from the resource bundle. Some keys will
     * have a number of possible options (in the form key, key.1, key.2 etc). If
     * a key has several options, one will be selected randomly.
     * 
     * @param key		Key to use to find a phrase.
     * @return			The selected phrase, or null if none found.
     */
    private String getPhrase(String key) {
    	String		text = null;
    	
    	text = config.getProperty(key);
    	if (text != null) {
    		int		i = 0;
    		while (config.getProperty(key+"."+(i+1)) != null) i++;
    		if (i > 0) {
    			int		choice = (int)(Math.random() * (i+1));
    			//System.out.println("Going for choice "+choice+" out of "+i);
    			if (choice != 0) text = config.getProperty(key+"."+choice);
    		}
    	}
    	//System.out.println("Got ["+key+"] ["+text+"]");
    	
    	return text;
    }
    
    private Properties		config = null;
    private Planet			planet = null;
    private ObjectFactory	factory = null;
		
	public Civilisation(ObjectFactory factory, Planet planet, String name) {
		this.factory = factory;
		if (name == null) name = "civilisation";
		if (configuration.get(name) == null) {
			getConfiguration(name);
		}
		this.planet = planet;
		this.config = configuration.get(name);
	}
	
	private static final long	THOUSAND = 1000;
	private static final long	MILLION =  1000000;
	private static final long	BILLION =  1000000000;
	
	public void generate(long population, int techLevel) {
		String		phrase = null;

		switch (planet.getHabitability()) {
		case Garden:
			phrase = "ideal";
			break;
		case Habitable:
			phrase = "habitable";
			break;
		case Unpleasant:
			phrase = "habitable";
			break;
		case Difficult:
			phrase = "difficult";
			break;
		case Inhospitable:
			phrase = "inhospitable";
		case Hostile:
			phrase = "hostile";
			break;
		case VeryHostile:
			phrase = "hostile";
			break;
		}
		// Don't bother populating if the population will be too small.
		if (population < 100) {
			return;
		} else if (population < 10 * THOUSAND) {
			phrase += ".tiny";
		} else if (population < 1 * MILLION) {
			phrase += ".small";
		} else if (population < 100 * MILLION) {
			phrase += ".medium";
		} else if (population < 1 * BILLION) {
			phrase += ".large";
		} else {
			phrase += ".huge";
		}
		
		switch (techLevel) {
		case 0: case 1:
			phrase += ".primitive";
			break;
		case 2: case 3: case 4:
			phrase += ".medieval";
			break;
		case 5: case 6:
			phrase += ".industrial";
			break;
		case 7: case 8:
			phrase += ".technological";
			break;
		case 9: case 10:
			phrase += ".advanced";
			break;
		default:
			phrase += ".ultratech";
			break;
		}
		
		String	culture = getWord(getPhrase(phrase));
		long	cultureSize = 100;
		if (culture.indexOf(",") != -1) {
			cultureSize = Integer.parseInt(culture.split(",")[1]);
			culture = culture.split(",")[0];
		}
		String		facilityName = getPhrase(culture+".name");
		System.out.println(phrase+": "+facilityName+" ("+cultureSize+"%)");
		
		String		government = getWord(getPhrase(culture+".government"));
		String		tradeCodes = getPhrase(culture+".codes");
		String		facilityList = getPhrase(culture+".facilities");
		System.out.println("Government:  "+government);
		System.out.println("Trade Codes: "+tradeCodes);

		Hashtable<Integer,Long>		facilities = new Hashtable<Integer,Long>();
		Facility					facility = null;
		
		try {
			facility = Constants.getFacility(facilityName);
			facilities.put(facility.getId(), cultureSize);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		for (String f : facilityList.split(" ")) {
			String		type = f.split(",")[0];
			long		size = Integer.parseInt(f.split(",")[1]);
			try {
				facility = Constants.getFacility(getPhrase(type+".name"));
				System.out.println(facility.getName()+" ["+size+"]");
				facilities.put(facility.getId(), size);
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		planet.setPopulation(population);
		planet.setGovernment(GovernmentType.valueOf(government));
		planet.setLawLevel(1+Die.d4() + planet.getGovernment().getLawModifier());
		planet.setTechLevel(techLevel + planet.getGovernment().getTechModifier());
		planet.persist();
		factory.setFacilitiesForPlanet(planet.getId(),facilities);
	}
	
	private String getWord(String text) {
		String[]	words = text.split(" ");
		return words[Die.rollZero(words.length)];
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
		
		String		text = getPhrase(key);
		if (text == null) return;

		if (buffer.length() > 0) {
			buffer.append(" ");
		}
		buffer.append(parse(text));
	}
}
