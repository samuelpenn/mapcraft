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
public abstract class WorldGenerator {
    protected Map         map = null;
    protected WorldUtils  utils = null;
    
    protected int         GREY = 1;
    protected int         YELLOW = 16;
    protected int         RED = 32;
    
    // Types of worlds.
    public static final int    SELENIAN = 1;
    public static final int    HERMIAN = 2;
    public static final int    AREAN = 3;
    public static final int    VENUSIAN = 4;
    public static final int    PELAGIC = 5;
    
    public static final int    JOVIAN = 10;
    public static final int    CRYOJOVIAN = 11;
    public static final int    SUPERJOVIAN = 12;
    public static final int    MACROJOVIAN = 13;
    
    public static final int    GAIAN = 20;
    public static final int    PROTOGAIAN = 21;

    protected int         worldType = SELENIAN;
    
    protected void
    log(String message) {
        System.out.println(message);
    }
    
    protected String
    toColour(int c) {
        if (c < 0) c = 0;
        if (c > 255) c = 255;
        
        String      string = Integer.toHexString(c);
        
        if (string.length() < 2) {
            string = "0"+string;
        }
        return string;
    }
    
    protected String
    toColour(int r, int g, int b) {
        String  red = toColour(r); 
        String  green = toColour(g);
        String  blue = toColour(b);
        
        return "#"+red+green+blue;
    }
    
    public
    WorldGenerator(String name, int radius, int scale) {
        try {
            map = new Map(name, radius, scale);
            map.loadTerrainSet("terrain/celestia.xml");
            map.setImageDir("hexagonal/world");
            
            utils = new WorldUtils(map);

            worldType = 0;
        } catch (MapException e) {
            e.printStackTrace();
        }
    }
    
    public abstract void
    setWorldType(int worldType);
    
    public void
    save(String filename) throws java.io.IOException {
        map.save(filename);
    }
    
    /**
     * Get the left (western) edge of the map at this row.
     * 
     * @param y     Row of the map to find the left edge for.
     * @return      X coordinate of left edge.
     */
    protected int
    getLeft(int y) {
        int     left = -1;
        
        for (int x=0; x < map.getWidth(); x++) {
            if (isValid(x, y)) {
                left = x;
                break;
            }
        }
        
        return left;
    }
    
