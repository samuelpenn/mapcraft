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
 * Jovian worlds are large gas giants, with a relatively small solid
 * core. The 'surface' of the world is considered to be the top of
 * the atmosphere, sense it is so dense nothing can be seen below this.
 * 
 * @author Samuel Penn
 */
public class JovianWorld extends WorldGenerator {

    
    public
    JovianWorld(String name, int radius, int scale) {
        super(name, radius, scale);
        
        log("JovianWorld: ["+name+"] "+radius+"km @"+scale+"km");
    }

    /**
     * Setup the world type for Jovian worlds.
     */
    public void setWorldType(int worldType) {
        this.worldType = worldType;
        
        switch (worldType) {
        case JOVIAN:
        case SUPERJOVIAN:
        case MACROJOVIAN:
            setupJovian();
            break;
        case CRYOJOVIAN:
            setupCryoJovian();
            break;
        }
    }
    
    private void
    setupJovian() {
        TerrainSet      ts = map.getTerrainSet();

        int     r = 6 + (int)(Math.random()*5); // 8
        int     g = 6 + (int)(Math.random()*5); // 8
        int     b = 4 + (int)(Math.random()*3); // 6

        for (int i = 1; i < 17; i++) {
            String      tag = "jovian."+i;
            String      colour = toColour(75+i*r, 50+i*g, 25+i*b);

            ts.add((short)(GREY+i-1), tag, tag, colour);
        }        
    }
    
    private void
    setupCryoJovian() {
        TerrainSet      ts = map.getTerrainSet();

        int     r = 3 + (int)(Math.random()*3); // 12
        int     g = 5 + (int)(Math.random()*5); // 12
        int     b = 6 + (int)(Math.random()*5); // 12

        for (int i = 1; i < 17; i++) {
            String      tag = "cryo."+i;
            String      colour = toColour(10+i*r, 50+i*g, 75+i*b);

            ts.add((short)(GREY+i-1), tag, tag, colour);
        }        
    }
    
    public void
    generate() {
        switch (worldType) {
        case JOVIAN:
        case SUPERJOVIAN:
        case MACROJOVIAN:
            generateJovian();
            break;
        case CRYOJOVIAN:
            generateCryoJovian();
            break;
        }
    }
    
    private void
    generateJovian() {
        int     t = 1 + (int)(Math.random()*16);
        for (int y=0; y < map.getHeight(); y++) {
            if (Math.random() < 0.3 && t > 1) {
                t-=1;
            }
            if (Math.random() > 0.3 && t < 16) {
                t+=1;
            }
            if (Math.random() < 0.05) {
                t = 1 + (int)(Math.random()*8 + Math.random()*8);
            }
            for (int x=0; x < map.getWidth(); x++) {
                if (isValid(x, y)) {
                    try {
                        map.setTerrain(0, x, y, (short)t);
                    } catch (MapOutOfBoundsException e) {
                        // Ignore.
                    }
                }
            }
        }
    }
    
    private void
    generateCryoJovian() {
        generateJovian();
    }

}
