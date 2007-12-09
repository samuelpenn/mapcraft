/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.systems.Planet;

/**
 * A RESTful implementation of a web service that enables access to planet
 * resources.
 * 
 * A request should always be of the format /planet/<planetId>. Individual
 * methods will define further requirements.
 * 
 * @author Samuel Penn
 * @created 2007/12/3
 *
 */
public class PlanetServlet extends HttpServlet {
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
			response.sendError(400, "Badly formatted planet id");
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
			Planet				planet = factory.getPlanet(id);

			if (property == null) {
				getFullPage(factory, planet, format, request, response);
			} else {
				getProperty(factory, planet, format, property, request, response);
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
	private void getFullPage(ObjectFactory factory, Planet planet, String format, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (format.equals("html")) {
			// Output information as HTML.
			response.setContentType("text/html");
			response.getOutputStream().print(outputHTML(planet, request.getContextPath()));
		} else if (format.equals("txt")) {
			// Output information as XML (with content type of plain text).
			response.setContentType("text/plain");
			response.getOutputStream().print(outputXML(planet, request.getContextPath()));
		} else if (format.equals("jpg")) {
			// Output map of the world, as JPEG.
	        ByteArrayOutputStream	stream = null;
	        
	        if (request.getParameter("globe") != null) {
	        	stream = factory.getPlanetGlobe(planet.getId());
	        } else {
	        	stream = factory.getPlanetMap(planet.getId());
	        }
	        if (stream == null) {
	        	response.sendError(404, "Planet ["+planet.getId()+"] does not have the requested image available");
	        	return;
	        }
	        response.setContentType("image/jpeg");
	        ServletOutputStream	out = response.getOutputStream();
	        out.write(stream.toByteArray());
		} else {
			// Output information as XML.
			response.setContentType("text/xml");
			response.getOutputStream().print(outputXML(planet, request.getContextPath()));
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
	 * Output information about the planet as HTML. Also adds HTML header
	 * and footer since the raw planet HTML is only an HTML snippet.
	 * 
	 * @param planet	Planet to output data on.
	 * @return			String containing the HTML page.
	 */
	private String outputHTML(Planet planet, String contextPath) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = contextPath+"/data/css/system.css";
		
		buffer.append("<html>\n<head>\n<title>"+planet.getName()+"</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
		buffer.append("</head><body>\n");
		buffer.append(planet.toHTML());
		buffer.append("</body></html>\n");
		
		return buffer.toString();
	}
	
	private String outputXML(Planet planet, String contextPath) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = contextPath+"/data/xslt/planet.xsl";

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		if (stylesheet != null) {
			buffer.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>\n");
		}
		buffer.append(planet.toXML());
		
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
