
package uk.co.demon.bifrost.rpg.xmlmap;

import uk.co.demon.bifrost.rpg.xmlmap.*;
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
    protected String        name, author;
    protected String        cvsversion;

    /**
     * Exception class, raised when an error occurs during
     * processing of an XML document. Used as a generic
     * exception, to make things easier to catch.
     */
    public class XMLException extends Exception {
        public
        XMLException() {
            super();
        }

        public
        XMLException(String msg) {
            super(msg);
        }
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
            throw new MapException("Cannot parse XML data");
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
            cvsversion = getTextNode("/map/header/cvsversion");


            System.out.println(name);
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
                throw new XMLException("Node not found");
            }
            
            text = getTextNode(node);
        } catch (TransformerException te) {
            throw new XMLException("Cannot find text node");
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
    public String getCVSVersion() { return cvsversion; }

    /**
     * Returns the version number, stripped of any CVS tags.
     * Not currently implemented - just returns the cvsversion.
     */
    public String getVersion() {
        return cvsversion;
    }
    

    public TerrainSet
    getTerrainSet() throws XMLException {
        Node        node;
        TerrainSet  set = new TerrainSet();
        String      name, description;
        String      image;
        int         i = 0;
        
        System.out.println("Getting TerrainSet from XML");

        try {
            node = getNode("/map/terrainset");

            NodeList list = getNodeList(node, "terrain");

            for (i=0; i < list.getLength(); i++) {
                Node            terrain = list.item(i);
                NamedNodeMap    values;
                Node            value;
                short   id;

                if (terrain != null) {
                    values = terrain.getAttributes();
                    id = (short)getIntNode(values.getNamedItem("id"));

                    name = getTextNode(terrain, "name");
                    description = getTextNode(terrain, "description");
                    image = getTextNode(terrain, "image");

                    System.out.println(name);

                    set.add(id, name, description, image);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
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

        // The 'node' is pointing at the <tileset> element of the
        // XML map file. It should look something like this:
        // <tileset id="root">
        //    <dimensions>
        //        <scale>64</scale>
        //        <width>72</width>
        //        <height>56</height>
        //    </dimension>
        //    <tiles>
        //        <tile.../>
        //    </tiles>
        // </tileset>

        name = getTextNode(id, ".");
        scale = getIntNode(node, "dimensions/scale");
        width = getIntNode(node, "dimensions/width");
        height = getIntNode(node, "dimensions/height");

        try {
            // First, create an empty tileset.
            System.out.println("Creating tileSet of w "+width+" and h "+height);
            tileSet = new TileSet(name, width, height, scale);

            // Next, populate the tiles with data from the XML.
            NodeList    tiles = getNodeList(node, "tiles/tile");
            if (tiles == null) {
                throw new XMLException("No tiles defined in this tileset");
            }

            for (int t=0; t < tiles.getLength(); t++) {
                Node            tile = tiles.item(t);
                Node            value;
                NamedNodeMap    values;

                if (tile != null) {
                    int     x, y;
                    short   terrain;

                    // Do NOT used the XPathAPI here, since the docs
                    // don't exagerate when they say it's slow.
                    values = tile.getAttributes();
                    x = getIntNode(values.getNamedItem("x"));
                    y = getIntNode(values.getNamedItem("y"));
                    terrain = (short) getIntNode(values.getNamedItem("terrain"));

                    tileSet.setTile(x, y, terrain);
                }
            }
        } catch (InvalidArgumentException iae) {
            throw new XMLException("Cannot create TileSet from XML");
        } catch (MapOutOfBoundsException mbe) {
            System.out.println(mbe);
            throw new XMLException("Tiles in tileset out of bounds");
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
     * Main class used only for testing.
     */
    public static void
    main(String args[]) {
        System.out.println("Start");
        try {
            MapXML      xml = new MapXML("map.cart");
            xml.getTileSets();
            xml.getTerrainSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
