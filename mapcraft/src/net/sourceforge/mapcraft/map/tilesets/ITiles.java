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
package net.sourceforge.mapcraft.map.tilesets;

import net.sourceforge.mapcraft.map.elements.*;

/**
 * @author sam
 *
 * Interface used internally to abstract access to tiles.
 * No error or bounds checking is performed - it is assumed that this
 * is all done by the caller.
 */
public interface ITiles {
    public Terrain terrain(int x, int y);
    public Terrain feature(int x, int y);
    public int altitude(int x, int y);
    public short terrainRotation(int x, int y);
    public short featureRotation(int x, int y);
    public boolean writable(int x, int y);
    public Area area(int x, int y);
    public boolean highlighted(int x, int y);
    
    public void setTerrain(int x, int y, Terrain terrain);
    public void setFeature(int x, int y, Terrain feature);
    public void setAltitude(int x, int y, int altitude);
    public void setTerrainRotation(int x, int y, short rotation);
    public void setFeatureRotation(int x, int y, short rotation);
    public void setArea(int x, int y, Area area);
    public void setWritable(int x, int y, boolean writable);
    public void setHighlighted(int x, int y, boolean highlighted);
    
    /**
     * Copy a single tile from another set of tiles to this one. The
     * coordinates to copy from, and copy to, should be provided. All the
     * attributes of the source tile is copied into the destination tile.
     * The destination tile is totally overwritten.
     * 
     * @param source    Source set of tiles to copy from.
     * @param fromX     X coordinate in source map.
     * @param fromY     Y coordinate in source map.
     * @param toX       X coordinate in this map.
     * @param toY       Y coordinate in this map.
     */
    public void copyFrom(ITiles source, int fromX, int fromY, int toX, int toY);
}
