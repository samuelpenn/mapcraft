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
 * A RESTful implementation of a web service that enables access to the universe.
 * 
 * A request should always be of the format /universe
 * 
 * @author Samuel Penn
 * @created 2007/12/4
 *
 */
public class Sectors extends HttpServlet {
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
		
		
		
		if (uri == null || uri.startsWith("/index") || uri.equals("/")) {
			System.out.println("Universe list");
			universeMap(request, response);
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

		// Syntax is "sector/subsector" or "sector/coordinate"
		// Where sector is "id", "name" or "x,y"
		
		String	sectorValue = uri.replaceAll("([0-9a-zA-Z, ']+)([/.].*)?", "$1");
		if (sectorValue.startsWith("/")) sectorValue = sectorValue.substring(1);
		System.out.println(sectorValue);
		
		ObjectFactory		factory = null;
		Sector				sector = null;
		StarSystem			system = null;
		
		try {
			factory = new ObjectFactory();
			if (sectorValue.matches("-?[0-9]+,-?[0-9]+")) {
				// This is an x,y coordinate.
				System.out.println("Sector coordinate");
				int x = Integer.parseInt(sectorValue.substring(0, sectorValue.indexOf(",")));
				int y = Integer.parseInt(sectorValue.substring(sectorValue.indexOf(",")+1));
				sector = new Sector(factory, x, y);
			} else if (sectorValue.matches("[0-9]+")) {
				System.out.println("Sector id");
				sector = new Sector(factory, Integer.parseInt(sectorValue));
			} else {
				System.out.println("Sector name");
				sector = new Sector(factory, sectorValue);
			}
			System.out.println(sector.getName());
		} catch (ObjectNotFoundException e) {
			response.sendError(404, "Cannot find sector ["+sectorValue+"]");
			factory.close();
			return;
		}
				
		if (uri.matches("/[^/]+/.+")) {
			String		part = uri.replaceAll("/[^/]+/([^/.]+)([/.].*)?", "$1");
			
			if (part.matches("[0-9][0-9][0-9][0-9]")) {
				response.getOutputStream().println("Coordinate");
				int		x = Integer.parseInt(part.substring(0, 2));
				int		y = Integer.parseInt(part.substring(2, 4));
				system = sector.getSystem(x, y);
				if (system == null) {
					response.sendError(404, "No star system at coordinates ["+x+","+y+"] in sector ["+sector.getName()+"]");
					factory.close();
					return;
				}
			} else {
				// Not currently implemented.
			}
		}
		
		if (format.equals("html")) {
			response.setContentType("text/html");
			if (system != null) {
				response.getOutputStream().print(system.toHTML());
			} else {
				outputHTML(sector, factory);
			}
		} else if (format.equals("txt")) {
			response.setContentType("text/plain");
		} else {
			response.setContentType("text/xml");
		}
		
		
		/*
		{
			try {
				factory = new ObjectFactory();
				sector = new Sector(factory, id);
				
				
			} catch (Throwable t) {
				t.printStackTrace();
				response.sendError(500, "Exception ("+t.getMessage()+")");
				return;
			} finally {
				factory.close();
				factory = null;
			}
		}
		*/
	}

	/**
	 * Generate a view onto the entire universe.
	 */
	private void universeMap(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		format = "html";
		String		uri = request.getPathInfo();
		
		if (uri == null) {
			// Just use defaults.
		} else if (uri.matches(".*\\.[a-z]+")) {
			format = uri.replaceAll(".*\\.([a-z]+)", "$1");
			if (format.equals("txt") || format.equals("xml") || format.equals("html") || format.equals("jpg")) {
				// Okay.
			} else {
				response.sendError(415, "Unrecognised format type ["+format+"]");
				return;
			}
		}

		try {
			ObjectFactory		factory = new ObjectFactory();
			Vector<Sector>		sectors = factory.getSectors();
		
			String				output = null;
			
			if (format.equals("html")) {
				response.setContentType("text/html");
				output = universeAsHTML(sectors);
			} else if (format.equals("xml")) {
				response.setContentType("text/plain");
				output = universeAsXML(sectors);				
			} else {
				response.setContentType("text/xml");
				output = universeAsXML(sectors);
			}
			response.getOutputStream().println(output);
		} catch (Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Exception ("+t.getMessage()+")");
			return;
		}
	}
	
