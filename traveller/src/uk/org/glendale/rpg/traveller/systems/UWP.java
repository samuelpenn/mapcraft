/*
 * Copyright (C) 2004 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.5 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems;

import java.util.StringTokenizer;
import java.util.TreeSet;

import uk.org.glendale.rpg.utils.Die;

/**
 * Generates and holds data on a Universal World Profile.
 * 
 * @author Samuel Penn
 */
public class UWP {
	private String	   uwp;
    private String     name;
    private int        sectorX;
    private int        sectorY;
    private int        x;
    private int        y;
    
    private String     starport;
    private int        diameter;
    private int        atmosphere;
    private int        hydrographic;
    private int        population;
    private int		   populationDigit;
    private int        government;
    private int        lawLevel;
    private int        techLevel;
    private int        giants;
    private int        belts;
    private TreeSet    tradeClasses;
    private String     zone;
    private String     base;
    private String	   tradeCodes;
    private String     allegiance;
    private String[]   stars = null;
    private String	   starData;
    private String	   sectorName = null;
    
    private boolean   error = false;
    
    
	/**
	 * Get the real value of the given UWP code digit. A UWP code ranges from
	 * 0 to 9, and A to Z, missing out I and O. A is 10, B 11 etc.
	 * 
	 * @param code		One character code to be checked.
	 * 
	 * @return			Numeric value of code, or -1 if invalid.
	 */
	private int getDigit(String line, int index) {
		String		codes = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
		String		code = line.substring(index, index+1);
		
		return codes.indexOf(code);
	}
	
	private String getAlpha(String line, int index) {
		return line.substring(index, index+1);		
	}
    
    /**
     * Create an empty profile. Used only for testing.
     * 
     */
    UWP() {
    }
     
    /**
     * Create a new profile from a one line extended UWP.
     * An extended profile look something like this:
     * eC Raweh              0139 B130300-B N Lo Ni Po De     A 920 Im G3 V M1 D
     * 0....^....1....^....2....^....3....^....4....^....5....^....6....^....7..
     */
    public UWP(String uwp) {
    	
    	if (!uwp.matches("[a-z][A-Z] .*")) {
    		uwp = "aA "+uwp;
    	}
    	
    	this.uwp = uwp;
        // Which sector is the world in?
        sectorName = uwp.substring(0, 2);
        
        try {
        	sectorY = Integer.parseInt(getAlpha(uwp, 0).toUpperCase(), 26);
        	sectorX = Integer.parseInt(getAlpha(uwp, 1).toUpperCase(), 26);
        } catch (NumberFormatException e) {
        	sectorX = sectorY = -1;
        }

        parseUWP();
    }
    /**
     * Create a new profile from a one line extended UWP.
     * An extended profile look something like this:
     * eC Raweh              0139 B130300-B N Lo Ni Po De     A 920 Im G3 V M1 D
     * 0....^....1....^....2....^....3....^....4....^....5....^....6....^....7..
     */
    public
    UWP(String uwp, String sectorName, int sectorX, int sectorY) {
    	this.uwp = uwp;
    	this.sectorName = sectorName;
    	this.sectorX = sectorX;
    	this.sectorY = sectorY;
    	
    	parseUWP();
    	
    	// What if we don't have a name? Make one up.
    	if (name == null || name.length() == 0) {
    		name = sectorName+" "+((getX()<10)?"0":"")+getX()+((getY()<10)?"0":"")+getY();
    	}
    	
    }
    private void parseUWP() {
        String    string = null;
        char      c = '\0';
        
        // World name.
        name = uwp.substring(3, 22).trim();
        
        // Location within the sector.
        try {
            string = uwp.substring(22, 24);
            x = Integer.parseInt(string);
            string = uwp.substring(24, 26);
            y = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            // What can we do?
            error = true;
        }
        
        // Read values for the standard part of the UWP.
        string = uwp.substring(27, 36);
        
        starport = getAlpha(string, 0);
        diameter = getDigit(string, 1);
        atmosphere = getDigit(string, 2);
        hydrographic = getDigit(string, 3);
        population = getDigit(string, 4);
        government = getDigit(string, 5);
        lawLevel = getDigit(string, 6);
        techLevel = getDigit(string, 8);
        
        String pbg = uwp.substring(57, 60);
        populationDigit = getDigit(pbg, 0);
        belts = getDigit(pbg, 1);
        if (belts == -1) {
        	// Unknown data, so randomise.
        	belts = Die.d4()-1;
        }
        giants = getDigit(pbg, 2);
        
        // Set the type of Imperial base.
        base = uwp.substring(37, 38).trim();
        zone = uwp.substring(55, 56).trim();
        allegiance = uwp.substring(61, 63);

        if (uwp.length() > 64) {
        	starData = uwp.substring(64);
        } else {
        	starData = "";
        }
        
        allegiance = uwp.substring(61, 63);
        
        tradeCodes = uwp.substring(39, 55);
    }
    
