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
            map = new Map(name, radius, 25);
            map.loadTerrainSet("terrain/celestia.xml");
            map.setImageDir("hexagonal/world");
            
            // Default to totally random map.
            randomise(1, 16);
            
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
     * Randomise all terrain between the values given.
     * @param min
     * @param max
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
    heightMap() {
        short     height = 100;
        for (int x=0; x < map.getWidth(); x++) {
            for (int y=0; y < map.getHeight(); y++) {
                try {
                    int     counted = 0;
                    int     total = 0;
                    
                    if (map.getTerrain(0, x, y) == 0) {
                        // Unwritable tile.
                        continue;
                    }
                    
                    if (x > 0 && map.getTerrain(0, x-1, y) > 0) {
                        counted ++;
                        total += map.getHeight(x-1, y);
                    }
                    
                    map.setHeight(x, y, height);
                } catch (MapOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void
    main(String[] args) {
        WorldGenerator  generator = null;
        
        generator = new WorldGenerator("foo", 2500);
    }
}
