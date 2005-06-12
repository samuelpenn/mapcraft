/*
 * Copyright (C) 2005 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.map.tilesets.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import java.sql.*;

/**
 * Create a new map in the database.
 * @author Samuel Penn
 */
class MapBuilder {
	static void create(String name, Properties properties, Connection cx) throws SQLException {
		createIndex(name, properties, cx);
		createTerrain(name, properties, cx);
    }
	
	/**
	 * Create an index for this map in the list of maps for this database.
	 * TODO: Currently no checking for duplicates is performed. This is a BUG.
	 * 
	 * @param name
	 * @param properties
	 * @param cx
	 * @throws SQLException
	 */
	private static void createIndex(String name, Properties properties, Connection cx) throws SQLException {
       int      width = 64;
       int      height = 64;
       int      scale = 1;
       String   shape = "hexagonal";
       String   template = "standard";
       String   description = name;
       
       width = Integer.parseInt(properties.getProperty("width", "64"));
       height = Integer.parseInt(properties.getProperty("height", "64"));
       scale = Integer.parseInt(properties.getProperty("scale", "1"));
       
       shape = properties.getProperty("shape", "hexagonal");
       template = properties.getProperty("template", "standard");
       description = properties.getProperty("description", name);
       
       StringBuffer     query = new StringBuffer("insert into mapcraft (");
       
       query.append("name, description, shape, template, ");
       query.append("width, height, scale) values (");
       query.append("'").append(name).append("', ");
       query.append("'").append(description).append("', ");
       query.append("'").append(shape).append("', ");
       query.append("'").append(template).append("', ");
       query.append(width).append(", ");
       query.append(height).append(", ");
       query.append(scale).append(")");
       
       Statement   stmnt = cx.createStatement();
       stmnt.executeUpdate(query.toString());
	
	}
	
	/**
	 * Create a default set of terrain entries for this map.
	 * 
	 * @param name
	 * @param properties
	 * @param cx
	 * @throws SQLException
	 */
	private static void createTerrain(String name, Properties properties, Connection cx) throws SQLException {
		// TODO: Create terrain entries for a new map.
	}

}
