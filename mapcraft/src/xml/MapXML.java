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
    protected String        format;
    protected String        tileShape;
    
    public static final String BASE64 = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");

    public static final String SQUARE = "Square";
    public static final String HEXAGONAL = "Hexagonal";
    
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
            throw new XMLException("Cannot find nodelist");
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
                    throw new XMLException("Node is not a text node");
                }
            } else {
                throw new XMLException("Node does not have text element");
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

        System.out.println("Getting TerrainSet from XML");

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

                    System.out.println(name);

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

    protected Tile
    getTileFromBlob(String data) {
        Tile    tile = null;
        short   terrain;
        short   height;
        short   hills;
        
        terrain = (short)fromBase64(data.substring(0, 2));
        height = (short)fromBase64(data.substring(2, 5));
        hills = (short)fromBase64(data.substring(5, 6));

        tile = new Tile(terrain, height, true);

        return tile;
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

        name = getTextNode(id, ".");
        scale = getIntNode(node, "dimensions/scale");
        width = getIntNode(node, "dimensions/width");
        height = getIntNode(node, "dimensions/height");

        try {
            // First, create an empty tileset.
            System.out.println("Creating tileSet of w "+width+" and h "+height);
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
                    for (y=0,i=0; i < data.length(); i+=8, y++) {
                        String  part = data.substring(i, i+8);
                        //terrain = Short.valueOf(part, 36).shortValue();

                        tileSet.setTile(x, y, getTileFromBlob(part));
                    }
                }
            }
            columns = null; // Free memory;

            // Now, get data on rivers. Since river data is a lot
            // less, we don't use blobs for rivers.
            System.out.println("Reading river data...");
            NodeList    rivers = getNodeList(node, "rivers/river");
            
            if (rivers != null) {
                for (int r=0; r < rivers.getLength(); r++) {
                    Node            river = rivers.item(r);
                    Node            value;
                    NamedNodeMap    values;
                    
                    if (river != null) {
                        int     x = 0, y = 0;
                        short   mask = 0;
                        
                        values = river.getAttributes();
                        x = getIntNode(values.getNamedItem("x"));
                        y = getIntNode(values.getNamedItem("y"));
                        mask = (short)getIntNode(values.getNamedItem("mask"));
                        tileSet.setRiverMask(x, y, mask);
                    }
                }
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
                    System.out.println("Adding tileset "+set.getName());
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
     * Return an array of all the sites in the map.
     */
    public void
    getSites(TileSet tileSet) throws XMLException {
        Site[]          sites = null;
        String          name, description;
        String          image;
        String          path;
        int             x, y, i = 0;
        short           type;
        NamedNodeMap    values;
        Node            value;

        System.out.println("Getting Sites from XML");

        try {
            NodeList    list = getNodeList("/map/sites/site");
            if (list == null || list.getLength() == 0) {
                // No sites found. This is perfectly valid.
                return;
            }

            for (i=0; i < list.getLength(); i++) {
                Node        node = list.item(i);
                Site        site = null;


                if (node != null) {
                    values = node.getAttributes();
                    type = (short)getIntNode(values.getNamedItem("type"));
                    x = getIntNode(values.getNamedItem("x"));
                    y = getIntNode(values.getNamedItem("y"));

                    name = getTextNode(node, "name");
                    description = getTextNode(node, "description");

                    System.out.println("Site ["+name+"] at "+x+","+y);

                    tileSet.getTile(x, y).setSite(new Site(type, name, description));
                }
            }
            list = null;
        } catch (XMLException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XMLException("Error in getting Sites");
        }

        return;
    }
    
    /**
     * Return all the rivers in the map.
     *
    public Rivers
    getRivers() throws XMLException {
    }
    */


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
