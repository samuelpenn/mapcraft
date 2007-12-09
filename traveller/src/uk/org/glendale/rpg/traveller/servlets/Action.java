/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
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

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.rpg.traveller.Config;
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
public class Action extends HttpServlet {
    
 
    
    /**
     * Get the integer value of a Http parameter. If it doesn't exist, or isn't
     * a number, then assume that the value is zero.
     * 
     * @param request           Servlet request data.
     * @param parameter         Parameter to read.
     * @return                  The value of the parameter, or zero if unavailable.
     */
    private int getInt(HttpServletRequest request, String parameter) {
    	int i = 0;
        
        try {
        	String     value = request.getParameter(parameter);
            if (value != null) {
                i = Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
        	// Just set to zero if this isn't a number.
            i = 0;
        }
            
        return i;
    }
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    protected void error(PrintWriter out, String message) {
    	out.println("<error>");
    	out.println("<message>");
    	out.println(message);
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
        
        if (handle.isXML()) {
        	response.setContentType("text/xml");
        } else {
        	response.setContentType("text/plain");
        }
        
        if (handle.getAction() == null) {
        	error(out, "You must provide an action");
        } else if (handle.getAction().equals("terraform")) {
        	
        }
        if (handle.getRequestType() == null) {
        	// No request parameter provided.
        	error(out, "You must provide a \"type\" parameter of \"sector\", \"system\", \"star\" or \"planet\".");
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
		
		buffer.append("<?xml version=\"1.0\"?>\n");
		
		if (stylesheet != null) {
			buffer.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>\n");
		}
		
		return buffer.toString();
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
				sector = new Sector(handle.getId());
			} else if (handle.getName() != null) {
				sector = new Sector(handle.getName());
			} else {
				sector = new Sector(handle.getX(), handle.getY());
			}
			
			PrintWriter		out = handle.getWriter();
			
			if (handle.isXML()) {
				out.print(getXMLHeader("xslt/sector.xsl"));
			}

			out.print("<sector xmlns=\"http://yagsbook.sourceforge.net/xml/traveller\" name=\"");
			out.print(sector.getName());
			out.print("\" x=\"");
			out.print(sector.getX());
			out.print("\" y=\"");
			out.print(sector.getY());
			out.print("\" id=\"");
			out.print(sector.getId());
			out.println("\">");
			
			Iterator<StarSystem>	i = sector.listSystems().iterator();
			while (i.hasNext()) {
				StarSystem		ss = i.next();
				out.print("<system id=\"");
				out.print(ss.getId());
				out.print("\" name=\"");
				out.print(ss.getName());
				out.print("\" x=\"");
				out.print(ss.getX());
				out.print("\" y=\"");
				out.print(ss.getY());
				out.println("\"/>");
			}
			out.println("</sector>");
		} catch (ObjectNotFoundException e) {
			error(handle.getWriter(), "Failed to find sector ("+e.getMessage()+")");
		}
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
			out.print(system.toXML());
		} catch (ObjectNotFoundException e) {
			error(handle.getWriter(), "Failed to find system ("+e.getMessage()+")");
		} finally {
			factory.close();
		}
	}    
    
}
