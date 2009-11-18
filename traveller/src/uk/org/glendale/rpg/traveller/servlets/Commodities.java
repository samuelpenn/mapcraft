/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.NumberFormatter;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.civilisation.trade.*;
import uk.org.glendale.rpg.traveller.database.*;

/**
 * A RESTful implementation of a web service that enables access to
 * information about commodities, trade goods and resources.
 * 
 * Request formats:
 * 
 * /commodity/<id>.<format>
 * /commodity/<id>.<format>?children
 * 
 * Note that this servlet needs to support PUT and POST requests
 * since it will be used to update commodity information. It needs
 * to be behind some form of security.
 * 
 * @author Samuel Penn
 */
public class Commodities extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get information on a resource. By default, an XML description of the
	 * planet is returned, however the format can be modified by appending a
	 * filename extension to the id, e.g.:
	 *   /market/6801.html   An HTML page describing the planet.
	 *   /market/6801.txt    Return XML data as content type text.
	 *   
	 * For specific data, the txt format returns a simple text string.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		uri = request.getPathInfo();
		String		format = "xml";
		String		action = "info";
		int			id = 0;
		
		System.out.println("Called Commodities.doGet()");
		
		try {
			id = Integer.parseInt(uri.replaceAll("/([0-9]+)([/.].*)?", "$1"));
		} catch (NumberFormatException e) {
			// Should be impossible, since the regexp will have failed by this point.
			response.sendError(400, "Incorrect request format ["+uri+"]");
			return;
		}
		
		if (request.getParameter("children") != null) {
			action = "children";
		}

		if (uri.matches(".*\\.[a-z]+")) {
			format = uri.replaceAll(".*\\.([a-z]+)", "$1");
			if (format.equals("txt") || format.equals("xml") || format.equals("json")) {
				// Okay.
			} else {
				response.sendError(415, "Unrecognised format type ["+format+"]");
				return;
			}
		}

		try {
			if (format.equals("json")) {
				response.setContentType("application/json");
			} else {
				response.setContentType("text/xml");
			}
			if (action.equals("children")) {
				response.getOutputStream().print(listChildren(id, format));
			} else if (action.equals("info")) {
				response.getOutputStream().print(getCommodity(id, format));				
			}
		} catch (Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Exception ("+t.getMessage()+")");
			return;
		}
	}
	
	private String listChildren(int parentId, String format) {
		Hashtable<Integer,Commodity>	commodities = Constants.getCommodities();

		StringBuffer		buffer = new StringBuffer();
		
		if (format.equals("xml") || format.equals("txt")) {
			buffer.append("<?xml version='1.0'?>\n");
			buffer.append("<commodities parent='"+parentId+"'>\n");
			for (Commodity c : commodities.values()) {
				if (c.getParentId() == parentId) {
					buffer.append(c.toXML()+"\n");
				}
			}
			buffer.append("</commodities>\n");
		} else if (format.equals("json")) {
			buffer.append("{ 'list': [ ");
			boolean		first = true;
			for (Commodity c : commodities.values()) {
				if (c.getParentId() == parentId) {
					if (first) first = false; else buffer.append(", ");
					buffer.append(c.toJSON());
				}
			}
			buffer.append("] }");
		}
		
		return buffer.toString();
	}
	
	private String getCommodity(int id, String format) {
		Commodity	commodity = Constants.getCommodity(id);
		
		if (commodity == null) return null;
		
		return "<xml version='1.0'?>\n"+commodity.toXML();
	}
}
