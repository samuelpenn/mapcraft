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

import net.sourceforge.mapcraft.map.MapOutOfBoundsException;
import net.sourceforge.mapcraft.map.TerrainSet;

/**
 * @author Samuel Penn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GaianWorld extends WorldGenerator {
    private static final int    OCEAN_BASE = 1;
    private static final int    OCEAN_NUMBER = 10;
    
    private static final int    LAND_BASE = 11;
    private static final int    LAND_NUMBER = 20;
    
    private int                 sea = 70;
    
    public
    GaianWorld(String name, int radius, int scale) {
        super(name, radius, scale);
    }
    
    /**
     * Setup details for an Earth-like world.
     */
    private void
    setupGaian() {
        TerrainSet      ts = map.getTerrainSet();
        
        log("setupGaian:");
        
        for (int i=0; i < OCEAN_NUMBER; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(50+i*5, 50+i*5, 150+i*5);
            ts.add((short)(OCEAN_BASE + i), tag, tag, colour);
        }
        
        for (int i=0; i < LAND_NUMBER; i++) {
            String      tag = "land."+i;
            String      colour = toColour(50+i*3, 100+i*5, 50+i*4);
            ts.add((short)(i+LAND_BASE), tag, tag, colour);
        }
        
        log("setupGaian: Terrain size "+ts.size());

        return;
    }
    
    /**
     * Setup details for a Proto-Earth world. These have land
     * masses and an ocean, but no land life. Atmosphere may be
     * primitive, and the temperature is probably warm.
     */
    private void
    setupProtoGaian() {
        TerrainSet      ts = map.getTerrainSet();
        
        log("setupProtoGaian:");
        
        for (int i=1; i < 17; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(50+i*5, 50+i*5, 150+i*5);
            ts.add((short)(i), tag, tag, colour);
        }
        
        for (int i=1; i < 17; i++) {
            String      tag = "land."+i;
            String      colour = toColour(100+i*5, 100+i*5, 50+i*4);
            ts.add((short)(i+16), tag, tag, colour);
        }
        return;
    }

    /**
     * Define the type of Gaian world that will be generated.
     */
    public void
    setWorldType(int worldType) {
        this.worldType = worldType;
        
        switch (worldType) {
        case GAIAN:
            setupGaian();
            break;
        case PROTOGAIAN:
            setupProtoGaian();
            break;
        }

    }
    
    public void
    generate() {
        switch (worldType) {
        case GAIAN:
            generateGaian();
            break;
        case PROTOGAIAN:
            generateProtoGaian();
            break;
        }
    }
    
    private int
    shortestPath(int y, int x0, int x1) {
        int     d = 0;
        int     d1 = 0;
        int     x = 0;
        
        if (x0 == x1) {
            return 0;
        } else if (x1 < x0) {
            x = x1;
            x1 = x0;
            x0 = x;
        }
        // x0 is now always to the left of x1 (i.e. it is smaller).
        
        // Simple distance.
        d = x1 - x0;
        
        // Wrap-around distance.
        d1 = x0 + (getRight(y) - x1);
        if (d1 < d) d = d1;
                
        return d;
    }
    
    /**
     * Change all heights into a scale from 0 - 10. Then work out what
     * percentage of the surface is that height or lower.
     */
    private void
    munge() {
        short   low = 30000;
        short   high = -30000;
        short   min = 0;
        short   max = 10;
        
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
            
            int     range = high - low;
            int[]   bucket = new int[range+1];
            int     count = 0;
            System.out.println("Bucket size = "+bucket.length);
            
            // Fill the bucket with count of number of each height.
            for (int y=0; y < map.getHeight(); y++) {
                int     left = getLeft(y);
                int     right = getRight(y);
                
                for (int x=left; x <= right; x++) {
                    int h = map.getHeight(x, y) - low;
                    bucket[h] = bucket[h] + 1;
                    map.setHeight(x, y, (short)h);
                    count++;
                }
            }
            for (int i=0; i < bucket.length; i++) {
                System.out.println(i+": "+bucket[i]);
            }
            int     NUM = 20;
            int     t = 0, b = 0;
            for (int i=0; i < NUM; i++) {
                int     l = (count * i)/NUM;
                System.out.println(i+": "+l+"/"+bucket.length);
                while (t < l) {
                    t += bucket[b];
                    bucket[b++] = (i+1)*5;
                }
            }
            for (;b < bucket.length; b++) {
                bucket[b] = 100;
            }
            for (int i=0; i < bucket.length; i++) {
                System.out.println(i+": "+bucket[i]);
            }

            for (int y=0; y < map.getHeight(); y++) {
                int     left = getLeft(y);
                int     right = getRight(y);
                
                for (int x=left; x <= right; x++) {
                    int h = map.getHeight(x, y);
                    map.setHeight(x, y, (short)bucket[h]);
                }
            }
            
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
       
    }
    
    /**
     * Create basic landscape, working out heights of each tile according
     * to landmasses etc.
     */
    private void
    landscape() {
        short       height = 100;
        int         NUM = 20;
        int[]       hp = new int[NUM];
        int[]       xp = new int[NUM];
        int[]       yp = new int[NUM];

        // Work out some random data points.
        for (int i=0; i < NUM; i++) {
            yp[i] = (int)(map.getHeight() * Math.random());
            int min = getLeft(yp[i]);
            int max = getRight(yp[i]);
            xp[i] = min + (int) ((max - min) * Math.random());
            
            hp[i] = (int) (Math.random() * 100);
        }
            
        for (int y=0; y < map.getHeight(); y++) {
            
            int     left = getLeft(y);
            int     right = getRight(y);

            for (int x = left; x <= right; x++) {
                int         total = 0;
                for (int i=0; i < NUM; i++) {
                    int         dx = shortestPath(y, x, xp[i]);
                    int         dy = y - yp[i];                     
                    int         d = (int)(Math.sqrt(dx*dx + dy*dy));
                    total += hp[i] / (d+1);
                }
                try {
                    if (total > 100) {
                        total = 100;
                    }
                    map.setHeight(x, y, (short) (total+Math.random()*10));
                } catch (MapOutOfBoundsException e) {
                    
                }
            }
        }        
    }
    
    protected void
    colour(int oceanPercentage) {
        try {
            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (map.getTerrain(0, x, y) != 0) {
                        if (map.getHeight(x, y) <= oceanPercentage) {
                            map.setTerrain(x, y, (short)OCEAN_BASE);
                        } else {
                            map.setTerrain(x, y, (short)LAND_BASE);
                        }
                    }
                }
            }            
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
        }
        
    }
    
    protected void
    generateGaian() {
        landscape();
        munge();
        colour(70);
    }
    
    protected void
    generateProtoGaian() {
        heightMap();
        colourByHeight(1, 31);
    }

}
