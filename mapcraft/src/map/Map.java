package uk.co.demon.bifrost.rpg.xmlmap;

import uk.co.demon.bifrost.rpg.xmlmap.*;

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
public class Map {
    // Basic indentity fields.
    private String  filename;
    private String  name;

    // XML backend data.
    private MapXML  xml;

    // Data sets
    TerrainSet      terrainSet = null;
    TileSet         tileSets[] = null;
    
    // State fields
    private String  currentSetName = null;
    private int     currentSet = 0;

    // Legacy
    private Tile    tiles[][];
    private int     width = 0;
    private int     height = 0;
    private int     scale = 0;

    public
    Map(String name, int width, int height, int scale) throws MapException {
        this.name = name;
        
        tileSets = new TileSet[1];
        tileSets[0] = new TileSet("root", width, height, scale);
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
            System.out.println("Reading tilesets");
            tileSets = xml.getTileSets();
            terrainSet = xml.getTerrainSet();
            
            setCurrentSet("root");
        } catch (MapException mape) {
            throw mape;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MapException("Failed to load map");
        }
    }
    
    public void
    loadTerrainSet(String filename) {
        MapXML  xml;

        try {
            xml = new MapXML(filename);
            terrainSet = xml.getTerrainSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() { return name; }

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
    rescaleMap(int newScale) {
        int     h, w; // Height and width of new map.
        int     x, y; // Iterators.

        /*
        if (newScale == this.scale) {
            // Do nothing.
            return;
        } else if (newScale > this.scale) {
            // Loosing information.
        } else {
            // Adding information.
            // newScale < scale. This means the map is getting
            // bigger (e.g. it was 1:64km, now it's 1:32km).
            int     sf = (scale / newScale); // Scale factor.
            int     nx, ny;

            h = height * sf;
            w = width * sf;

            Tile    newMap[][] = new Tile[h][w];

            for (y = 0; y < h; y++) {
                for (x=0; x < w; x++) {
                    Tile    tile = getTile(x/sf, y/sf);
                    newMap[y][x] = new Tile(tile);
                }
            }
            this.tiles = newMap;
            this.width = w;
            this.height = h;
        }

        System.out.println("Finished rescaling map");

        this.scale = newScale;
        */
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
        /*
        for (y = yoff; y < yoff + h; y++) {
            for (x = xoff; x < xoff + w; x++) {
                Tile    tile = new Tile(getTerrain(x,y), getHeight(x,y),
                                        isWritable(x,y));
                submap.setTile(tile, x, y);
            }
        }
        */

        return submap;
    }

    /**
     * Save the map as an XML file.
     */
    public void
    saveLongFormat(String filename) throws IOException {
        FileWriter      writer = new FileWriter(filename);
        int             x, y;
        int             i;

        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<map>\n");
        writer.write("    <header>\n");
        writer.write("        <name>"+name+"</name>\n");
        writer.write("        <author>Unknown</author>\n");
        writer.write("        <cvsversion>$Revision$</cvsversion>\n");
        writer.write("    </header>\n");

        // Terrain Set
        writer.write("    <terrainset>\n");

        // Iterate over the terrain types, and save them out.
        Iterator iter = terrainSet.iterator();
        while (iter.hasNext()) {
            Terrain t = (Terrain)iter.next();

            writer.write("        <terrain id=\""+t.getId()+"\">\n");
            writer.write("            <name>"+t.getName()+"</name>\n");
            writer.write("            <description>"+t.getDescription()+"</description>\n");
            writer.write("            <image>"+t.getImagePath()+"</image>\n");
            writer.write("            <solid value=\"false\"/>\n");
            writer.write("        </terrain>\n");
        }
        writer.write("    </terrainset>\n");

        // Now go through each of the tilesets in turn.
        for (i=0; i < tileSets.length; i++) {
            writer.write("    <tileset id=\""+tileSets[i].getName()+"\">\n");
            writer.write("        <dimensions>\n");
            writer.write("            <scale>"+tileSets[i].getScale()+"</scale>\n");
            writer.write("            <width>"+tileSets[i].getWidth()+"</width>\n");
            writer.write("            <height>"+tileSets[i].getHeight()+"</height>\n");
            writer.write("        </dimensions>\n");

            writer.write("        <tiles>\n");
            for (y = 0; y < tileSets[i].getHeight(); y++) {
                for (x = 0; x < tileSets[i].getWidth(); x++) {
                    Tile tile = null;
                    try {
                        tile = tileSets[i].getTile(x, y);
                    } catch (MapOutOfBoundsException e) {
                    }

                    if (tile != null) {
                        writer.write("        ");
                        writer.write("<tile ");
                        writer.write("x=\""+x+"\" y=\""+y+"\" ");
                        writer.write("terrain=\""+tile.getTerrain()+"\" ");
                        writer.write("/>\n");
                    }
                }
            }
            writer.write("        </tiles>\n");
            writer.write("    </tileset>\n\n");
        }
        writer.write("</map>\n");

        writer.close();
    }
    

    /**
     * Save the map as an XML file.s
     */
    public void
    save(String filename) throws IOException {
        FileWriter      writer = new FileWriter(filename);
        int             x, y;
        int             i;

        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<map>\n");
        writer.write("    <header>\n");
        writer.write("        <name>"+name+"</name>\n");
        writer.write("        <author>Unknown</author>\n");
        writer.write("        <cvsversion>$Revision$</cvsversion>\n");
        writer.write("        <format>1.1</format>\n");
        writer.write("    </header>\n");

        // Terrain Set
        writer.write("    <terrainset>\n");

        // Iterate over the terrain types, and save them out.
        Iterator iter = terrainSet.iterator();
        while (iter.hasNext()) {
            Terrain t = (Terrain)iter.next();

            writer.write("        <terrain id=\""+t.getId()+"\">\n");
            writer.write("            <name>"+t.getName()+"</name>\n");
            writer.write("            <description>"+t.getDescription()+"</description>\n");
            writer.write("            <image>"+t.getImagePath()+"</image>\n");
            writer.write("            <solid value=\"false\"/>\n");
            writer.write("        </terrain>\n");
        }
        writer.write("    </terrainset>\n");

        // Now go through each of the tilesets in turn.
        for (i=0; i < tileSets.length; i++) {
            writer.write("    <tileset id=\""+tileSets[i].getName()+"\">\n");
            writer.write("        <dimensions>\n");
            writer.write("            <scale>"+tileSets[i].getScale()+"</scale>\n");
            writer.write("            <width>"+tileSets[i].getWidth()+"</width>\n");
            writer.write("            <height>"+tileSets[i].getHeight()+"</height>\n");
            writer.write("        </dimensions>\n");

            writer.write("        <tiles>\n");

            for (x=0; x < tileSets[i].getWidth(); x++) {
                System.out.println("Writing column "+x);

                writer.write("        ");
                writer.write("<column x=\""+x+"\">");

                StringBuffer    terrain = new StringBuffer();
                String          tmp;

                for (y=0; y < tileSets[i].getHeight(); y++) {
                    try {
                        Tile    tile = tileSets[i].getTile(x, y);
                        tmp = Integer.toString(tile.getTerrain(), 36);
                        if (tmp.length() < 2) {
                            tmp = "0"+tmp;
                        }

                        if ((y%32)==0) {
                            terrain.append("\n");
                            terrain.append("            ");
                        }

                        terrain.append(tmp);
                    } catch (MapOutOfBoundsException e) {
                    }
                }
                writer.write(terrain.toString());
                writer.write("\n");
                writer.write("        ");
                writer.write("</column>\n");
             }
            writer.write("        </tiles>\n");
            writer.write("    </tileset>\n\n");
        }
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

            terrainSet = xml.getTerrainSet();
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

    public static void
    main(String args[]) {
        Map         map;
        Options     options;

        try {
            options = new Options(args);
            System.out.println("Hello");
            if (options.isOption("-create")) {
                int width = options.getInt("-width");
                int height = options.getInt("-height");
                int scale = options.getInt("-scale");
                String name = options.getString("-create");
                String terrain = options.getString("-terrain");

                System.out.println("Creating map "+name+" "+width+"x"+height);
                map = new Map(name, width, height, scale);
                map.loadTerrainSet(terrain);
                map.save(name+".map");
            } else if (options.isOption("-load")) {
                map = new Map(options.getString("-load"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
