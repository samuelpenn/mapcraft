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

import java.util.Vector;
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

    protected Vector    rivers = null;
    protected Vector    things = null;

    private Parent      parent = null;

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

    public void
    setParent(int scale, int x, int y) {
        parent = new Parent(scale, x, y);
    }

    public int
    getParentsScale() {
        if (parent == null) {
            return scale;
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

    /**
     * After the tileset has been cropped, translate all the things to their
     * new correct position, and remove any things which are not in the
     * cropped area.
     *
     * This should be called after the tiles have been cropped, all that is
     * needed is the original crop top-left coordinates, in order to work
     * out how far to translate everything.
     */
    private void
    cropAllThings(int x, int y) {
        Thing       thing = null;
        Vector      list = new Vector();

        // Translation coords are in tiles. Things are positioned in
        // hundreths of a tile.
        x *= 100;
        y *= 100;

        for (int i=0; i < things.size(); i++) {
            boolean     okay = true;

            thing = (Thing)things.elementAt(i);
            thing.setPosition(thing.getX()-x, thing.getY()-y);

            System.out.println(thing);

            if (thing.getX() < 0) okay = false;
            if (thing.getY() < 0) okay = false;
            if (thing.getX() > width * 100) okay = false;
            if (thing.getY() > height * 100) okay = false;

            if (okay) {
                list.add(thing);
            }
        }

        setThings(list);
    }

    private void
    cropAllRivers(int x, int y) {
        Path        path = null;
        Vector      list = new Vector();
         // Translation coords are in tiles. Things are positioned in
        // hundreths of a tile.
        x *= 100;
        y *= 100;

        for (int i=0; i < rivers.size(); i++) {
            boolean     okay = true;

            path = (Path)rivers.elementAt(i);
            path.move(0-x, 0-y);

            if (okay) {
                list.add(path);
            }
        }

        setRivers(list);
   }

    /**
     * Crop this TileSet to the given size. The width and height must both
     * be positive - if not, a MapOutOfBoundsException is thrown.
     */
    void
    crop(int x, int y, int w, int h) throws MapOutOfBoundsException {

        // X-coordinate must be even. This is because of the strange
        // effect of stuttered hexagonal tiles.
        if (x%2 != 0) {
            x-=1;
            w+=1;
        }

        System.out.println("Cropping "+x+","+y+","+w+","+h);

        int     oldScale = scale;
        if (isChild()) {
            oldScale = parent.getScale();
        }
        parent = new Parent(oldScale, x, y);

        // Perform sanity checks.
        checkBounds(x, y);
        checkBounds(x + w, y + h);
        if (w < 1 || h < 1) {
            throw new MapOutOfBoundsException("Crop size must be positive");
        }
        Tile[][]    cropped = new Tile[h][w];
        for (int ix=0; ix < w; ix++) {
            for (int iy=0; iy < h; iy++) {
                cropped[iy][ix] = tiles[y+iy][x+ix];
            }
        }
        tiles = cropped;
        width = w;
        height = h;

        cropAllThings(x, y);
        cropAllRivers(x, y);
    }

    private void
    scaleAllThings(double factor) {
        for (int i=0; i < things.size(); i++) {
            Thing   thing = (Thing)things.elementAt(i);

            thing.setX((int)(thing.getX()*factor));
            thing.setY((int)(thing.getY()*factor));
        }
    }

    private void
    scaleAllRivers(double factor) {
        Path        path = null;
        Vector      list = new Vector();

        for (int i=0; i < rivers.size(); i++) {
            boolean     okay = true;

            path = (Path)rivers.elementAt(i);
            path.scale(factor);

            if (okay) {
                list.add(path);
            }
        }

        setRivers(list);
    }

    /**
     * Rescale the map. The scale of the tiles is changed, and, the number
     * of tiles is changed to reflect the new scale. If changing to a smaller
     * scale, then the number of tiles is increased.
     */
    boolean
    rescale(int newScale) {
        double  factor = (double)scale / (double)newScale;

        if (newScale == scale) {
            // Trivial case.
            return true;
        }

        int x = 0, y = 0;
        if (isChild()) {
            x = parent.getXOffset();
            y = parent.getYOffset();
        }
        parent = new Parent(scale, x, y);

        scaleAllThings(factor);
        scaleAllRivers(factor);

        if (newScale > scale) {
            return scaleLarger(newScale);
        }

        if (newScale < scale) {
            return scaleSmaller(newScale);
        }
        return true;
    }

    private boolean
    scaleLarger(int newScale) {
        return false;
    }

    /**
     * The new scale is smaller than current scale, so the map will get
     * bigger.
     */
    private boolean
    scaleSmaller(int newScale) {
        if (scale%newScale != 0) {
            // Can only cope with exact multiples.
            return false;
        }

        int         factor = scale / newScale;
        int         newWidth = width * factor;
        int         offset = factor/2;
        int         newHeight = height * factor + offset;
        Tile[][]    scaled = new Tile[newHeight][newWidth];

        System.out.println("scaleSmaller: factor "+factor+" "+newWidth+"x"+newHeight);

        for (int x=0;  x < newWidth; x++) {
            boolean even = (x/factor)%2 == 0;
            int     top = even?0:offset;
            int     bottom = even?offset:0;

            for (int y=0; y < top; y++) {
                scaled[y][x] = new Tile(tiles[0][x/factor]);
            }
            for (int y=newHeight-bottom; y < newHeight; y++) {
                scaled[y][x] = new Tile(tiles[height-1][x/factor]);
            }

            for (int y=0; y < newHeight-offset; y++) {
                scaled[y+top][x] = new Tile(tiles[y/factor][x/factor]);
            }
        }
        width = newWidth;
        height = newHeight;
        tiles = scaled;

        return true;
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
    isWritable(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).isWritable();
    }

    /**
     * @deprecated;
     */
    public boolean
    isRiver(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).isRiver();
    }

    /**
     * @deprecated
     */
    public short
    getRiverMask(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getRiverMask();
    }

    /**
     * @deprecated
     */
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
    /**
     * Crop the tiles to the given area. The map is searched for all tiles
     * which match the area, and a rectangle is formed which encloses all
     * these tiles. The rectangle will then be grown in each direction by
     * the size of the margin, if it is positive.
     *
     * The margin will not take the cropped area out beyond the edges of
     * the original map.
     *
     * @param area      Area id to be cropped to.
     * @param margin    Number of tiles to add as a margin.
     */
    public void
    cropToArea(short area, int margin) throws MapOutOfBoundsException {
        int     minX, minY, maxX, maxY;
        int     x, y;
        boolean found = false;

        minX = minY = maxX = maxY = -1;
        for (x=0; x < width; x++) {
            for (y=0; y < height; y++) {
                try {
                    if (getArea(x, y) == area) {
                        if (!found || x < minX) {
                            minX = x;
                        }
                        if (!found || x > maxX) {
                            maxX = x;
                        }
                        if (!found || y < minY) {
                            minY = y;
                        }
                        if (!found || y > maxY) {
                            maxY = y;
                        }
                        found = true;
                    }
                } catch (MapOutOfBoundsException moobe) {
                }
            }
        }

        if (margin > 0) {
            minX -= margin;
            minY -= margin;
            maxX += margin;
            maxY += margin;

            if (minX < 0) minX = 0;
            if (minY < 0) minY = 0;
            if (maxX >= width) maxX = width-1;
            if (maxY >= height) maxY = height-1;
        }

        crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
    }

    /**
     * Replace all the rivers with the new set of rivers.
     */
    void
    setRivers(Vector rivers) {
        this.rivers = rivers;
    }

    void
    setThings(Vector things) {
        this.things = things;
    }

    Vector
    getRivers() {
        return rivers;
    }

    Vector
    getThings() {
        return things;
    }

    Path
    getRiver(int id) {
        Path        path;

        id--;
        if (id > rivers.size()) {
            return null;
        }
        path = (Path)rivers.elementAt(id);

        return path;
    }

    /**
     * Create and add a new river to the map.
     */
    int
    addRiver(String name, int x, int y) throws MapOutOfBoundsException {
        Path    path = new Path(name, x, y);

        //tileSets[0].getTile(x, y).setRiver(true);
        rivers.add(path);

        return rivers.size();
    }

    void
    extendRiver(int id, int x, int y) throws MapOutOfBoundsException {
        Path    river = getRiver(id);

        river.add(x, y);
    }

    void
    removeThing(int id) {
        things.remove(id);
    }

    void
    addThing(Thing thing) {
        things.add(thing);
    }


}

