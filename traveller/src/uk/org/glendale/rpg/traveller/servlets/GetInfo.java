/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.6 $
 * $Date: 2007/05/13 19:12:27 $
 */

package uk.org.glendale.rpg.traveller.servlets;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.sectors.*;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Servlet which provides information on the known universe. The type of
 * information requested is determined by the type attribute. The specific
 * item is identified by either an id, name or x,y tuple.
 * 
 * @author Samuel Penn
 */
public class GetInfo extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    protected void error(PrintWriter out, String message) {
    	out.println("<error>");
    	out.println("<message>");
    	out.println(escape(message));
    	out.println("</message>");
    	out.println("</error>");
    }

    /**
     * Process requests from a client.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
		Handle		handle = new Handle(request, response);
        PrintWriter	out = handle.getWriter();
        
        Log.init(getServletContext().getRealPath("/"), 2);
        
        if (handle.isXML()) {
        	response.setContentType("text/xml");
        } else if (handle.isHTML()) {
        	response.setContentType("text/html");
        } else {
        	response.setContentType("text/plain");
        }
        
        
        if (handle.getRequestType() == null) {
        	// No request parameter provided.
        	error(out, "You must provide a \"type\" parameter of \"sector\", \"system\", \"star\" or \"planet\".");
        } else if (handle.getRequestType().equalsIgnoreCase("universe")) {
        	getUniverseInfo(handle);
        } else if (handle.getRequestType().equalsIgnoreCase("sector")) {
        	getSectorInfo(handle);
        } else if (handle.getRequestType().equalsIgnoreCase("system")) {
        	getSystemInfo(handle);
        } else if (handle.getRequestType().equalsIgnoreCase("star")) {
        	
        } else if (handle.getRequestType().equalsIgnoreCase("planet")) {

        } else {
        	// Type of request is not valid.
        	error(out, "You must provide a type parameter of \"sector\", \"system\", \"star\" or \"planet\".");
        }        
    }
	
	private String getXMLHeader(String stylesheet) {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		if (stylesheet != null) {
			buffer.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>\n");
		}
		
		return buffer.toString();
	}
	
	private String getHTMLHeader(String title, String stylesheet) {
		StringBuffer		buffer = new StringBuffer();
		
		buffer.append("<html>\n<head>\n<title>"+title+"</title>\n");
		if (stylesheet != null) {
			buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
		}
		buffer.append("</head><body>\n");
		
		return buffer.toString();
	}
	
	private String getHTMLFooter() {
		return "</body></html>\n";
	}
	
	private void getUniverseInfo(Handle handle) {
		ObjectFactory		factory = new ObjectFactory();
		
		try {
			Vector<Sector>		list = factory.getSectors();
			Log.info("Listing "+list.size()+" sectors");
			
			PrintWriter		out = handle.getWriter();
			if (handle.isXML()) {
				out.print(getXMLHeader(null));
			}
			out.println("<universe xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\">");
			for (Sector sector : list) {
				out.println(sector.toXML());
			}
			out.println("</universe>");
		} finally {
			factory.close();
		}
		
	}
	
	/**
	 * Get information on a single sector, including a list of the star systems
	 * within the sector.
	 * 
	 * @param handle		Client information.
	 */
	private void getSectorInfo(Handle handle) {
		Sector			sector = null;
		
		try {
			if (handle.getId() > 0) {
				Log.info("Get sector ["+handle.getId()+"]");
				sector = new Sector(handle.getId());
			} else if (handle.getName() != null) {
				Log.info("Get sector ["+handle.getName()+"]");
				sector = new Sector(handle.getName());
			} else {
				Log.info("Get sector ["+handle.getX()+","+handle.getY()+"]");
				sector = new Sector(handle.getX(), handle.getY());
			}
			
			PrintWriter		out = handle.getWriter();
			
			if (handle.isXML()) {
				out.print(getXMLHeader("xslt/sector.xsl"));
			}

			out.print("<sector xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\" name=\"");
			out.print(escape(sector.getName()));
			out.print("\" x=\"");
			out.print(sector.getX());
			out.print("\" y=\"");
			out.print(sector.getY());
			out.print("\" id=\"");
			out.print(sector.getId());
			out.println("\">");
			
			for (int y=0; y < 4; y++) {
				for (int x=0; x < 4; x++) {
					out.print("<subsector x=\""+x+"\" y=\""+y+"\" name=\"");
					out.print(escape(sector.getSubSectorName(x*8+1, y*10+1)));
					out.println("\"/>");
				}
			}
			
			Iterator<StarSystem>	i = sector.listSystems().iterator();
			while (i.hasNext()) {
				StarSystem		ss = i.next();
				
				if (ss.getX() == handle.getDetailX() && ss.getY() == handle.getDetailY()) {
					ss.hasWater(0);
					out.println(ss.toXML());
				} else {
					out.print("<system id=\"");
					out.print(ss.getId());
					out.print("\" name=\"");
					out.print(escape(ss.getName()));
					out.print("\" x=\"");
					out.print(ss.getX());
					out.print("\" y=\"");
					out.print(ss.getY());				
					out.println("\"/>");
				}
			}
			out.println("</sector>");
		} catch (ObjectNotFoundException e) {
			Log.error("Failed to get sector info", e);
			error(handle.getWriter(), "Failed to find sector ("+e.getMessage()+")");
		}
	}
	
	private String escape(String text) {
		text = text.replaceAll("&", "&amp;");
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		text = text.replaceAll("'", "&apos;");
		text = text.replaceAll("\"", "&quot;");
		return text;
	}
	
	private void getSystemInfo(Handle handle) {
		ObjectFactory	factory = new ObjectFactory();
		StarSystem		system = null;
		
		try {
			if (handle.getId() > 0) {
				system = new StarSystem(factory, handle.getId());
			} else if (handle.getName() != null) {
				system = new StarSystem(factory, handle.getName());
			}
			
			if (system == null) {
				throw new ObjectNotFoundException("System is null");
			}
			
			PrintWriter		out = handle.getWriter();
			if (handle.isXML()) {
				out.print(getXMLHeader("xslt/system.xsl"));
			}
			if (handle.isHTML()) {
				out.println(getHTMLHeader(system.getName(), "css/system.css"));
				out.println(system.toHTML());
				out.println(getHTMLFooter());
			} else {
				out.print(system.toXML());
			}
		} catch (ObjectNotFoundException e) {
			error(handle.getWriter(), "Failed to find system ("+e.getMessage()+")");
		} finally {
			factory.close();
		}
	}    
    
}
