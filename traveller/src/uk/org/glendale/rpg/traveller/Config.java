/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2006/07/09 16:28:30 $
 */

package uk.org.glendale.rpg.traveller;

import java.util.*;

/**
 * Gives access to the configuration options. Configuration is stored in the config.properties
 * found in the uk.org.glendale.cities package.
 *
 *  * @author Samuel Penn
 */
public class Config {
    private static final String     BUNDLE = "uk.org.glendale.rpg.traveller.config";
    private static Properties       props = new Properties();
    private static String			universe = null;

    static {
        ResourceBundle      bundle = null;

        bundle = ResourceBundle.getBundle(BUNDLE);
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            props.setProperty(key, bundle.getString(key));
        }
        universe = props.getProperty("universe");
    }
    
    private static long getLong(String name, long base) {
    	String      value = props.getProperty(universe+"."+name);
        
        if (value == null) {
            return base;
        }
        
        try {
            base = Long.parseLong(value);
        } catch (NumberFormatException e) {
            // Just return the base supplied number.
        }
        
        return base;
    }

    private static int getInt(String name, int base) {
        String      value = props.getProperty(universe+"."+name);
        
        if (value == null) {
            return base;
        }
        
        try {
            base = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Just return the base supplied number.
        }
        
        return base;
    }

    public static Properties getProperties(){
        return props;
    }

    /**
     * The root URL for the server the application is running on.
     * 
     * @return	Root URL of application, ending in a slash. 
     */
    public static String getBaseUrl() {
   	    String path = props.getProperty(universe+".server.url", "http://dev.glendale.org.uk/traveller/");
   	    if (!path.endsWith("/")) {
   	    	path += "/";
   	    }
   	    return path;
    }
    
    public static String getDatabaseHost() {
   	    return props.getProperty(universe+".database.hostname");
    }
    
    public static String getDatabaseName() {
   	    return props.getProperty(universe+".database.name");    
    }
    
    public static String getDatabaseUser() {
   	    return props.getProperty(universe+".database.user");
    }
    
    public static String getDatabasePassword() {
   	    return props.getProperty(universe+".database.password");
    }
    
    /**
     * Path the WAR should be deployed to on the application server.
     */
    public static String getDeploymentPath() {
    	return props.getProperty(universe+".server.deploy");
    }
    
    public static String getTitle() {
    	return props.getProperty(universe+".title");
    }

    public static String getName() {
    	return props.getProperty(universe+".server.name");
    }
}
