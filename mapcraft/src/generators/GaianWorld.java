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

import java.io.*;
import java.util.Properties;

import net.sourceforge.mapcraft.map.MapOutOfBoundsException;
import net.sourceforge.mapcraft.map.Terrain;
import net.sourceforge.mapcraft.map.TerrainSet;

/**
 * @author Samuel Penn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GaianWorld extends WorldGenerator {
    private static final int    OCEAN_BASE = 1;
    private static final int    OCEAN_NUMBER = 4;
    
    private static final int    LAND_BASE = 5;
    private static final int    LAND_NUMBER = 50;
    
    private static final int    ICE_BASE = 55;
    private static final int    ICE_NUMBER = 5;
    
    
    private int                 ocean = 70;
    private int                 islands = 10;
    private int                 humidity = 50;
    private int                 glacial = 10;
    private int                 tilt = 23;
    
    public
    GaianWorld(String name, int radius, int scale) {
        super(name, radius, scale);
        /*
        try {
            Properties  props = new Properties();
            props.load(new FileInputStream("/home/sam/gaian.properties"));
            setOcean(Integer.parseInt(props.getProperty("ocean", "70")));
            setIslands(Integer.parseInt(props.getProperty("islands", "10")));
            setHumidity(Integer.parseInt(props.getProperty("humidity", "50")));
            setGlacial(Integer.parseInt(props.getProperty("glacial", "10")));
            setTilt(Integer.parseInt(props.getProperty("tilt", "23")));
        } catch (Exception e) {
            
        }
        */
        System.out.println("GainWorld: ["+name+"] "+radius+"km @"+scale+"km");
    }
    
    public void
    setOcean(int percentage) {
        this.ocean = percentage;
    }
    
    public void
    setIslands(int number) {
        this.islands = number;
    }
    
    public void
    setHumidity(int percentage) {
        this.humidity = percentage;
    }
    
    public void
    setGlacial(int percentage) {
        this.glacial = percentage;
    }
    
    public void
    setTilt(int degrees) {
        this.tilt = degrees;
    }
    
    private int
    die(int size) {
        return (int)(Math.random()*size)+1;
    }
    
    /**
     * Setup details for an Earth-like world.
     */
    private void
    setupGaian() {
        TerrainSet      ts = map.getTerrainSet();
        
        log("setupGaian:");
        
        int     baseOcean = die(50)+die(50);
        int     baseOceanRed = baseOcean + die(20) - die(20);
        int     baseOceanGreen = baseOcean + die(20) - die(20);
        
        // Sort out ocean terrain types. Dark ocean is deeper ocean.
        for (int i=0; i < OCEAN_NUMBER; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(baseOceanRed + i*10, 
                                          baseOceanGreen + i*10, 
                                          150+i*25);
            ts.add((short)(OCEAN_BASE + i), tag, tag, colour);
        }
        
        int     landRed = 5 + die(10);
        int     landBlue = 5 + die(10);
        
        for (int i=0; i < LAND_NUMBER; i++) {
            String      tag = "land."+i;
            String      colour = toColour(250-i*landRed, 250-i*3, 100-i*landBlue);

            ts.add((short)(i+LAND_BASE), tag, tag, colour);
            
        }

        // Sort out glacial terrain types.
        for (int i=0; i < ICE_NUMBER; i++) {
            String      tag = "land."+i;
            String      colour = toColour(200+i*10, 200+i*10, 200+i*10);
            ts.add((short)(i+ICE_BASE), tag, tag, colour);
        }

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
        
        int     baseOcean = die(50)+die(50);
        int     baseOceanRed = baseOcean + die(40) - die(40);
        int     baseOceanGreen = baseOcean + die(40) - die(40);
        
        // Sort out ocean terrain types. Dark ocean is deeper ocean.
        for (int i=0; i < OCEAN_NUMBER; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(baseOceanRed + i*10, 
                                          baseOceanGreen + i*10, 
                                          150+i*25);
            ts.add((short)(OCEAN_BASE + i), tag, tag, colour);
        }
        
        int     landRed = 2 + die(8);
        int     landBlue = 5 + die(10);
        
        for (int i=0; i < LAND_NUMBER; i++) {
            String      tag = "land."+i;
            String      colour = toColour(250-i*landRed, 250-i*10, 100-i*landBlue);

            ts.add((short)(i+LAND_BASE), tag, tag, colour);
            
        }

        // Sort out glacial terrain types.
        for (int i=0; i < ICE_NUMBER; i++) {
            String      tag = "land."+i;
            String      colour = toColour(200+i*10, 200+i*10, 200+i*10);
            ts.add((short)(i+ICE_BASE), tag, tag, colour);
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
        d1 = (x0 - getLeft(y)) + (getRight(y) - x1);
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
        
        log("munge:");

        try {
            log("munge: Find altitude range");
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

            int     range = high - low;
            int[]   bucket = new int[range+1];
            int     count = 0;
            
            // Fill the bucket with count of number of each height.
            log("munge: Populate height distribution bucket");
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
//            for (int i=0; i < bucket.length; i++) {
//                System.out.println(i+": "+bucket[i]);
//            }
            int     NUM = 20;
            int     t = 0, b = 0;
            log("munge: Set bucket percentages");
            for (int i=0; i < NUM; i++) {
                int     l = (count * i)/NUM;
//                System.out.println(i+": "+l+"/"+bucket.length);
                while (t < l && b < bucket.length) {
                    t += bucket[b];
                    bucket[b++] = (i+1)*5;
                }
            }
            int     falseMax = 0;
            for (;b < bucket.length; b++) {
                bucket[b] = (int)(100 + Math.sqrt(falseMax++));
            }

            log("munge: Munge height map");
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
        
        log("landscape:");

        // Work out some random data points.
        for (int i=0; i < NUM; i++) {
            yp[i] = (int)(map.getHeight() * Math.random());
            int min = getLeft(yp[i]);
            int max = getRight(yp[i]);
            xp[i] = min + (int) ((max - min) * Math.random());
            
            hp[i] = (int) (Math.random() * 100);
        }

        log("landscape: Work out height of terrain");
        int     count = 1;
        for (int y=0; y < map.getHeight(); y++) {
            int     left = getLeft(y);
            int     right = getRight(y);
            
            if (y >= (count * map.getHeight()/10)) {
                log("landscape: "+count*10+"% complete");
                count++;
            }

            for (int x = left; x <= right; x++) {
                int         total = 0;
                for (int i=0; i < NUM; i++) {
                    int         dx = shortestPath(y, x, xp[i]);
                    int         dy = y - yp[i];                     
                    int         d = (int)(Math.sqrt(dx*dx + dy*dy));
                    total += hp[i] / (d+1);
                }
                try {
                    map.setHeight(x, y, (short) (total+Math.random()*islands));
                } catch (MapOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
        log("landscape: Finished");
    }

    private short
    getOceanTerrain(int x, int y) throws MapOutOfBoundsException {
        short       t = OCEAN_BASE;
        short       h = map.getHeight(x, y);
        int         latitude = y;

        if (y > map.getHeight()/2) {
            latitude = map.getHeight() - y;
        }
        latitude = (int)(90 * (latitude/(map.getHeight()/2.0)));
        
        if (h > ocean-5) {
            t = OCEAN_BASE+4;
        } else if (h > ocean-10) {
            t = OCEAN_BASE+3;
        } else if (h > ocean-15) {
            t = OCEAN_BASE+2;
        } else if (h > ocean-20) {
            t = OCEAN_BASE+1;
        } else {
            t = OCEAN_BASE;
        }
        
        if (latitude < glacial/2) {
            t = ICE_BASE + ICE_NUMBER - 1;
        }
        
        if ((latitude + Math.random()*5)< glacial) {
            Terrain     f = map.getFeatureSet().getTerrain("ice");
            if (f != null) {
                map.setFeature(x, y, f.getId());
            }
        }
        
        return t;
    }
    
    /**
     * Get the fertility of a given latitude, based on the world parameters.
     * Fertility is affected by temperature, humidity and axial tilt.
     * Tropical regions should tend towards low fertility, temperate and
     * equatorial towards high fertility.
     * 
     * @param latitude      Positive latitude, in degrees from the equator.
     * 
     * @return              Fertility, +ve is good, -ve is poor.
     */
    private int
    getBaseFertility(int latitude) {
        int     fertility = 0;
        // General fertility goes up if conditions are humid.
        fertility += (int)(Math.sqrt(humidity-50));
        // General fertility goes down for cold worlds.
        if (glacial > 10) {
            fertility -= (int)(Math.sqrt(glacial-10));
        }
        // More fertile towards the equator.
        fertility += (int)(Math.sqrt(90 - latitude));
        
        // Less fertile at the tropics, where deserts are possible.

        int     tropics = Math.abs((tilt - latitude)/2) + 1;
        if (tropics < (70-humidity)) {
            fertility -= (70-humidity)/tropics;
        }

        return fertility * 5;
    }
    
    private short
    getLandTerrain(int x, int y) throws MapOutOfBoundsException {
        short       t = LAND_BASE;
        int         roll = (int)(Math.random()*5);
        int         latitude = y;
        
        if (y > map.getHeight()/2) {
            latitude = map.getHeight() - y;
        }
        latitude = (int)(90 * (latitude/(map.getHeight()/2.0)));
        // Now make sure latitude is 0 for equator, 90 for pole.
        latitude = (90 - latitude);
        
        try {
            int     heightMod = 0;
            if (map.getHeight(x, y) < ocean+5) {
                heightMod = 0;
            } else if (map.getHeight(x, y) < ocean+10) {
                heightMod = 1;
            } else if (map.getHeight(x, y) < ocean+15) {
                heightMod = 2;
            } else if (map.getHeight(x, y) < ocean+25) {
                heightMod = 3;
            } else {
                heightMod = 4;
            }
            
            if (latitude + roll > (90 - glacial)) {
                t = (short)(ICE_BASE + heightMod);
            } else {
                roll = (int)(Math.random()*20);
                int     fertility = getBaseFertility(latitude) + roll;
                
                switch (heightMod) {
                case 0:
                    fertility += 0;
                    break;
                case 1:
                    fertility += 5;
                    break;
                case 2:
                    fertility += 10;
                    break;
                case 3:
                    fertility += 0;
                    break;
                case 4:
                    fertility -= 5;
                    break;
                }
                
                if (fertility < 0) fertility = 0;
                if (fertility > 49) fertility = 49;
                
                t = (short)(LAND_BASE + fertility);
            }
    
            if (map.getHeight(x, y) > 105) {
                map.setFeature(x, y, map.getFeatureSet().getTerrain("highmnts").getId());
            } else if (map.getHeight(x, y) > 100) {
                map.setFeature(x, y, map.getFeatureSet().getTerrain("lowmnts").getId());
            }
        } catch (Exception e) {
            
        }
        return t;
    }
    
    protected void
    colour() {
        try {
            int     iceLine = (int)(map.getHeight() * (glacial/100.0));

            for (int x=0; x < map.getWidth(); x++) {
                for (int y=0; y < map.getHeight(); y++) {
                    if (map.getTerrain(0, x, y) != 0) {
                        if (map.getHeight(x, y) <= ocean) {
                            map.setTerrain(x, y, getOceanTerrain(x, y));
                        } else {
                            map.setTerrain(x, y, getLandTerrain(x, y));
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
        colour();
    }
    
    protected void
    generateProtoGaian() {
        setHumidity(0);
        landscape();
        munge();
        colour();
    }

}
