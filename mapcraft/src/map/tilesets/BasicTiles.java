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

import net.sourceforge.mapcraft.map.elements.Area;
import net.sourceforge.mapcraft.map.elements.Terrain;

/**
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BasicTiles implements ITiles, Cloneable {
    private Terrain[][]     terrain;
    private Terrain[][]     feature;
    private Area[][]        area;
    private int[][]         altitude;
    private boolean[][]     writable;
    private boolean[][]     highlighted;
    private byte[][]        terrainRotation;
    private byte[][]        featureRotation;
    
    public BasicTiles(int width, int height) {
        terrain = new Terrain[width][height];
        feature = new Terrain[width][height];
        area = new Area[width][height];
        altitude = new int[width][height];
        writable = new boolean[width][height];
        highlighted = new boolean[width][height];
        terrainRotation = new byte[width][height];
        featureRotation = new byte[width][height];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#terrain(int, int)
     */
    public Terrain terrain(int x, int y) {
        return terrain[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#feature(int, int)
     */
    public Terrain feature(int x, int y) {
        return feature[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#altitude(int, int)
     */
    public int altitude(int x, int y) {
        return altitude[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#terrainRotation(int, int)
     */
    public short terrainRotation(int x, int y) {
        return (short)terrainRotation[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#featureRotation(int, int)
     */
    public short featureRotation(int x, int y) {
        return (short)featureRotation[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#writable(int, int)
     */
    public boolean writable(int x, int y) {
        return writable[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#area(int, int)
     */
    public Area area(int x, int y) {
        return area[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#highlighted(int, int)
     */
    public boolean highlighted(int x, int y) {
        return highlighted[x][y];
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setTerrain(int, int, net.sourceforge.mapcraft.map.elements.Terrain)
     */
    public void setTerrain(int x, int y, Terrain terrain) {
        this.terrain[x][y] = terrain;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setFeature(int, int, net.sourceforge.mapcraft.map.elements.Terrain)
     */
    public void setFeature(int x, int y, Terrain feature) {
        this.feature[x][y] = feature;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setAltitude(int, int, int)
     */
    public void setAltitude(int x, int y, int altitude) {
        this.altitude[x][y] = altitude;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setTerrainRotation(int, int, short)
     */
    public void setTerrainRotation(int x, int y, short rotation) {
        this.terrainRotation[x][y] = (byte)rotation;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setFeatureRotation(int, int, short)
     */
    public void setFeatureRotation(int x, int y, short rotation) {
        this.featureRotation[x][y] = (byte)rotation;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setArea(int, int, net.sourceforge.mapcraft.map.elements.Area)
     */
    public void setArea(int x, int y, Area area) {
        this.area[x][y] = area;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setWritable(int, int, boolean)
     */
    public void setWritable(int x, int y, boolean writable) {
        this.writable[x][y] = writable;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#setHighlighted(int, int, boolean)
     */
    public void setHighlighted(int x, int y, boolean highlighted) {
        this.highlighted[x][y] = highlighted;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.tilesets.ITiles#copyFrom(net.sourceforge.mapcraft.map.tilesets.ITiles, int, int, int, int)
     */
    public void copyFrom(ITiles source, int fromX, int fromY, int toX, int toY) {
        terrain[toX][toY] = source.terrain(fromX, fromY);
        feature[toX][toY] = source.feature(fromX, fromY);
        area[toX][toY] = source.area(fromX, fromY);
        writable[toX][toY] = source.writable(fromX, fromY);
        highlighted[toX][toY] = source.highlighted(fromX, fromY);
        terrainRotation[toX][toY] = (byte)source.terrainRotation(fromX, fromY);
        featureRotation[toX][toY] = (byte)source.featureRotation(fromX, fromY);
        altitude[toX][toY] = source.altitude(fromX, fromY);
    }
    
    public Object clone() {
        // TODO: Implement clone()
        return null;
    }

}
