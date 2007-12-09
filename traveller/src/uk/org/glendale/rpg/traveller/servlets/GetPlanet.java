/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.1 $
 * $Date: 2007/12/02 21:56:17 $
 */

package uk.org.glendale.rpg.traveller.servlets;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.map.SubSectorImage;
import uk.org.glendale.rpg.traveller.sectors.*;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Servlet which returns an image for a given planet.
 * 
 * Map is returned as a jpeg image.
 * 
 * @author Samuel Penn
 */
public class GetPlanet extends HttpServlet {
     
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

		Log.init(getServletContext().getRealPath("/"), 2);
        
        response.setContentType("image/jpeg");

        int			id = 0;
        boolean		globe = false;
        String		uri = request.getPathInfo();
        String		name = uri.replaceAll(".*/", "");
        name = name.replaceAll("[^0-9]+", "");
        if (uri.indexOf("/globe") != -1) {
        	globe = true;
        }
        Log.info("Getting planet image ["+name+"]");
        
        id = Integer.parseInt(name);

        ObjectFactory			factory = new ObjectFactory();
        ByteArrayOutputStream	stream = null;
        
        if (globe) {
        	stream = factory.getPlanetGlobe(id);
        } else {
        	stream = factory.getPlanetMap(id);
        }

        ServletOutputStream	out = response.getOutputStream();
        out.write(stream.toByteArray());
    }
}
