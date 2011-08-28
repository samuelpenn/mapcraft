/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

import java.io.File;
import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMethod;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.SubSectorImage;
import uk.org.glendale.worldgen.server.AppManager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Provides REST API for accessing sub sector maps. A sub sector is
 * identified by the id of the sector, plus the sub-sector designator
 * A-P. Alternatively a scale can be specified which is the width of
 * a tile in pixels.
 * 
 * @author Samuel Penn
 */
//@Controller
public class SubSectorAPI {
	
	/**
	 * Gets an image of a given sub-sector. The scale sets the size of
	 * the map (which is always 8x10 tiles). If bleed is true, then
	 * neighbouring half tiles are drawn right up to the edge of the
	 * map. This allows multiple maps to be displayed alongside each
	 * other seamlessly.
	 * 
	 * @param sectorId		Id of the sector to be mapped.
	 * @param subSector		Sub-sector to map, A-P.
	 * @param scale			Scale of each hex, in pixels.
	 * @param bleed			If true, draw outside hexes.
	 * @return
	 */
	//@GET
	//@Produces("image/jpeg")
	//@RequestMapping(value="/subsector/{sectorId}/{subSector}", method=RequestMethod.GET)
	@ResponseBody
	public File getImage(@PathVariable("sectorId") int sectorId, @PathVariable("subSector") SubSector subSector, 
			             @RequestParam(value="scale", defaultValue="32") int scale,
			             @RequestParam(value="bleed", defaultValue="32") boolean bleed) {
		
		//SubSector subSector = SubSector.valueOf(sub.toUpperCase());
		System.out.println(sectorId+": "+subSector);
		
		SubSectorImage	map = new SubSectorImage(sectorId, subSector);
		map.setStandalone(!bleed);
		map.setScale(scale);
		
		
		//return new File(AppManager.getRootPath()+"/images/globe.jpg");
		
		try {
	        String		mapPath = "images/subsectors/"+sectorId+"-"+subSector+"x"+scale+(bleed?"b":"")+".jpg";
	        String		root = AppManager.getRootPath();
	        //root = getServletContext().getRealPath("/");
	        File		file = new File(root+"/"+mapPath);
	        if (!file.exists()) {
	        	// Image of this subsector does not exist, so need to create it.
	        	System.out.println("Image ["+mapPath+"] does not exist");
	        	SubSectorImage.setSymbolBase("file:"+root+"/images/symbols/");

	        	try {
	        		SimpleImage			image = map.getImage();
	        		if (image != null) {
	        			System.out.println("Saving image file");
	        			image.save(file);
	        		}
	        	} catch (ObjectNotFoundException e) {
	        		e.printStackTrace();
	        	} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if (!file.exists()) {
	        	System.out.println("Hmm... Image still doesn't exist");
	        } else {
	        	
	        	return file;
	        }
		} finally {
			
		}
		
		return null;
	}
}
