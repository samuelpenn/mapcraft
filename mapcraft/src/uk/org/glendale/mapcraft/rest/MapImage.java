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

package uk.org.glendale.mapcraft.rest;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.mapcraft.graphics.MapSector;
import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapManager;

import javax.ws.rs.*;
import javax.ws.rs.Path;

/**
 * 
 * Servlet which returns a map.
 * 
 * Url: /mapcraft/map/mapname?x=0&y=0&w=32&h=40&s=8
 * 
 * @author Samuel Penn
 */
@Path("/map/{mapname}")
public class MapImage {
	
	private int getInteger(String param) {
		int		value = 0;
		if (param != null) {
			try {
				value = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				
			}
		}
		return value;
	}
	
	@GET
	@Produces("/image/jpeg")
	public void getImage(@PathParam("mapname") String mapName) {
		
	}
	
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	String	urlPath = request.getPathInfo();
    	String	paramMap = null;
    	String  paramArea = null;
    	
    	if (urlPath == null || urlPath.length() == 0) {
    		response.sendError(400, "Need to specify a map");
    	}
    	
    	String[]	paths = urlPath.split("/+");
    	if (paths.length > 1) {
    		// Name of the map to retrieve.
    		paramMap = paths[1];
    	}
    	if (paths.length > 2) {
    		// Name of the area to draw.
    		paramArea = paths[2];
    	}
    	int		x = getInteger(request.getParameter("x"));
    	int		y = getInteger(request.getParameter("y"));
    	int		width = getInteger(request.getParameter("w"));
    	int		height = getInteger(request.getParameter("h"));
    	int		scale = getInteger(request.getParameter("s"));
    	
		AppManager		app = AppManager.getInstance();
		
		System.out.println(paramMap);
		
		try {
			MapManager		manager = new MapManager(app.getDatabaseConnection());
			
			MapInfo			info = manager.getMap("test");
			System.out.println(info.getTitle());
			MapData			data = new MapData(info, app.getDatabaseConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Process requests from a client.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
    }
	
}
