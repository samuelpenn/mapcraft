/* Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.navigate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;

@RequestMapping("/api/navigate/")
@Controller
public class NavigateAPI {
	@Autowired
	private StarSystemFactory 	starSystemFactory;
	
	@Autowired
	private SectorFactory		sectorFactory;
	
	public int getAbsoluteX(StarSystem system) {
		return system.getX() - system.getSector().getX() * Sector.WIDTH;
	}
	
	public int getAbsoluteY(StarSystem system) {
		return system.getY() - system.getSector().getY() * Sector.HEIGHT;
	}
	
	private static int getDistance(int x0, int y0, int x1, int y1) {
		int		d = 0;
		
		int		dx = Math.abs(x0 - x1);
		int		dy = Math.abs(y0 - y1) - (int)Math.ceil(dx / 2.0);
		
		if (dy < 0) {
			dy = 0;
		}
				
		return dx + dy;
/*
        y0 = y0 - (x0)/2;
        y1 = y1 - (x1)/2;

        if (x1 <= x0) {
            int     x = x0;
            int     y = y0;

            x0 = x1;
            y0 = y1;
            x1 = x;
            y1 = y;
        }

        if (y1 > y0) {
            d = x1 - x0 + y1 - y0;
        } else if (x0+y0 > x1+y1) {
            d = y0 - y1;
        } else {
            d = x1 - x0;
        }

        return d;
*/
	}
	
	/**
	 * Gets the distance between two star systems.
	 * 
	 * @param id1		First star system.
	 * @param id2		Second star system.
	 * 
	 * @return			Distance in parsecs.
	 */
	@ResponseBody
	@RequestMapping(value="/distance", method=RequestMethod.GET)
	public int getDistance(@RequestParam int id1, @RequestParam int id2) {
		StarSystem	a = starSystemFactory.getStarSystem(id1);
		StarSystem	b = starSystemFactory.getStarSystem(id2);
		
		if (a == null || b == null) {
			throw new IllegalArgumentException("Star systems not found");
		}
		int		ax = getAbsoluteX(a);
		int		ay = getAbsoluteY(a);
		int		bx = getAbsoluteX(b);
		int		by = getAbsoluteY(b);
		
		return 0;
	}
	
	public static void main(String[] args) {
		int		x0 = 5;
		int 	y0 = 5;
		int		x1 = 6;
		int		y1 = 7;
		
		System.out.println(getDistance(x0, y0, x1, y1));
	}
}
