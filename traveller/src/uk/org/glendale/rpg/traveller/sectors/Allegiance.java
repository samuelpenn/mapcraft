/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.7 $
 * $Date: 2006/10/07 10:18:42 $
 */

package uk.org.glendale.rpg.traveller.sectors;

import java.io.*;
import java.sql.*;
import java.util.*;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.PostScript;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.UWP;
import uk.org.glendale.rpg.utils.Die;

/**
 * Defines an allegiance. Used to represent who a star system belongs to. An allegiance
 * has a 2-digit code, as well as a colour and full name. The colour is used to represent
 * systems of this allegiance on the maps.
 *  
 * @author Samuel Penn
 */
public class Allegiance {
	private int			id = 0;
	private String		code = null;
	private String		name = null;
	private String		colour = null;

	public Allegiance(String code, String name, String colour) {
		this.code = code;
		this.name = name;
		this.colour = colour;
		
		persist();
	}

	public Allegiance(String code, String name) {
		this.code = code;
		this.name = name;
		this.colour = "#000000";
		
		persist();
	}
	
	
	/**
	 * Get the allegiance represented by this id.
	 * 
	 * @param id		Id of the sector to be fetched.
	 * @throws ObjectNotFoundException
	 */
	public Allegiance(int id) throws ObjectNotFoundException {
		if (!read("id="+id)) {
			throw new ObjectNotFoundException("Could not find an allegiance with id "+id);
		}
	}
	
	public Allegiance(String nameOrCode) throws ObjectNotFoundException {
		boolean		found = false;
	
		if (nameOrCode == null) {
			throw new ObjectNotFoundException("null Allegiance not found");
		} else if (nameOrCode.length()==2) {
			found = read("code='"+nameOrCode+"'");
		} else {
			found = read("name='"+name+"'");
		}
		
		if (!found) {
			throw new ObjectNotFoundException("Could not find a sector named ["+name+"]");
		}
	}
	
	
	/**
	 * Read sector information from the database. If a matching sector is found,
	 * then returns true and populates this object's fields.
	 * 
	 * @param query		Query to use to find a sector.
	 * 
	 * @return				True if sector exists, false otherwise.
	 */
	private boolean read(String query) {
		ObjectFactory		fac = new ObjectFactory();
		ResultSet			rs = null;
		
		try {
			rs = fac.read("allegiance", query);
			if (rs.next()) {
				id = rs.getInt("id");
				name = rs.getString("name");
				code = rs.getString("code");
				colour = rs.getString("colour");
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) rs.close();
			} catch (SQLException e) {
				// Really don't care.
			}
			fac.close();
		}
		
		return true;
	}
	
	/**
	 * Store the allegiance in the database.
	 */
	public void persist() {
		ObjectFactory				fac = new ObjectFactory();
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		try {
			data.put("id", id);
			data.put("code", code);
			data.put("name", name);
			data.put("colour", colour);
			
			int auto = fac.persist("allegiance", data);
			if (id == 0) id = auto;
		} finally {
			fac.close();
		}
	}
	
	public int getId() {
		return id;
	}
		
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getColour() {
		return colour;
	}
	
	
	
	public static void readAllegiances() {
		File		file = new File("data/allegiances.txt");
		
        FileReader          reader = null;
        LineNumberReader    input = null;
        try {
            String    line = null;
            
            reader = new FileReader(file);
            input = new LineNumberReader(reader);
            
            for (line = input.readLine(); line != null; line = input.readLine()) {
            	System.out.println(input.getLineNumber()+": "+line);
            	
            	String	code = line.substring(0, 2);
            	String  name = line.substring(3);

            	Allegiance 		allegiance = new Allegiance(code, name);
            }
        } catch (IOException e) {
        }		
	}
	
	
	public static void main(String[] args) {
		readAllegiances();
	}
}
