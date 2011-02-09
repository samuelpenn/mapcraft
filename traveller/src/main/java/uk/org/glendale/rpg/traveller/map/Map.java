/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.8 $
 * $Date: 2007/12/09 17:45:17 $
 */

package uk.org.glendale.rpg.traveller.map;

import java.io.File;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.sectors.Sector;

/**
 * Create a Traveller style map from the database data. Map is created in
 * Postscript format.
 * 
 * @author Samuel Penn
 */
public class Map {
	ObjectFactory	factory = null;
	private int		x = 0;
	private int		y = 0;
	
	public Map(int x, int y) {
		factory = new ObjectFactory();
		this.x = x;
		this.y = y;
	}
	
	public void draw(File file) throws ObjectNotFoundException {
		PostScript		ps = new PostScript(file);
		Sector			sector = new Sector(factory, x, y);
		
		ps.setTopMargin(1650);
		ps.setLeftMargin(50);
		ps.setScale(23);
		ps.plotHexMap(32, 40);
		ps.plotFont("Helvetica", 9);
		ps.plotText(50, 1651, sector.getName());
		
		sector.plotSystems(ps);
		
		ps.close();
	}

	public void drawTiled2(File file) throws ObjectNotFoundException {
		Sector			sector = new Sector(factory, x, y);
		
		for (int x=1; x < 32; x+=16) {
			for (int y=1; y < 40; y+=20) {
				File		tile = new File("map_"+x+"_"+y+".ps");
				PostScript	ps = new PostScript(tile);
				
				ps.setTopMargin(1460);
				ps.setLeftMargin(150);
				ps.setScale(40);
				ps.plotHexMap(16, 20, x-1, y-1);
				ps.plotText(150, 1480, sector.getName());
				sector.plotSystems(ps, x, y, x+15, y+19);

				ps.close();
			}
		}
	}
	
	private String safe(String name) {
		return name.replaceAll(" ", "_");
	}
	
	public void drawTiled4(File file) throws ObjectNotFoundException {
		Sector			sector = new Sector(factory, x, y);
		
		for (int x=1; x < 32; x+=8) {
			for (int y=1; y < 40; y+=10) {
				File		tile = new File("/home/sam/tmp/tiled/"+safe(sector.getName())+"_"+Sector.getSubSector(x, y).toString()+".ps");
				PostScript	ps = new PostScript(tile);
				
				ps.setTopMargin(1475);
				ps.setLeftMargin(150);
				ps.setScale(80);
				ps.plotHexMap(8, 10, x-1, y-1);
				ps.plotText(50, 1485, sector.getName()+": "+sector.getSubSectorName(x, y));
				sector.plotSystems(ps, x, y, x+7, y+9);

				ps.close();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Map		map = new Map(0, 0);
		//map.drawTiled4(new File("map.ps"));
		map.draw(new File("map.ps"));
	}
}
