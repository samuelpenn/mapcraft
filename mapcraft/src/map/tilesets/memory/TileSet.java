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

package net.sourceforge.mapcraft.map.tilesets.memory;

import java.util.*;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.*;
import net.sourceforge.mapcraft.map.tilesets.AbstractTileSet;


/**
 * Defines a set of tiles for use in a map. The set is a two
 * dimensional array of Tile objects.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class TileSet extends AbstractTileSet implements Cloneable {
    

    protected ArrayList    paths = null;
    protected ArrayList    things = null;
    

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

        paths = new ArrayList();
        things = new ArrayList();
        
        tiles = new Tiles(width, height);
    }

    /**
     * Make a new copy of this TileSet object.

     */
    public Object
    clone() throws CloneNotSupportedException {
        TileSet     ts = null;

        try {
            ts = new TileSet(mapName, mapWidth, mapHeight, mapScale);

            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    ts.tiles.copyFrom(tiles, x, y, x, y);
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
    protected void
    cropAllThings(int x, int y) {
        Thing       thing = null;
        ArrayList   list = new ArrayList();

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

            thing = (Thing)things.get(i);
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

        things = list;
    }

    /**
     * Translate all paths (rivers, roads) to match the new origin.
     * Removal of paths that don't fit on the new map is not currently
     * supported.
     */
    protected void
    cropAllPaths(int x, int y) {
        Path        path = null;
        ArrayList   list = new ArrayList();
        // Translation coords are in tiles. Things are positioned in
        // hundredths of a tile.
        x *= 100;
        y *= 100;

        if (paths == null) {
            // Nothing to do.
            return;
        }

        for (int i=0; i < paths.size(); i++) {
            boolean     okay = true;

            path = (Path)paths.get(i);
            path.move(0-x, 0-y);

            if (path.getMinX() > mapWidth * 100) okay = false;
            if (path.getMinY() > mapHeight * 100) okay = false;
            if (path.getMaxX() < 0) okay = false;
            if (path.getMaxY() < 0) okay = false;

            if (okay) {
                list.add(path);
            }
        }

        paths = list;
   }

    /**
     * Crop this TileSet to the given size. The width and height must both
     * be positive - if not, a MapOutOfBoundsException is thrown.
     */
    public void
    crop(int x, int y, int w, int h) throws MapOutOfBoundsException {

        // X-coordinate must be even. This is because of the strange
        // effect of stuttered hexagonal tiles.
        if (x%2 != 0) {
            x-=1;
            w+=1;
        }

        int     oldScale = mapScale;
        if (isChild()) {
            oldScale = getParentsScale();
        }
        setParent(oldScale, x, y);

        // Perform sanity checks.
        checkBounds(x, y);
        checkBounds(x + w, y + h);
        if (w < 1 || h < 1) {
            throw new MapOutOfBoundsException("Crop size must be positive");
        }
        Tiles       cropped = new Tiles(w, h);
        for (int ix=0; ix < w; ix++) {
            for (int iy=0; iy < h; iy++) {
                cropped.copyFrom(tiles, x+ix, y+iy, ix, iy);
            }
        }
        tiles = cropped;
        mapWidth = w;
        mapHeight = h;

        cropAllThings(x, y);
        cropAllPaths(x, y);
    }

    private void
    scaleAllThings(double factor) {
        for (int i=0; i < things.size(); i++) {
            Thing   thing = (Thing)things.get(i);

            thing.setX((int)(thing.getX()*factor));
            thing.setY((int)(thing.getY()*factor));
        }
    }

    private void
    scaleAllPaths(double factor) {
        Path        path = null;
        ArrayList   list = new ArrayList();

        for (int i=0; i < paths.size(); i++) {
            boolean     okay = true;

            path = (Path)paths.get(i);
            path.scale(factor);

            if (okay) {
                list.add(path);
            }
        }

        setPaths((Path[])list.toArray(new Path[1]));
    }

    /**
     * Rescale the map. The scale of the tiles is changed, and, the number
     * of tiles is changed to reflect the new scale. If changing to a smaller
     * scale, then the number of tiles is increased.
     */
    public void
    rescale(int newScale) {
        double  factor = (double)mapScale / (double)newScale;

        if (newScale == mapScale) {
            // Trivial case.
            return;
        }

        int x = 0, y = 0;
        if (isChild()) {
            x = getParentsXOffset();
            y = getParentsYOffset();
        }
        setParent(mapScale, x, y);

        scaleAllThings(factor);
        scaleAllPaths(factor);

        if (newScale > mapScale) {
            scaleLarger(newScale);
        } else if (newScale < mapScale) {
            scaleSmaller(newScale);
        }
        return;
    }

    protected void
    scaleLarger(int newScale) {
        return;
    }

    /**
     * The new scale is smaller than current scale, so the map will get
     * bigger.
     */
    protected void
    scaleSmaller(int newScale) {
        System.out.println("scaleSmaller: "+mapScale+" -> "+newScale);
        if (mapScale%newScale != 0) {
            // Can only cope with exact multiples.
            // return 0;
        }

        double      factor = mapScale / newScale;
        int         newWidth = (mapWidth * mapScale)/newScale;
        int         newHeight = (mapHeight * mapScale)/newScale;

        System.out.println("New width x height = "+newWidth+"x"+newHeight);

        Tiles    scaled = new Tiles(newWidth, newHeight);

        // top and bottom take care of the gaps caused by hexes at
        // the top and bottom edges of the map.
        for (int x=0; x < newWidth; x++) {
            boolean     xIsEven = (((x * mapScale)/newScale)%2 == 0);
            int         ox = (x * newScale)/mapScale;

            int         top = (int)(0.5 + (0.5 * mapScale / newScale));
            int         bottom = (int)(0.5 * mapScale / newScale);

            if (xIsEven) {
                for (int y = newHeight - bottom; y < newHeight; y++) {
                    scaled.copyFrom(tiles, ox, mapHeight-1, x, y);
                }
            } else {
                for (int y = 0; y < top; y++) {
                    scaled.copyFrom(tiles, ox, 0, x, y);
                }
            }
            for (int y = 0; y < newHeight; y++) {
                int     oy = (y * newScale)/mapScale;
                scaled.copyFrom(tiles, ox, oy, x, y);
            }
        }
        mapWidth = newWidth;
        mapHeight = newHeight;
        tiles = scaled;
        mapScale = newScale;

        return;
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
    cropToArea(Area area, int margin) throws MapOutOfBoundsException {
        int     minX, minY, maxX, maxY;
        int     x, y;
        boolean found = false;

        minX = minY = maxX = maxY = -1;
        for (x=0; x < mapWidth; x++) {
            for (y=0; y < mapHeight; y++) {
                if (tiles.area(x, y) == area) {
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
            }
        }

        if (margin > 0) {
            minX -= margin;
            minY -= margin;
            maxX += margin;
            maxY += margin;

            if (minX < 0) minX = 0;
            if (minY < 0) minY = 0;
            if (maxX >= mapWidth) maxX = mapWidth-1;
            if (maxY >= mapHeight) maxY = mapHeight-1;
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
        for (x=0; x < mapWidth; x++) {
            for (y=0; y < mapHeight; y++) {
                if (tiles.highlighted(x, y)) {
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
        if (maxX >= mapWidth) maxX = mapWidth - 1;
        if (maxY >= mapHeight) maxY = mapHeight - 1;

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
        if (maxX >= mapWidth) maxX = mapWidth - 1;
        if (maxY >= mapHeight) maxY = mapHeight - 1;

        crop(minX, minY, maxX-minX + 1, maxY-minY + 1);
    }



    public Path[] getPaths() {
        return (Path[])paths.toArray(new Path[1]);
    }

    public Thing[] getThings() {
        return (Thing[])things.toArray(new Thing[1]);
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
            names[i] = ((Thing)things.get(i)).getName();
        }

        return names;
    }

    public Thing
    getThing(String name) {
        Thing       thing = null;

        for (int i=0; i < things.size(); i++) {
            thing = (Thing)things.get(i);

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
            count = paths.size();
        } else {
            for (int i = 0; i < paths.size(); i++) {
                Path    path = (Path)paths.get(i);
                if (path.getType() == type) {
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }

        names = new String[count];
        for (int i = 0, j = 0; i < paths.size(); i++) {
            Path        path = (Path)paths.get(i);
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
        if (id > paths.size()) {
            return null;
        }
        path = (Path)paths.get(id);

        return path;
    }

    public Path
    getPath(String name) {
        Path        path;

        for (int i = 0; i < paths.size(); i++) {
            path = (Path)paths.get(i);

            if (path.getName().equals(name)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Create and add a new river or road to the map.
     */
    public Path
    addPath(String name, short type, short style, int x, int y) {
        Path    path = new Path(name, type, style, x, y); 
        paths.add(path);

        return path;
    }


    public void
    removeThing(Thing thing) {
        things.remove(thing);
    }

    public void
    addThing(Thing thing) {
        things.add(thing);
    }
    
    public void setThings(Thing[] things) {
        this.things = new ArrayList();
        
        if (things != null) {
            for(int i=0; i < things.length; i++) {
                this.things.add(things[i]);
            }
        }
    }

    public void setPaths(Path[] paths) {
        this.paths = new ArrayList();
        
        if (paths != null) {
            for(int i=0; i < paths.length; i++) {
                this.paths.add(paths[i]);
            }
        }
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
    public int
    changeArea(Area oldArea, Area newArea) {
        int     count = 0;
        
        if (oldArea != null) {
            for (int x=0; x < mapWidth; x++) {
                for (int y=0; y < mapHeight; y++) {
                    if (tiles.area(x, y) == oldArea) {
                        tiles.setArea(x, y, newArea);
                        count++;
                    }
                }
            }
        }
        return count;
    }
}

