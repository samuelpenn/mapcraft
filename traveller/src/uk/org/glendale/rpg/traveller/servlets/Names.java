/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/01/01 11:04:14 $
 */
package uk.org.glendale.rpg.traveller.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.Name;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.Star;
import uk.org.glendale.rpg.traveller.systems.StarSystem;

/**
 * A RESTful implementation of a web service that generates random names.
 * 
 * A request should always be of the format /name
 * 
 * @author Samuel Penn
 * @created 2007/12/4
 *
 */
public class Names extends HttpServlet {
	/**
	 * Get information on a resource. By default, an XML description of the
	 * planet is returned, however the format can be modified by appending a
	 * filename extension to the id, e.g.:
	 *   /name/<type>/<detail>   An HTML page describing the planet.
	 *   /name/vilani
	 *   /name/vilani/female
	 *   
	 * For specific data, the txt format returns a simple text string.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		uri = request.getPathInfo();
		String		format = "xml";
		
		if (uri == null || uri.length() == 0) {
			response.sendError(400, "Malformed URL");
			return;
		}
		String[]	parts = uri.split("/");
		
		if (parts.length == 1) {
			response.sendError(400, "Malformed URL");
			return;
		}
		String		style = null;
		String		modifier = null;
		int			number = 1;
		if (parts.length > 1) {
			style = parts[1];
		}
		if (parts.length > 2) {
			modifier = parts[2];
		}
		if (request.getParameter("number") != null) {
			try {
				number = Integer.parseInt(request.getParameter("number"));
			} catch (NumberFormatException e) {
				response.sendError(400, "Badly formatted number parameter ["+request.getParameter("number")+"]");
				return;
			}
		}
		response.setContentType("text/plain");
		Name		name = new Name(style);
		for (int i=0; i < number; i++) {
			response.getOutputStream().println(name.getName(modifier));
		}
	}
}
