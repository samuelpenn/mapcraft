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
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.Star;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.StarSystem.Zone;

/**
 * A RESTful implementation of a web service that enables access to system
 * resources.
 * 
 * A request should always be of the format /system/<systemId>. Individual
 * methods will define further requirements.
 * 
 * @author Samuel Penn
 * @created 2007/12/4
 *
 */
public class Systems extends HttpServlet {
	/**
	 * Delete a resource from the universe. Currently this is always forbiddon.
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(403, "Forbiddon");
	}
	
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doHead(request, response);
	}
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doOptions(request, response);
	}
	
	/**
	 * Get information on a resource. By default, an XML description of the
	 * planet is returned, however the format can be modified by appending a
	 * filename extension to the id, e.g.:
	 *   /planet/6801.html   An HTML page describing the planet.
	 *   /planet/6801.txt    Return XML data as content type text.
	 *   /planet/6801.jpg	 Return a map of the planet (if available).
	 *   
	 * To get specific information on a planet, use:
	 *   /planet/6801/description.html
	 *   /planet/6801/name.txt
	 *   
	 * For specific data, the txt format returns a simple text string.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		uri = request.getPathInfo();
		String		format = "xml";
		String		property = null;
		String		name = null;
		int			id = 0;
		
		System.out.println(uri);
		
		// Very quick test to ensure that the URL format is correct.
		/*
		if (!uri.matches("/[0-9]+([/.].*)?")) {
			response.sendError(400, "Incorrect request format ["+uri+"]");
			return;
		}
		*/
		
		try {
			name = uri.replaceAll("/([^/]+)([/.].*)?", "$1");
			if (name.endsWith(".html") || name.endsWith(".xml") || name.endsWith(".txt") || name.endsWith(".rss")) {
				name = name.substring(0, name.lastIndexOf("."));
			}
			System.out.println(name);
			id = Integer.parseInt(name);
		} catch (NumberFormatException e) {
			// Should be impossible, since the regexp will have failed by this point.
			//response.sendError(400, "Badly formatted system id");
			//return;
			id = -1;
		}

		format = uri.replaceAll("/([^/]+)([/.].*)?", "$1");
		if (format.endsWith(".html") || format.endsWith(".xml") || format.endsWith(".txt") || format.endsWith(".rss")) {
			format = format.replaceAll(".*\\.", "");
			if (format.equals("txt") || format.equals("xml") || format.equals("html") || format.equals("jpg") || format.equals("rss")) {
				// Okay.
			} else {
				response.sendError(415, "Unrecognised format type ["+format+"]");
				return;
			}
		}
		
		if (uri.matches("/[0-9]+/.*")) {
			property = uri.replaceAll("/[0-9]+/([^.]+)(\\.[a-z]+)?", "$1");
		}
		
