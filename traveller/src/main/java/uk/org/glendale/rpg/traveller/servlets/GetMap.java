/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/20 15:48:23 $
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
 * Servlet which returns a map for a given sub sector. The subsector is 
 * identified by either a sector id or by sector coordinates, together
 * with a subsector id or subsector coordinates.
 * 
 * Subsector id is A-P, coordinates are 0-3,0-3
 * Sector id is the unique datbase id, or an x,y sector pair.
 * 
 * Map is returned as a jpeg image.
 * 
 * @author Samuel Penn
 */
public class GetMap extends HttpServlet {
     
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
        
        // Sector information.
        int			sectorId = 0;
        int			x = 0;
        int			y = 0;
        // Subsector information.
        String		subSector = null;
        int			sx = 0;
        int			sy = 0;
        int			scale = 48;
        
        if (request.getParameter("id") != null) {
        	sectorId = Integer.parseInt(request.getParameter("id"));
        } else {
        	if (request.getParameter("x") != null) {
        		x = Integer.parseInt(request.getParameter("x"));
        	}
        	if (request.getParameter("y") != null) {
        		y = Integer.parseInt(request.getParameter("y"));
        	}
        	if (request.getParameter("scale") != null) {
        		scale = Integer.parseInt(request.getParameter("scale"));
        	}
        	try {
        		Sector		sector = new Sector(x, y);
        		sectorId = sector.getId();
        	} catch (ObjectNotFoundException e) {
        		System.out.println("Sector does not exist");
        		return;
        	}
        }
        if (scale < 16 || scale > 128) {
        	scale = 48;
        }
        
        if (request.getParameter("sub") != null) {
        	subSector = request.getParameter("sub");
        	int		idx = "ABCDEFGHIJKLMNOP".indexOf(subSector);
        	if (idx >= 0) {
        		sx = idx % 4;
        		sy = idx / 4;
        	}
        } else {
        	if (request.getParameter("sx") != null) {
        		sx = Integer.parseInt(request.getParameter("sx"));
        	}
        	if (request.getParameter("sy") != null) {
        		sy = Integer.parseInt(request.getParameter("sy"));
        	}
        	if (sx >= 0 && sy >= 0 && sx < 4 && sy < 4) {
        		subSector = "ABCDEFGHIJKLMNOP".substring((sx+sy*4), (sx+sy*4)+1);
        	}
        }
        
        String		mapPath = "images/subsectors/"+sectorId+"-"+subSector+"x"+scale+".jpg";
        String		root = "/";
        root = getServletContext().getRealPath("/");
        File		file = new File(root+"/"+mapPath);
        if (!file.exists()) {
        	// Image of this subsector does not exist, so need to create it.
        	System.out.println("Image ["+mapPath+"] does not exist");
        	SubSectorImage.setSymbolBase("file:"+root+"/images/symbols/");
        	SubSectorImage		map = new SubSectorImage(sectorId, sx, sy);
        	map.setScale(scale);
        	try {
        		SimpleImage			image = map.getImage();
        		if (image != null) {
        			System.out.println("Saving image file");
        			image.save(file);
        		}
        	} catch (ObjectNotFoundException e) {
        		e.printStackTrace();
        	}
        }
        if (!file.exists()) {
        	System.out.println("Hmm... Image still doesn't exist");
        	return;
        }
        FileInputStream		fis = new FileInputStream(file);
        ServletOutputStream	out = response.getOutputStream();
        int					BUFFERSIZE = 65536;
        byte[]				buffer = new byte[BUFFERSIZE];
        int					read = 0;
        while ((read = fis.read(buffer)) != -1) {
        	out.write(buffer, 0, read);
        }
    }
}
