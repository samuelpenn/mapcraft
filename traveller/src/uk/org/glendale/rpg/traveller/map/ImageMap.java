/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */

package uk.org.glendale.rpg.traveller.map;

import java.awt.Image;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.sectors.Allegiance;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.graphics.*;

/**
 * Class for generating large scale maps of a star sector as bitmap images.
 * Star systems are represented as a single dot on the map, with no other
 * information presented.
 * 
 * @author Samuel Penn
 */
public class ImageMap {
	private ObjectFactory		factory = null;
	private String				sectorName = null;
	private Sector				sector = null;
	private String				root = null;
    
	public ImageMap(String sectorName, String root) throws ObjectNotFoundException {
		this.sectorName = sectorName;
		this.root = root;
		
		sector = new Sector(sectorName);
    }

    
    /**
     * Draw the map.
     * 
     * @param size      Width of each tile.
     * 
     * @return			Bitmap image of the sector.
     */
    public SimpleImage drawMap(int size) {
        int         	width = 32 * size;
        int         	height = 40 * size + size/2;
        SimpleImage		map = null;
		factory = new ObjectFactory();

		try {
	        map = new SimpleImage(width, height, "#FFFFFF");

	        Allegiance		a = null;
	    	String			colour = "#000000";
	        String			lastAl = "XXX";
	        for (StarSystem ss: sector.getSystems()) {
	        	int			x = ss.getX() - 1;
	        	int			y = ss.getY() - 1;
	        	String		code = ss.getAllegiance();
	        	if (code == null) {
	        		colour = "#000000";
	        	} else if (code.equals(lastAl)) {
	        		// Just use last colour.
	        	} else {
	        		lastAl = code;
	        		a = ss.getAllegianceData();
	        		if (a != null) {
	        			colour = a.getColour();
	        		} else {
	        			colour = "#000000";
	        		}
	        	}
	        	if (a != null) {
	        		colour = a.getColour();
	        	}
        		map.circle(x*size, y*size + ((x%2)*size/2), size/2, colour);
	        }
		} finally {
			factory.close();
		}
        
        return map;
    }

    public static void main(String[] args) throws Exception {
    	ObjectFactory		factory = new ObjectFactory();
        String root = "/home/sam/src/traveller/webapp/images";

		for (int y=6; y >= -7; y--) {
			for (int x=-13; x <= 8; x++) {
				Sector		sector = null;
				try {
					sector = new Sector(factory, x, y);
				} catch (ObjectNotFoundException e) {
					continue;
				}
				File		file = new File(root+"/sectors/"+sector.getId()+".jpg");
				
				ImageMap		map = new ImageMap(sector.getName(), root);
				System.out.println("Drawing sector "+sector.getId()+": ["+sector.getName()+"]");
				map.drawMap(2).save(file);				
			}
		}
		factory.close();
    }
}
