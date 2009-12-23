/**
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
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

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.codes.*;
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
	
	private String hex(int value) {
		String		codes = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		if (value < 0) {
			System.out.println(">>>> Hex ["+value+"]");
			return "0";
		} else if (value > codes.length()) {
			System.out.println(">>>> Hex ["+value+"]");
			return "Z";
		}
		
		return codes.substring(value, value+1);
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
    	
    	if (!uwp.matches("[0-9a-z][0-9A-Z] .*")) {
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
    
    public UWP(ObjectFactory factory, StarSystem system) {
    	try {
    		Sector		sector = new Sector(factory, system.getSectorId());
    		this.sectorName = sector.getName();
    		this.sectorX = sector.getX();
    		this.sectorY = sector.getY();
    	} catch (ObjectNotFoundException e) {
    		// Just skip sector information.
    		this.sectorX = this.sectorY = -1;
    	}
    	
    	this.name = system.getName();
    	this.x = system.getX();
    	this.y = system.getY();
    	if (system.getAllegianceData() != null) {
    		this.allegiance = system.getAllegianceData().getCode();
    	} else {
    		this.allegiance = "Na";
    	}
    	this.base = " ";
    	if (system.hasNavalBase()) this.base = "N";
    	if (system.hasScoutBase()) this.base = "S";
    	switch (system.getZone()) {
    	case Green:
    		this.zone = "G";
    		break;
    	case Amber:
    		this.zone = "A";
    		break;
    	case Red:
    		this.zone = "R";
    		break;
    	}
    	this.starData = "";
    	for (Star star : system.getStars()) {
    		if (starData.length() > 0) starData += " ";
    		starData += star.getSpectralType() + " "+ star.getStarClass();
    		if (starData.length() > 10) break;
    	}
    	
    	Planet		mainWorld = system.getMainWorld();
    	if (mainWorld != null) {
    		this.starport = mainWorld.getStarport().toString();
    		this.diameter = (int)(mainWorld.getRadius() / 1250);
    		this.atmosphere = atmosphereCode(mainWorld.getAtmosphereType(), mainWorld.getAtmospherePressure());
    		this.government = governmentCode(mainWorld.getGovernment());
    		this.lawLevel = lawCode(mainWorld.getLawLevel());
    		this.techLevel = techCode(mainWorld.getTechLevel());
    		this.population = mainWorld.getPopulationLog();
    		this.hydrographic = mainWorld.getHydrographics()/10;
    		this.populationDigit = Integer.parseInt((""+mainWorld.getPopulation()).substring(0, 1));
    		this.belts = this.giants = 0;
    		for (Planet p : system.getPlanets()) {
    			if (p.getType().isTerrestrial()) this.belts ++;
    			if (p.getType().isJovian()) this.giants ++;
    		}
    		if (!mainWorld.isMoon()) this.belts--;
    		this.tradeCodes = "";
    		for (String t : mainWorld.getTradeCodes()) {
    			this.tradeCodes += t+" ";
    		}
    	} else {
    		this.starport = "X";
    		this.diameter = 0;
    		this.atmosphere = 0;
    		this.government = 0;
    		this.lawLevel = 0;
    		this.techLevel = 0;
    		this.population = 0;
    		this.hydrographic = 0;
    		this.populationDigit = 0;
    		this.belts = this.giants = 0;
    		for (Planet p : system.getPlanets()) {
    			if (p.getType().isTerrestrial()) this.belts ++;
    			if (p.getType().isJovian()) this.giants ++;
    		}
    		this.tradeCodes = "";
    	}
    	if (this.population == 0) this.allegiance = "Un";
    	
    	if (diameter > 15) {
    		System.out.println("Strange diameter ["+diameter+"] ["+mainWorld.getName()+"] ["+mainWorld.getRadius()+"km] ["+mainWorld.getType()+"]");
    	}
    	
    	if (belts < 0) belts = 0;
    	if (giants < 0) giants = 0;

    	uwp = String.format("%s%s %-18s %4s %s%s%s%s%s%s%s-%s %1s %-15s %s %d%d%d %s %s", 
    			hex(sectorX+11).toLowerCase(),
    			hex(sectorY+5).toUpperCase(),
    			name,
    			Sector.getCoordinate(x, y),
    			starport,
    			hex(diameter),
    			hex(atmosphere),
    			hex(hydrographic),
    			hex(population),
    			hex(government),
    			hex(lawLevel),
    			hex(techLevel),
    			base,
    			tradeCodes,
    			zone,
    			populationDigit,
    			belts,
    			giants,
    			allegiance,
    			starData);
    }
    
    /**
     * Convert an atmosphere type and pressure into a UWP code.
     */
    private int atmosphereCode(AtmosphereType atmosphere, AtmospherePressure pressure) {
    	int		code = 0;
    	switch (atmosphere) {
    	case Vacuum:
    		break;
    	case Standard: case HighOxygen: case LowOxygen: case HighCarbonDioxide:
    		switch (pressure) {
    		case Trace:
    			code = 1;
    			break;
    		case VeryThin:
    			code = 3;
    			break;
    		case Thin:
    			code = 5;
    			break;
    		case Dense: case SuperDense:
    			code = 8;
    			break;
    		default:
    			code = 6;
    		}
    		break;
    	case Pollutants:
    	case Tainted:
    	case OrganicToxins:
    		switch (pressure) {
    		case Trace:	case VeryThin:
    			code = 2;
    			break;
    		case Thin:
    			code = 4;
    			break;
    		case Dense: case SuperDense:
    			code = 9;
    			break;
    		default:
    			code = 7;
    		}
    		break;
    	case NitrogenCompounds: case CarbonDioxide:
    		code = 10;
    		break;
    	case Chlorine: case Flourine:
    		code = 11;
    		break;
    	case Hydrogen: case SulphurCompounds:
    		code = 12;
    		break;
    	default:
    		code = 13;
    		break;
    	}
    	
    	return code;
    }
    
    private int governmentCode(GovernmentType government) {
    	return government.ordinal();
    }
    
    /**
     * Get the UWP version of the law level. Internally we use
     * a smaller scale, so the values need to be mapped.
     */
    private int lawCode(int lawLevel) {
    	switch (lawLevel) {
    	case 0: return 0;
    	case 1: return 1;
    	case 2: return 3;
    	case 3: return 5;
    	case 4: return 6;
    	case 5: return 8;
    	case 6: return 9;
    	}
    	return 0;
    }
    
    private int techCode(int techLevel) {
    	if (techLevel < 9) return techLevel;
    	switch (techLevel) {
    	case  9: return 10;
    	case 10: return 12;
    	case 11: return 14;
    	case 12: return 15;
    	}
    	return techLevel+3;
    }
    
    
    private void parseUWP() {
        String    string = null;
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
    
    /**
     * Try and work out what the allegiance should be. Much of the data is
     * post-3rd Imperium, so allegiances need to be retrofitted back to a
     * classic Traveller era. Also, I want a simplified allegiance system
     * anyway.
     */
    private String sanitiseAllegiance(String code) {
    	String[] im = { "Li", "Fi", "St" };
    	String[] jp = { "Jo", "Jp", "JP", "Jr", "Ju", "Jv" };
    	
    	return code;
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
