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

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.*;
import net.sourceforge.mapcraft.map.interfaces.ITileSet;

/**
 * An Abstract implementation of the ITileSet interface. Provides basic
 * support for standard functionality, but ignores how the data is stored
 * and accessed.
 */
public abstract class AbstractTileSet implements ITileSet {
    private Parent        parent = null;
    protected int         mapScale = 0;
    protected int         mapHeight = 0;
    protected int         mapWidth = 0;
    protected String      mapName = null;
    
    protected ITiles      tiles = null;

    /**
     * The Parent class keeps track of the parent of this tileSet. If this
     * tileset is rescaled or cropped, then it holds details about the
     * original parent, so that the new map can be fed back into the
     * original at a later date.
     */
    private class Parent {
        private int     scale;
        private int     xOffset;
        private int     yOffset;

        Parent(int scale, int xOffset, int yOffset) {
            this.scale = scale;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        int getScale() { return scale; }
        int getXOffset() { return xOffset; }
        int getYOffset() { return yOffset; }
    }
    
    /**
     * Empty constructor.
     */
    public AbstractTileSet() { };
    

    public void
    setParent(int scale, int x, int y) {
        parent = new Parent(scale, x, y);
    }

    public int
    getParentsScale() {
        if (parent == null) {
            return mapScale;
        }
        return parent.getScale();
    }

    public int
    getParentsXOffset() {
        if (parent == null) {
            return 0;
        }
        return parent.getXOffset();
    }

    public boolean
    isChild() {
        return (parent == null)?false:true;
    }

    public int
    getParentsYOffset() {
        if (parent == null) {
            return 0;
        }
        return parent.getYOffset();
    }
    


    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getName()
     */
    public String getName() {
        return mapName;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getMapHeight()
     */
    public int getMapHeight() {
        return mapHeight;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getMapWidth()
     */
    public int getMapWidth() {
        return mapWidth;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getScale()
     */
    public int getScale() {
        return mapScale;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setScale(int)
     */
    public void setScale(int scale) throws IllegalArgumentException {
        if (scale < 1) {
            throw new IllegalArgumentException("Scale must be strictly positive");
        }
        this.mapScale = scale;
    }
    
    /**
     * Check that the provided coordinates are legal map coordinates. If
     * they are not, then an exception is thrown.
     * 
     * @param x     X coordinate to check.
     * @param y     Y coordinate to check.
     * @throws MapOutOfBoundsException
     */
    protected void checkBounds(int x, int y)
                   throws MapOutOfBoundsException {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            throw new MapOutOfBoundsException("Coordinates are outside map");
        }
    }
    
    
    public void setTerrain(int x, int y, Terrain terrain) 
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setTerrain(x, y, terrain);
    }
    
    public Terrain getTerrain(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.terrain(x, y);
    }
    
    public void setFeature(int x, int y, Terrain feature) 
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setFeature(x, y, feature);
    }
    
    public Terrain getFeature(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.feature(x, y);
    }
    
    public void setAltitude(int x, int y, int h) 
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setAltitude(x, y, h);
    }

    
    public int getAltitude(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.altitude(x, y);
    }
    
    public void setArea(int x, int y, Area area) 
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setArea(x, y, area);
    }
    
    public Area getArea(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.area(x, y);
    }
    
    public void setWritable(int x, int y, boolean writable)
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setWritable(x, y, writable);
    }
    
    public boolean isWritable(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.writable(x, y);
    }
    
    public void setHighlighted(int x, int y, boolean highlighted)
                throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setHighlighted(x, y, highlighted);
    }
    
    public boolean isHighlighted(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.highlighted(x, y);
    }
    
    
    
    public short
    getFeatureRotation(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.featureRotation(x, y);
    }

    public void
    setFeatureRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setFeatureRotation(x, y, rotation);
    }

    public short
    getTerrainRotation(int x, int y) throws MapOutOfBoundsException {
        checkBounds(x, y);
        return tiles.terrainRotation(x, y);
    }

    public void
    setTerrainRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        checkBounds(x, y);
        tiles.setTerrainRotation(x, y, rotation);
    }

    

    protected void
    cropAllThings(int x, int y) {
        return;
    }
    
    protected void
    cropAllPaths(int x, int y) {
        return;
    }
    
    
    
    /**
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#crop(int, int, int, int)
     */
    protected void 
    crop(ITiles cropped, int x, int y, int width, int height)
            throws MapOutOfBoundsException {

        // X-coordinate must be even. This is because of the strange
        // effect of stuttered hexagonal tiles.
        if (x%2 != 0) {
            x-=1;
            width+=1;
        }

        int     oldScale = mapScale;
        if (isChild()) {
            oldScale = parent.getScale();
        }
        parent = new Parent(oldScale, x, y);

        // Perform sanity checks.
        checkBounds(x, y);
        checkBounds(x + width, y + height);
        if (width < 1 || height < 1) {
            throw new MapOutOfBoundsException("Crop size must be positive");
        }
        for (int ix=0; ix < width; ix++) {
            for (int iy=0; iy < height; iy++) {
                cropped.copyFrom(tiles, x+ix, y+iy, ix, iy);
            }
        }
        tiles = cropped;
        mapWidth = width;
        mapHeight = height;

        cropAllThings(x, y);
        cropAllPaths(x, y);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#rescale(int)
     */
    public void rescale(int newScale) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }



    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToArea(short, int)
     */
    public void cropToArea(short area, int margin)
            throws MapOutOfBoundsException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToHighlighted()
     */
    public void cropToHighlighted() throws MapOutOfBoundsException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToThing(java.lang.String, short)
     */
    public void cropToThing(String name, short radius) throws MapException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToPath(java.lang.String, short)
     */
    public void cropToPath(String name, short margin) throws MapException {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#changeArea(net.sourceforge.mapcraft.map.elements.Area, net.sourceforge.mapcraft.map.elements.Area)
     */
    public int changeArea(Area oldArea, Area newArea) {
        // TODO Auto-generated method stub
        return 0;
    }

}