    /**
     * Get the right (eastern) edge of the map for this y.
     * @param y     Row of the map to find the right edge for.
     * @return      X coordinate of right edge.
     */
    protected int
    getRight(int y) {
        int     left = -1, right = -1;
        
        for (int x=0; x < map.getWidth(); x++) {
            if (isValid(x, y) && left == -1) {
                left = x;
            }
            if (left > -1 && !isValid(x, y)) {
                right = x-1;
                break;
            }
        }
        if (right == -1) right = map.getWidth()-1;
        
        return right;
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
    
    protected boolean
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
    
    /**
     * Generates a random height map for the world, trying to
     * smooth out changes in the height. Any given tile will tend
     * to have a similar height to surrounding tiles.
     */
    protected void
    heightMap() {
        short     height = 1000;
        for (int y=0; y < map.getHeight(); y++) {
            int     left = -1, right = map.getWidth();
            for (int x = 0; x < map.getWidth(); x++) {
                if (isValid(x, y) && left == -1) {
                    left = x;
                }
                if (!isValid(x, y) && left > -1) {
                    right = x;
                    break;
                }
            }

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

                    // Generate random height, based on surrounding tiles.
                    if (counted > 0) {
                        height = (short) (0.5 + ((1.0 * total) / (1.0 * counted)));
                        height += (short)(0.5+(Math.random()-Math.random())*20);
                    }
                    
                    // If we're close to the eastern edge, take into
                    // account the west edge so we don't get mismatch.
                    int     rd = right - x;
                    height -= (height - map.getHeight(left, y))/rd;
                    
                    if (Math.random()<0.1) {
                        height += Math.random()*20;
                    }
                    
                    map.setHeight(x, y, height);
                } catch (MapOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Generate a random height map for the world. Each tile is
     * considered independently of the surrounding tiles, so it
     * is truly without structure, and not realistic.
     */
    protected void
    randomHeight(int height, int variance) {
        try {
            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (isValid(x, y)) {
                        double h = Math.random() - Math.random();
                        h *= variance;
                        map.setHeight(x, y, (short)(height + h));
                    }
                }
            }
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Must be called after a fill operation. Unsets any
     * highlight flags on the tiles, these are used to cope
     * with the recursive nature of the fill algorithm.
     */
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
    
    /**
     * Fill part of the map, setting to a given height. The fill
     * continues until a height difference greater than threshold
     * is found. The current height of the tile is averaged with
     * the specified height. The method is recursive, and the
     * threshold is reduced on each recursive step.
     * 
     * unsetMap() should be called after a call to this method.
     * 
     * @param x             X coordinate to start at.
     * @param y             Y coordinate to start at.
     * @param height        Height to set to.
     * @param threshold     Maximum difference in height.
     * 
     * @return              Count of number of tiles changed.
     */
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
            } else if (!map.isHighlighted(x, y)) {
                int     left = getLeft(y);
                int     right = getRight(y);
                map.setHighlighted(x, y, true);
                if (x < left) {
                    count += fill(right, y, height, threshold);
                } else if (x > right) {
                    count += fill(left, y, height, threshold);
                }
            }
        } catch (MapOutOfBoundsException e) {
            // Ignore.
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
    
    
    public void
    crater(int ox, int oy, int r, int depth) throws MapOutOfBoundsException {
        int     height =  map.getHeight(ox, oy) - depth;

        for (int y=0; y < map.getHeight(); y++) {
            for (int x=0; x < map.getWidth(); x++) {
                if (isValid(x, y)) {
                    if (sphericalDistance(ox, oy, x, y) < r) {
                        //map.setHeight(x, y, (short)height);
                        raise(x, y, -depth);
                        map.setFeature(x, y, (short)2);
                    }
                }
            }
        }
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
        double  dx = 0, dy = 0;
        int     midx = map.getWidth() / 2;
        int     midy = map.getHeight() / 2;
        double  px0, py0, px1, py1;
        
        dy = utils.declination(y0) - utils.declination(y1);
        dx = utils.ra(x0, y0) - utils.ra(x1, y1); 
        
        
        d = (int)Math.sqrt((dx*dx) + (dy*dy));
        
        return d;
    }
    
    public void
    generate() {
        heightMap();
    }
    
    public static void
    main(String[] args) throws Exception {
        WorldGenerator  generator = null;
        WorldUtils      utils = new WorldUtils(314, 157, 75);
  /*      
        System.out.println(utils.declination(50));
        System.out.println(utils.declination(78));
        System.out.println(utils.declination(100));
        System.out.println(utils.declination(150));
        
        System.out.println(utils.ra(150, 78));
        System.out.println(utils.ra(150, 5));
        System.out.println(utils.ra(150, 150));
*/        

        generator = new GaianWorld("foo", 6500, 50);
        //generator = new TerrestrialWorld("foo", 2500, 50);
        //generator = new JovianWorld("foo", 2500, 50);
        //generator.setWorldType(SELENIAN);
        //generator.setWorldType(HERMIAN);
        //generator.setWorldType(AREAN);
        //generator.setWorldType(JOVIAN);
        //generator.setWorldType(PELAGIC);
        //generator.setWorldType(GAIAN);
        generator.setWorldType(PROTOGAIAN);
        generator.generate();
        
        System.out.println(generator.map.getTerrainSet().size());
        
        //generator.randomHeight();
        //generator.colourByHeight(1, 16);
        
        generator.save("/tmp/foo.map");

    }
}
