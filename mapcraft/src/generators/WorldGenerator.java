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
    private WorldUtils  utils = null;
    
    private int         GREY = 1;
    private int         YELLOW = 16;
    private int         RED = 32;
    
    // Types of worlds.
    private static final int    SELENIAN = 1;
    private static final int    HERMIAN = 2;
    private static final int    AREAN = 3;
    
    private int         worldType = SELENIAN;
    
    private String
    toColour(int c) {
        if (c < 0) c = 0;
        if (c > 255) c = 255;
        
        String      string = Integer.toHexString(c);
        
        if (string.length() < 2) {
            string = "0"+string;
        }
        return string;
    }
    
    private String
    toColour(int r, int g, int b) {
        String  red = toColour(r); 
        String  green = toColour(g);
        String  blue = toColour(b);
        
        return "#"+red+green+blue;
    }
    
    public
    WorldGenerator(String name, int radius) {
        try {
            map = new Map(name, radius, 50);
            map.loadTerrainSet("terrain/celestia.xml");
            map.setImageDir("hexagonal/world");
            
            utils = new WorldUtils(map);
            // Default to totally random map.
            //randomise(1, 16);
            //heightMap();
            setWorldType(HERMIAN);
        } catch (MapException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Define the terrain types for Moon-like Selenian worlds.
     * These tend to consist of greys, with no real colour.
     */
    private void
    setupSelenian() {
        TerrainSet      ts = map.getTerrainSet();

        for (int i = 1; i < 17; i++) {
            String      tag = "grey."+i;
            int         c = 50 + i*12;
            String      colour = toColour(c, c, c);
            ts.add((short)(GREY+i-1), tag, tag, colour);
        }
    }
    
    /**
     * Define the terrain types for Mercury-like Hermian worlds.
     * These tend to have reddish orange uplands with yellow or
     * white plains.
     */
    private void
    setupHermian() {
        TerrainSet      ts = map.getTerrainSet();
        
        for (int i = 1; i < 17; i++) {
            String      tag = "hermian."+i;
            String      colour = toColour(i*15, i*(i-1), 2*i*(i-8));
            colour = toColour(255 - (i*12), 255 - (i*17), 200 - (i*25));
            ts.add((short)(GREY+i-1), tag, tag, colour);
        }
    }
    
    /**
     * Setup Mars-like worlds.
     */
    private void
    setupArean() {
        TerrainSet      ts = map.getTerrainSet();

        for (int i = 1; i < 17; i++) {
            String      tag = "red."+i;
            String      colour = toColour(i*12, 0, 0);
            ts.add((short)(RED+i-1), tag, tag, colour);
        }

        for (int i = 1; i < 17; i++) {
            String      tag = "yellow."+i;
            String      colour = toColour(i*12, i*15, 0);
            ts.add((short)(YELLOW+i-1), tag, tag, colour);
        }
    }
    
    public void
    setWorldType(int worldType) {
        this.worldType = worldType;
        switch (worldType) {
        case SELENIAN:
            setupSelenian();
            break;
        case HERMIAN:
            setupHermian();
            break;
        case AREAN:
            setupArean();
            break;
        }
    }
    
    protected void
    save(String filename) throws java.io.IOException {
        map.save(filename);
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
            
            // Random fills impacts.
            for (int a=0; a < 100; a++) {
                int     x = (int)(Math.random() * map.getWidth());
                int     y = (int)(Math.random() * map.getHeight());
                int     r = (int)(Math.random() * 30) + 15;
                int     h = (int)(Math.random() * 100) + 50;
                
                //fill(x, y, map.getHeight(x, y), 20);
                System.out.println("Filled "+fill(x, y,
                                map.getHeight(x, y), 120));
                unsetMap();
                //raise(x, y, r+10, h/3);
                //raise(x, y, r, -h);
            }
            // Random asteroid impacts.
            for (int a=0; a < 100; a++) {
                int     x = (int)(Math.random() * map.getWidth());
                int     y = (int)(Math.random() * map.getHeight());
                int     r = (int)(Math.random() * 30) + 5;
                int     h = (int)(Math.random() * 30) + 10;
                
                crater(x, y, r, h);
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
    
    
    public void
    crater(int ox, int oy, int r, int depth) {
        for (int y=0; y < map.getHeight(); y++) {
            for (int x=0; x < map.getWidth(); x++) {
                if (isValid(x, y)) {
                    if (sphericalDistance(ox, oy, x, y) < r) {
                        raise(x, y, -depth);
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
    
    public static void
    main(String[] args) throws Exception {
        WorldGenerator  generator = null;
        WorldUtils      utils = new WorldUtils(314, 157, 50);
  /*      
        System.out.println(utils.declination(50));
        System.out.println(utils.declination(78));
        System.out.println(utils.declination(100));
        System.out.println(utils.declination(150));
        
        System.out.println(utils.ra(150, 78));
        System.out.println(utils.ra(150, 5));
        System.out.println(utils.ra(150, 150));
*/        

        generator = new WorldGenerator("foo", 2500);
        
        generator.randomHeight();
        generator.colourByHeight(1, 16);
        
        generator.save("/tmp/foo.map");

    }
}
