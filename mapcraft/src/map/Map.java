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
public class Map implements Cloneable {
    // Basic indentity fields.
    private String  filename;
    private String  name;
    private String  version;
    private String  date;

    private String  author;
    private String  id;
    private String  parent;
    private String  format;
    private String  imagedir;

    // XML backend data.
    private MapXML  xml;

    // Data sets
    TerrainSet      terrainSet = null;
    TerrainSet      thingSet = null;
    TerrainSet      featureSet = null;
    TileSet         tileSets[] = null;
    AreaSet         areaSet = null;

    private int     tileShape = HEXAGONAL;
    private int     type = WORLD;

    // State fields
    private String  currentSetName = null;
    private int     currentSet = 0;

    // Constants
    public static final int SQUARE = 1;
    public static final int HEXAGONAL = 2;

    public static final int WORLD = 1;
    public static final int LOCAL = 2;

    /**
     * Create a new Map object from an old one.
     */
    public
    Map(Map map) {
        filename = map.filename;
        name = map.name;
        version = map.version;
        date = map.date;
        author = map.author;
        id = map.id;
        parent = map.parent;
        format = map.format;
        imagedir = map.imagedir;
        xml = null;

        terrainSet = map.terrainSet;
        thingSet = map.thingSet;
        featureSet = map.featureSet;
        tileSets = map.tileSets;
        areaSet = map.areaSet;
        setPaths(map.getPaths());
        setThings(map.getThings());

        tileShape = map.tileShape;
        type = map.type;

        currentSetName = map.currentSetName;
        currentSet = map.currentSet;
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
        this.name = name;
        this.id = name+"."+System.currentTimeMillis();
        this.parent = "none";
        this.author = "Unknown";

        tileSets = new TileSet[1];
        tileSets[0] = new TileSet("root", width, height, scale);
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
        this.name = name;
        this.id = name+"."+System.currentTimeMillis();
        this.parent = "none";
        this.author = "Unknown";

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
     */
    public void
    fork() {
        for (int i=0; i < tileSets.length; i++) {
            tileSets[i].setParent(tileSets[i].getScale(), 0, 0);
        }
        parent = id;
        id = id + "." + System.currentTimeMillis();
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
        this.filename = filename;
        try {
            System.out.println("Loading map");
            xml = new MapXML(filename);
            tileSets = xml.getTileSets();
            terrainSet = xml.getTerrainSet("basic");
            thingSet = xml.getTerrainSet("things");
            thingSet.setAnySize(true);
            featureSet = xml.getTerrainSet("features");
            areaSet = xml.getAreas();

            for (int i = 0; i < tileSets.length; i++) {
                setCurrentSet(i);
                setThings(xml.getThings(tileSets[i].getName()));
                setPaths(xml.getPaths(tileSets[i].getName()));
            }

            this.filename = filename;
            this.name = xml.getName();
            this.format = xml.getFormat();
            this.imagedir = xml.getImageDir();
            this.id = xml.getId();
            this.parent = xml.getParent();
            this.author = xml.getAuthor();
            setTileShape(xml.getTileShape());
            setType(xml.getType());

            System.out.println("LOADED MAP ["+name+"]");
            System.out.println("Author     "+author);
            System.out.println("Id         "+id+"/"+parent);
            System.out.println("Shape      "+((tileShape == SQUARE)?"Square":"Hexagonal"));

            setCurrentSet("root");
        } catch (MapException mape) {
            throw mape;
        } catch (Exception e) {
            throw new MapException("Failed to load map ("+e.getMessage()+")");
        }
    }


    public Object
    clone() throws CloneNotSupportedException {
        Map     m = null;

        try {
            m = new Map(name, tileSets[0].getWidth(), tileSets[0].getHeight(),
                        tileSets[0].getScale());
            m.filename = "/tmp/clone.map";
            //m.tiles = (TileSet)tiles.clone();
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

    public String getName() { return name; }

    public String getFilename() { return filename; }

    public String getImageDir() { return imagedir; }

    public void
    setImageDir(String imagedir) {
        this.imagedir = imagedir;
    }

    /**
     * Get the shape of the tiles - either SQUARE or HEXAGONAL.
     */
    public int getTileShape() { return tileShape; }

    /**
     * Set the shape of the tiles to use for the map, either
     * SQUARE or HEXAGONAL.
     */
    public void setTileShape(int shape) {
        this.tileShape = shape;
    }

    /**
     * Set the shape of the tiles, using the name of the
     * shape rather than the integral value.
     */
    public void
    setTileShape(String shape) {
        if (shape.equals(MapXML.SQUARE)) {
            this.tileShape = SQUARE;
        } else {
            this.tileShape = HEXAGONAL;
        }
    }

    public void
    setType(String typeName) {
        if (typeName.equals(MapXML.LOCAL)) {
            this.type = LOCAL;
        } else {
            this.type = WORLD;
        }
    }

    public void
    setType(int type) {
        this.type = type;
    }

    public int getType() { return type; }

    /**
     * Get the width of the current tile set.
     */
    public int
    getWidth() {
        return getWidth(currentSet);
    }

    /**
     * Get the width of the specified tile set.
     */
    public int
    getWidth(int set) {
        return tileSets[set].getWidth();
    }

    /**
     * Get the height of the current tile set.
     */
    public int
    getHeight() {
        return getHeight(currentSet);
    }


    /**
     * Get the height of the specified tile set.
     */
    public int
    getHeight(int set) {
        return tileSets[set].getHeight();
    }

    /**
     * Get the scale of the current tile set.
     */
    public int
    getScale() {
        return tileSets[currentSet].getScale();
    }

    /**
     * Get the scale of the specified tile set.
     */
    public int
    getScale(int set) {
        return tileSets[set].getScale();
    }

    public int getCurrentSet() { return currentSet; }

    /**
     * Define the current TileSet to use by default. If any
     * of the query operations don't specify a TileSet, this
     * default one is used.
     */
    public void
    setCurrentSet(String set) throws MapException {
        for (int i =0; i < tileSets.length; i++) {
            if (tileSets[i].getName().equals(set)) {
                currentSetName = set;
                currentSet = i;
                return;
            }
        }

        throw new MapException("No such tile set");
    }

    public void
    setCurrentSet(int set) throws MapException {
        if (set < 0 || set >= tileSets.length) {
            throw new MapException("No such tile set");
        }

        currentSet = set;
        currentSetName = tileSets[set].getName();
    }


    public Tile
    getTile(int x, int y) throws MapOutOfBoundsException  {
        return getTile(currentSet, x, y);
    }

    /**
     * Return the tile at the given coordinate.
     */
    public Tile
    getTile(int set, int x, int y) throws MapOutOfBoundsException {
        if (set < 0 || set >= tileSets.length) {
            throw new MapOutOfBoundsException("No such tileset");
        }
        return tileSets[set].getTile(x, y);
    }

    public void
    setTile(int set, Tile tile, int x, int y) throws MapOutOfBoundsException {
        tileSets[set].setTile(x, y, tile);
    }
    
    
    public short
    getTerrain(int x, int y) throws MapOutOfBoundsException {
        return getTerrain(currentSet, x, y);
    }

    public short
    getTerrain(int set, int x, int y) throws MapOutOfBoundsException {
        return tileSets[set].getTerrain(x, y);
    }


    public short
    getFeature(int x, int y) throws MapOutOfBoundsException {
        return tileSets[currentSet].getTile(x, y).getFeature();
    }

    public  Vector
    getThings() {
        return tileSets[0].getThings();
    }

    public void
    setThings(Vector things) {
        tileSets[0].setThings(things);
    }

    public void
    removeThing(int s) {
        tileSets[0].removeThing(s);
    }

    public void
    addThing(Thing s) {
        tileSets[0].addThing(s);
    }

    /**
     * Set the terrain of the given tile for the currently
     * selected TileSet.
     *
     * @param x     X coordinate of Tile.
     * @param y     y coordinate of Tile.
     * @param t     Terrain value to set Tile to.
     *
     * @throws MapOutOfBoundsException  Coordinates outside TileSet.
     */
    public void
    setTerrain(int x, int y, short t) throws MapOutOfBoundsException {
        setTerrain(currentSet, x, y, t);
    }

    /**
     * Set the terrain of the given tile for the specified
     * TileSet.
     *
     * @param set   Id of TileSet.
     * @param x     X coordinate of Tile.
     * @param y     y coordinate of Tile.
     * @param t     Terrain value to set Tile to.
     *
     * @throws MapOutOfBoundsException  Coordinates outside TileSet.
     */
    public void
    setTerrain(int set, int x, int y, short t) throws MapOutOfBoundsException {
        tileSets[set].setTerrain(x, y, t);
    }

    public Area
    getArea(int set, int x, int y) throws MapOutOfBoundsException {
        int a = tileSets[set].getArea(x, y);
        return areaSet.getArea(a);
    }

    public Area
    getArea(int x, int y) throws MapOutOfBoundsException {
        return getArea(currentSet, x, y);
    }

    /**
     * Get the descriptive name of the area of the given tile.
     */
    public String
    getAreaName(int set, int x, int y) throws MapOutOfBoundsException {
        int     a = tileSets[set].getArea(x, y);
        Area    area = areaSet.getArea(a);
        String  name = null;

        if (area != null) {
            name = area.getName();
        }

        return name;
    }

    public Area
    getAreaParent(int set, int x, int y) throws MapOutOfBoundsException {
        Area    area = getArea(set, x, y);
        Area    parent = null;
        int     pid = 0;

        if (area != null) {
            area.getParent();
        }

        if (pid > 0) {
            parent = areaSet.getArea(pid);
        }

        return parent;
    }

    public Area
    getAreaParent(int x, int y) throws MapOutOfBoundsException {
        return getAreaParent(currentSet, x, y);
    }

    public int
    getAreaParentId(int id) {
        Area    area = areaSet.getArea(id);
        int     parent = 0;

        if (area != null) {
            parent = area.getParent();
        }

        return parent;
    }

    public String
    getAreaParentName(int set, int x, int y) throws MapOutOfBoundsException {
        Area    parent = getAreaParent(set, x, y);
        String  name = null;

        if (parent != null) {
            name = parent.getName();
        }

        return name;
    }

    public void
    setArea(int set, int x, int y, short area) throws MapOutOfBoundsException {
        tileSets[set].setArea(x, y, area);
    }

    public void
    setArea(int x, int y, short area) throws MapOutOfBoundsException {
        setArea(currentSet, x, y, area);
    }

    public Area
    getAreaByName(String name) {
        System.out.println("Looking for ["+name+"]");
        return areaSet.getArea(name);
    }

    public short
    getHeight(int x, int y) throws MapOutOfBoundsException {
        return tileSets[currentSet].getHeight(x, y);
    }

    public boolean
    isWritable(int x, int y) throws MapOutOfBoundsException {
        return tileSets[currentSet].isWritable(x, y);
    }

    public void
    setFeatureRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        if (tileShape == SQUARE) {
            rotation /= 90;
        } else {
            rotation /= 60;
        }
        tileSets[currentSet].setFeatureRotation(x, y, rotation);
    }

    public short
    getFeatureRotation(int x, int y) throws MapOutOfBoundsException {
        short r = tileSets[currentSet].getFeatureRotation(x, y);

        if (tileShape == SQUARE) {
            r *= 90;
        } else {
            r *= 60;
        }
        return r;
    }

    public void
    setTerrainRotation(int x, int y, short rotation) throws MapOutOfBoundsException {
        if (tileShape == SQUARE) {
            rotation /= 90;
        } else {
            rotation /= 60;
        }
        tileSets[currentSet].setTerrainRotation(x, y, rotation);
    }

    public short
    getTerrainRotation(int x, int y) throws MapOutOfBoundsException {
        short r = tileSets[currentSet].getTerrainRotation(x, y);

        if (tileShape == SQUARE) {
            r *= 90;
        } else {
            r *= 60;
        }
        return r;
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
        tileSets[currentSet].setScale(scale);
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
        TileSet     original = tileSets[0];
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

        resized = new TileSet(tileSets[0].getName(), newWidth,
                                          newHeight, tileSets[0].getScale());

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
        tileSets[0] = resized;

    }

    /**
     * Crop the specified tileset to the given rectangle.
     */
    public boolean
    crop(int set, int x, int y, int w, int h) {
        try {
            tileSets[set].crop(x, y, w, h);
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
    cropToArea(int set, short area, int margin) {
        try {
            System.out.println("Cropping to area "+area+" with margin "+margin);
            tileSets[set].cropToArea(area, margin);
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }

        return true;
    }



    private void
    writeTerrainSet(TerrainSet set, FileWriter writer) throws IOException {
        writer.write("    <terrainset id=\""+set.getId()+
                     "\" path=\""+set.getPath()+"\">\n");

        // Iterate over the terrain types, and save them out.
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            Terrain t = (Terrain)iter.next();
            String  path = t.getImagePath();
            path = path.replaceAll(".*/", "");

            writer.write("        <terrain id=\""+t.getId()+"\">\n");
            writer.write("            <name>"+t.getName()+"</name>\n");
            writer.write("            <description>"+t.getDescription()+"</description>\n");
            writer.write("            <image>"+path+"</image>\n");
            writer.write("            <solid value=\"false\"/>\n");
            writer.write("        </terrain>\n");
        }
        writer.write("    </terrainset>\n");
    }

    private void
    writeAreaSet(FileWriter writer) throws IOException {
        if (areaSet == null) {
            return;
        }
        writer.write("    <areas>\n");
        Iterator    iter = areaSet.iterator();
        while (iter.hasNext()) {
            Area    a = (Area)iter.next();

            writer.write("        <area id=\""+a.getId()+"\" name=\""+a.getName()+"\"/>\n");
        }
        writer.write("    </areas>\n");
    }

    /**
     * Write out the header of the map file.
     */
    private void
    writeHeader(FileWriter writer) throws IOException {
        writer.write("    <header>\n");
        writer.write("        <name>"+name+"</name>\n");
        writer.write("        <author>"+author+"</author>\n");
        writer.write("        <id>"+id+"</id>\n");
        writer.write("        <parent>"+parent+"</parent>\n");
        writer.write("        <cvs>\n");
        writer.write("            <version>$Revision$</version>\n");
        writer.write("            <date>$Date$</date>\n");
        writer.write("        </cvs>\n");
        if (getType() == LOCAL) {
            writer.write("        <type>Local</type>\n");
        } else {
            writer.write("        <type>World</type>\n");
        }
        if (getTileShape() == SQUARE) {
            writer.write("        <shape>Square</shape>\n");
        } else {
            writer.write("        <shape>Hexagonal</shape>\n");
        }
        writer.write("        <imagedir>"+imagedir+"</imagedir>\n");
        writer.write("        <format>0.2.1</format>\n");
        writer.write("    </header>\n");
    }

    /**
     * Write out tileset data as XML. The data is written on a per
     * column basis, with each column written as a 'blob' of base64
     * data. Whitespace is ignored in the blob, so is used to make it
     * more readable. Each Tile is represented by 8 characters of
     * information, and there are eight tiles per row (64 character
     * wide lines).
     *
     * Data is stored as follows: tthhhmca
     *      tt = terrain type
     *      hhh = height (m), 0= -100km
     *      m = mountains/hills
     *      c = coastline flags
     *      a = area
     *
     * Base64 encoding is as follows:
     * ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/
     */
    private void
    writeTileSet(TileSet set, FileWriter writer) throws IOException {
        int         x,y;

        writer.write("    <tileset id=\""+set.getName()+"\">\n");
        writer.write("        <dimensions>\n");
        writer.write("            <scale>"+set.getScale()+"</scale>\n");
        writer.write("            <width>"+set.getWidth()+"</width>\n");
        writer.write("            <height>"+set.getHeight()+"</height>\n");
        writer.write("        </dimensions>\n\n");

        if (set.isChild()) {
            writer.write("        <parent>\n");
            writer.write("            <scale>"+set.getParentsScale()+"</scale>\n");
            writer.write("            <x>"+set.getParentsXOffset()+"</x>\n");
            writer.write("            <y>"+set.getParentsYOffset()+"</y>\n");
            writer.write("        </parent>\n\n");
        }

        writer.write("        <tiles>\n");

        for (x=0; x < set.getWidth(); x++) {
            writer.write("            ");
            writer.write("<column x=\""+x+"\">");

            StringBuffer    terrain = new StringBuffer();
            String          tmp;

            for (y=0; y < set.getHeight(); y++) {
                try {
                    Tile    tile = set.getTile(x, y);
                    String  t="AA", h="AA", m="AA", c="A", f="A", a="AA";

                    try {
                        t = MapXML.toBase64(tile.getTerrainRaw(), 2);
                        h = MapXML.toBase64(tile.getHeight()+1000, 2);
                        h = "AA"; // HACK!
                        m = MapXML.toBase64(tile.getFeatureRaw(), 2);
                        a = MapXML.toBase64(tile.getArea(), 2);
                        c = "A";
                        f = "A";
                    } catch (Exception e) {
                        System.out.println("Got exception writing tile "+x+","+y);
                        System.out.println(tile);
                        System.exit(0);
                    }
                    c = "A";

                    tmp = t + h + m + a + c + f + " ";

                    if ((y%5)==0) {
                        terrain.append("\n");
                        terrain.append("                ");
                    }
                    terrain.append(tmp);
                } catch (MapOutOfBoundsException e) {
                }
            }
            writer.write(terrain.toString());
            writer.write("\n");
            writer.write("            ");
            writer.write("</column>\n");
        }
        writer.write("        </tiles>\n");

        // Now write out the rivers for this set.
        writePaths(writer);
        // ... and the things.
        writeThings(writer);

        writer.write("    </tileset>\n\n");
        writer.flush();
    }

    public void
    writePaths(FileWriter writer) throws IOException {
        int         i = 0, e = 0;
        Vector      rivers = getPaths();
        Path        river = null;
        Vector      elements;
        String[]    types = { "unknown", "start", "end", "path", "join" };

        if (rivers == null || rivers.size() == 0) {
            return;
        }

        writer.write("        <paths>\n");
        for (i=0; i < rivers.size(); i++) {
            river = (Path)rivers.elementAt(i);
            writer.write("            <path name=\""+river.getName()+"\" "+
                         "type=\""+river.getTypeAsString()+"\" style=\""+
                         river.getStyleAsString()+"\">\n");
            elements = river.getElements();
            for (e=0; e < elements.size(); e++) {
                Path.Element    element = (Path.Element)elements.elementAt(e);
                int             x = element.getX();
                int             y = element.getY();
                int             w = river.getWidth();
                int             t = element.getType();

                writer.write("                ");
                writer.write("<"+types[t]+" x=\""+x+"\" y=\""+y+"\" width=\""+w+"\"/>\n");
            }
            writer.write("            </path>\n\n");
        }
        writer.write("        </paths>\n");

    }

    public void
    writeThings(FileWriter writer) throws IOException {
        Vector      things = getThings();
        String      pad = "        ";
        int         i = 0;

        if (things != null && things.size() > 0) {
            writer.write(pad+"<things>\n");
            for (i=0; i < things.size(); i++) {
                Thing    thing = (Thing)things.elementAt(i);
                writer.write(pad+"    <thing type=\""+thing.getType()+"\" "+
                                        "x=\""+thing.getX()+
                                        "\" y=\""+thing.getY()+"\" "+
                                        "rotation=\""+thing.getRotation()+"\">\n");
                writer.write(pad+"        <name>"+thing.getName()+"</name>\n");
                writer.write(pad+"        <description>");
                writer.write(thing.getDescription());
                writer.write("</description>\n");
                writer.write(pad+"        <font>"+thing.getFontSize()+"</font>\n");
                writer.write(pad+"        <importance>"+thing.getImportance()+"</importance>\n");

                if (thing.getPropertyCount() > 0) {
                    writer.write(pad+"        <properties>\n");
                    Enumeration     keys = thing.getProperties().keys();
                    while (keys.hasMoreElements()) {
                        String  key = (String)keys.nextElement();
                        String  value = thing.getProperty(key);

                        writer.write(pad+"            ");
                        writer.write("<property name=\""+key+"\">"+value+"</property>\n");
                    }

                    writer.write(pad+"        </properties>\n");
                }

                writer.write(pad+"    </thing>\n");
            }
            writer.write(pad+"</things>\n");
        }
    }


    /**
     * Save the map as an XML file.
     */
    public void
    save(String filename) throws IOException {
        FileWriter      writer = new FileWriter(filename);
        int             x, y;
        int             i;

        System.out.println("Saving map");

        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<map>\n");

        // Header.
        writeHeader(writer);

        // Terrain Sets
        writer.write("    <!-- Standard terrain set -->\n");
        writeTerrainSet(terrainSet, writer);
        writer.write("    <!-- Things -->\n");
        writeTerrainSet(thingSet, writer);
        writer.write("    <!-- Features -->\n");
        writeTerrainSet(featureSet, writer);

        writer.write("    <!-- TileSets contain blob data for the tiles -->\n");
        writer.write("    <!-- Format is: \"tthhffaacu\" for each tile. -->\n");
        writer.write("    <!--     tt = Terrain type    hh = height     -->\n");
        writer.write("    <!--     ff = Feature         aa = area       -->\n");
        writer.write("    <!--     c  = coast mask      u  = unused     -->\n");
        writer.write("    <!-- Each blob is Base 64 encoded.            -->\n");
        // Now go through each of the tilesets in turn.
        for (i=0; i < tileSets.length; i++) {
            try {
                setCurrentSet(i);
                writeTileSet(tileSets[i], writer);
            } catch (MapException me) {
            }
        }

        writeAreaSet(writer);
        writer.write("</map>\n");

        writer.close();
    }

    /**
     * Load the map from an XML file.
     *
     * @param filename  Location to load map from.
     */
    public void
    load(String filename) throws IOException {
        try {
            MapXML  xml = new MapXML(filename);

            this.filename = filename;
            this.name = xml.getName();
            this.format = xml.getFormat();
            this.id = xml.getId();
            this.parent = xml.getParent();

            terrainSet = xml.getTerrainSet("basic");
            thingSet = xml.getTerrainSet("things");
            featureSet = xml.getTerrainSet("features");
            tileSets = xml.getTileSets();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
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
     * Get the terrain set used by this map. This describes
     * all the possible terrains.
     */
    public TerrainSet
    getTerrainSet() {
        return terrainSet;
    }

    public TerrainSet
    getThingSet() {
        return thingSet;
    }

    public TerrainSet
    getFeatureSet() {
        return featureSet;
    }

    /**
     * Return a set of all the areas defined in the map. This does not
     * include where the areas cover, but does say what the names of the
     * areas are, and their ids.
     *
     * @return  An AreaSet listing all the areas in the map.
     */
    public AreaSet
    getAreaSet() {
        return areaSet;
    }

    /**
     * Return a Vector of Path elements, each one representing a river.
     */
    public Vector
    getPaths() {
        return tileSets[currentSet].getPaths();
    }

    public void
    setPaths(Vector paths) {
        tileSets[currentSet].setPaths(paths);
    }

    /**
     * Returns the river with identifier 'id'. This is the position of
     * the river in the Vector, +1.
     */
    public Path
    getPath(int id) {
        return tileSets[currentSet].getPath(id);
    }





    /**
     * Create and add a new river to the map.
     */
    public int
    addPath(String name, int x, int y) {
        return tileSets[0].addPath(name, x, y);
    }

    public void
    extendPath(int id, int x, int y) {
        tileSets[0].extendPath(id, x, y);
    }

    public void
    unselectPaths() {
        Path    path = null;
        int     id = 0;

        for (id=1; id <= tileSets[0].getPaths().size(); id++) {
            System.out.println("Unselecting river "+id);
            unselectPath(id);
        }
    }

    public void
    unselectPath(int id) {
        Path    river = tileSets[0].getPath(id);

        river.setHighlighted(false);
    }

    public void
    selectPath(int id) {
        Path river = getPath(id);

        river.setHighlighted(true);
    }

    /**
     * Return the distance in tiles between any two tiles. The tiles are
     * specified by their x and y coordinates.
     *
     * This does not yet work.
     */
    public static int
    distance(int x0, int y0, int x1, int y1) {
        int         d = 0;
        int         x = Math.abs(x0 - x1);
        int         y = Math.abs(y0 - y1);

        d = x + Math.abs(x - y) + y;
        d = (d+1) /2;

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

    public String
    getParent() {
        return parent;
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