		ObjectFactory	factory = null;
		try {
			factory = new ObjectFactory();
			StarSystem			system = null;
			Vector<StarSystem>	list = null;
			if (id > 0) {
				system = new StarSystem(factory, id);
			} else if (name.equals("search") && format.equals("rss") && request.getParameter("n")!=null) {
				list = factory.findStarSystems(request.getParameter("n")); 
			} else {
				system = new StarSystem(factory, name);
			}

			if (list != null) {
				getListPage(factory, list, format, request, response);
			} else if (property == null) {
				getFullPage(factory, system, format, request, response);
			} else {
				//getProperty(factory, system, format, property, request, response);
			}
		} catch (ObjectNotFoundException e) {
			response.sendError(404, "Cannot find planet with id ["+id+"] ["+name+"] ["+format+"] ["+request.getParameter("n")+"]");
		} catch (Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Exception ("+t.getMessage()+")");
		} finally {
			factory.close();
		}
	}
	
	/**
	 * Output a full page of data to the client. The format of tha data will
	 * depend on the format requested.
	 * 
	 * @param factory		ObjectFactory giving access to the universe.
	 * @param planet		Planet to get information on.
	 * @param format		Format of the response.
	 * @param request		Request object.
	 * @param response		Response object.
	 * @throws IOException
	 */
	private void getFullPage(ObjectFactory factory, StarSystem system, String format, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (format.equals("html")) {
			// Output information as HTML.
			response.setContentType("text/html");
			response.getOutputStream().print(system.toHTML());
		} else if (format.equals("txt")) {
			// Output information as XML (with content type of plain text).
			response.setContentType("text/plain");
			response.getOutputStream().print(outputXML(system));
		} else {
			// Output information as XML.
			response.setContentType("text/xml");
			response.getOutputStream().print(outputXML(system));
		}
	}
	
	private void getListPage(ObjectFactory factory, Vector<StarSystem> list, String format, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/rss+xml");
		response.getOutputStream().print(outputRSS(factory, list));
	}

	/**
	 * Output a single property back to the client. Only text or XML are supported.
	 * @param factory
	 * @param planet
	 * @param format
	 * @param property
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void getProperty(ObjectFactory factory, Planet planet, String format, String property, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (format.equals("txt")) {
			response.setContentType("text/plain");
			response.getOutputStream().print(getProperty(planet, property));
		} else if (format.equals("xml")) {
			response.setContentType("text/xml");
			response.getOutputStream().print("<?xml version=\"1.0\"?>\n");
			response.getOutputStream().print("<planet id=\""+planet.getId()+"\">\n<"+property+">"+getProperty(planet, property)+"</"+property+">\n</planet>\n");			
		} else {
			response.sendError(415, "Requests for a single property can only return txt or xml");
		}
	}
	
	private String getProperty(Planet planet, String property) {
		String		value = null;
		
		if (property.equals("name")) {
			value = planet.getName();
		} else if (property.equals("description")) {
			value = planet.getDescription();
		} else if (property.equals("population")) {
			value = "" + planet.getPopulation();
		}
		
		if (value == null) value = "";
		
		return value;
	}

	/**
	 * Output information about the system as HTML.
	 * 
	 * @param system	System to output data on.
	 * @return			String containing the HTML page.
	 */
	private String outputHTML(StarSystem system, ObjectFactory factory) throws ObjectNotFoundException {
		return system.toHTML();
	}

	private String outputXML(StarSystem system) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = Config.getBaseUrl()+"xslt/system.xsl";

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		if (stylesheet != null) {
			buffer.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>\n");
		}
		buffer.append(system.toXML());
		
		return buffer.toString();
	}
	
	private String outputRSS(ObjectFactory factory, Vector<StarSystem> list) {
		StringBuffer		buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<rss version='2.0'>\n");
		buffer.append("<channel><title>Star Systems</title>\n");
		buffer.append("<link>http://dev.glendale.org.uk/traveller/</link>");
		buffer.append("<description>List of star systems</description>");
		
		for (StarSystem s : list) {
			String	sectorName = "";
			try {
				Sector		sector = new Sector(factory, s.getSectorId());
				sectorName = " / "+sector.getName();
			} catch (ObjectNotFoundException e) {
			}
			buffer.append("<item><title>"+s.getName()+sectorName+" ("+s.getXAsString()+s.getYAsString()+")</title>");
			buffer.append("<link>http://dev.glendale.org.uk/traveller/system/"+s.getId()+".html</link>");
			buffer.append("<description>");
			Planet		mainWorld = s.getMainWorld();
			if (s != null) {
				buffer.append(mainWorld.getName()+" ("+mainWorld.getType()+") "+mainWorld.getStarport()+"/"+mainWorld.getTechLevel());
				buffer.append("; ");
				buffer.append(mainWorld.getGovernment()+"; Population "+mainWorld.getShortPopulation());
			}
			buffer.append("</description>");
			buffer.append("</item>");
		}
		buffer.append("</channel></rss>\n");
		return buffer.toString();
	}
	
	/**
	 * Add a note to a planet. A planet can have any number of notes attached to it.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		uri = request.getPathInfo();
		int			id = 0;
		
		// Very quick test to ensure that the URL format is correct.
		if (!uri.matches("/[0-9]+([/.].*)?")) {
			response.sendError(400, "Incorrect request format ["+uri+"]");
			return;
		}
		
		try {
			id = Integer.parseInt(uri.replaceAll("/([0-9]+)([/.].*)?", "$1"));
		} catch (NumberFormatException e) {
			// Should be impossible, since the regexp will have failed by this point.
			response.sendError(400, "Badly formatted planet id");
			return;
		}
		
		InputStreamReader		reader = new InputStreamReader(request.getInputStream());
		LineNumberReader		lines = new LineNumberReader(reader);
		StringBuffer			buffer = new StringBuffer();
		String					line = null;
		
		while ((line = lines.readLine()) != null) {
			buffer.append(line);
		}
		
		response.getOutputStream().print("<p>"+id+": "+buffer.toString()+"</p>");
	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(403, "Forbiddon");
	}
}
