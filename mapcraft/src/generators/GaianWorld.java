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
 * @author Samuel Penn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GaianWorld extends WorldGenerator {
    
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
        
        for (int i=1; i < 17; i++) {
            String      tag = "ocean."+i;
            String      colour = toColour(50+i*5, 50+i*5, 150+i*5);
            ts.add((short)(i), tag, tag, colour);
        }
        
        for (int i=1; i < 17; i++) {
            String      tag = "land."+i;
            String      colour = toColour(50+i*3, 100+i*5, 50+i*4);
            ts.add((short)(i+16), tag, tag, colour);
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
    
    protected void
    generateGaian() {
        heightMap();
        colourByHeight(1, 32);
    }
    
    protected void
    generateProtoGaian() {
        heightMap();
        colourByHeight(1, 32);
    }

}
