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

import net.sourceforge.mapcraft.map.MapException;
import net.sourceforge.mapcraft.map.MapOutOfBoundsException;
import net.sourceforge.mapcraft.map.TerrainSet;
import net.sourceforge.mapcraft.map.interfaces.ITileSet;

/**
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TerrestrialWorld extends WorldGenerator {
    public
    TerrestrialWorld(String name, int radius, int scale) {
        super(name, radius, scale);
        System.out.println("Creating TerrestrialWorld ["+name+
                           "] "+radius+"km @"+scale+"km");
    }
    /**
     * Define the terrain types for Moon-like Selenian worlds.
     * These tend to consist of greys, with no real colour.
     */
    private void
    setupSelenian() {
        TerrainSet      ts = map.getTerrainSet();

        System.out.println("setupSelenian:");
        
        int     r = 11 + (int)(Math.random()*3); // 12
        int     g = 11 + (int)(Math.random()*3); // 12
        int     b = 11 + (int)(Math.random()*3); // 12

        for (int i = 1; i < 17; i++) {
            String      tag = "grey."+i;
            String      colour = toColour(50+i*r, 50+i*g, 50+i*b);

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
        
        log("setupHermian: ...");

        if (ts == null) {
            log("setupHermian: Null terrain set");
        }

        int     r = 11 + (int)(Math.random()*3); // 12
        int     g = 15 + (int)(Math.random()*4); // 17
        int     b = 20 + (int)(Math.random()*10); // 25

        for (int i = 1; i < 17; i++) {
            String      tag = "hermian."+i;
            String      colour = null;

            colour = toColour(255 - (i*r), 255 - (i*g), 200 - (i*b));
            ts.add((short)(GREY+i-1), tag, tag, colour);
        }
    }
    
    /**
     * Setup Mars-like worlds.
     */
    private void
    setupArean() {
        TerrainSet      ts = map.getTerrainSet();
        
        System.out.println("setupArean:");

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
     * Pelagic worlds are wet greenhouse worlds, with a huge
     * world ocean, no land, and very high temperatures and
     * pressure.
     */
    private void
    setupPelagic() {
        TerrainSet      ts = map.getTerrainSet();
        
        System.out.println("setupPelagic:");
        
        for (int i=1; i < 17; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(100+i*2, 150+i*3, 200+i*3);
            ts.add((short)(GREY+i-1), tag, tag, colour);
        }
    }
    
    /**
     * Set the type of the world. Possible values are SELENIAN,
     * HERMIAN, PELAGIC and AREAN.
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
        case PELAGIC:
            setupPelagic();
            break;
        }
    }
    
    /**
     * Generate surface map for this world. How the map is generated
     * depends on the type of world set with setWorldType().
     */
    public void
    generate() throws MapException {
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
        case PELAGIC:
            generatePelagic();
            break;
        }        
    }
    
    /**
     * Generate a heavily cratered Selenian world. Lots of impact
     * craters of varying sizes. Later craters will be smaller than
     * the older craters.
     */
    protected void
    generateSelenian() throws MapException {
        System.out.println("Selenian:");
        // Start with a very random height map.
        randomHeight(1000, 100);
        ITileSet        set = map.getTileSet(0);
        // Random fills impacts.
        System.out.println("Selenian: Generating flood plains");
        for (int a=0; a < 100; a++) {
            int     y = (int)(Math.random() * set.getMapHeight());

            int     left = getLeft(y);
            int     right = getRight(y);
            
            int     x = left + (int)(Math.random() * (right - left));
            int     r = (int)(Math.random() * 30) + 15;
            int     h = (int)(Math.random() * 100) + 50;

            try {
                fill(x, y, set.getAltitude(x, y), 120);
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
            int     y = (int)(Math.random() * set.getMapHeight());
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
        for (int y=0; y < set.getMapHeight(); y++) {
            for (int x=0; x < set.getMapWidth(); x++) {
                if (isValid(x, y)) {
                    try {
                        if (set.getAltitude(x, y) > 1100) {
                            set.setFeature(x, y, set.getFeatureSet().getTerrain(5));
                        } else if (set.getAltitude(x, y) > 1070) {
                            set.setFeature(x, y, set.getFeatureSet().getTerrain(4));
                        } else if (set.getAltitude(x, y) > 1040) {
                            set.setFeature(x, y, set.getFeatureSet().getTerrain(3));
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
    generateHermian() throws MapException {
        log("generateHermian:");
        
        heightMap();
        
        log("generateHermian: Adding craters");
        for (int a=0; a < 200; a++) {
            int     y = (int)(Math.random() * map.getTileSet(0).getMapHeight());
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
                log("generateHermian: Exception ("+e.getMessage()+")");
            }
        }
        applySpecialEffects();
        colourByHeight(1, 16);
    }
    
    /**
     * Generate a world ocean. There are no structural details on
     * this world, just a single world spanning ocean.
     */
    protected void
    generatePelagic() {
        randomise(1, 16);
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
    applyBulge() throws MapException {
        int     y = (int)(Math.random() * map.getTileSet(0).getMapHeight());
        int     top = 0, bottom = map.getTileSet(0).getMapHeight();
        
        if (y < map.getTileSet(0).getMapHeight()/2) {
            bottom = y;
        } else {
            top = y;
        }
        
        for (y = top; y < bottom; y++) {
            for (int x=0; x < map.getTileSet(0).getMapWidth(); x++) {
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
    applyTrench() throws MapException {
        int     y = (int)(Math.random() * map.getTileSet(0).getMapHeight());
        // Make it tend towards the equator.
        y = ((map.getTileSet(0).getMapHeight()/2)+y)/2;

        for (int x=getLeft(y); x <= getRight(y); x++) {
            raise(x, y-2, -25);
            raise(x, y-1, -50);
            raise(x, y, -75);
            raise(x, y+1, -50);
            raise(x, y+2, -25);
        }
    }

}