    public String getZone() {
    	return zone;
    }
    
    public String getBase() {
    	return base;
    }
    
    public String getTradeCodes() {
    	return tradeCodes;
    }
    
    public String getAllegiance() {
    	return allegiance;
    }

    /**
     * The name of this world or star system.
     */
    public String
    getName() {
        return name;
    }
    
    public String getSectorName() {
    	return sectorName;
    }
    
    /**
     * X coordinate of the sector to which the world
     * belongs. Zero is most distant explored spinward.
     */
    public int
    getSectorX() {
        return sectorX;
    }
    
    /**
     * Y coordinate of the sector to which the world
     * belongs. Zero is most distantly explored coreward.
     */
    public int
    getSectorY() {
        return sectorY;
    }
    
    /**
     * X coordinate of world within its sector. Ranges
     * from 1 to 32.
     */
    public int
    getX() {
        return x;
    }
    
    /**
     * Y coordinate of world within its sector. Ranges
     * from 1 to 40.
     */
    public int
    getY() {
        return y;
    }
    
    /**
     * Starport type. Ranges from 0 to 5, with 0 being an empty
     * field, to 5 being a full fledged starport of the most
     * advanced type.
     */
    public String
    getStarPort() {
        return starport;
    }
    
    /**
     * Diameter of world in 10^3 miles. Zero represents
     * an asteroid belt.
     */
    public int
    getDiameter() {
        return diameter;
    }
    
    /**
     * Type of atmosphere on this world. Very basically,
     * represents the following:
     * 0 None, 1 trace, 2 very thin tainted, 3 very thin,
     * 4 thin tainted, 5 thin, 6 standard, 7 standard tainted,
     * 8 dense, 9 dense tainted, 10 exotic, 11 corrosive, 12 insidious.
     */
    public int
    getAtmosphere() {
        return atmosphere;
    }
    
    /**
     * Hydographic percentage. Percentage of the world's surface
     * which is covered in water.
     */
    public int
    getHydrographic() {
        return hydrographic;
    }
    
    public int getPopulation() {
        return population;
    }
    
    public int getPopulationDigit() {
    	return populationDigit;
    }
    
    public int
    getGovernment() {
        return government;
    }
    
    public int
    getLawLevel() {
        return lawLevel;
    }
    
    public int
    getTechLevel() {
        return techLevel;
    }
    
    public String[]
    getStars() {
        return stars;
    }
    
    /**
     * Get the raw star data, before it has been processed.
     */
    public String getStarData() {
    	return starData;
    }
    
    /**
     * Get a count of the number of gas giants in the system.
     */
    public int
    getGasGiants() {
        return giants;
    }
    
    public int
    getPlanetoidBelts() {
        return belts;
    }
    
    /**
     * Get the spectral class of the specified star.
     * 
     * @param   i   Index to star.
     * @return      Spectral type of the form "G5".
     */
    public String
    getSpectrum(int i) {
        return stars[i].substring(0, 2).trim();
    }
    
    public String
    getLuminosity(int i) {
        return stars[i].substring(3).trim();
    }
    
    /**
     * Set the population of the world to be zero, and remove all signs of
     * civilisation.
     */
    public void depopulate(String name) {
    	if (name != null) {
    		this.name = name;
    	}
    	population = 0;
    	populationDigit = 0;
    	
    	starport = "X";
    	techLevel = 0;
    	lawLevel = 0;
    	government = 0;
    }
    
    private void debug(String name, String value) {
    	System.out.println(name+": ["+value+"]");
    }
    
    public String toString() {
    	return uwp;
    }
    
    public void dump() {
    	System.out.println(uwp);
    	debug("Sector", ""+sectorX+","+sectorY);
    	debug("Name", getName());
    	debug("X", ""+getX());
    	debug("Y", ""+getY());
    	debug("StarPort", getStarPort());
    	debug("Diameter", ""+getDiameter());
    	debug("Atmosphere", ""+getAtmosphere());
    	debug("Hydro", ""+getHydrographic());
    	debug("Population", ""+getPopulationDigit()+"E"+getPopulation());
    	debug("Government", ""+getGovernment());
    	debug("Law", ""+getLawLevel());
    	debug("Tech", ""+getTechLevel());
    }

}
