/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.generators;

import net.sourceforge.mapcraft.map.Map;

/**
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WorldUtils {
    private int         width;
    private int         height;
    private int         scale;
    
    public
    WorldUtils(Map map) {
        width = map.getWidth();
        height = map.getHeight();
        scale = map.getScale();
    }
    
    public
    WorldUtils(int width, int height, int scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;
    }
    
    
    /**
     * Get declination, returning degrees above or below the
     * equator for the given value of y.
     * 
     * @param y     Vertical hex, 0 = north pole, +ve = going south.
     * @return      Angle in degrees, +ve = north, -ve = south.
     */
    public double
    declination(int y) {
        double      dy = 0;

        // h is height of the map.
        // l is height of half the map (quarter circumference).
        double  l = height / 2.0;
        // Distance from the equater.
        dy = l - y;
        return dy / l * 90.0; 
    }
    
    /**
     * Return right ascension for X, given an X and Y coordinate.
     * @param x
     * @param y
     * @return
     */
    public double
    ra(int x, int y) {
        double      dy = declination(y);
        double      dx = 0;
        double      s = Math.sin(Math.toRadians(dy));
        
        //System.out.println(x+","+y+" : ");

        // Width of the world at this y.
        double      r = width * Math.cos(Math.toRadians(dy));
        //System.out.println("Radius "+r);

        // w is the width of the world at this point.
        dx = x - width / 2.0;
        //System.out.println("dx="+dx);

        return dx / (r / 4.0) * 90.0;
    }
    
}
