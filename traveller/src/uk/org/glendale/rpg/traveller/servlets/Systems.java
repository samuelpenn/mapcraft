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
			response.sendError(400, "Badly formatted system id");
			return;
		}

		if (uri.matches(".*\\.[a-z]+")) {
			format = uri.replaceAll(".*\\.([a-z]+)", "$1");
			if (format.equals("txt") || format.equals("xml") || format.equals("html") || format.equals("jpg")) {
				// Okay.
			} else {
				response.sendError(415, "Unrecognised format type ["+format+"]");
				return;
			}
		}
		
		if (uri.matches("/[0-9]+/.*")) {
			property = uri.replaceAll("/[0-9]+/([^.]+)(\\.[a-z]+)?", "$1");
		}
		
		try {
			ObjectFactory		factory = new ObjectFactory();
			StarSystem			system = factory.getStarSystem(id);

			if (property == null) {
				getFullPage(factory, system, format, request, response);
			} else {
				//getProperty(factory, system, format, property, request, response);
			}
		} catch (ObjectNotFoundException e) {
			response.sendError(404, "Cannot find planet with id ["+id+"]");
			return;
		} catch (Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Exception ("+t.getMessage()+")");
			return;
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
			try {
				response.getOutputStream().print(outputHTML(system, factory));
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.getOutputStream().print(e.getMessage());
			}
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
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = Config.getBaseUrl()+"css/systems.css";
		Sector  			sector = new Sector(factory, system.getSectorId());
		
		buffer.append("<html>\n<head>\n<title>"+system.getName()+" System</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
        buffer.append("<script type=\"text/javascript\" src=\""+Config.getBaseUrl()+"scripts/system.js\"></script>\n");
		buffer.append("</head><body>\n");
		
		buffer.append("<div id=\"header\">\n");
		buffer.append("<h1>"+system.getName()+"</h1>\n");
		
		buffer.append("<p>\n");
		buffer.append(sector.getName()+" / "+sector.getSubSectorName(system.getX(), system.getY())+" - "+system.getXAsString()+system.getYAsString());
		buffer.append(" ("+system.getAllegianceData().getName()+")");
		if (system.getZone() != Zone.Green) {
			buffer.append(" / "+system.getZone().toString());
		}
		buffer.append("</p>\n");
		buffer.append("</div>\n");
		// Simple map of the whole solar system.
		Vector<Planet>	planets = factory.getPlanetsBySystem(system.getId());
		buffer.append("<div id=\"map\">\n");
		int		lastX = 0, x = 0;
		for (Star star : system.getStars()) {
			String		image = Config.getBaseUrl()+"images/stars/"+star.getSpectralType().toString().substring(0, 1)+".png";
			int			ssize = (int)Math.pow((star.getStarClass().getRadius() * 600000), 0.3);
			lastX = 0;
			buffer.append("<table><tr>");
			
			buffer.append("<td><img src=\""+image+"\" width=\""+ssize+"\" height=\""+ssize+"\" title=\""+star.getName()+"\"/></td>");
			for (int i=0; i < planets.size(); i++) {
				Planet		planet = planets.elementAt(i);
				if (planet.getParentId() == star.getId() && !planet.isMoon()) {
					x = planet.getDistance() / 3 - lastX;
					lastX = planet.getDistance() / 3;
					String		pimg = Config.getBaseUrl()+"planet/"+planet.getId()+".jpg?globe";
					int			size = (int)Math.pow(planet.getRadius(), 0.3);
					buffer.append("<td width=\""+x+"px\">");
					buffer.append("<img src=\""+pimg+"\" width=\""+size+"\" height=\""+size+"\" title=\""+planet.getName()+"\" align=\"left\" valign=\"center\"/>");
					buffer.append("</td>");
				}
			}
			buffer.append("</tr></table>\n");
		}
		buffer.append("</div>\n");
		
		buffer.append("<div id=\"stars\">\n");
		buffer.append("<table id=\"tabs\">\n");
		buffer.append("<tr>");
		int		idx = 0;
		for (Star star : system.getStars()) {
			String		image = Config.getBaseUrl()+"images/stars/"+star.getSpectralType().toString().substring(0, 1)+".png";
			buffer.append("<td style=\"border: 1pt solid black\">");
			buffer.append("<img src=\""+image+"\" width=\"64\" height=\"64\" onclick=\"selectStar('"+(idx++)+"')\"/>");
			buffer.append("</td>");
		}
		buffer.append("</tr></table>\n");
		buffer.append("</div>\n");
		
		buffer.append("<div id=\"planets\">\n");
		idx = 0;
		String	style = "";
		for (Star star : system.getStars()) {
			buffer.append("<div id=\"planets_"+(idx++)+"\" class=\"planets\" "+style+">\n");
			
			buffer.append("<p>"+star.getName()+"</p>");
			buffer.append("<p>"+star.getStarClass()+" / "+star.getSpectralType()+"</p>");
			
			for (int i=0; i < planets.size(); i++) {
				Planet	planet = planets.elementAt(i);
				if (planet.getParentId() == star.getId() && !planet.isMoon()) {
					buffer.append(planet.toHTML());
				}
			}

			buffer.append("</div>\n");
			style = "style=\"display: none;\"";
		}
		buffer.append("</div>\n");
		
		buffer.append("</body></html>\n");
		
		return buffer.toString();
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
