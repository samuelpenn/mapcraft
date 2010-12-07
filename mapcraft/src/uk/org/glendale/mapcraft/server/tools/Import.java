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
import java.util.Iterator;
import java.util.ResourceBundle;

import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.server.AppManager;
import uk.org.glendale.mapcraft.server.database.MapData;
import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;
import net.sourceforge.mapcraft.map.TerrainSet;
import net.sourceforge.mapcraft.map.elements.Area;
import net.sourceforge.mapcraft.map.elements.Terrain;
import net.sourceforge.mapcraft.map.elements.Thing;
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

		Map				map = manager.getMap("eorthe");
		MapInfo			info = map.getInfo();

		// Old style XML map to export from
		String		xmlPath = "application/maps/island.map";
		xmlPath = "/home/sam/rpg/habisfern/encyclopedia/src/maps/weidany.map";
		
		net.sourceforge.mapcraft.map.Map	xmlMap = new net.sourceforge.mapcraft.map.Map(xmlPath);
		
		ITileSet	tiles = xmlMap.getTileSet(0);
		int			scale = tiles.getScale();
		int			xOffset = tiles.getParentsXOffset();
		int			yOffset = tiles.getParentsYOffset();
		int			parentScale = tiles.getParentsScale();
		
		for (int a=0; a < xmlMap.getAreaSet().size(); a++) {
			Area area = xmlMap.getAreaSet().getArea(a+1);
			System.out.println(area.getUri());
			if (info.getNamedArea(area.getUri()) == null) {
				info.addNamedArea(area.getUri(), area.getName(), 0);
			}
		}		
		
		xOffset = 3424 + xOffset * parentScale/5;
		yOffset = 640 + yOffset * parentScale/5;
		if (scale < 5 || scale%5 != 0) {
			throw new IllegalArgumentException("Imported map has unsupported scale "+scale);
		}
		scale /= 5;
		System.out.println("Scale: "+scale);
		
		for (int y=0; y < tiles.getMapHeight(); y++) {
			if (y%1 == 0) {
				System.out.print("\nY: "+y+"/"+tiles.getMapHeight());
			}
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

				switch (terrainId) {
				case 0: case 1: case 2:
					System.out.print(".");
					break;
				default:
					System.out.print("#");
				}
				if (terrainId < 3) {
					//continue;
				}
				
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
				
				Area	area = tiles.getArea(x, y);
				int		areaId = 0;
				if (area != null) {
					areaId = info.getNamedArea(area.getUri()).getId();
				}
				for (int xx=0; xx < scale; xx++) {
					if ((x*scale + xx) > map.getInfo().getWidth()) {
						break;
					}
					for (int yy=0; yy < scale; yy++) {
						if ((y*scale + yy) > map.getInfo().getHeight()) {
							break;
						}
						//map.setTile(x*scale + xx, y*scale + yy, terrainId, featureId, areaId);
						map.setTerrain(xOffset + x*scale + xx, yOffset + y*scale + yy, terrainId);
						map.setFeature(xOffset + x*scale + xx, yOffset + y*scale + yy, featureId);	
						map.setNamedArea(xOffset + x*scale + xx, yOffset + y*scale + yy, areaId);
					}
				}
			}
		}
		System.out.println("Done tiles");
		for (Thing xmlThing : tiles.getThings()) {
			if (xmlThing == null) {
				continue;
			}
			String 	name = xmlThing.getName();
			String 	title = xmlThing.getDescription();
			short	importance = (short)xmlThing.getImportance();
			String 	type = xmlMap.getThingSet().getTerrain(xmlThing.getType()).getName();
			
			uk.org.glendale.mapcraft.map.Thing	thing = info.getThing(type);
			if (thing == null) {
				System.out.println("Unable to find thing ["+type+"]");
				break;
			}
			
			// The XMLThing will have an X/Y that is tile coordinate * 100.
			int		x = xmlThing.getX() * scale;
			int		y = xmlThing.getY() * scale;
			
			info.createNamedPlace(thing, name, title, importance, xOffset + x/100, yOffset + y/100, x%100, y%100);
		}
		System.out.println("Saving");
		map.saveAll();
		System.out.println("Done!");
	}
}
