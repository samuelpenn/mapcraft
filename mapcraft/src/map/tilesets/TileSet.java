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

package net.sourceforge.mapcraft.map.tilesets;

import java.util.Vector;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.*;

/**
 * Defines a set of tiles for use in a map. The set is a two
 * dimensional array of Tile objects.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class TileSet extends AbstractTileSet implements Cloneable {

    protected Tile[][]  tiles;
    
    private int[][]     terrain;
    private int[][]     feature;
    private short[][]   height;
    private boolean[][] writable;

    protected Vector    rivers = null;
    protected Vector    things = null;
    
    // This mask is used for rotated terrain and features.
    private static final int    MASK = 100000;
    
    protected int terrain(int x, int y) {
        return terrain[y][x]%MASK;
    }
    
    protected int feature(int x, int y) {
        return feature[y][x]%MASK;
    }
    
    protected int height(int x, int y) {
        return height[y][x];
    }
    
    protected int terrainRotation(int x, int y) {
        return terrain[y][x] / MASK;
    }
    
    protected int featureRotation(int x, int y) {
        return feature[y][x] / MASK;
    }
    
    protected boolean writable(int x, int y) {
        return writable[y][x];
    }

    /**
     * Create a new, empty, TileSet of the specified width and height.
     * 
     * @param name      Name of this TileSet.
     * @param width     Width, in tiles.
     * @param height    Height, in tiles.
     * @param scale     Scale, in whatever units are being used.
     * @throws InvalidArgumentException
     */
    public TileSet(String name, int width, int height, int scale)
            throws InvalidArgumentException {

        if (name == null || name.length() == 0) {
            throw new InvalidArgumentException("TileSet name must not be empty");
        }

        if (width < 1 || height < 1) {
            throw new InvalidArgumentException("TileSet dimensions must be strictly positive");
        }

        if (scale < 1) {
            throw new InvalidArgumentException("TileSet scale must be strictly positive");
        }

        // Set basic variables defining this TileSet.
        this.mapName = name;
        this.mapWidth = width;
        this.mapHeight = height;
        this.mapScale = scale;

        rivers = new Vector();
        things = new Vector();
        
        this.terrain = new int[mapHeight][mapWidth];
        this.feature = new int[mapHeight][mapWidth];
        this.height = new short[mapHeight][mapWidth];
        this.writable = new boolean[mapHeight][mapWidth];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.writable[y][x] = true;
                this.terrain[y][x] = 1;
            }
        }
    }

    /**
     * Make a new copy of this TileSet object.

     */
    public Object
    clone() throws CloneNotSupportedException {
        TileSet     ts = null;

        try {
            ts = new TileSet(mapName, mapWidth, mapHeight, mapScale);

            ts.terrain = new int[mapHeight][mapWidth];
            ts.feature = new int[mapHeight][mapWidth];
            ts.height = new short[mapHeight][mapWidth];
            ts.writable = new boolean[mapHeight][mapWidth];
            
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    ts.terrain[y][x] = terrain[y][x];
                    ts.feature[y][x] = feature[y][x];
                    ts.height[y][x] = height[y][x];
                    ts.writable[y][x] = writable[y][x];
                }
            }
        } catch (InvalidArgumentException e) {
            // Something gone wrong really here, how could the original
            // settings be invalid?
            throw new CloneNotSupportedException("Original object invalid, cannot clone");
        }

        return (Object)ts;
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

        if (things == null) {
            // Nothing to do.
            return;
        }

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
            if (thing.getX() > mapWidth * 100) okay = false;
            if (thing.getY() > mapHeight * 100) okay = false;

            if (okay) {
                list.add(thing);
            }
        }

        setThings(list);
    }

    /**
     * Translate all paths (rivers, roads) to match the new origin.
     * Removal of paths that don't fit on the new map is not currently
     * supported.
     */
    private void
    cropAllPaths(int x, int y) {
        Path        path = null;
        Vector      list = new Vector();
        // Translation coords are in tiles. Things are positioned in
        // hundreths of a tile.
        x *= 100;
        y *= 100;

        if (rivers == null) {
            // Nothing to do.
            return;
        }

        for (int i=0; i < rivers.size(); i++) {
            boolean     okay = true;

            path = (Path)rivers.elementAt(i);
            path.move(0-x, 0-y);

            if (path.getMinX() > width * 100) okay = false;
            if (path.getMinY() > height * 100) okay = false;
            if (path.getMaxX() < 0) okay = false;
            if (path.getMaxY() < 0) okay = false;

            if (okay) {
                list.add(path);
            }
        }

        setPaths(list);
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
        cropAllPaths(x, y);
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
    scaleAllPaths(double factor) {
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

        setPaths(list);
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
        scaleAllPaths(factor);

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
        System.out.println("scaleSmaller: "+scale+" -> "+newScale);
        if (scale%newScale != 0) {
            // Can only cope with exact multiples.
            //return false;
        }

        double      factor = scale / newScale;
        int         newWidth = (width * scale)/newScale;
        int         newHeight = (height * scale)/newScale;

        System.out.println("New width x height = "+newWidth+"x"+newHeight);

        Tile[][]    scaled = new Tile[newHeight][newWidth];

        // top and bottom take care of the gaps caused by hexes at
        // the top and bottom edges of the map.
        for (int x=0; x < newWidth; x++) {
            boolean     xIsEven = (((x * scale)/newScale)%2 == 0);
            int         ox = (x * newScale)/scale;

            int         top = (int)(0.5 + (0.5 * scale / newScale));
            int         bottom = (int)(0.5 * scale / newScale);

            if (xIsEven) {
                for (int y = newHeight - bottom; y < newHeight; y++) {
                    scaled[y][x] = new Tile(tiles[height-1][ox]);
                }
            } else {
                for (int y = 0; y < top; y++) {
                    scaled[y][x] = new Tile(tiles[0][ox]);
                }
            }
            for (int y = 0; y < newHeight; y++) {
                int     oy = (y * newScale)/scale;
                scaled[y][x] = new Tile(tiles[oy][ox]);
            }
        }
        width = newWidth;
        height = newHeight;
        tiles = scaled;
        scale = newScale;

        return true;
    }

    /**
     * Throw an exception if the provided coordinates are outside of the
     * map boundary.
     *
     * @param x    X coordinate of point to check.
     * @param y    Y coordinate of point to check.
     */
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

    /**
     * Set a given tile according to information in a blob.
     * Tiles are stored in the XML file as a Base64 encoded blob of data,
     * 10 characters long (8 characters for older formats). The data is
     * unencoded, and attributes on the tile are set from this data.
     * <br/>
     * A blob consists of the following format: tthhmmaacf
     * <br/>
     * tt - Terrain<br/>
     * hh - Height (not yet supported)<br/>
     * mm - Mountains/features<br/>
     * aa - Area<br/>
     * c  - Coastline flags (not yet supported)<br/>
     * f  - Misc flags (not yet supported)<br/>
     *
     * @param x    X coordinate of tile to set.
     * @param y    Y coordinate of tile to set.
     */
    public void
    setTileFromBlob(int x, int y, String blob) throws MapOutOfBoundsException {
        Tile    tile = null;
        short   terrain;
        short   height;
        short   hills;
        int     area;

        if (blob.length() == 8) {
            // Old 0.0.X format file, 8 chars per blob.
            terrain = (short)fromBase64(blob.substring(0, 2));
            height = (short)fromBase64(blob.substring(2, 5));
            hills = (short)fromBase64(blob.substring(5, 6));
            area = fromBase64(blob.substring(7, 8));
        } else {
            // New format, 10 chars per blob.
            terrain = (short)fromBase64(blob.substring(0, 2));
            height = (short)fromBase64(blob.substring(2, 4));
            hills = (short)fromBase64(blob.substring(4, 6));
            area = fromBase64(blob.substring(6, 8));
        }

        height -= 100000; // Baseline

        tile = new Tile(terrain, height, (terrain!=0));
        tile.setFeature(hills);
        tile.setArea((short)area);

        setTile(x, y, tile);
    }

    public String
    getTileAsBlob(int x, int y) throws MapOutOfBoundsException {
        StringBuffer    blob = new StringBuffer();
        Tile            tile = null;

        tile = tiles[y][x];

        // Set the terrain data.
        blob.append(toBase64(tile.getTerrainRaw(), 2));
        // Set height data.
        // blob.append(toBase64(tile.getHeight()+1000, 2));
        blob.append("AA");
        blob.append(toBase64(tile.getFeatureRaw(), 2));
        blob.append(toBase64(tile.getArea(), 2));
        blob.append("A"); // Coasts
        blob.append("A"); // Flags

        return blob.toString();
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
                    System.out.println(moobe);
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

        if (found) {
            crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
        }
    }


    /**
     * Crop the tiles to the highlighted region. If nothing is highlighted,
     * then no cropping is performed.
     */
    public void
    cropToHighlighted() throws MapOutOfBoundsException {
        int     minX, minY, maxX, maxY;
        int     x, y;
        boolean found = false;

        minX = minY = maxX = maxY = -1;
        for (x=0; x < width; x++) {
            for (y=0; y < height; y++) {
                try {
                    if (isHighlighted(x, y)) {
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
                    System.out.println(moobe);
                }
            }
        }

        if (found) {
            crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
        }
    }

    public void
    cropToThing(String name, short radius) throws MapException {
        Thing   thing = getThing(name);

        if (thing == null) {
            throw new MapException("Cannot find thing ["+name+"] to crop to");
        }

        int     xp = thing.getX() / 100;
        int     yp = thing.getY() / 100;
        int     minX, minY, maxX, maxY;

        minX = xp - radius;
        minY = yp - radius;
        maxX = xp + radius;
        maxY = yp + radius;

        if (minX < 0) minX = 0;
        if (minY < 0) minY = 0;
        if (maxX >= width) maxX = width - 1;
        if (maxY >= height) maxY = height - 1;

        crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
    }

    public void
    cropToPath(String name, short margin) throws MapException {
        Path    path = getPath(name);

        if (path == null) {
            throw new MapException("Cannot find path ["+name+"] to crop to");
        }

        int     minX = path.getMinX()/100 - margin;
        int     minY = path.getMinY()/100 - margin;
        int     maxX = path.getMaxX()/100 + margin;
        int     maxY = path.getMaxY()/100 + margin;

        if (minX < 0) minX = 0;
        if (minY < 0) minY = 0;
        if (maxX >= width) maxX = width - 1;
        if (maxY >= height) maxY = height - 1;

        crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
    }

    /**
     * Replace all the rivers with the new set of rivers.
     */
    void
    setPaths(Vector rivers) {
        this.rivers = rivers;
    }

    void
    setThings(Vector things) {
        this.things = things;
    }

    Vector
    getPaths() {
        return rivers;
    }

    Vector
    getThings() {
        return things;
    }

    /**
     * Get a list of the names of all the things in this TileSet.
     * The list is returned as an array of Strings. It is not guaranteed
     * that all things will have unique names.
     *
     * @return      Array of strings holding all things in the map.
     *              null if there are no things defined.
     */
    public String[]
    getThingNames() {
        if (things.size() == 0) {
            return null;
        }

        String[]    names = new String[things.size()];

        for (int i=0; i < things.size(); i++) {
            names[i] = ((Thing)things.elementAt(i)).getName();
        }

        return names;
    }

    public Thing
    getThing(String name) {
        Thing       thing = null;

        for (int i=0; i < things.size(); i++) {
            thing = (Thing)things.elementAt(i);

            if (thing.getName().equals(name)) {
                return thing;
            }
        }

        return null;
    }

    /**
     * Get the names of roads and rivers as an array. The type should be
     * either 0 for all types, Path.ROAD for roads or Path.RIVER for rivers.
     * If no paths of the given type are found, then null is returned.
     */
    public String[]
    getPathNames(short type) {
        String[]    names = null;
        int         count = 0;

        if (type == 0) {
            count = rivers.size();
        } else {
            for (int i = 0; i < rivers.size(); i++) {
                Path    path = (Path)rivers.elementAt(i);
                if (path.getType() == type) {
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }

        names = new String[count];
        for (int i = 0, j = 0; i < rivers.size(); i++) {
            Path        path = (Path)rivers.elementAt(i);
            if (type == 0 || path.getType() == type) {
                names[j++] = path.getName();
            }
        }

        return names;
    }

    Path
    getPath(int id) {
        Path        path;

        id--;
        if (id > rivers.size()) {
            return null;
        }
        path = (Path)rivers.elementAt(id);

        return path;
    }

    Path
    getPath(String name) {
        Path        path;

        for (int i = 0; i < rivers.size(); i++) {
            path = (Path)rivers.elementAt(i);

            if (path.getName().equals(name)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Create and add a new river or road to the map.
     */
    int
    addPath(String name, int x, int y) {
        rivers.add(new Path(name, x, y));

        return rivers.size();
    }

    int
    addPath(String name, short type, short style, int x, int y) {
        rivers.add(new Path(name, type, style, x, y));

        return rivers.size();
    }


    void
    extendPath(int id, int x, int y) {
        Path    river = getPath(id);

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

    public short
    getFeatureRotation(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getFeatureRotation();
    }

    public void
    setFeatureRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        getTile(x, y).setFeatureRotation(rotation);
    }

    public short
    getTerrainRotation(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).getTerrainRotation();
    }

    public void
    setTerrainRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        getTile(x, y).setTerrainRotation(rotation);
    }

    /**
     * Check to see if the given tile is highlighted.
     */
    public boolean
    isHighlighted(int x, int y) throws MapOutOfBoundsException {
        return getTile(x, y).isHighlighted();
    }

    public void
    setHighlighted(int x, int y, boolean hl) throws MapOutOfBoundsException {
        getTile(x, y).setHighlighted(hl);
    }

    public static final String BASE64 = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                                                   "abcdefghijklmnopqrstuvwxyz"+
                                                   "0123456789");

    /**
     * Convert an integer into its Base64 representation as a string
     * of the specified width. If the resulting string is too short,
     * then it is padded with 'A' (0).
     */
    public static String
    toBase64(int value, int width) {
        String  result = "";

        while (value > 0) {
            int     digit = value % 64;
            value /= 64; // Integer division.

            result = BASE64.substring(digit, digit+1) + result;
        }

        while (result.length() < width) {
            result = "A"+result;
        }

        return result;
    }

    /**
     * Convert a Base64 string into an integer.
     */
    public static int
    fromBase64(String base64) {
        int         value = 0;
        int         i = 0;
        int         c = 0;

        for (i = 0; i < base64.length(); i++) {
            c = BASE64.indexOf(base64.substring(i, i+1));
            value += c * (int)Math.pow(64, base64.length() - i -1);
        }

        return value;
    }

    /**
     * Find all occurrences of a given area on a map, and change them to be
     * the new area. Often used when an area is deleted, and it is to be set
     * to the parent area (or zero, if it has no parent).
     * 
     * @param oldArea       Area to be found and changed.
     * @param newArea       Area to set to, or zero to delete.
     */
    public void
    changeArea(short oldArea, short newArea) {
        System.out.println("changeArea: "+oldArea+" -> "+newArea);
        if (oldArea == 0) {
            // Do nothing.
            return;
        }
        for (int x=0; x < width; x++) {
            for (int y=0; y < height; y++) {
                try {
                    if (getArea(x, y) == oldArea) {
                        setArea(x, y, newArea);
                    }
                } catch (MapOutOfBoundsException e) {
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setTerrain(int, int, net.sourceforge.mapcraft.map.elements.Terrain)
     */
    public void setTerrain(int x, int y, Terrain terrain) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getMapHeight()
     */
    public int getMapHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getMapWidth()
     */
    public int getMapWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setFeature(int, int, net.sourceforge.mapcraft.map.elements.Terrain)
     */
    public void setFeature(int x, int y, Terrain feature) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setPaths(net.sourceforge.mapcraft.map.elements.Path[])
     */
    public void setPaths(Path[] paths) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setThings(net.sourceforge.mapcraft.map.elements.Thing[])
     */
    public void setThings(Thing[] things) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#removeThing(net.sourceforge.mapcraft.map.elements.Thing)
     */
    public void removeThing(Thing thing) {
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

