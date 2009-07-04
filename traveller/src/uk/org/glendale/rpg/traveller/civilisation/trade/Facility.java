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
	
	Hashtable<Integer,Integer>	inputMap = null;
	Hashtable<Integer,Integer>	outputMap = null;
		
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
		
	}
	
	private Hashtable<Integer,Integer> getIOMapFromString(String string) {
		Hashtable<Integer,Integer>	map = new Hashtable<Integer,Integer>();
		
		if (string == null || string.length() == 0) {
			return map;
		}
		
		String[]		maps = string.split(";");
		for (int m=0; m < maps.length; m++) {
			String[]	n = maps[m].split(",");
			if (n.length == 2) {
				String		key = n[0];
				String		units = n[1];
				
				if (key.equalsIgnoreCase("X")) key = "0";
				if (units.equalsIgnoreCase("X")) units = "0";
				try {
					map.put(Integer.parseInt(key), Integer.parseInt(units));
				} catch (NumberFormatException e) {
					System.out.println("Unable to recognise ["+maps[m]+"] for facility ["+name+"]");
				}
			}
		}
		return map;
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
