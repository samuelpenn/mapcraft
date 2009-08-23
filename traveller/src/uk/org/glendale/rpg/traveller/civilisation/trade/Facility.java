/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

import java.sql.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Each civilised planet will have one or more facilities. These produce
 * and/or consume resources. Some represent residential areas, others
 * mining, industrial or agricultural production.
 * 
 * @author Samuel Penn
 */
public class Facility {
	private	int					id = 0;
	private String				name = null;
	private FacilityType		type = null;
	private String				image = null;
	private String				codes = null;
	private int					techLevel = 0;
	private int					capacity = 0;
	private int					resourceId = 0;

	private Hashtable<String,Double>	requirementCodes = null;
	private Hashtable<String,Double>	productionCodes = null;
	
	class Mapping {
		private int		sourceId = 0;
		private int		secondaryId = 0;
		
		Mapping(int sourceId) {
			this.sourceId = sourceId;
		}
		Mapping(int sourceId, int secondaryId) {
			this.sourceId = sourceId;
			this.secondaryId = secondaryId;
		}
		
		int getSourceId() {
			return sourceId;
		}
		
		int getSecondaryId() {
			return secondaryId;
		}
	}
	
	Vector<Mapping>	inputMap = null;
	Vector<Mapping>	outputMap = null;
		
	/**
	 * Create a new facility, based on database result set.
	 */
	public Facility(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.name = rs.getString("name");
		this.type = FacilityType.valueOf(rs.getString("type"));
		this.image = rs.getString("image");
		this.techLevel = rs.getInt("techLevel");
		this.capacity = rs.getInt("capacity");
		this.resourceId = rs.getInt("resource_id");
		
		// Inputs and outputs are in a string, of the format:
		// <id>,<units>;<id>,<units>;...
		String inputs = rs.getString("inputs");
		String outputs = rs.getString("outputs");
		
		inputMap = getIOMapFromString(inputs);
		outputMap = getIOMapFromString(outputs);
		
		requirementCodes = getCodeList(inputs);
		productionCodes = getCodeList(outputs);
		
	}
	
	private Hashtable<String,Double> getCodeList(String string) {
		Hashtable<String,Double>  list = new Hashtable<String,Double>();

		if (string == null || string.length() == 0) {
			return list;
		}
		// A code may be of the format <code> or <code>,<percentage>
		for (String code : string.split(";")) {
			if (code.matches("[0-9]+.*")) continue;
			double		modifier = 1.00;
			if (code.indexOf(",") != -1) {
				try {
					int		mod = Integer.parseInt(code.split(",")[1]);
					if (mod > 0 && mod < 1000) {
						modifier = mod/100.0;
					}
				} catch (NumberFormatException e) {
					// Just ignore any invalid data for now.
				}
				code = code.split(",")[0];
			}
			list.put(code, modifier);
		}
		return list;
	}
	
	private Vector<Mapping> getIOMapFromString(String string) {
		Vector<Mapping>	map = new Vector<Mapping>();
		
		if (string == null || string.length() == 0) {
			return map;
		}
		
		String[]		maps = string.split(";");
		for (int m=0; m < maps.length; m++) {
			String[]	n = maps[m].split(",");
			try {
				if (n.length == 1) {
					map.add(new Mapping(Integer.parseInt(n[0])));
				} else if (n.length == 2) {
					String		key = n[0];
					String		units = n[1];
					
					if (key.equalsIgnoreCase("X")) key = "0";
					if (units.equalsIgnoreCase("X")) units = "0";
					map.add(new Mapping(Integer.parseInt(key), Integer.parseInt(units)));
				}
			} catch (NumberFormatException e) {
				// Assume that this is a new format requirement.
				//System.out.println("Unable to recognise ["+maps[m]+"] for facility ["+name+"]");
			}
		}
		return map;
	}
	
	public Vector<Mapping> getInputs() {
		return inputMap;
	}

	public Vector<Mapping> getOutputs() {
		return outputMap;
	}
	
	/**
	 * Given a list of facilities, return one identified by its name.
	 */
	public static Facility getByName(Hashtable<Integer,Facility> list, String name) {
		for (Facility f : list.values()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}		
		return null;
	}
	
	/**
	 * Given a list of facilities, get a sub list of all those of a particular type.
	 */
	public static Hashtable<Integer,Facility> getByType(Hashtable<Integer,Facility> list, FacilityType type) {
		Hashtable<Integer,Facility> ret = new Hashtable<Integer,Facility>();
		
		for (Facility f : list.values()) {
			if (f.getType() == type) {
				ret.put(f.getId(), f);
			}
		}
		
		return ret;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getImage() {
		return image;
	}
		
	public int getCapacity() {
		return capacity;
	}

	/**
	 * The primary resource type that this facility relies on.
	 * Mines and Agriculture turn it into commodities, Residential
	 * consumes it.
	 */
	public int getResourceId() {
		return resourceId;
	}
	
	public int getTechLevel() {
		return techLevel;
	}
	
	/**
	 * The general type of this facility.
	 */
	public FacilityType getType() {
		return type;
	}	
}