	private String universeAsXML(Vector<Sector> sectors) {
		return "";
	}
	
	private String universeAsHTML(Vector<Sector> sectors) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = "";

		buffer.append("<html>\n<head>\n<title>"+Config.getTitle()+"</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
        buffer.append("<script type=\"text/javascript\" src=\""+Config.getBaseUrl()+"scripts/system.js\"></script>\n");
		buffer.append("</head><body>\n");
		
		buffer.append("<div id=\"header\">\n");
		buffer.append("<h1>"+Config.getTitle()+"</h1>");
		buffer.append("</div>\n");
		
		buffer.append("<div id=\"sectorlist\">\n");
		buffer.append("<ul>\n");
		for (Sector sector : sectors) {
			buffer.append("<li id=\"sector_"+sector.getId()+"\"><a href=\""+Config.getBaseUrl()+"sector/"+sector.getId()+".html\">");
			buffer.append(sector.getName()+" ("+sector.getX()+","+sector.getY()+")");
			buffer.append("</a></li>\n");
		}
		buffer.append("</ul>\n");
		buffer.append("</div>\n");
		
		buffer.append("</body></html>\n");
		
		return buffer.toString();
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
	private String outputHTML(Sector sector, ObjectFactory factory) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = Config.getBaseUrl()+"css/systems.css";
		
		buffer.append("<html>\n<head>\n<title>"+sector.getName()+"</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
        buffer.append("<script type=\"text/javascript\" src=\""+Config.getBaseUrl()+"scripts/system.js\"></script>\n");
		buffer.append("</head><body>\n");
		
		buffer.append("<div id=\"header\">\n");
		buffer.append("<h1>"+sector.getName()+"</h1>\n");
		
		buffer.append("<p>\n");
		buffer.append("<b>Star systems: </b>"+sector.getSystemCount());
		buffer.append("</p><b><b>Sub sectors: </b>");
		for (int y=0; y < 4; y++) {
			for (int x=0; x < 4; x++) {
				buffer.append(sector.getSubSectorName(x*8+1, y*10+1));
				if (x < 3 || y < 3) {
					buffer.append(", ");
				} else {
					buffer.append(".");
				}
			}
		}
		buffer.append("</p>");		
		buffer.append("</div>\n");
		
		buffer.append("<div id=\"mainbody\">");
		for (Sector.SubSector ss : Sector.SubSector.values()) {
			int		xoff = ss.getXOffset();
			int		yoff = ss.getYOffset();
			buffer.append("<h3>"+sector.getSubSectorName(xoff, yoff)+"</h3>\n");

			buffer.append("<table>");
			buffer.append("<tr><th>XY</th><th>System</th><th>Planets</th><th>Population</th><th>TL</th></tr>\n");
			for (int y=yoff; y < yoff+11; y++) {
				for (int x=xoff; x < xoff+9; x++) {
					StarSystem system = sector.getSystem(x, y);
					if (system != null) {
						buffer.append("<tr>");
						buffer.append("<td>"+system.getXAsString()+system.getYAsString()+"</td>\n");
						buffer.append("<td><a href=\""+Config.getBaseUrl()+"system/"+system.getId()+".html\">"+system.getName()+"</a></td>\n");
						buffer.append("<td>"+system.getPlanetCount()+"</td>\n");
						buffer.append("<td>"+system.getMaxPopulation()+"</td>\n");
						buffer.append("<td>"+system.getMaxTechLevel()+"</td>\n");
						buffer.append("</tr>\n");
					}
				}
			}
			buffer.append("</table>");
		}
		buffer.append("</div>");
		
		buffer.append("</body></html>\n");
		
		return buffer.toString();
	}
	
	private ByteArrayOutputStream outputJPEG(Sector sector, ObjectFactory factory) {
		return null;
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
