package uk.co.demon.bifrost.rpg.xmlmap;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;

import java.util.*;

/**
 * A component which displays a Map as part of a GUI.
 * The map data is held in the Map object. Should be
 * extended in order to give editing functions (for
 * instance).
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class MapViewer extends JPanel {
    protected Map         map;
    protected int         x_offset;
    protected int         y_offset;
    protected String      tileSize = "medium";
    protected int         tileXSize = 30;
    protected int         tileYSize = 35;
    protected int         tileYOffset = 20;

    protected Toolkit     toolkit = null;
    protected Image[]     icons = null;
    protected Image       outlineIcon = null;

    protected IconSet     iconSet = null;
    protected IconSet     riverSet = null;
    protected IconSet     siteSet = null;
    
    protected boolean     showGrid = true;

    protected void
    log(int level, String message) {
        System.out.println(message);
    }

    protected void debug(String message) { log(4, message); }
    protected void info(String message) { log(3, message); }
    protected void warn(String message)  { log(2, message); }
    protected void error(String message) { log(1, message); }
    protected void fatal(String message) { log(0, message); }


    /**
     * Basic constructor. Creates an empty map.
     */
    public MapViewer() {
        super(true);

        toolkit = Toolkit.getDefaultToolkit();

        icons = new Image[32];
        icons[0] = toolkit.getImage("images/"+tileSize+"/black.gif");
        icons[1] = toolkit.getImage("images/"+tileSize+"/deepsea.gif");
        icons[2] = toolkit.getImage("images/"+tileSize+"/sea.gif");
        icons[3] = toolkit.getImage("images/"+tileSize+"/plains.gif");
        icons[4] = toolkit.getImage("images/"+tileSize+"/woods.gif");

        try {
            map = new Map("Map", Preferences.MAP_WIDTH,
                    Preferences.MAP_HEIGHT, 64);
            map.setBackground((short)1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected IconSet
    readIcons(String name, TerrainSet set) {
        IconSet     iconSet = new IconSet(name);
        Iterator    iter = set.iterator();

        if (iter == null || !iter.hasNext()) {
            warn("No iterator");
            return null;
        }

        iconSet = new IconSet(map.getName());
        while (iter.hasNext()) {
            Terrain t = (Terrain)iter.next();
            if (t != null) {
                info("Defining terrain "+t.getName());
                short   id = t.getId();
                String  path = t.getImagePath();
                Image   icon = toolkit.getImage("images/"+tileSize+"/"+path);

                debug("Adding icon "+path+" for terrain "+id);
                iconSet.add(id, icon);
            }
        }
        
        outlineIcon = toolkit.getImage("images/"+tileSize+"/outline.png");
        
        return iconSet;
    }

    /**
     * Constructor to load a map from a file and display it.
     */
    public
    MapViewer(String filename) {
        super(true);

        toolkit = Toolkit.getDefaultToolkit();

        try {
            map = new Map(filename);

            System.out.println("Setting up terrain icons");
            TerrainSet  set = map.getTerrainSet();
            
            
            
            
            /*
            Iterator    iter = set.iterator();

            if (iter == null || !iter.hasNext()) {
                System.out.println("No iterator");
                return;
            }

            iconSet = new IconSet(map.getName());
            while (iter.hasNext()) {
                Terrain t = (Terrain)iter.next();
                if (t != null) {
                    System.out.println("Defining terrain "+t.getName());
                    short   id = t.getId();
                    String  path = t.getImagePath();
                    Image   icon = toolkit.getImage("images/"+tileSize+"/"+path);

                    System.out.println("Adding icon "+path+" for terrain "+id);
                    iconSet.add(id, icon);
                }
            }
            */
            iconSet = readIcons("terrain", set);
            siteSet = readIcons("sites", map.getPlaceSet());

            riverSet = new IconSet("rivers");
            for (short i = 0; i < 64; i++) {
                String  name = "/rivers/"+((i<10)?"0":"")+i+".gif";
                Image   icon = toolkit.getImage("images/"+tileSize+"/"+name);
                riverSet.add(i, icon);
            }


            map.setCurrentSet("root");

            // Now work out how big we want to be. Can assume we'll
            // be placed in a scrollable widget of some sort, so don't
            // worry about screen size or anything like that.
            int realWidth, realHeight;

            realWidth = tileXSize * (map.getWidth()+1);
            realHeight = tileYSize * (map.getHeight()+1);

            setPreferredSize(new Dimension(realWidth, realHeight));
        } catch (MapException e) {
            e.printStackTrace();
        }
    }

    /**
     * Paint the entire map component. No attempt at cropping is performed,
     * the entire map is redrawn each time even if not all of it can be
     * seen. At some point this needs to be fixed.
     */
    public void
    paintComponent(Graphics g) {
        super.paintComponent(g);
        int             x=0, y=0;
        int             xp, yp, ypp;

        try {
            for (y = 0; y < map.getHeight(); y++) {
                yp = y * tileYSize;

                for (x = 0; x < map.getWidth(); x++) {

                    xp = x * tileXSize;
                    ypp = yp + ((x%2 == 0)?0:tileYOffset);


                    short t = map.getTerrain(x, y);
                    Image icon = iconSet.getIcon(t);
                    g.drawImage(icon, xp, ypp, this);

                    // Now display a river, if one is needed.
                    if (map.isRiver(x, y)) {
                        icon = riverSet.getIcon(map.getRiverMask(x, y));
                        g.drawImage(icon, xp, ypp, this);
                    }
                    
                    // Display grid if asked for.
                    if (showGrid) {
                        g.drawImage(outlineIcon, xp, ypp, this);
                    }

                    // If tile has a site associated with it,
                    // display it along with its name.
                    if (map.isSite(x, y)) {
                        icon = siteSet.getIcon(map.getSiteMask(x, y));
                        g.drawImage(icon, xp, ypp, this);
                    }
                }
            }
            // Now draw labels. Need to draw these last so
            // that they don't get overwritten by tiles.
            for (y = 0; y < map.getHeight(); y++) {
                yp = y * tileYSize;
                for (x = 0; x < map.getWidth(); x++) {
                    if (map.isSite(x, y)) {
                        // Show name, should be configurable.
                        xp = x * tileXSize;
                        ypp = yp + ((x%2 == 0)?0:tileYOffset);
                        g.drawString(map.getSite(x, y).getName(), xp, ypp);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to paint tile "+x+", "+y);
        }
    }

    /**
     * Paint a single tile on the map. Tile is referenced by its
     * coordinate.
     */
    public void
    paintTile(int x, int y) {
        Graphics    g = this.getGraphics();
        int         xp, yp;

        xp = x * tileXSize;
        yp = y * tileYSize + ((x%2 == 0)?0:tileYOffset);

        try {
            short   t = map.getTerrain(x, y);
            Image   icon = iconSet.getIcon(t);
            g.drawImage(icon, xp, yp, this);

            // Now display a river, if one is needed.
            if (map.isRiver(x, y)) {
                icon = riverSet.getIcon(map.getRiverMask(x, y));
                g.drawImage(icon, xp, yp, this);
            }

            if (map.isSite(x, y)) {
                icon = siteSet.getIcon(map.getSiteMask(x, y));
                g.drawImage(icon, xp, yp, this);
                // Show name, should be configurable.
                g.drawString(map.getSite(x, y).getName(), xp, yp);
            }
        } catch (Exception e) {
            System.out.println("Failed to paint tile "+x+", "+y);
        }

    }

    /**
     * Main method for use when testing.
     */
    public static void
    main(String args[]) {
        JFrame      frame = new JFrame("Map Viewer");
        MapViewer   view = new MapViewer("harn.map");

        frame.getContentPane().add(view);
        frame.setSize(new Dimension(600,700));
        frame.setVisible(true);
    }
}
