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
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TerrestrialWorld extends WorldGenerator {
    public
    TerrestrialWorld(String name, int radius) {
        super(name, radius);
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
            String      colour = toColour(i*12, i*6, 0);
            ts.add((short)(GREY+i-1), tag, tag, colour);
        }

        for (int i = 1; i < 17; i++) {
            String      tag = "yellow."+i;
            String      colour = toColour(i*12, i*15, 0);
            ts.add((short)(GREY+16+i-1), tag, tag, colour);
        }
    }
    
    /**
     * Set the type of the world. Possible values are SELENIAN,
     * HERMIAN and AREAN.
     */
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
    
    /**
     * Generate surface map for this world. How the map is generated
     * depends on the type of world set with setWorldType().
     */
    public void
    generate() {
        switch (worldType) {
        case SELENIAN:
            generateSelenian();
            break;
        case HERMIAN:
            generateHermian();
            break;
        case AREAN:
            generateArean();
            break;
        }        
    }
    
    /**
     * Generate a heavily cratered Selenian world. Lots of impact
     * craters of varying sizes. Later craters will be smaller than
     * the older craters.
     */
    protected void
    generateSelenian() {
        System.out.println("Selenian:");
        // Start with a very random height map.
        randomHeight(1000, 100);
        
        // Random fills impacts.
        System.out.println("Selenian: Generating flood plains");
        for (int a=0; a < 100; a++) {
            int     y = (int)(Math.random() * map.getHeight());

            int     left = getLeft(y);
            int     right = getRight(y);
            
            int     x = left + (int)(Math.random() * (right - left));
            int     r = (int)(Math.random() * 30) + 15;
            int     h = (int)(Math.random() * 100) + 50;

            try {
                fill(x, y, map.getHeight(x, y), 120);
            } catch (MapOutOfBoundsException e) {
                e.printStackTrace();
            }
            unsetMap();
            //raise(x, y, r+10, h/3);
            //raise(x, y, r, -h);
        }
        // Random asteroid impacts.
        System.out.println("Selenian: Generating impact craters");
        for (int a=0; a < 100; a++) {
            int     y = (int)(Math.random() * map.getHeight());
            int     left = getLeft(y)+10;
            int     right = getRight(y)-10;
            if (left >= right) continue;
            int     x = left + (int)(Math.random() * (right - left));
            int     r = (int)(Math.random() * 10) + 3;
            int     h = (int)(Math.random() * 20) + 20;
            
            try {
                if (isValid(x,y)) {
                    crater(x, y, r, h);
                }
            } catch (MapOutOfBoundsException e) {
                // Ignore.
            }
        }
        
        // Now set hills and mountains.
        for (int y=0; y < map.getHeight(); y++) {
            for (int x=0; x < map.getWidth(); x++) {
                if (isValid(x, y)) {
                    try {
                        if (map.getHeight(x, y) > 1100) {
                            map.setFeature(x, y, (short)5);
                        } else if (map.getHeight(x, y) > 1070) {
                            map.setFeature(x, y, (short)4);
                        } else if (map.getHeight(x, y) > 1040) {
                            map.setFeature(x, y, (short)3);
                        }
                    } catch (MapOutOfBoundsException e) {
                        // Ignore.
                    }
                }
            }
        }
        applySpecialEffects();        
        colourByHeight(1, 16);
    }
    
    /**
     * Generate a Mercury-like world. Desolate like Selenian worlds,
     * but more colourful.
     */
    protected void
    generateHermian() {
        heightMap();
        
        for (int a=0; a < 200; a++) {
            int     y = (int)(Math.random() * map.getHeight());
            int     left = getLeft(y)+10;
            int     right = getRight(y)-10;
            if (left >= right) continue;
            int     x = left + (int)(Math.random() * (right - left));
            int     r = (int)(Math.random() * 5) + 3;
            int     h = (int)(Math.random() * 20) + 20;
            
            try {
                if (isValid(x,y)) {
                    crater(x, y, r, h);
                }
            } catch (MapOutOfBoundsException e) {
                // Ignore.
            }
        }
        applySpecialEffects();
        colourByHeight(1, 16);
    }
    
    /**
     * Generate a Mars-like world.
     */
    protected void
    generateArean() {
        heightMap();
        colourByHeight(1, 16);
    }
    
    /**
     * Apply any special effects. Special effects may be randomly
     * applied to a world. Normally at most one will be applied.
     * Special effects are unusual features which stand out from
     * the rest of the world.
     */
    public void
    applySpecialEffects() {
        //applyBulge();
    }
    
    
    /**
     * Apply a bulge to one of the hemispheres of the world.
     * The height of either the northern or southern hemisphere
     * is raised by a set amount.
     */
    public void
    applyBulge() {
        int     y = (int)(Math.random() * map.getHeight());
        int     top = 0, bottom = map.getHeight();
        
        if (y < map.getHeight()/2) {
            bottom = y;
        } else {
            top = y;
        }
        
        for (y = top; y < bottom; y++) {
            for (int x=0; x < map.getWidth(); x++) {
                if (isValid(x, y)) {
                    raise(x, y, y-top);
                }
            }
        }
    }
    
    /**
     * Apply a trench which runs around the world east to west.
     * The trench is quite deep, and very straight.
     */
    public void
    applyTrench() {
        int     y = (int)(Math.random() * map.getHeight());
        // Make it tend towards the equator.
        y = ((map.getHeight()/2)+y)/2;

        for (int x=getLeft(y); x <= getRight(y); x++) {
            raise(x, y-2, -25);
            raise(x, y-1, -50);
            raise(x, y, -75);
            raise(x, y+1, -50);
            raise(x, y+2, -25);
        }
    }

}
