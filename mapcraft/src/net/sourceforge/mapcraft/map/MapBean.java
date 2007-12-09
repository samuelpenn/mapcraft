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

import net.sourceforge.mapcraft.xml.*;

import java.io.*;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.*;

import net.sourceforge.mapcraft.map.interfaces.*;
import net.sourceforge.mapcraft.map.elements.*;
import net.sourceforge.mapcraft.map.tilesets.TileSet;
import net.sourceforge.mapcraft.utils.Options;

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
public class MapBean implements Cloneable {
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
    ITileSet        tileSets[] = null;
    
    TerrainSet      terrainSet = null;
    TerrainSet      thingSet = null;
    TerrainSet      featureSet = null;
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

    public
    MapBean() {
    }

    /**
     * Create a new Map object from an old one.
     */
    public
    MapBean(MapBean map) {
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

        tileShape = map.tileShape;
        type = map.type;

        currentSetName = map.currentSetName;
        currentSet = map.currentSet;
    }

    /**
     * Load a map from an existing XML file and construct
     * the necessary data sets.
     *
     * @param filename  Filename of map to load.
     * @throws          MapException
     */
    public
    MapBean(String filename) throws MapException {
        this.filename = filename;
        try {
            xml = new MapXML(filename);
            tileSets = xml.getTileSets();
            terrainSet = xml.getTerrainSet("basic");
            thingSet = xml.getTerrainSet("things");
            thingSet.setAnySize(true);
            featureSet = xml.getTerrainSet("features");
            areaSet = xml.getAreas();

            for (int i = 0; i < tileSets.length; i++) {
                setCurrentSet(i);
                getTileSet(i).setThings(xml.getThings(tileSets[i].getName()));
                getTileSet(i).setPaths(xml.getPaths(tileSets[i].getName()));
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

            setCurrentSet("root");
            System.out.println("MapBean: Finished reading XML data");
        } catch (MapException mape) {
            throw mape;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MapException("Failed to load map ("+e.getMessage()+")");
        }
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
    MapBean(String name, int width, int height, int scale) throws MapException {
        setName(name);
        setId(name+"."+System.currentTimeMillis());
        setParent("none");
        setAuthor("Unknown");

        tileSets = new TileSet[1];
        tileSets[0] = new TileSet("root", width, height, scale);
    }



    public Object
    clone() throws CloneNotSupportedException {
        MapBean     m = null;

        try {
            m = new MapBean(name, tileSets[0].getMapWidth(), 
                            tileSets[0].getMapHeight(),
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


    public final String
    getName() { return name; }

    public final void
    setName(String name) {
        this.name = name;
    }

    public final String
    getFilename() { return filename; }

    public final  void
    setFilename(String filename) {
        this.filename = filename;
    }

    public final String
    getImageDir() { return imagedir; }

    public final void
    setImageDir(String imagedir) {
        this.imagedir = imagedir;
    }

    public final void
    setParent(String parent) {
        this.parent = parent;
    }

    public final String
    getId() {
        return id;
    }

    public final  void
    setId(String id) {
        this.id =  id;
    }

    public final String
    getAuthor() {
        return author;
    }

    public final  void
    setAuthor(String author) {
        this.author =  author;
    }

    /**
     * Get the shape of the tiles - either SQUARE or HEXAGONAL.
     */
    public final int
    getTileShape() { return tileShape; }

    /**
     * Set the shape of the tiles to use for the map, either
     * SQUARE or HEXAGONAL.
     */
    public final void
    setTileShape(int shape) {
        this.tileShape = shape;
    }

    /**
     * Set the shape of the tiles, using the name of the
     * shape rather than the integral value.
     */
    public final void
    setTileShape(String shape) {
        if (shape.equals(MapXML.SQUARE)) {
            this.tileShape = SQUARE;
        } else {
            this.tileShape = HEXAGONAL;
        }
    }

    public final void
    setType(String typeName) {
        if (typeName.equals(MapXML.LOCAL)) {
            this.type = LOCAL;
        } else {
            this.type = WORLD;
        }
    }

    public final void
    setType(int type) {
        this.type = type;
    }

    public final int
    getType() { return type; }

    /**
     * Get the specified TileSet. The default TileSet is always zero.
     * Each TileSet contains information on the actual map, including
     * width, height and individual tiles.
     * 
     * @param set
     * @return
     */
    public ITileSet getTileSet(int set) throws MapException {
        if (set < 0 || set >= tileSets.length) {
            throw new MapException("TileSet selection out of bounds");
        }
        return tileSets[set];
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


    public Area
    getAreaParent(int set, int x, int y) throws MapException {
        Area    area = getTileSet(set).getArea(x, y);
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
    getAreaParent(int x, int y) throws MapException {
        return getAreaParent(currentSet, x, y);
    }

    public int
    getAreaParentId(int id) {
        Area    area = areaSet.getArea(id);
        int     parent = 0;

        if (area != null && area.getParent() != null) {
            parent = area.getParent().getId();
        }

        return parent;
    }

    public String
    getAreaParentName(int set, int x, int y) throws MapException {
        Area    parent = getAreaParent(set, x, y);
        String  name = null;

        if (parent != null) {
            name = parent.getName();
        }

        return name;
    }

    public Area
    getAreaByName(String name) {
        System.out.println("Looking for ["+name+"]");
        return areaSet.getArea(name);
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
            writer.write("            <description>"+t.getDescription());
            writer.write("</description>\n");
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

            writer.write("        <area id=\""+a.getId()+"\" name=\"");
            writer.write(a.getName()+"\" ");
            writer.write("uri=\""+a.getUri()+"\"/>\n");
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
    writeTileSet(ITileSet set, FileWriter writer) throws IOException {
        int         x,y;

        writer.write("    <tileset id=\""+set.getName()+"\">\n");
        writer.write("        <dimensions>\n");
        writer.write("            <scale>"+set.getScale()+"</scale>\n");
        writer.write("            <width>"+set.getMapWidth()+"</width>\n");
        writer.write("            <height>"+set.getMapHeight()+"</height>\n");
        writer.write("        </dimensions>\n\n");

        if (set.isChild()) {
            writer.write("        <parent>\n");
            writer.write("            <scale>"+set.getParentsScale()+"</scale>\n");
            writer.write("            <x>"+set.getParentsXOffset()+"</x>\n");
            writer.write("            <y>"+set.getParentsYOffset()+"</y>\n");
            writer.write("        </parent>\n\n");
        }

        writer.write("        <tiles>\n");

        for (x=0; x < set.getMapWidth(); x++) {
            writer.write("            ");
            writer.write("<column x=\""+x+"\">");

            StringBuffer    terrain = new StringBuffer();
            String          tmp;

            for (y=0; y < set.getMapHeight(); y++) {
                tmp = MapXML.tileToBlob(set, x, y);

                if ((y%5)==0) {
                    terrain.append("\n");
                    terrain.append("                ");
                }
                terrain.append(tmp);
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
        Path[]      paths = tileSets[0].getPaths();
        Path        path = null;
        Vector      elements;
        String[]    types = { "unknown", "start", "end", "path", "join" };

        if (paths == null || paths.length == 0) {
            return;
        }

        writer.write("        <paths>\n");
        for (i=0; i < paths.length; i++) {
            path = paths[i];
            writer.write("            <path name=\""+path.getName()+"\" "+
                         "type=\""+path.getTypeAsString()+"\" style=\""+
                         path.getStyleAsString()+"\">\n");
            elements = path.getElements();
            for (e=0; e < elements.size(); e++) {
                Path.Element    element = (Path.Element)elements.elementAt(e);
                int             x = element.getX();
                int             y = element.getY();
                int             w = path.getWidth();
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
        Thing[]     things = tileSets[0].getThings();
        String      pad = "        ";
        int         i = 0;

        if (things != null && things.length > 0) {
            writer.write(pad+"<things>\n");
            for (i=0; i < things.length; i++) {
                Thing    thing = things[i];
                writer.write(pad+"    <thing type=\""+thing.getType()+"\" "+
                                        "x=\""+thing.getX()+
                                        "\" y=\""+thing.getY()+"\" "+
                                        "rotation=\""+thing.getRotation()+"\">\n");
                writer.write(pad+"        <name>"+thing.getName()+"</name>\n");
                writer.write(pad+"        <description>");
                writer.write(thing.getDescription());
                writer.write("</description>\n");
                writer.write(pad+"        <font>"+thing.getFontSize()+"</font>\n");
                writer.write(pad+"        <importance>");
                writer.write(thing.getImportance()+"</importance>\n");

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
                me.printStackTrace();
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
    public final TerrainSet
    getTerrainSet() {
        return terrainSet;
    }

    public final TerrainSet
    getThingSet() {
        return thingSet;
    }

    public final TerrainSet
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
    public final AreaSet
    getAreaSet() {
        return areaSet;
    }

    public final void
    unselectPaths() {
        Path[]  paths = tileSets[0].getPaths();
        int     id = 0;

        for (id=1; id <= paths.length; id++) {
            System.out.println("Unselecting river "+id);
            paths[id].setHighlighted(false);
        }
    }

    public final void
    selectPath(Path path) {
        path.setHighlighted(true);
    }



    public final String
    getParent() {
        return parent;
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
