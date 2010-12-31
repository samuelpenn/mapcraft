/*
 * Copyright (C) 2010 Samuel Penn, sam@glendale.org.uk
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
import java.sql.*;

import uk.org.glendale.mapcraft.MapEntityException;
import uk.org.glendale.mapcraft.graphics.MapSector;
import uk.org.glendale.mapcraft.graphics.MapSector.Scale;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.NamedArea;
import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;

import javax.ws.rs.*;

/**
 * 
 * Servlet which returns a map.
 * 
 * Url: /mapcraft/rest/map/{mapname}?x=0&y=0&w=32&h=40&s=8
 * 
 * @author Samuel Penn
 */
@Path("/map/{mapname}")
public class MapImage {
	
	@GET
	@Path("/world")
	@Produces("image/jpeg")
	public File getImageOfWorld(@PathParam("mapname") String mapName,
								@QueryParam("force") @DefaultValue("false") boolean force) throws SQLException, IOException {
		
		MapManager		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
		
		Map				map = manager.getMap(mapName);
		MapInfo			info = map.getInfo();
				
		String		root = AppManager.getInstance().getRootPath();
		String		filename = String.format("%s-world.jpg", mapName);
		File		image = new File(root+"/images/cache/"+filename);
		
		if (force || !image.exists()) {
			MapSector	imageMap = new MapSector(map, new File(root+"/images/map/style/colour"));
			
			imageMap.setScale(Scale.SECTOR);
			
			imageMap.drawOverviewMap(1);
			imageMap.save(image);
		}
		
		manager.disconnect();
		return image;
	}
	
	/**
	 * Gets an image for the given named area. A single image of the whole area
	 * will be returned. Normally, only tiles within the named area will be
	 * drawn, and tiles outside of it will be shaded showing only a land/water
	 * difference.
	 * 
	 * @param mapName		Name of map to draw.
	 * @param areaName		URI of Named Area to display.
	 * @param borderSize	Size of border in tiles. Defaults to 1.
	 * @param scale			Scale to be used.
	 * @param bleed			If true, draw the full rectangle, otherwise leave tiles outside the area blank.
	 * @param force			Force a redraw of the map, ignoring the cache.
	 * @return
	 */
	@GET
	@Path("/area/{areaname}")
	@Produces("image/jpeg")
	public File getImageOfArea(@PathParam("mapname") String mapName,
								 @PathParam("areaname") String areaName,
								 @QueryParam("border") @DefaultValue("1") int borderSize,
								 @QueryParam("scale") @DefaultValue("STANDARD") Scale scale,
								 @QueryParam("bleed") @DefaultValue("false") boolean bleed,
								 @QueryParam("force") @DefaultValue("false") boolean force) throws SQLException, MapEntityException, IOException {

		MapManager		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
		
		Map				map = manager.getMap(mapName);
		MapInfo			info = map.getInfo();
		
		if (borderSize < 0) {
			throw new IllegalArgumentException("Cannot specify negative coordinates");
		}
		
		NamedArea	area = info.getNamedArea(areaName);
		
		String		root = AppManager.getInstance().getRootPath();
		String		filename = String.format("%s-%s.jpg", mapName, areaName);
		File		image = new File(root+"/images/cache/"+filename);
		
		if (force || !image.exists()) {
			MapSector	imageMap = new MapSector(map, new File(root+"/images/map/style/colour"));
			
			imageMap.setScale(scale);
			
			imageMap.setBleeding(bleed);
			imageMap.drawMap(area, borderSize);
			imageMap.save(image);
		}
		
		manager.disconnect();
		return image;
	}
	
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
		
		manager.disconnect();
		return image;
	}	
}
