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
        
        setWorldType(HERMIAN);
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
    
    /**
     * Set the type of the world.
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
    
    protected void
    generateSelenian() {
        heightMap();
        colourByHeight(1, 16);
    }
    
    protected void
    generateHermian() {
        heightMap();
        colourByHeight(1, 16);
    }
    
    protected void
    generateArean() {
        heightMap();
        colourByHeight(1, 16);
    }

}
