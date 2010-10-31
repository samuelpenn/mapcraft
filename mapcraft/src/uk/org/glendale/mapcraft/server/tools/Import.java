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
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;
import net.sourceforge.mapcraft.map.elements.Area;
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

		Map				map = manager.getMap("test2");
		MapInfo			info = map.getInfo();

		// Old style XML map to export from
		String		xmlPath = "application/maps/island.map";
		xmlPath = "/home/sam/rpg/habisfern/encyclopedia/src/maps/euressa.map";
		
		net.sourceforge.mapcraft.map.Map	xmlMap = new net.sourceforge.mapcraft.map.Map(xmlPath);
		
		ITileSet	tiles = xmlMap.getTileSet(0);
		int			scale = tiles.getScale();
		int			xOffset = tiles.getParentsXOffset();
		int			yOffset = tiles.getParentsYOffset();
		int			parentScale = tiles.getParentsScale();
		
		for (int a=0; a < xmlMap.getAreaSet().size(); a++) {
			Area area = xmlMap.getAreaSet().getArea(a+1);
			System.out.println(area.getUri());
			info.addNamedArea(area.getUri(), area.getName(), 0);
		}
		
		
		if (scale < 5 || scale%5 != 0) {
			throw new IllegalArgumentException("Imported map has unsupported scale "+scale);
		}
		scale /= 5;
		System.out.println("Scale: "+scale);
		
		for (int y=0; y < tiles.getMapHeight(); y++) {
			for (int x=0; x < tiles.getMapWidth(); x++) {
				Terrain		t = tiles.getTerrain(x, y);
				String		terrainName = t.getName();
				int			terrainId = -1;
				int			featureId = 0;
				
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
				
				int	areaId = info.getNamedArea(tiles.getArea(x, y).getUri()).getId();
				for (int xx=0; xx < scale; xx++) {
					for (int yy=0; yy < scale; yy++) {
						map.setTerrain(x*scale + xx, y*scale + yy, terrainId);
						map.setFeature(x*scale + xx, y*scale + yy, featureId);	
						map.setNamedArea(x*scale + xx, y*scale + yy, areaId);
					}
				}
			}
		}
		map.saveAll();
	}
}
