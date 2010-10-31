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

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.mapcraft.graphics.MapSector;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
	
	
	/**
	 * Gets an image for the given part of the specified map. The map coordinates
	 * are from the top left of the map. The origin of the entire map is (0, 0).
	 * If bleeding is specified, then the map is drawn right to the edge of the
	 * drawable area, including tiles outside the normal bounds. This allows
	 * smaller maps to be tiled together perfectly.
	 * 
	 * Once drawn, a map is cached on disc.
	 * 
	 * @param mapName		Map to retrieve image for.
	 * @param x				X origin coordinate for the map (left).
	 * @param y				Y original coordinate for the map (top).
	 * @param width			Width of the map to draw.
	 * @param height		Height of the map to draw.
	 * @param scale			Scale to use for each tile (in pixels).
	 * @param bleed			If true, use bleeding.
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	@GET
	@Produces("image/jpeg")
	public File getImage(@PathParam("mapname") String mapName,
						@QueryParam("x") int x, @QueryParam("y") int y,
						@QueryParam("w") @DefaultValue("32") int width, 
						@QueryParam("h") @DefaultValue("40") int height,
						@QueryParam("s") @DefaultValue("8") int scale,
						@QueryParam("b") @DefaultValue("false") boolean bleed,
						@QueryParam("f") @DefaultValue("false") boolean force) throws SQLException, IOException {
		
		MapManager		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
		
		Map				map = manager.getMap(mapName);
		MapInfo			info = map.getInfo();
		
		if (x < 0 || y < 0 || width < 0 || height < 0) {
			throw new IllegalArgumentException("Cannot specify negative coordinates");
		}
		
		if (x > info.getWidth()) {
			throw new IllegalArgumentException("X origin exceeds map width of "+info.getWidth());
		}
		if (y > info.getHeight()) {
			throw new IllegalArgumentException("Y origin exceeds map height of "+info.getHeight());
		}
		
		String		root = AppManager.getInstance().getRootPath();
		String		filename = String.format("%s-%d-%d-%d-%d-%d%s.jpg", mapName, x, y, width, height, scale, bleed?"b":"");
		File		image = new File(root+"/images/cache/"+filename);
		
		if (force || !image.exists()) {
			MapSector	imageMap = new MapSector(map, new File(root+"/images/map/style/colour"));
			imageMap.setBleeding(bleed);
			imageMap.drawMap(x, y, width, height);
			imageMap.save(image);
		}
		return image;
	}	
}
