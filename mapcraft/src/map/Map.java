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

import uk.co.demon.bifrost.rpg.mapcraft.xml.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.*;

import uk.co.demon.bifrost.utils.Options;

/**
 * A class object which describes a region as one or more maps.
 * Each view of the map is held as a TileSet. The "root" TileSet
 * is the top level map, normally at the largest scale. Other
 * TileSets can be stored as part of the same map object, all
 * views, possibly at higher resolution, of regions within the
 * "root" TileSet.
 *
 * This class does not provide any display functionality, it
 * is merely a holder for the map data.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class Map extends MapBean implements Cloneable {

    /**
     * Create a new Map object from an old one.
     */
    public
    Map(Map map) {
        super(map);
    }

    /**
     * Create a brand new map, of the given size and scale. Details such
     * as the tile shape and terrain definitions should be given later
     * using the relevent setter methods.
     *
     * @param name      Name of the map.
     * @param width     Width of the map, in tiles.
     * @param height    Height of the map, in tiles.
     * @param scale     Map scale - what each tile represents (either km or m).
     */
    public
    Map(String name, int width, int height, int scale) throws MapException {
        super(name, width, height, scale);
    }

    /**
     * Create a new map, based on a world of a given radius. The map
     * will be a hexagonal world map. Height and width are calculated
     * from the scale, and the radius of the world. A sinusoidal
     * projection is used, so the corners of the map will be blank.
     *
     * @param name      Name of the map.
     * @param radius    World radius, in km.
     * @param scale     Tile width, in km.
     */
    public
    Map(String name, int radius, int scale) throws MapException {
        setName(name);
        setId(name+"."+System.currentTimeMillis());
        setParent("none");
        setAuthor("Unknown");

        int     height = (int) (radius * Math.PI) / scale;
        int     width = (int) (radius * 2 * Math.PI) / scale;

        tileSets = new TileSet[1];
        tileSets[0] = new TileSet("root", width, height, scale);

        int     y, x;
        for (y = 0; y < height; y++) {
            // y is the index vertically across the 'flat' map surface.
            // The proportion of the distance y gives the proportion of angle
            // traversed around the world.
            double  angle = (180.0 * (y+0.5)) / height;

            // t is the radius of the world, in 'tiles'.
            double  t = radius / scale;
            double  r = t * Math.sin(Math.toRadians(angle));

            // w is the width of the world at this point.
            int     w = (int) Math.round(2.0 * r * Math.PI);

            int     s = (int) Math.round((width/2.0) - (w/2.0));

            System.out.println("["+y+"] angle "+(int)angle+" r "+r+" w "+w+" s "+s);
            for (x = 0; x < width; x++) {
                try {
                    if (x >= s && x <= s+w) {
                        tileSets[0].setTile(x, y, new Tile((short)1, (short)0, true));
                    } else {
                        tileSets[0].setTile(x, y, new Tile((short)0, (short)0, false));
                    }
                } catch (MapOutOfBoundsException mobe) {
                }
            }

        }

    }

    /**
     * This map becomes a child of the original.
     * Should be called <b>before</b> performing some operation such as
     * crop(), so we keep a reference to the original map. This enables
     * the new map to be merged back into the parent at a later date.
     *
     * The id of the map is appended with a new identifier, to
     * distinguish it from the original.
     */
    public void
    fork() {
        for (int i=0; i < tileSets.length; i++) {
            tileSets[i].setParent(tileSets[i].getScale(), 0, 0);
        }
        setParent(getId());
        setId(getId() + "." + System.currentTimeMillis());
    }

    public void
    unwrapWorld() {
        int         y, x;
        int         width, height;
        TileSet     unwrapped = null;

        width = tileSets[0].getWidth();
        height = tileSets[0].getHeight();

        try {
            unwrapped = new TileSet("root", width, height, tileSets[0].getScale());
        } catch (InvalidArgumentException ie) {
        }

        for (y=0; y < height; y++) {
            int     w = 0;
            int     start = -1;
            for (x=0; x < width; x++) {
                try {
                    if (getTerrain(x, y) != 0) {
                        w++;
                        if (start == -1) {
                            start = x;
                        }
                    }
                } catch (MapOutOfBoundsException me) {
                }
            }
            // w now holds number of 'real' tiles in this row.
            for (x=0; x < width; x++) {
                int     i = (int)(x * (1.0 * w / width));
                try {
                    unwrapped.setTile(x, y, tileSets[0].getTile(start + i, y));
                } catch (MapOutOfBoundsException moobe) {
                }
            }
        }

        tileSets[0] = unwrapped;
    }


    /**
     * Load a map from an existing XML file and construct
     * the necessary data sets.
     *
     * @param filename  Filename of map to load.
     * @throws          MapException
     */
    public
    Map(String filename) throws MapException {
        super(filename);
    }


    public Object
    clone() throws CloneNotSupportedException {
        Map     m = null;

        try {
            m = new Map(getName(), tileSets[0].getWidth(), tileSets[0].getHeight(),
                        tileSets[0].getScale());
            m.setFilename("/tmp/clone.map");
            m.terrainSet = (TerrainSet)terrainSet.clone();
            m.thingSet = (TerrainSet)thingSet.clone();
            m.featureSet = (TerrainSet)featureSet.clone();
            m.areaSet = (AreaSet)areaSet.clone();
        } catch (MapException e) {
        }

        return (Object)m;
    }

    public void
    loadTerrainSet(String filename) {
        MapXML  xml;

        try {
            xml = new MapXML(filename);
            terrainSet = xml.getTerrainSet("basic");
            thingSet = xml.getTerrainSet("things");
            featureSet = xml.getTerrainSet("features");
            setImageDir(xml.getImageDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * Sets the scale of the map, in km per tile object.
     * The scale should be a power of two.
     * This merely changes the value of the scale, and does
     * not cause any other change to the map.
     *
     * @param scale     Width of each tile, in km. Must be a power of two.
     */
    public void
    setScale(int scale) {
        tileSets[getCurrentSet()].setScale(scale);
    }

    public void
    rescale(int set, int newScale) {
        tileSets[set].rescale(newScale);
    }


    /**
     * Resize the map. The width and height of the map is changed,
     * with blank columns and rows being added as necessary.
     *
     * @param newWidth  New width of the map, in tiles.
     * @param newHeight New height of the map, in tiles.
     * @param atRight   If true, new columns are inserted/removed on the right,
     *                  otherwise inserted/removed on the left.
     * @param atBottom  If true, new columns are inserted/removed on the bottom,
     *                  otherwise inserted/removed on the top.
     */
    public void
    resize(int newWidth, int newHeight, boolean atRight, boolean atBottom)
                throws InvalidArgumentException {

        TileSet     resized = null;
        TileSet     original = tileSets[getCurrentSet()];
        int         dx, dy;

        // Get the deltas for the changing width/height.
        dx = newWidth - original.getWidth();
        dy = newHeight - original.getHeight();

        if (dx < 0) {
            throw new InvalidArgumentException("New width must not be smaller than old width");
        }

        if (dy < 0) {
            throw new InvalidArgumentException("New height must not be smaller than old height");
        }

        if (dx == 0 && dy == 0) {
            // No op.
            return;
        }

        resized = new TileSet(tileSets[getCurrentSet()].getName(), newWidth,
                         newHeight, tileSets[getCurrentSet()].getScale());

        int     xOffset = 0;
        int     yOffset = 0;
        if (atRight) {
            xOffset = dx;
        }
        if (!atBottom) {
            yOffset = dy;
        }

        int     x, y;
        for (y = 0; y < original.getHeight(); y++) {
            for (x = 0; x < original.getWidth(); x++) {
                try {
                    resized.setTile(x+xOffset, y+yOffset, original.getTile(x, y));
                } catch (MapOutOfBoundsException oobe) {
                }
            }
        }
        tileSets[getCurrentSet()] = resized;

    }

    /**
     * Crop the specified tileset to the given rectangle.
     */
    public boolean
    crop(int x, int y, int w, int h) {
        try {
            tileSets[getCurrentSet()].crop(x, y, w, h);
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }

        return true;
    }

    /**
     * Crop the specified tileset to the given area. A rectangle is
     * calculated to surround all tiles which match the given area.
     * If a positive margin is specified, the rectangle is grown in
     * each direction by the margin size.
     */
    public boolean
    cropToArea(short area, int margin) {
        try {
            tileSets[getCurrentSet()].cropToArea(area, margin);
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }

        return true;
    }

    public boolean
    cropToHighlighted() {
        try {
            tileSets[getCurrentSet()].cropToHighlighted();
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }

        return true;
    }

    public boolean
    cropToThing(String name, short radius) {
        try {
            tileSets[getCurrentSet()].cropToThing(name, radius);
        } catch (MapException e) {
            return false;
        }

        return true;
    }

    public boolean
    cropToPath(String name, short margin) {
        try {
            tileSets[getCurrentSet()].cropToPath(name, margin);
        } catch (MapException e) {
            return false;
        }

        return true;
    }


    /**
     * Load a map from an image file. The pixels are converted
     * into tiles.
     */
    public void
    loadImage(String filename) throws IOException {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image   image = toolkit.getImage(filename);
            // The only way to get h/w without messing around with
            // Observers, is to use an ImageIcon.

            ImageIcon icon = new ImageIcon(image);
            PixelGrabber pg;
            int     w, h;

            w = icon.getIconWidth();
            h = icon.getIconHeight();
            System.out.println("Got image size of " + w +" x "+ h);
            icon = null; // No longer need the ImageIcon.

            int     pixels[] = new int[w * h];
            pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);

            System.out.println("Grabbed pixels");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Return the distance in tiles between any two tiles. The tiles are
     * specified by their x and y coordinates.
     *
     * This does not yet work.
     */
    public int
    distance(int x0, int y0, int x1, int y1) {
        int         d = 0;

        if (getTileShape() == SQUARE) {
            int         x = Math.abs(x0 - x1);
            int         y = Math.abs(y0 - y1);

            d = x + Math.abs(x - y) + y;
            d = (d+1) /2;
        } else if (getTileShape() == HEXAGONAL) {
            // Change coordinate system.
            y0 = y0 - (x0)/2;
            y1 = y1 - (x1)/2;

            // Ensure x1 > x0.
            if (x1 <= x0) {
                int     x = x0;
                int     y = y0;

                x0 = x1;
                y0 = y1;
                x1 = x;
                y1 = y;
            }

            if (y1 > y0) {
                d = x1 - x0 + y1 - y0;
            } else if (x0+y0 > x1+y1) {
                d = y0 - y1;
            } else {
                d = x1 - x0;
            }
        }

        return d;
    }

    /**
     * Returns true if the two tiles are either the same or next to each
     * other, false otherwise.
     */
    public static boolean
    isNextTo(int x0, int y0, int x1, int y1) {
        if (x0 == x1 && y0 == y1) {
            return true;
        }

        // Are they in the same column?
        if (x0 == x1) {
            if (Math.abs(y0 - y1) <= 1) {
                return true;
            }
        }

        // Are they in neighbouring columns?
        if (Math.abs(x0 - x1) == 1) {
            if (y0 == y1) {
                return true;
            }

            // x0 column is 'higher'
            if (x0 %2 == 0) {
                if (y0 == y1 + 1) {
                    return true;
                }
            } else {
                if (y0 == y1 - 1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return the (zero based) index for the nearest Thing to the given
     * x/y coordinate. If 'max' is non-zero, then any Things more than
     * 'max' away are ignored.
     */
    public int
    getNearestThingIndex(int x, int y, int max) {
        Vector  things = getThings();
        Thing   thing = null;
        int     index = -1;
        int     sx=0, sy=0;
        int     min = -1;
        int     d = 0;

        System.out.println("getNearestThingIndex: "+x+","+y+" ("+max+")");

        max = max * max;

        for (int i = 0; i < things.size(); i++) {
            thing = (Thing)things.elementAt(i);
            sx = x - thing.getX();
            sy = y - thing.getY();
            // We're only looking for the smallest distance, so no need
            // to bother getting the square root.
            d = sx * sx + sy * sy;
            System.out.println("thing: "+sx+", "+sy+" "+d);
            if ((min == -1 || d < min) && (max == 0 || d < max)) {
                min = d;
                index = i;
                System.out.println("Nearest is "+index+" at "+min);
            }
        }

        return index;
    }

    /**
     * Return nearest Thing for the given coordinates.
     * @see getNearestThingIndex
     */
    public Thing
    getNearestThing(int x, int y, int max) {
        Thing   thing = null;
        int     index = getNearestThingIndex(x, y, max);

        if (index >= 0) {
            thing = (Thing)getThings().elementAt(index);
        }

        return thing;
    }

    /**
     * Merges the supplied map into this map. Both maps must have the same
     * parent, and must be the same scale. It is assumed that they are both
     * from different regions on the parent map, but that they overlap to
     * some extent. Differences in the supplied map overwrite this map.
     */
    public boolean
    merge(Map merge) {
        System.out.println("Merging maps");

        if (!getParent().equals(merge.getParent())) {
            System.out.println("Maps do not have the same parent");
            return false;
        }

        // Alpha is our map, Beta is the map that is being merged from.
        TileSet     alpha = tileSets[0];
        TileSet     beta = merge.tileSets[0];

        if (alpha.getScale() != beta.getScale()) {
            System.out.println("Maps are not the same scale");
            return false;
        }

        // X offset, Y offset, width and height for alpha and beta maps.
        int         ax, ay, aw, ah;
        int         bx, by, bw, bh;

        ax = alpha.getParentsXOffset();
        ay = alpha.getParentsYOffset();
        aw = alpha.getWidth();
        ah = alpha.getHeight();

        bx = beta.getParentsXOffset();
        by = beta.getParentsYOffset();
        bw = beta.getWidth();
        bh = beta.getHeight();

        int         dx = ax - bx;
        int         dy = ay - by;

        // Since both have the same parent, scale should be the same.
        dx = dx * alpha.getParentsScale()/alpha.getScale();
        dy = dy * alpha.getParentsScale()/alpha.getScale();

        System.out.println("merge: dx "+dx+" dy "+dy);
        try {
            for (int y=0; y < bh; y++) {
                if (y - dy < 0 || y - dy >= ah) {
                    continue;
                }
                for (int x=0; x < bw; x++) {
                    if (x - dx < 0 || x - dx >= aw) {
                        continue;
                    }
                    Tile        b = beta.getTile(x, y);
                    Tile        a = alpha.getTile(x-dx, y-dy);
                    alpha.setTile(x-dx, y-dy, b);

                    // Now work out areas - this can be complicated.
                    String      aName = getAreaName(0, x-dx, y-dy);
                    String      bName = merge.getAreaName(0, x, y);
                    if (bName == null) {
                        // Set area to be zero.
                        alpha.setArea(x-dx, y-dy, (short)0);
                    } else if (aName == null || !aName.equals(bName)) {
                        if (getAreaByName(bName) != null) {
                            short     id = (short)getAreaByName(bName).getId();
                            alpha.setArea(x-dx, y-dy, id);
                        } else {
                            // Area doesn't exist in our map. Try the area's parent.
                            String  parent = merge.getAreaParentName(0, x, y);
                            Area    newArea = getAreaByName(parent);
                            if (newArea != null) {
                                alpha.setArea(x-dx, y-dy, (short)newArea.getId());
                            }
                        }
                    }
                }
            }
        } catch (MapOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Returns statistics on the specified area, returning an XML string
     * describing it.
     *
     * <area name="Foo">
     *     <size>45</size>
     * </area>
     */
    public String
    getAreaStats(String name) {
        StringBuffer        buffer = new StringBuffer("");
        int                 set = 0;

        try {
            Area    area = getAreaByName(name);
            int     id = area.getId();
            int     tiles = 0;

            for (int x=0; x < getWidth(set); x++) {
                for (int y=0; y < getHeight(set); y++) {
                    Area    a = getArea(set, x, y);
                    if (a != null && (a.getId() == id || a.getParent() == id)) {
                        tiles++;
                    }
                }
            }

            buffer.append("<area name=\""+name+"\"><size>"+tiles+"</size></area>");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    public String
    getAreaStats() {
        StringBuffer        buffer = new StringBuffer("");

        try {
            buffer.append("<areas>\n");

            Iterator    iter = areaSet.iterator();
            while (iter.hasNext()) {
                Area    area = (Area)iter.next();
                buffer.append(getAreaStats(area.getName())+"\n");
            }
            buffer.append("</areas>\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }


    public static void
    main(String args[]) {
        Map         map = null;
        Options     options;

        try {
            options = new Options(args);

            if (options.isOption("-create")) {
                int width = options.getInt("-width");
                int height = options.getInt("-height");
                int scale = options.getInt("-scale");
                String name = options.getString("-create");
                String terrain = options.getString("-terrain");
                boolean square = options.isOption("-square");

                System.out.println("Creating map "+name+" "+width+"x"+height);
                map = new Map(name, width, height, scale);
                if (square) {
                    map.setTileShape(Map.SQUARE);
                }
                map.loadTerrainSet(terrain);
                map.save(name+".map");
            } else if (options.isOption("-load")) {
                map = new Map(options.getString("-load"));
            } else if (options.isOption("-rewrite")) {
                map = new Map(options.getString("-rewrite"));
                map.save("new.map");
            } else if (options.isOption("-resize")) {
                int     w = options.getInt("-width");
                int     h = options.getInt("-height");
                String filename = options.getString("-resize");

                map = new Map(filename);
                map.resize(w, h, false, false);
                map.save("resized.map");
            } else if (options.isOption("-euressa")) {
                // Change a 25km map to 250km map (16x10).
                // Change to world map, of size 176x88.
                map = new Map("maps/Euressa.map");
                //map.rescaleMap(250);
                map.resize(91, 28, false, false);
                map.resize(176, 88, true, true);
                map.save("new.map");
            } else if (options.isOption("-world")) {
                map = new Map("Earth", 6400, 125);
                map.loadTerrainSet("terrain/hexagonal.xml");
                map.setImageDir("hexagonal/standard");
                map.save("earth.map");
            }

            if (map != null && options.isOption("-areastats")) {
                System.out.println(map.getAreaStats());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
