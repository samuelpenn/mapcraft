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
package net.sourceforge.mapcraft.map.tilesets.memory;

import net.sourceforge.mapcraft.map.tilesets.ITiles;
import net.sourceforge.mapcraft.map.elements.*;

/**
 * Implements a map of tiles. This implementation stores the tiles directly
 * in memory, using as little space as possible. As per the interface, this
 * only provides basic access to reading and writing individual tiles. All
 * clever logic (such as cropping etc) should be performed by the TileSet
 * implementation which references this class.
 * 
 * @author Samuel Penn
 */
public class Tiles implements ITiles {
    private int     width = 0;
    private int     height = 0;
    
    private Terrain[][] terrain;
    private Terrain[][] feature;
    private short[][]   altitude;
    private boolean[][] writable;
    private boolean[][] highlighted;
    private short[][]   featureRotation;
    private short[][]   terrainRotation;
    private Area[][]    area;
    
    /**
     * Create a new, empty, map of tiles of the given height and width.
     * 
     * @param width     Desired width of the map.
     * @param height    Desired height of the map.
     */
    public Tiles(int width, int height) {
        this.width = width;
        this.height = height;

        this.terrain = new Terrain[height][width];
        this.feature = new Terrain[height][width];
        this.altitude = new short[height][width];
        this.writable = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.writable[y][x] = true;
                this.terrain[y][x] = null;
                this.feature[y][x] = null;
                this.writable[y][x] = true;
                this.altitude[y][x] = 0;
            }
        }
    }
    
    // This mask is used for rotated terrain and features.
    private static final int    MASK = 100000;
    
    public Terrain terrain(int x, int y) {
        return terrain[y][x];
    }
    
    public void setTerrain(int x, int y, Terrain value) {
        terrain[y][x] = value;
    }
    
    public Terrain feature(int x, int y) {
        return feature[y][x];
    }

    public void setFeature(int x, int y, Terrain value) {
        feature[y][x] = value;
    }
    
    public int altitude(int x, int y) {
        return altitude[y][x];
    }
    
    public void setAltitude(int x, int y, int value) {
        altitude[y][x] = (short)value;
    }
    
    public short terrainRotation(int x, int y) {
        return terrainRotation[y][x];
    }
    
    public void setTerrainRotation(int x, int y, short value) {
        terrainRotation[y][x] = value;
    }
    
    public short featureRotation(int x, int y) {
        return featureRotation[y][x];
    }

    public void setFeatureRotation(int x, int y, short value) {
        featureRotation[y][x] = value;
    }
    
    public boolean writable(int x, int y) {
        return writable[y][x];
    }
    
    public void setWritable(int x, int y, boolean value) {
        writable[y][x] = value;
    }
    
    public boolean highlighted(int x, int y) {
        return highlighted[y][x];
    }
    
    public void setHighlighted(int x, int y, boolean value) {
        highlighted[y][x] = value;
    }

    
    public Area area(int x, int y) {
        return area[y][x];
    }
    
    public void setArea(int x, int y, Area value) {
        area[y][x] = value;
    }

    public void 
    copyFrom(ITiles source, int fromX, int fromY, int toX, int toY) {
        setTerrain(toX, toY, source.terrain(fromX, fromY));
        setFeature(toX, toY, source.feature(fromX, fromY));
        setWritable(toX, toY, source.writable(fromX, fromY));
        setTerrainRotation(toX, toY, source.terrainRotation(fromX, fromY));
        setFeatureRotation(toX, toY, source.featureRotation(fromX, fromY));
    }
}
