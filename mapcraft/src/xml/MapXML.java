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

package uk.co.demon.bifrost.rpg.mapcraft.xml;

import uk.co.demon.bifrost.rpg.mapcraft.map.*;
import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.apache.xml.utils.DOMBuilder;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

/**
 * Class for reading/writing a Map from/to an XML file. This acts
 * as a wrapper to the XML structure of the document. It is designed
 * to be used when loading/saving a map from the filesystem.
 *
 * There is no real support for using the class to manage an interactive
 * map as it is being edited - a map's contents can be obtained, and an
 * entirely new map can be built from scratch before writing to a file,
 * but efficient changing of individual contents is not possible.
 *
 * No knowledge of XML should be required by users of this class,
 * other than xpath. None of the XML classes (e.g. Node/Document)
 * should be needed to use this class.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class MapXML {
    protected Document      document;
    protected String        name, author, id, parent;
    protected String        version, date;
    protected String        format, type;
    protected String        imagedir;
    protected String        tileShape;

    public static final String BASE64 = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                                                   "abcdefghijklmnopqrstuvwxyz"+
                                                   "0123456789");

    public static final String SQUARE = "Square";
    public static final String HEXAGONAL = "Hexagonal";

    public static final String LOCAL = "Local";
    public static final String WORLD = "World";

    private void
    debug(String message) {
        System.out.println(message);
    }

    private void
    warning(String message) {
        System.out.println("Warning: "+message);
    }

    private void
    error(String message) {
        System.out.println("ERROR: "+message);
    }

    public
    MapXML() {
    }

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
     * Public constructor which reads a new map DOM from a
     * specified file. The file should be on the local filesystem.
     *
     * @param filename  Filename of the file to load.
     *
     * @throws  XMLException if anything goes wrong
     */
    public
    MapXML(String filename) throws MapException {
        try {
            load(filename);
        } catch (IOException ioe) {
            throw new MapException("Cannot load XML document");
        } catch (XMLException xmle) {
            throw new MapException("Cannot parse XML data ("+xmle.getMessage()+")");
        }
    }

    /**
     * Load a map document from the local filesystem. Map is
     * parsed and inserted into DOM structure for later querying.
     *
     * @param filename  Filename of the file to load.
     */
    private void
    load(String filename) throws XMLException, IOException {
        System.out.println("MapXML.Load "+filename);
        try {
            InputSource             in;
            FileInputStream         fis;
            DocumentBuilderFactory  dbf;
            Node                    node;
            NodeList                nodeList;

            fis = new FileInputStream(filename);
            in = new InputSource(fis);
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            document = dbf.newDocumentBuilder().parse(in);

            System.out.println("Parsed document "+filename);

            name = getTextNode("/map/header/name");
            author = getTextNode("/map/header/author");
            version = getTextNode("/map/header/cvs/version");
            date = getTextNode("/map/header/cvs/date");
            format = getTextNode("/map/header/format");
            id = getTextNode("/map/header/id");
            parent = getTextNode("/map/header/parent");
            tileShape = getTextNode("/map/header/shape");
            type = getTextNode("/map/header/type");
            imagedir = getTextNode("/map/header/imagedir");

            System.out.println(name+","+id+","+parent+","+author);
        } catch (XMLException xe) {
            throw xe;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLException("Failed to load XML document "+filename);
        }
        return;
    }




    /**
     * Return the node of the root document defined by the
     * xpath query. Since this uses the XPathAPI, it is not
     * guaranteed to be efficient.
     *
     * @param xpath     XPath query to requested node.
     * @return          First matching node. May be null.
     */
    protected Node
    getNode(String xpath) throws XMLException {
        return getNode(document, xpath);
    }

    /**
     * Return the descendent of the provided node, described
     * by the xpath query. This uses the XPathAPI, so is not
     * likely to be efficient.
     *
     * @param root      Node to perform the search on.
     * @param xpath     XPath to look for.
     * @return          First matching node. May be null.
     */
    protected Node
    getNode(Node root, String xpath) throws XMLException {
        Node        node = null;
        try {
            node = XPathAPI.selectSingleNode(root, xpath);
        } catch (TransformerException te) {
            throw new XMLException("Cannot find node");
        }

        return node;
    }

    /**
     * Gets a list of nodes from the root document described
     * by the xpath. All matching nodes will be returned. This
     * uses the XPathAPI so isn't necessarily efficient.
     *
     * @param xpath     XPath to search for.
     * @return          List of matching nodes. May be null.
     */
    protected NodeList
    getNodeList(String xpath) throws XMLException {
        return getNodeList(document, xpath);
    }

    /**
     * Gets a list of nodes described by the XPath string,
     * searching down from the supplied node. All matching
     * nodes will be returned. This uses the XPathAPI, so
     * isn't efficient.
     *
     * @param root      Root node to search from.
     * @param xpath     Xpath to search for.
     * @return          List of matching nodes. May be null.
     */
    protected NodeList
    getNodeList(Node root, String xpath) throws XMLException {
        NodeList        list = null;
        try {
            list = XPathAPI.selectNodeList(root, xpath);
        } catch (TransformerException te) {
            throw new XMLException("Cannot find nodelist ["+xpath+"]");
        }

        return list;
    }

    public String
    getTextNode(String xpath) throws XMLException {
        return getTextNode(document, xpath);
    }

    protected String
    getTextNode(Node root, String xpath) throws XMLException {
        Node        node = null;
        String      text = null;

        try {
            node = XPathAPI.selectSingleNode(root, xpath);
            if (node == null) {
                throw new XMLException("Node \""+xpath+"\" not found");
            }

            text = getTextNode(node);
        } catch (TransformerException te) {
            throw new XMLException("Cannot find node \""+xpath+"\"");
        }

        return text;
    }

    protected String
    getTextNode(Node node) throws XMLException {
        String  text = null;

        try {
            if (node.hasChildNodes()) {
                node = node.getFirstChild();
                if (node.getNodeType() == Node.TEXT_NODE) {
                    text = node.getNodeValue();
                } else {
                    throw new XMLException("Node ["+node.getNodeName()+
                                           "] is not a text node");
                }
            } else {
                throw new XMLException("Node ["+node.getNodeName()+
                                       "] does not have text element");
            }
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XMLException("Node does not have text element");
        }

        return text;
    }

    public int
    getIntNode(String xpath) throws XMLException {
        return getIntNode(document, xpath);
    }

    protected int
    getIntNode(Node root, String xpath) throws XMLException {
        String  value;
        int     number;

        value = getTextNode(root, xpath);

        try {
            number = (new Integer(value).intValue());
        } catch (NumberFormatException nfe) {
            throw new XMLException("Value is not an integer");
        }

        return number;
    }

    protected int
    getIntNode(Node root) throws XMLException {
        String  value;
        int     number;

        value = getTextNode(root);

        try {
            number = (new Integer(value).intValue());
        } catch (NumberFormatException nfe) {
            throw new XMLException("Value is not an integer");
        }

        return number;
    }

    protected int
    getIntNode(String xpath, int dflt) {
        try {
            return getIntNode(xpath);
        } catch (Exception e) {
            return dflt;
        }
    }

    protected int
    getIntNode(Node root, int dflt) {
        try {
            return getIntNode(root);
        } catch (Exception e) {
            return dflt;
        }
    }

    protected int
    getIntNode(Node root, String xpath, int dflt) {
        try {
            return getIntNode(root, xpath);
        } catch (Exception e) {
            return dflt;
        }
    }


    /**
     * Returns the name of the map.
     */
    public String getName() { return name; }

    /**
     * Returns the map author.
     */
    public String getAuthor() { return author; }

    /**
     * The map is designed to support CVS, and by default
     * has a $Revision$ tag in it. This property gives
     * access to the CVS revision value.
     */
    public String getCVSVersion() { return version; }
    public String getCVSDate() { return date; }

    /**
     * Returns the version number, stripped of any CVS tags.
     * Not currently implemented - just returns the cvsversion.
     */
    public String getVersion() {
        return version;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getId() { return id; }
    public String getFormat() { return format; }
    public String getParent() { return parent; }
    public String getTileShape() { return tileShape; }
    public String getType() { return type; }
    public String getImageDir() { return imagedir; }

    /**
     * Return all the terrains from the named terrainset in the XML data.
     * If no terrainset of the given name is not found, then an XMLException
     * is raised.
     *
     * @param setId     Id of the terrain set to fetch.
     * @return          The full TerrainSet that is found.
     */
    public TerrainSet
    getTerrainSet(String setId) throws XMLException {
        Node            node;
        TerrainSet      set = null;
        String          name, description;
        String          image;
        String          path;
        int             i = 0;
        NamedNodeMap    values;
        Node            value;

        try {
            node = getNode("/map/terrainset[@id='"+setId+"']");
            if (node == null) {
                throw new XMLException("No TerrainSet named "+setId+" was found");
            }
            values = node.getAttributes();
            path = getTextNode(values.getNamedItem("path"));
            if (path == null) {
                path = ".";
            }

            set = new TerrainSet(setId, path);
            NodeList list = getNodeList(node, "terrain");

            for (i=0; i < list.getLength(); i++) {
                Node            terrain = list.item(i);
                short   id;

                if (terrain != null) {
                    values = terrain.getAttributes();
                    id = (short)getIntNode(values.getNamedItem("id"));

                    name = getTextNode(terrain, "name");
                    description = getTextNode(terrain, "description");
                    image = getTextNode(terrain, "image");

                    set.add(id, name, description, path+"/"+image);
                }
            }
            list = null;
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XMLException("Error in getting TerrainSet");
        }

        return set;
    }


    protected TileSet
    getTileSet(Node node) throws XMLException {

        // Simple check to ensure we are pointing at the right XML.
        if (!node.getNodeName().equals("tileset")) {
            throw new XMLException("Node is not a tileset");
        }

        NamedNodeMap    attrs = node.getAttributes();
        Node            id = attrs.getNamedItem("id");

        TileSet         tileSet = null;

        int             scale, width, height;
        String          name = null;
        int             blobSize = 10;

        if (format.startsWith("0.0.")) {
            System.out.println("WARNING: Old format file, converting");
            blobSize = 8;
        }

        name = getTextNode(id, ".");
        scale = getIntNode(node, "dimensions/scale");
        width = getIntNode(node, "dimensions/width");
        height = getIntNode(node, "dimensions/height");

        try {
            // First, create an empty tileset.
            System.out.println("Creating tileSet "+format+" of w "+width+" and h "+height);
            tileSet = new TileSet(name, width, height, scale);

            // Next, populate the tiles with data from the XML.
            NodeList    columns = getNodeList(node, "tiles/column");
            if (columns == null) {
                throw new XMLException("No columns defined in this tileset");
            }

            for (int t=0; t < columns.getLength(); t++) {
                Node            column = columns.item(t);
                Node            value;
                NamedNodeMap    values;

                // A column consists of an X coordinate, and stream
                // data for all the rows in the column. Rows are stored
                // as 2 digit base 36 numbers. White space is ignored.
                if (column != null) {
                    int     x=0, y=0;
                    short   terrain=0;
                    int     i;

                    // Do NOT used the XPathAPI here, since the docs
                    // don't exagerate when they say it's slow.
                    values = column.getAttributes();
                    x = getIntNode(values.getNamedItem("x"));

                    String  data = getTextNode(column).replaceAll(" |\n|\t", "");
                    for (y=0,i=0; i < data.length(); i+=blobSize, y++) {
                        String  part = data.substring(i, i+blobSize);
                        //terrain = Short.valueOf(part, 36).shortValue();

                        tileSet.setTileFromBlob(x, y, part);
                    }
                }
            }
            columns = null; // Free memory;

            try {
                System.out.println("Looking for tileset parent");
                int     parentScale, parentX, parentY;
                parentScale = getIntNode(node, "parent/scale");
                parentX = getIntNode(node, "parent/x");
                parentY = getIntNode(node, "parent/y");

                tileSet.setParent(parentScale, parentX, parentY);
            } catch (XMLException pxmle) {
            }
        } catch (InvalidArgumentException iae) {
            throw new XMLException("Cannot create TileSet from XML");
        } catch (MapOutOfBoundsException mbe) {
            System.out.println(mbe);
            throw new XMLException("Tiles in tileset out of bounds");
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(name+" "+scale+"km "+width+"x"+height);

        return tileSet;
    }

    /**
     * Returns a nodelist of all the tilesets.
     */
    public TileSet[]
    getTileSets() {
        NodeList    nodes;
        ArrayList   list = new ArrayList(1);
        int         i;
        TileSet     set = null;

        try {
            nodes = getNodeList("/map/tileset");
            for (i=0; i < nodes.getLength(); i++) {
                Node            item = nodes.item(i);

                set = getTileSet(item);
                if (set != null) {
                    list.add(set);
                }
            }

        } catch (XMLException xe) {
            // XML Exception probably means no nodes were found.
            // Just return a null list.
            xe.printStackTrace();
            return null;
        }


        // ArrayList.toArray() doesn't seem to work for some reason.
        // Fails with a class cast exception at runtime.
        TileSet[]   ret = new TileSet[list.size()];
        for (i=0; i < list.size(); i++) {
            ret[i] = (TileSet)list.get(i);
        }

        return ret;
    }

    /**
     * Return an array of all the things in the map.
     */
    public Vector
    getThings(String set) throws XMLException {
        Vector          things = new Vector();
        String          name, description;
        int             fontSize, importance;
        String          image;
        String          path;
        int             x, y, i = 0;
        short           type, rotation;
        NamedNodeMap    values;
        Node            value;
        String          xpath = "/map/things/thing";

        if (!format.equals("0.1.0")) {
            xpath = "/map/tileset[@id='"+set+"']/things/thing";
        }

        try {
            NodeList    list = getNodeList(xpath);
            if (list == null || list.getLength() == 0) {
                // No things found. This is perfectly valid.
                System.out.println("No things found at ["+xpath+"]");
                return things;
            }
            System.out.println("Found "+list.getLength()+" things to load");

            for (i=0; i < list.getLength(); i++) {
                Node        node = list.item(i);
                Thing       thing = null;

                if (node != null) {
                    values = node.getAttributes();
                    try {
                        type = (short)getIntNode(values.getNamedItem("type"));
                        x = getIntNode(values.getNamedItem("x"));
                        y = getIntNode(values.getNamedItem("y"));
                    } catch (Exception e1) {
                        warning("Missing core attribute on thing "+i);
                        continue;
                    }
                    try {
                        rotation = (short)getIntNode(values.getNamedItem("rotation"));
                    } catch (Exception e2) {
                        rotation = 0;
                    }

                    name = getTextNode(node, "name");
                    description = getTextNode(node, "description");
                    fontSize = getIntNode(node, "font", Thing.MEDIUM);
                    importance = getIntNode(node, "importance", Thing.NORMAL);
                    thing = new Thing(type, name, description, x, y);
                    thing.setFontSize(fontSize);
                    thing.setImportance(importance);
                    thing.setRotation(rotation);
                    things.add(thing);

                    NodeList    props = getNodeList(node, "properties/property");
                    if (props != null && props.getLength() > 0) {
                        String      key = null, val = null;
                        debug("Found properties for thing ["+thing.getName()+"]");
                        for (int p=0; p < props.getLength(); p++) {
                            Node    pnode = props.item(p);
                            key = getTextNode(pnode, "@name");
                            val = getTextNode(pnode, ".");

                            thing.setProperty(key, val);
                        }

                    }
                }
            }
            list = null;
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XMLException("Error in getting Things");
        }

        return things;
    }

    /**
     * Return an AreaSet for this map.
     */
    public AreaSet
    getAreas() throws XMLException {
        String          name, description;
        String          image;
        String          path;
        int             id, x, y, i = 0;
        int             parent;
        NamedNodeMap    values;
        Node            value;
        AreaSet         areas = new AreaSet();

        try {
            NodeList    list = getNodeList("/map/areas/area");
            if (list == null || list.getLength() == 0) {
                // No areas found. This is perfectly valid.
                return areas;
            }

            for (i=0; i < list.getLength(); i++) {
                Node        node = list.item(i);
                Area        area = null;

                if (node != null) {
                    values = node.getAttributes();
                    id = getIntNode(values.getNamedItem("id"));
                    name = getTextNode(values.getNamedItem("name"));
                    if (values.getNamedItem("parent") != null) {
                        parent = getIntNode(values.getNamedItem("parent"));
                    } else {
                        parent = 0;
                    }
                    areas.add(id, name, parent);
                }
            }
            list = null;
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XMLException("Error in getting Areas");
        }

        return areas;
    }

    /**
     * Return all the rivers in the given tileset.
     *
     */
    public Vector
    getPaths(String set) throws XMLException {
        Vector          rivers = new Vector();
        String          xpath = "/map/tileset[@id='"+set+"']/paths/path";

        if (format.equals("0.1.0")) {
            warning("Old format river elements");
            xpath = "/map/rivers/river";
        }

        try {
            NodeList    list = getNodeList(xpath);
            if (list == null || list.getLength() == 0) {
                // Try again, with the 0.2.0 format for rivers.
                xpath = "/map/tileset[@id='"+set+"']/rivers/river";
                list = getNodeList(xpath);

                if (list == null || list.getLength() == 0) {
                    return rivers;
                }
            }

            // For each <path> in the document
            for (int i=0; i < list.getLength(); i++) {
                Node            node = list.item(i);
                NamedNodeMap    values = null;
                Node            child = null;
                Path            path = null;

                if (node != null) {
                    values = node.getAttributes();
                    String  name = getTextNode(values.getNamedItem("name"));
                    String  ptype, style;

                    try {
                        ptype = getTextNode(values.getNamedItem("type"));
                    } catch (Exception e1) {
                        ptype = "river";
                    }

                    try {
                        style = getTextNode(values.getNamedItem("style"));
                    } catch (Exception e2) {
                        style = "plain";
                    }

                    System.out.println("Adding "+ptype+" ["+name+"/"+style+"]");

                    // Now get each path element in turn. First should be a <start/>
                    child = node.getFirstChild();
                    values = child.getAttributes();

                    NodeList children = node.getChildNodes();
                    for (int c = 0; c < children.getLength(); c++) {
                        int     type = Path.PATH;

                        child = children.item(c);
                        values = child.getAttributes();

                        if (child.getNodeName().equals("start")) {
                            type = Path.START;
                            path = new Path(name, getIntNode(values.getNamedItem("x")),
                                                  getIntNode(values.getNamedItem("y")));
                            path.setWidth(getIntNode(values.getNamedItem("width")));
                            path.setType(ptype);
                            path.setStyle(style);
                            continue;
                        } else if (child.getNodeName().equals("end")) {
                            type = Path.END;
                        } else if (child.getNodeName().equals("path")) {
                            type = Path.PATH;
                        } else {
                            continue;
                        }

                        path.add(type, getIntNode(values.getNamedItem("x")),
                                       getIntNode(values.getNamedItem("y")));
                    }
                    rivers.add(path);
                }
            }
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLException("Error in gettings paths");
        }

        return rivers;
    }



    public static void
    testEncoding(int v, int w) {
        String e = MapXML.toBase64(v, w);

        System.out.println("("+v+","+w+") = "+e+" = "+fromBase64(e));
    }


    /**
     * Main class used only for testing.
     */
    public static void
    main(String args[]) {
        System.out.println("Start");
        String b;

        testEncoding(1, 1);
        testEncoding(10, 2);
        testEncoding(100, 3);
        testEncoding(1000, 4);
        testEncoding(10000, 4);

        /*
        try {
            MapXML      xml = new MapXML("map.cart");
            xml.getTileSets();
            xml.getTerrainSet("basic");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
