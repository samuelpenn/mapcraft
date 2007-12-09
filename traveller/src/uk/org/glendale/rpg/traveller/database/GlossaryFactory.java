/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/13 16:36:26 $
 */

package uk.org.glendale.rpg.traveller.database;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.Log;

import java.sql.*;
import java.util.*;

import uk.org.glendale.database.Database;
import uk.org.glendale.rpg.traveller.glossary.GlossaryEntry;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Factory class which generates Traveller objects - stars, systems and planets.
 * These should only be one instance of this in an application.
 * 
 * @author Samuel Penn
 *
 */
public class GlossaryFactory {
	private Database				db = null;
	private static GlossaryFactory	instance = null;
	
	public GlossaryFactory() {
		System.out.println("GlossaryFactory: Creating new database connection");
		db = Database.connect(Config.getDatabaseHost(), Config.getDatabaseName(),
							  Config.getDatabaseUser(), Config.getDatabasePassword());
	}
	
	private static GlossaryFactory getInstance() {
		instance = new GlossaryFactory();
		return instance;
	}
	
	public void close() {
		db.disconnect();
	}
		
	public ResultSet read(String table, String where) throws SQLException {
		String		sql = "select * from "+table;
		
		if (where != null && where.length() > 0) {
			sql += " where "+where;
		}
		ResultSet	rs = null;
		
		rs = db.query(sql);
		
		return rs;
	}
	
	private String makeSafe(String value) {
		if (value == null) {
			return "";
		}
		value.replaceAll("'", "''");
		return value;
	}
	
	public GlossaryEntry getEntry(String uriName) throws GlossaryException {
		GlossaryEntry		entry = null;
		ResultSet			rs = null;
		
		try {
			rs = read("glossary", "uri='"+makeSafe(uriName.toLowerCase())+"'");
			if (rs.next()) {
				int		id = rs.getInt("id");
				String	uri = rs.getString("uri");
				String	title = rs.getString("title");
				String	text = rs.getString("message");
				
				entry = new GlossaryEntry(id, uri, title, text);
			} else {
				throw new GlossaryException("Unable to find glossary item ["+uriName+"]");
			}
		} catch (SQLException e) {
			Log.error(e);
			throw new GlossaryException("Database error whilst searching glossary ("+e.getMessage()+")");
		} finally {
			db.tidy(rs);
		}
		
		return entry;
	}
	
	/**
	 * Get an iterator over all the glossary entries in alphabetical order, or
	 * over a subset if a pattern is specified. The pattern should be an SQL
	 * LIKE pattern, such as 'a%' to return all entries beginning with a. A
	 * lower() function is applied to the title if matching against the pattern.
	 * 
	 * @param	pattern		Pattern to match title against, or null for all entries.
	 * @return				Iterator over all entries returned.
	 */
	public Iterator<GlossaryEntry>	iterator(String pattern) throws GlossaryException {
		ResultSet		rs = null;
		ArrayList<GlossaryEntry>	list = new ArrayList<GlossaryEntry>();

		try {
			if (pattern == null || pattern.length() == 0) {
				rs = read("glossary", "order by title asc");
			} else {
				rs = read("glossary", "lower(title) like '"+pattern+"' order by title asc");
			}
			while (rs.next()) {
				int		id = rs.getInt("id");
				String	uri = rs.getString("uri");
				String	title = rs.getString("title");
				String	text = rs.getString("message");
				
				list.add(new GlossaryEntry(id, uri, title, text));
			}
		} catch (SQLException e) {
			Log.error(e);
			throw new GlossaryException("Database error whilst building index ("+e.getMessage()+")");
		} finally {
			db.tidy(rs);
		}
		
		return list.iterator();
	}
	
	public void setEntry(GlossaryEntry entry) throws GlossaryException {
		Hashtable<String,Object>	columns = new Hashtable<String,Object>();
		
		try {
			columns.put("uri", entry.getUri());
			columns.put("title", entry.getTitle());
			columns.put("message", entry.getText());
			
			db.replace("glossary", columns, "uri='"+entry.getUri()+"'");
		} catch (SQLException e) {
			throw new GlossaryException("Unable to set glossary item ["+entry.getUri()+"]"); 
		}
	}
}
