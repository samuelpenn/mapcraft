/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 */
package uk.org.glendale.mapcraft.server.tools;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;

import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapManager;
import net.sourceforge.mapcraft.map.elements.Terrain;
import net.sourceforge.mapcraft.map.interfaces.ITileSet;

/**
 * Imports an XML map file from earlier versions of Mapcraft. 
 * 
 * @author Samuel Penn
 */
public class Import {
	
	private static Hashtable<String,String>		mapping = new Hashtable<String,String>();
	
	static {
		ResourceBundle		bundle = ResourceBundle.getBundle("uk.org.glendale.mapcraft.server.tools.mappings");
		Enumeration<String>	keys = bundle.getKeys();
		
		while (keys.hasMoreElements()) {
			String	key = keys.nextElement();
			String	value = bundle.getString(key);
			
			mapping.put(key, value);
		}
	}
	
	public static void main(String[] args) throws Exception {
		// New style map we want to import into.
		AppManager		app = new AppManager();
		MapManager		manager = new MapManager(app.getDatabaseConnection());
		
		MapInfo			info = manager.getMap("test2");
		MapData			data = new MapData(info, app.getDatabaseConnection());
		Map				map = new Map(info, data);

		// Old style XML map to export from
		String		xmlPath = "application/maps/island.map";
		xmlPath = "/home/sam/rpg/habisfern/encyclopedia/src/maps/euressa.map";
		
		net.sourceforge.mapcraft.map.Map	xmlMap = new net.sourceforge.mapcraft.map.Map(xmlPath);
		
		ITileSet		tiles = xmlMap.getTileSet(0);
		
		for (int y=0; y < tiles.getMapHeight(); y++) {
			for (int x=0; x < tiles.getMapWidth(); x++) {
				Terrain		t = tiles.getTerrain(x, y);
				String		terrainName = t.getName();
				int			terrainId = -1;
				int			featureId = -1;
				
				if (mapping.get(terrainName) == null) {
					System.out.println(terrainName);
					continue;
				} else if (info.getTerrain(mapping.get(terrainName)) == null) {
					System.out.println(terrainName);
					continue;
				}
				terrainId = info.getTerrain(mapping.get(terrainName)).getId();
				
				Terrain		f = tiles.getFeature(x, y);
				if (f.getName().equals("lowhills")) {
					featureId = info.getFeature("hills.low").getId();
				} else if (f.getName().equals("highhills")) {
					featureId = info.getFeature("hills.high").getId();
				} else if (f.getName().equals("foothills")) {
					featureId = info.getFeature("mountains.low").getId();
				} else if (f.getName().equals("lowmnts")) {
					featureId = info.getFeature("mountains.medium").getId();
				} else if (f.getName().equals("highmnts")) {
					featureId = info.getFeature("mountains.high").getId();
				} else if (f.getName().equals("marsh")) {
					featureId = info.getFeature("wetlands").getId();
				} else if (f.getName().equals("ice")) {
					featureId = info.getFeature("ice").getId();
				}
				
				
				map.setTerrain(x, y, terrainId);
				map.setFeature(x, y, featureId);
			}
		}
		map.saveAll();
	}
}
