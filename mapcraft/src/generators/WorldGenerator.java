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

import net.sourceforge.mapcraft.map.*;

/**
 * Randomly generate a complete world.
 * 
 * @author Samuel Penn
 */
public class WorldGenerator {
    private Map         map = null;
    
    public
    WorldGenerator(String name, int radius) {
        try {
            map = new Map(name, radius, 50);
            map.loadTerrainSet("terrain/celestia.xml");
            map.setImageDir("hexagonal/world");
            
            // Default to totally random map.
            //randomise(1, 16);
            //heightMap();
            randomHeight();
            colourByHeight(1, 16);
            
            map.save("/tmp/"+name+".map");
            
            
        } catch (MapException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void
    saveImage() {
        //MapImage        image = new MapImage();
    }
    
    /**
     * Randomise all terrain between the values given. The
     * algorithm used is totally random, so will give a
     * speckled map with no structure.
     * 
     * @param min   Low value for terrain id (inclusive).
     * @param max   High value for terrain id (inclusive).
     */
    protected void
    randomise(int min, int max) {
        for (int x=0; x < map.getWidth(); x++) {
            for (int y=0; y < map.getHeight(); y++) {
                short   t = (short)(min + Math.random()*(1+max-min));
                try {
                    if (map.getTerrain(0, x, y) != 0) {
                        map.setTerrain(x, y, t);
                    }
                } catch (MapOutOfBoundsException e) {
                    
                }
            }
        }
    }
    
    protected void
    colourByHeight(int min, int max) {
        short   low = 30000;
        short   high = -30000;
        
        try {
            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (map.getTerrain(0, x, y) != 0) {
                        if (map.getHeight(x, y) < low) {
                            low = map.getHeight(x, y);
                        }
                        if (map.getHeight(x, y) > high) {
                            high = map.getHeight(x, y);
                        }
                    }
                }
            }
            
            System.out.println("Lowest elevation = "+low);
            System.out.println("Highest elevation = "+high);
            
            int     range = max - min;
            int     delta = (high - low) / range;
            
            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (map.getTerrain(0, x, y) != 0) {
                        short   h = (short)(map.getHeight(x, y) - low);
                        short   t = (short) (min + (h / delta));
                        map.setTerrain(x, y, t);
                    }
                }
            }
            
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
        
    }
    
    private boolean
    isValid(int x, int y) {
        try {
            if (map.getTerrain(0, x, y) == 0) {
                return false;
            }
        } catch (MapOutOfBoundsException e) {
            return false;
        }
        
        return true;
    }
    
    protected void
    heightMap() {
        short     height = 1000;
        for (int y=0; y < map.getHeight(); y++) {
            int     left = -1;
            for (int x=0; x < map.getWidth(); x++) {
                try {
                    int     counted = 0;
                    int     total = 0;
                    
                    if (map.getTerrain(0, x, y) == 0) {
                        // Unwritable tile.
                        continue;
                    }
                    if (left == -1) left = x;
                    
                    if (isValid(x-1, y)) {
                        total += map.getHeight(x-1, y);
                        counted++;
                    }
                    
                    if (isValid(x, y-1)) {
                        total += map.getHeight(x, y-1);
                        counted++;
                    }
                    if (x%2 == 0) {
                        if (isValid(x+1, y-1)) {
                            total += map.getHeight(x+1, y-1);
                            counted++;
                        }
                        if (isValid(x-1, y-1)) {
                            total += map.getHeight(x-1, y-1);
                            counted++;
                        }
                    }

                    if (counted > 0) {
                        height = (short) (0.5 + (total / counted));
                        height += (short)(0.5+(Math.random()-Math.random())*20);
                    }
                    
                    map.setHeight(x, y, height);
                } catch (MapOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void
    randomHeight() {
        int     height = 1000;
        try {
            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (isValid(x, y)) {
                        double h = Math.random() - Math.random();
                        h *= 100;
                        map.setHeight(x, y, (short)(height + h));
                    }
                }
            }
            
            // Random asteroid impacts.
            for (int a=0; a < 100; a++) {
                int     x = (int)(Math.random() * map.getWidth());
                int     y = (int)(Math.random() * map.getHeight());
                int     r = (int)(Math.random() * 30) + 15;
                int     h = (int)(Math.random() * 100) + 50;
                
                //fill(x, y, map.getHeight(x, y), 20);
                System.out.println("Filled "+fill(x, y,
                                map.getHeight(x, y), 100));
                unsetMap();
                //raise(x, y, r+10, h/3);
                //raise(x, y, r, -h);
            }
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    protected void
    unsetMap() {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y=0; y < map.getHeight(); y++) {
                try {
                    map.setHighlighted(x, y, false);
                } catch (MapOutOfBoundsException e) {
                    // Don't care.
                }
            }
        }
    }
    
    protected int
    fill(int x, int y, int height, int threshold) {
        int     count = 0;
        try {
            if (isValid(x, y) && !map.isHighlighted(x, y)) {
                map.setHighlighted(x, y, true);
                if (Math.abs(height - map.getHeight(x, y)) > threshold) {
                    return 0;
                }
                int h = (height + map.getHeight(x, y)) / 2;
                map.setHeight(x, y, (short)h);
                //height = h;
                count += fill(x, y-1, height, threshold-1);
                count += fill(x, y+1, height, threshold-1);
                count += fill(x-1, y, height, threshold-1);
                count += fill(x+1, y, height, threshold-1);
                int     a = -1;
                if (x%2 == 1) a = +1;
                count += fill(x-1, y+a, height, threshold-1);
                count += fill(x+1, y+a, height, threshold-1);
                count += 1;
            }
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    protected void
    setHeight(int x, int y, int height) {
        try {
            map.setHeight(x, y, (short) height);
        } catch (MapOutOfBoundsException e) {
            System.out.println("Failed to set height at ["+x+","+y+"]");
        }
    }

    protected void
    raise(int x, int y, int height) {
        try {
            short       h = map.getHeight(x, y);
            h += (short) height;
            map.setHeight(x, y, h);
        } catch (MapOutOfBoundsException e) {
            System.out.println("Failed to raise height at ["+x+","+y+"]");
        }
    }
    
    /**
     * Raise or lower a region of the map by a specified height.
     * 
     * @param x
     * @param y
     * @param radius
     * @param height
     */
    protected void
    raise(int x0, int y0, int radius, int height) {
        int     minX = x0 - radius;
        int     minY = y0 - radius;
        int     maxX = x0 + radius;
        int     maxY = y0 + radius;
        
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isValid(x, y)) {
                    if (sphericalDistance(x, y, x0, y0) <= radius) {
                        raise(x, y, height);
                    }
                }
            }
        }
    }
    
    /**
     * Work out the real distance between two points. The distance
     * is in hexes, but 'as the crow flies', which is different
     * from the result returned by map.distance(). This should give
     * a more circular result than the latter.
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    protected int
    distance(int x0, int y0, int x1, int y1) {
        int     d = 0;
        
        d = (int) Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
        
        return d;
    }
    
    /**
     * The distance between two points on the surface of a sphere.
     * This tries to work out what the distance will be once the
     * map has been wrapped around a sphere. Equatorial distances
     * will remain similar, but polar distances will differ greatly.
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    protected int
    sphericalDistance(int x0, int y0, int x1, int y1) {
        int     d = 0;
        int     dx = 0, dy = 0;
        int     midx = map.getWidth() / 2;
        int     midy = map.getHeight() / 2;
        double  px0, py0, px1, py1;
        
        dy = y0 - y1;
        
        x0 -= midx;
        x1 -= midx;
        
        y0 -= midy;
        y1 -= midy;
        
        y0 = (int)Math.abs(90 * (map.getHeight()/(2.0 * y0)));
        y1 = (int)Math.abs(90 * (map.getHeight()/(2.0 * y1)));
        
        x0 *= Math.sin(Math.toRadians(y0));
        x1 *= Math.sin(Math.toRadians(y1));
        
        d = (int)Math.sqrt((x1-x0) * (x1-x0) + (dy*dy));
        
        return d;
    }
    
    public static void
    main(String[] args) {
        WorldGenerator  generator = null;
        
        generator = new WorldGenerator("foo", 2500);
    }
}
