/*
 * Copyright (C) 2002 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version. See the file COPYING.
 *
 * $Revision$
 * $Date$
 */

package uk.co.demon.bifrost.rpg.mapcraft.map;

import uk.co.demon.bifrost.rpg.mapcraft.map.*;

/**
 * Defines a set of tiles for use in a map. The set is a two
 * dimensional array of Tile objects.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class TileSet implements Cloneable {
    protected String    name;
    protected int       width;
    protected int       height;
    protected int       scale;

    protected Tile[][]  tiles;

    public
    TileSet(String name, int width, int height, int scale)
            throws InvalidArgumentException {

        if (name == null || name.length() == 0) {
            throw new InvalidArgumentException("TileSet name must not be empty");
        }

        if (width < 1 || height < 1) {
            throw new InvalidArgumentException("TileSet dimensions must be positive");
        }

        if (scale < 1) {
            throw new InvalidArgumentException("TileSet scale must be positive");
        }

        this.name = name;
        this.width = width;
        this.height = height;
        this.scale = scale;

        System.out.println("Creating tileset of "+width+"x"+height);
        this.tiles = new Tile[height][width];
        int     x, y;
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                this.tiles[y][x] = new Tile((short)1, (short)0, true);
            }
        }
    }

    public Object
    clone() throws CloneNotSupportedException {
        TileSet     t = null;

        try {
            t = new TileSet(name, width, height, scale);

            t.tiles = new Tile[height][width];
            int     x, y;
            for (y = 0; y < height; y++) {
                for (x = 0; x < width; x++) {
                    t.tiles[y][x] = (Tile)tiles[y][x].clone();
                }
            }
        } catch (InvalidArgumentException e) {
            // Something gone wrong really here, how could the original
            // settings be invalid?
            throw new CloneNotSupportedException("Original object invalid, cannot clone");
        }

        return (Object)t;
    }

    private void
    checkBounds(int x, int y) throws MapOutOfBoundsException {
        if (x >= width || x < 0) {
            throw new MapOutOfBoundsException("Tile x:"+x+" out of map bounds");
        }
        if (y >= height || y < 0) {
            throw new MapOutOfBoundsException("Tile y:"+y+" out of map bounds");
        }
    }

    public void
    setTile(int x, int y, short terrain) throws MapOutOfBoundsException {
        checkBounds(x, y);

        try {
            if (tiles[y][x] == null) {
                tiles[y][x] =  new Tile(terrain, (short)0, true);
            } else {
                tiles[y][x].setTerrain(terrain);
            }
        } catch (ArrayIndexOutOfBoundsException abe) {
            throw new MapOutOfBoundsException("Tile x:"+x+", y:"+y+" out of map bounds");
        }
    }

    /**
     * Set the given tile to be equal to the provided tile.
     *
     * @param x     X coordinate of tile to set.
     * @param y     Y coordinate of tile to set.
     * @param tile  Tile to set tile to.
     */
    public void
    setTile(int x, int y, Tile tile) throws MapOutOfBoundsException {
        checkBounds(x, y);

        try {
            tiles[y][x] = tile;
        } catch (ArrayIndexOutOfBoundsException abe) {
            throw new MapOutOfBoundsException("Tile x:"+x+", y:"+y+" out of map bounds");
        }
    }

    public Tile
    getTile(int x, int y) throws MapOutOfBoundsException {
        if (x < 0 || x >= width) {
            throw new MapOutOfBoundsException("Tile x:"+x+" out of map bounds");
        }
        if (y < 0 || y >= height) {
            throw new MapOutOfBoundsException("Tile y:"+y+" out of map bounds");
        }
        
        return tiles[y][x];
    }

    
    public void
    setTerrain(int x, int y, short t) throws MapOutOfBoundsException {
        checkBounds(x, y);
        
        tiles[y][x].setTerrain(t);
    }
    
    public void
    setHeight(int x, int y, short h) throws MapOutOfBoundsException {
        checkBounds(x, y);
        
        tiles[y][x].setHeight(h);
    }

    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public int getScale() { return scale; }
    
    /**
     * Set the scale for the TileSet. The scale change does
     * not perform any resizing of the TileSet.
     *
     * @param scale     Scale, in km, to set TileSet to.
     */
    public void
    setScale(int scale) {
        this.scale = scale;
    }


    /**
     * Get the terrain id for the particular tile.
     *
     * @param x     X coordinate of the tile.
     * @param y     Y coordinate of the tile.
     *
     * @return      Id of the terrain for this tile.
     */
    public short
    getTerrain(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getTerrain();
    }

    /**
     * Get the height (in metres) for the particular tile.
     *
     * @param x     X coordinate of the tile.
     * @param y     Y coordinate of the tile.
     *
     * @return      Id of the terrain for this tile.
     */
    public short
    getHeight(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getHeight();
    }
    
    public boolean
    isRiver(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).isRiver();
    }

    public short
    getRiverMask(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getRiverMask();
    }

    public void
    setRiverMask(int x, int y, short mask) throws MapOutOfBoundsException {
        getTile(x, y).setRiverMask(mask);
    }

    public short
    getArea(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getArea();
    }

    public void
    setArea(int x, int y, short area) throws MapOutOfBoundsException {
        getTile(x, y).setArea(area);
    }

    public short
    getFeature(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getFeature();
    }

    public void
    setFeature(int x, int y, short feature) throws MapOutOfBoundsException {
        getTile(x, y).setFeature(feature);
    }
}

