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
    Vector          rivers = null;
    Vector          things = null;

    private int     tileShape = HEXAGONAL;
    private int     type = WORLD;

    // State fields
    private String  currentSetName = null;
    private int     currentSet = 0;

    // Legacy
    private Tile    tiles[][];
    private int     width = 0;
    private int     height = 0;
    private int     scale = 0;

    // Constants
    public static final int SQUARE = 1;
    public static final int HEXAGONAL = 2;

    public static final int WORLD = 1;
    public static final int LOCAL = 2;


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
     * Create a new map, based on a world of a given radius.
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
            System.out.println("["+y+"] width ["+w+"] start ["+start+"]");
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
            featureSet = xml.getTerrainSet("features");
            areaSet = xml.getAreas();

            things = xml.getThings();
            rivers = xml.getRivers();

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
            m = new Map(name, width, height, scale);
            m.filename = "/tmp/clone.map";
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


    public boolean
    isRiver(int x, int y) throws MapOutOfBoundsException {
        return isRiver(currentSet, x, y);
    }

    public boolean
    isRiver(int set, int x, int y) throws MapOutOfBoundsException {
        return tileSets[set].isRiver(x, y);
    }

    public short
    getRiverMask(int x, int y) throws MapOutOfBoundsException {
        return getRiverMask(currentSet, x, y);
    }

    public short
    getRiverMask(int set, int x, int y) throws MapOutOfBoundsException {
        return tileSets[set].getRiverMask(x, y);
    }

    public short
    getFeature(int x, int y) throws MapOutOfBoundsException {
        return tileSets[0].getTile(x, y).getFeature();
    }

    public  Vector
    getThings() {
        return things;
    }

    public void
    removeThing(int s) {
        things.remove(s);
    }

    public void
    addThing(Thing s) {
        things.add(s);
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
    getArea(int x, int y) throws MapOutOfBoundsException {
        int a = tileSets[currentSet].getArea(x, y);
        return areaSet.getArea(a);
    }

    public void
    setArea(int x, int y, short area) throws MapOutOfBoundsException {
        tileSets[currentSet].setArea(x, y, area);
    }

    public short
    getHeight(int x, int y) {
        if (x < 0 || x >= width) {
            return 0;
        }
        if (y < 0 || y >= height) {
            return 0;
        }
        return tiles[y][x].getHeight();
    }

    public boolean
    isWritable(int x, int y) {
        if (x < 0 || x >= width) {
            return false;
        }
        if (y < 0 || y >= height) {
            return false;
        }
        return tiles[y][x].isWritable();
    }


    /**
     * Set the terrain of the map to be all one value.
     * Also initialises the Tile array. Should be called
     * for new maps if a load() isn't being done.
     *
     * @param terrain   Terrain value to set map to.
     */
    public void
    setBackground(short t) {
        int x, y;

        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                if (tiles[y][x] == null) {
                    tiles[y][x] = new Tile();
                }
                tiles[y][x].setTerrain(t);
            }
        }
    }

    public void
    setRandom() {
        int x, y;

        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                if (tiles[y][x] == null) {
                    tiles[y][x] = new Tile();
                }
                tiles[y][x].setTerrain((short)(Math.random()*4));
            }
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
        this.scale = scale;
    }


    /**
     * Rescales the map. The width and height of the map is
     * changed to fit the current data into the new scale.
     * It should anti-aliase the result if resolution is
     * increasing, but currently this isn't supported.
     */
    public void
    rescaleMap(int newScale) throws InvalidArgumentException {
        int         h, w; // Height and width of new map.
        int         x, y; // Iterators.

        TileSet     rescaled = null;
        TileSet     original = tileSets[0];

        System.out.println("rescaleMap: "+newScale);
        System.out.println("Old scale: "+scale+" geom "+getWidth()+"x"+getHeight());

        if (newScale == scale) {
            // Trivial.
            return;
        } else if (newScale > getScale()) {
            // Every X hexes merges into a single hex.
            w = (getWidth()*getScale())/newScale;
            h = (getHeight()*getScale())/newScale;

            rescaled = new TileSet("root", w, h, newScale);
            for (x = 0; x < w; x++) {
                for (y = 0; y < h; y++) {
                    try {
                        rescaled.setTile(x, y,
                                original.getTile((x*newScale)/getScale(),
                                                 (y*newScale)/getScale()));
                    } catch (MapOutOfBoundsException oobe) {
                    }
                }
            }
        } else {
            throw new InvalidArgumentException("Scale not supported");
        }



        tileSets[0] = rescaled;
        setScale(newScale);
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
     * Returns a new Map object, which is a cropped subset of
     * the current map object. The scale of the new object is
     * identical to the scale of the current Map.
     */
    public Map
    submap(String newName, int xoff, int yoff, int w, int h) throws MapException {
        Map     submap = new Map(newName, w, h, this.scale);
        int     x, y;

        return submap;
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
        writer.write("        <format>0.1.0</format>\n");
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
        writer.write("        </dimensions>\n");

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
                        t = MapXML.toBase64(tile.getTerrain(), 2);
                        h = MapXML.toBase64(tile.getHeight()+1000, 2);
                        h = "AA"; // HACK!
                        m = MapXML.toBase64(tile.getFeature(), 2);
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
        writer.write("    </tileset>\n\n");
        writer.flush();
    }

    public void
    writeRivers(FileWriter writer) throws IOException {
        int         i = 0, e = 0;
        Path        river = null;
        Vector      elements;
        String[]    types = { "unknown", "start", "end", "path", "join" };

        if (rivers == null || rivers.size() == 0) {
            return;
        }

        writer.write("    <rivers>\n");
        for (i=0; i < rivers.size(); i++) {
            river = (Path)rivers.elementAt(i);
            writer.write("        <river name=\""+river.getName()+"\">\n");
            elements = river.getElements();
            for (e=0; e < elements.size(); e++) {
                Path.Element    element = (Path.Element)elements.elementAt(e);
                int             x = element.getX();
                int             y = element.getY();
                int             w = element.getWidth();
                int             t = element.getType();

                writer.write("            ");
                writer.write("<"+types[t]+" x=\""+x+"\" y=\""+y+"\" width=\""+w+"\"/>\n");
            }
            writer.write("        </river>\n\n");
        }
        writer.write("    </rivers>\n");

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
            writeTileSet(tileSets[i], writer);
        }

        if (things != null && things.size() > 0) {
            writer.write("    <things>\n");
            for (i=0; i < things.size(); i++) {
                Thing    thing = (Thing)things.elementAt(i);
                writer.write("        <thing type=\""+thing.getType()+"\" "+
                                        "x=\""+thing.getX()+
                                        "\" y=\""+thing.getY()+"\">\n");
                writer.write("            <name>"+thing.getName()+"</name>\n");
                writer.write("            <description>");
                writer.write(thing.getDescription());
                writer.write("</description>\n");
                writer.write("            <font>"+thing.getFontSize()+"</font>\n");
                writer.write("            <importance>"+thing.getImportance()+"</importance>\n");

                writer.write("        </thing>\n");
            }
            writer.write("    </things>\n");
        }

        writeAreaSet(writer);
        writeRivers(writer);

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
        /*
        TerrainSet      features = new TerrainSet("features", "images/hexagonal/standard/medium");

        features.add(new Terrain((short)0, "clear", "Clear", "hills/0.png"));
        features.add(new Terrain((short)1, "lowhills", "Low hills", "hills/1.png"));
        features.add(new Terrain((short)2, "highhills", "High hills", "hills/2.png"));
        features.add(new Terrain((short)3, "foothills", "Foot hills", "hills/3.png"));
        features.add(new Terrain((short)4, "lowmnts", "Low mountains", "hills/4.png"));
        features.add(new Terrain((short)5, "highmnts", "High mountains", "hills/5.png"));
        features.add(new Terrain((short)6, "marsh", "Marshland", "hills/marsh.png"));
        features.add(new Terrain((short)7, "ice", "Ice sheet", "hills/ice.png"));

        featureSet = features;
        return features;
        */
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
    getRivers() {
        return rivers;
    }

    /**
     * Returns the river with identifier 'id'. This is the position of
     * the river in the Vector, +1.
     */
    public Path
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
    public int
    addRiver(String name, int x, int y) throws MapOutOfBoundsException {
        Path    path = new Path(name, x, y);

        tileSets[0].getTile(x, y).setRiver(true);
        rivers.add(path);

        return rivers.size();
    }

    public void
    extendRiver(int id, int x, int y) throws MapOutOfBoundsException {
        Path    river = getRiver(id);

        tileSets[0].getTile(x, y).setRiver(true);
        river.add(x, y);
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
            thing = (Thing)things.elementAt(index);
        }

        return thing;
    }

    public static void
    main(String args[]) {
        Map         map;
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
            } else if (options.isOption("-zoomout")) {
                String  filename = options.getString("-zoomout");
                map = new Map(filename);
                map.rescaleMap(125);
                map.save("new.map");
            } else if (options.isOption("-euressa")) {
                // Change a 25km map to 250km map (16x10).
                // Change to world map, of size 176x88.
                map = new Map("maps/Euressa.map");
                map.rescaleMap(250);
                map.resize(91, 28, false, false);
                map.resize(176, 88, true, true);
                map.save("new.map");
            } else if (options.isOption("-world")) {
                map = new Map("Earth", 6400, 125);
                map.loadTerrainSet("terrain/hexagonal.xml");
                map.setImageDir("hexagonal/standard");
                map.save("earth.map");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
