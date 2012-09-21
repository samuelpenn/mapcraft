/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.util.List;

import uk.org.glendale.worldgen.astro.starsystem.StarSystemTO;

/**
 * Define a transfer object for a sector. Includes information on sub sectors
 * as well as basic star system information for the entire sector.
 * 
 * @author Samuel Penn
 */
public class SectorTO {
	public final int					id;
	public final String					name;
	public final int					x;
	public final int					y;
	public final String[]				subSectors = new String[16];
	public final List<StarSystemTO>		systems;
	
	public SectorTO(Sector sector, List<StarSystemTO> systems) {
		this.id = sector.getId();
		this.name = sector.getName();
		this.x = sector.getX();
		this.y = sector.getY();
		this.systems = systems;
		
		for (int i=0; i < 16; i++) {
			subSectors[i] = "ABCDEFGHIJKLMNOP".substring(i, i+1);
		}
	}
}
