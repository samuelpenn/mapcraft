/*
 * Copyright (C) 2002 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.map;

/**
 * Represents a single tile on a map. A tile is a square
 * area of fixed size, describing the most common terrain,
 * average height and other information.
 * <br/>
 * The tile itself does not know how 'big' it is - this
 * information is stored as part of the TileSet, since all
 * tiles in a TileSet must be of the same size.
 * <br/>
 * Tiles also lack knowledge of what terrain means - again
 * mappings between terrain values and 'real-world' meanings
 * is performed by the Map itself.
 * <br/>
 * Terrain and features can be displayed rotated.
 *
 * @see TileSet
 * @see Map
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
class Tile implements Cloneable {
    private short   terrain;
    private short   height;
    private short   feature;
    private boolean writable;
    private boolean highlighted;
    private short   area;

    private final static short MAXTYPES = 512;

    public String
    toString() {
        String      string = null;

        string = "Terrain ("+terrain+") Height ("+height+") Feature ("+feature+") ";

        return string;
    }

    /**
     * Construct an empty tile, of 0m height, and the default
     * terrain type (normally ocean).
     */
    Tile() {
        terrain = (short)1;
        height = (short)0;
        feature = (short)0;
        area = (short)0;
        writable = true;
        highlighted = false;
    }

    /**
     * Construct a tile from another Tile.
     *
     * @param tile  The Tile to be copied.
     */
    Tile(Tile tile) {
        this.terrain = tile.terrain;
        this.height = tile.height;
        this.writable = tile.writable;
        this.highlighted = tile.highlighted;
        this.feature = tile.feature;
        this.area = tile.area;
    }

    public Object
    clone() throws CloneNotSupportedException {
        Tile    t = new Tile();
        t.terrain = terrain;
        t.height = height;
        t.writable = writable;
        t.highlighted = highlighted;
        t.feature = feature;
        t.area = area;

        return (Object)t;
    }

    /**
     * Construct a fully described tile.
     *
     * @param terrain   Number representing the terrain type.
     * @param height    Height of the tile, in metres.
     * @param writable  If false, tile is 'off-map'.
     */
    Tile(short terrain, short height, boolean writable) {
        this.terrain = terrain;
        this.height = height;
        this.writable = writable;
    }

    /**
     * Set the terrain for this tile to be the defined type.
     * The Tile does not know what the terrain represents, it
     * is just an identifier.
     */
    void
    setTerrain(short t) {
        if (writable) {
            this.terrain = t;
        }
    }

    void
    setTerrainRotation(short r) {
        this.terrain = (short) ((terrain%MAXTYPES) + MAXTYPES*r);
    }

    short
    getTerrain() {
        return (short)(terrain%MAXTYPES);
    }

    short
    getTerrainRotation() {
        return (short)(terrain/MAXTYPES);
    }

    short
    getTerrainRaw() {
        return terrain;
    }

    void
    setHeight(short h) {
        if (writable) {
            this.height = h;
        }
    }

    short
    getHeight() {
        return height;
    }

    /**
     * Return the id of the feature on this tile, or zero if none.
     * Information on how the feature is rotated is not returned.
     */
    short
    getFeature() {
        return (short)(feature%MAXTYPES);
    }

    void
    setFeature(short feature) {
        if (writable) {
            this.feature = feature;
        }
    }

    /**
     * Return the raw feature data for this tile. This includes the
     * id and also the rotation flag.
     */
    short
    getFeatureRaw() {
        return feature;
    }

    void
    setFeatureRotation(short r) {
        this.feature = (short) ((feature%MAXTYPES) + MAXTYPES*r);
    }

    /**
     * Return the rotation of the feature on this tile. The rotation is
     * given as the number of sides it has been rotated through, so 0-3
     * for a square tile, or 0-5 for a hexagonal tile.
     */
    short
    getFeatureRotation() {
        return (short)(feature/MAXTYPES);
    }

    boolean
    isWritable() {
        return writable;
    }

    boolean
    isHighlighted() {
        return highlighted;
    }

    void
    setHighlighted(boolean h) {
        highlighted = h;
    }

    /**
     * Return the id of the area for this tile.
     */
    short
    getArea() {
        return area;
    }

    /**
     * Set the id of the area for this tile.
     */
    void
    setArea(short area) {
        if (writable) {
            this.area = area;
        }
    }

}
