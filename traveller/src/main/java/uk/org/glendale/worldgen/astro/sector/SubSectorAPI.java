package uk.org.glendale.worldgen.astro.sector;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.*;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.map.SubSectorImage;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Provides REST API for accessing sub sector maps. A sub sector is
 * identified by the id of the sector, plus the sub-sector designator
 * A-P. Alternatively a scale can be specified which is the width of
 * a tile in pixels.
 * 
 * @author Samuel Penn
 */
@Path("/subsector/{sectorId}/{subSector}")
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
	@GET
	@Produces("image/jpeg")
	public File getImage(@PathParam("sectorId") int sectorId, @PathParam("subSector") SubSector subSector, 
			             @DefaultValue("32") @QueryParam("scale") int scale,
			             @DefaultValue("false") @QueryParam("bleed") boolean bleed) {
		
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
