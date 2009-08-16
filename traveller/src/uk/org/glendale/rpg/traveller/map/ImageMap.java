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

	public ImageMap(Sector sector, String root) {
		this.sectorName = sector.getName();
		this.root = root;
		this.sector = sector;
	}
	
	public static SimpleImage drawDensityMap(int size) {
		ObjectFactory 				factory = new ObjectFactory();
		Hashtable<String,Long>		statistics = factory.getStatistics();
		
		// Work out the size of the universe.
		long	minX = statistics.get("minx");
		long	maxX = statistics.get("maxx");
		long	minY = statistics.get("miny");
		long	maxY = statistics.get("maxy");
		
		//minX = minY = -1;
		//maxX = maxY = +1;
		
		// Size of the image we need to create.
		int				width = (int)(32 * size * (1 + maxX - minX));
		int				height = (int)((40 * size + size/2) * (1 + maxY - minY));
		SimpleImage		map = new SimpleImage(width, height, "#000000");
		
		try {
			for (int x = (int)minX; x <= maxX; x++) {
				for (int y = (int)minY; y <= maxY; y++) {
					// For each sector in the universe...
					try {
						Sector		sector = new Sector(x, y);
						System.out.println(sector.getName()+" ("+sector.getX()+","+sector.getY()+")");
						
						int[][]		bitmap = new int[64][64];
						
						for (int mx=-3; mx < 35; mx++) {
							for (int my=-3; my < 43; my++) {
								bitmap[mx+5][my+5] =  (factory.getStarSystem(sector.getId(), mx+1, my+1)==null)?0:1;
							}
						}
						for (int my=0; my < 40; my++) {
							for (int mx=0; mx < 32; mx++) {
								// For each pixel in this sector...
								int		number = 0;
								for (int sx=-2; sx < 3; sx++) {
									for (int sy=-2; sy < 3; sy++) {
										// Count number of nearby systems.
										if (bitmap[mx+sx+6][my+sy+6] == 1) number++;
									}
								}
								String 	colour = Integer.toHexString(number*10).toUpperCase();
								if (colour.length() < 2) colour = "0"+colour;
								colour = "#" + colour + colour + colour;
								int		px = (int)(x-minX)*32*size + mx*size;
								int		py = (int)(y-minY)*40*size + my*size + ((mx%2)*size/2);
				        		map.circle(px, py, 1, colour);
				        		if (number < 2) {
				        			System.out.print(" ");
				        		} else if (number < 5) {
				        			System.out.print(".");
				        		} else if (number < 15) {
				        			System.out.print("0");
				        		} else {
				        			System.out.print("#");
				        		}
							}
							System.out.println("");
						}
						sector = null;
					} catch (ObjectNotFoundException e) {
						// Not sector at this location, so do nothing.
					}
					factory.close();
					factory = null;
					System.gc();
					Thread.sleep(5000);
					System.gc();
					Thread.sleep(1000);
					factory = new ObjectFactory();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			factory.close();
		}
        
        return map;		
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
	        map = new SimpleImage(width, height, "#000000");

	        Allegiance		a = null;
	    	String			colour = "#777777";
	        String			lastAl = "XXX";
	        for (StarSystem ss: sector.getSystems()) {
	        	int			x = ss.getX() - 1;
	        	int			y = ss.getY() - 1;
	        	String		code = ss.getAllegiance();
	        	if (code == null) {
	        		colour = "#777777";
	        	} else if (code.equals(lastAl)) {
	        		// Just use last colour.
	        	} else {
	        		lastAl = code;
	        		a = ss.getAllegianceData();
	        		if (a != null) {
	        			colour = a.getColour();
	        		} else {
	        			colour = "#777777";
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
    
    public static void drawAllSectors() throws Exception {
    	ObjectFactory		factory = new ObjectFactory();
        String root = "/home/sam/src/mapcraft/traveller/webapp/images";

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

    public static void main(String[] args) throws Exception {
    	drawAllSectors();
    	//drawDensityMap(1).save(new File("/home/sam/density.jpg"));
    }
}
