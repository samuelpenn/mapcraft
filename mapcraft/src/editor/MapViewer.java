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
package uk.co.demon.bifrost.rpg.mapcraft.editor;

import uk.co.demon.bifrost.rpg.mapcraft.map.*;
import uk.co.demon.bifrost.rpg.mapcraft.map.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;

import java.util.*;
import java.awt.geom.Line2D;

import com.sun.image.codec.jpeg.*;


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
    protected int         iconWidth = 30;
    protected int         iconHeight = 35;

    protected Toolkit     toolkit = null;
    protected Image[]     icons = null;
    protected Image       outlineIcon = null;
    protected String      imagePath = "images";

    protected IconSet     iconSet = null;
    protected IconSet     riverSet = null;
    protected IconSet     siteSet = null;
    protected IconSet     hillSet = null;

    private boolean       showGrid = true;
    private boolean       showLargeGrid = true;
    private boolean       showSites = true;
    private boolean       showHills = true;
    private boolean       showCoasts = true;
    private boolean       showRivers = true;
    private boolean       showRoads = true;

    protected Properties        properties;
    protected ViewProperties    views[];
    protected int               view = 0;

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
     * Inner class which holds properties of each view on the image.
     * A view is a scaling, so generally a map will have several view
     * scales, each with a different set of icons.
     */
    protected class ViewProperties {
        private Properties  properties;

        private String  viewName;
        private String  viewShape;
        private String  path;
        private int     iconHeight;
        private int     iconWidth;
        private int     tileHeight;
        private int     tileWidth;
        private int     tileOffset;
        private int     fontSize;

        public
        ViewProperties(String path) throws Exception {
            FileInputStream     input = null;
            String              file = path+"/icons.properties";

            this.path = path;
            properties = new Properties();

            try {
                input = new FileInputStream(file);
                properties.load(input);
                System.out.println("Loaded properties");
                
                viewName = (String)properties.get("view.name");
                viewShape = (String)properties.get("view.shape");
                
                iconHeight = (int)Integer.parseInt((String)properties.get("icon.height"));
                iconWidth = (int)Integer.parseInt((String)properties.get("icon.width"));

                tileHeight = (int)Integer.parseInt((String)properties.get("tile.height"));
                tileWidth = (int)Integer.parseInt((String)properties.get("tile.width"));
                tileOffset = (int)Integer.parseInt((String)properties.get("tile.offset"));

            } catch (Exception e) {
                System.out.println("Cannot load properties from file ["+file+"]");
                throw e;
            }
        }
        
        public String
        toString() {
            return viewName + " ("+iconWidth+"x"+iconHeight+")";
        }
        
        public String getPath() { return path; }

        /**
         * The name of the view, normally a descriptive name for the
         * size of the icons, such as 'Small' or 'Medium'.
         */
        public String getName() { return viewName; }
        
        /**
         * The shape used by the icons, either 'Square' or 'Hexagonal'.
         */
        public String getShape() { return viewShape; }
        
        /**
         * The actual physical height of each icon, in pixels.
         */
        public int getIconHeight() { return iconHeight; }

        /**
         * The actual physical width of each icon, in pixels.
         */
        public int getIconWidth() { return iconWidth; }
        
        /**
         * The height of a tile. For square maps, this is identical to the
         * icon height. For hexagonal maps, it may be different.
         */
        public int getTileHeight() { return tileHeight; }
        
        /**
         * The width of a tile. For square maps, this is identical to the
         * icon width. For hexagonal maps, it is smaller, since columns of
         * hexagons will overlap each other.
         */
        public int getTileWidth() { return tileWidth; }
        
        /**
         * Vertical offset of odd numbered columns. Zero for square maps,
         * positive (downwards offset) for hexagonal maps. Normally equal
         * to about half the height of a hexagonal tile. Note that the
         * first column is zero, and hence even.
         */
        public int getTileOffset() { return tileOffset; }

    }


    /**
     * Basic constructor. Creates an empty map.
     */
    public MapViewer() {
        super(true);

        toolkit = Toolkit.getDefaultToolkit();
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
                short   id = t.getId();
                String  path = views[view].getPath()+"/"+t.getImagePath();
                Image   icon = toolkit.getImage(path);

                iconSet.add(id, icon);
            }
        }

        outlineIcon = toolkit.getImage(views[view].getPath()+"/outline.png");

        return iconSet;
    }

    protected void
    readAllIcons() {
        iconSet = readIcons("terrain", map.getTerrainSet());
        siteSet = readIcons("sites", map.getPlaceSet());
        hillSet = readIcons("hills", map.getHillSet());
    }
    
    private ViewProperties
    getNewViewProperties(String path) {
        try {
            return new ViewProperties(path);
        } catch (Exception e) {
            return (ViewProperties) null;
        }
    }

    /**
     * Constructor to load a map from a file and display it.
     */
    public
    MapViewer(Properties properties, String filename) {
        super(true);

        toolkit = Toolkit.getDefaultToolkit();
        
        debug("Image path = "+properties.getProperty("path.images"));

        this.properties = properties;
        
        String path = properties.getProperty("path.images", "images");

        try {
            map = new Map(filename);
            path = path + "/"+map.getImageDir();

            views = new ViewProperties[7];
            views[0] = getNewViewProperties(path+"/xxsmall");
            views[1] = getNewViewProperties(path+"/xsmall");
            views[2] = getNewViewProperties(path+"/small");
            views[3] = getNewViewProperties(path+"/medium");
            views[4] = getNewViewProperties(path+"/large");
            views[5] = getNewViewProperties(path+"/xlarge");
            views[6] = getNewViewProperties(path+"/xxlarge");
            setView(3);

            readAllIcons();

            map.setCurrentSet("root");

        } catch (MapException e) {
            e.printStackTrace();
        }
    }


    public void
    setImagePath(String path) {
        this.imagePath = path;
    }

    public void
    loadMap(String filename) {
        try {
            map = new Map(filename);

            TerrainSet  set = map.getTerrainSet();


            iconSet = readIcons("terrain", set);
            siteSet = readIcons("sites", map.getPlaceSet());

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
    
    public void
    zoomIn() {
        if (view < 6) {
            setView(view+1);
        }
    }

    public void
    zoomOut() {
        if (view > 0) {
            setView(view-1);
        }
    }

    public void
    setView(int view) {
        if (views[view] == null) {
            return;
        }

        this.view = view;
        tileXSize = views[view].getTileWidth();
        tileYSize = views[view].getTileHeight();
        tileYOffset = views[view].getTileOffset();
        iconWidth = views[view].getIconWidth();
        iconHeight = views[view].getIconHeight();

        // Now work out how big we want to be. Can assume we'll
        // be placed in a scrollable widget of some sort, so don't
        // worry about screen size or anything like that.
        int realWidth, realHeight;

        realWidth = tileXSize * (map.getWidth()+1);
        realHeight = tileYSize * (map.getHeight()+1);

        setPreferredSize(new Dimension(realWidth, realHeight));
        
        readAllIcons();
        repaint();
    }


    /**
     * Is the map currently displaying the tile grid?
     */
    public boolean
    isShowGrid() { return showGrid; }

    /**
     * Set whether the map should show the tile grid.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowGrid(boolean show) {
        showGrid = show;
        paintComponent();
    }

    /**
     * Is the map currently displaying the large helper grid?
     */
    public boolean
    isShowLargeGrid() { return showLargeGrid; }
    
    /**
     * Set whether the map should show the helper grid.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowLargeGrid(boolean show) {
        showLargeGrid = show;
        paintComponent();
    }


    /**
     * Is the map currently displaying sites?
     */
    public boolean
    isShowSites() { return showSites; }

    /**
     * Set whether the map should show sites.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowSites(boolean show) {
        showSites = show;
        paintComponent();
    }

    /**
     * Is the map currently displaying hills?
     */
    public boolean
    isShowHills() { return showHills; }

    /**
     * Set whether the map should show hills.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowHills(boolean show) {
        showHills = show;
        paintComponent();
    }

    public boolean
    isShowCoasts() { return showCoasts; }

    public void
    setShowCoasts(boolean show) {
        showCoasts = show;
        paintComponent();
    }

    public boolean
    isShowRivers() { return showRivers; }

    public void
    setShowRivers(boolean show) {
        showRivers = show;
        paintComponent();
    }

    public boolean
    isShowRoads() { return showRoads; }

    public void
    setShowRoads(boolean show) {
        showRoads = show;
        paintComponent();
    }



    public void
    drawRivers(Graphics2D g) {
        int     i = 0, e = 0;

        info("Drawing all rivers");

        for (i=0; i < map.getRivers().size(); i++) {

            Path    path = (Path)map.getRivers().elementAt(i);
            Shape   shape = path.getGraphicsShape(g, tileXSize, tileYSize, tileYOffset,
                                                  iconWidth, iconHeight);

            info("Drawing shape for river");
            g.draw(shape);
        }
    }

    public void
    drawLargeGrid(Graphics2D g) {
    }



    public void
    paintComponent() {
        paintComponent(this.getGraphics());
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

        // Boundary rectangle which needs to be drawn.
        int             startX = 0;
        int             startY = 0;
        int             endX = map.getWidth();
        int             endY = map.getHeight();

        // Clip area to rectangle.
        Rectangle   clip = g.getClipBounds();
        if (clip != null) {
            startX = (int)(clip.getX()/tileXSize) -1;
            endX = (int)((clip.getX()+clip.getWidth())/tileXSize) + 1;

            startY = (int)(clip.getY()/tileYSize) -1;
            endY = (int)((clip.getY()+clip.getHeight())/tileYSize) + 1;
        } else {
            startX = startY = 0;
            endX = map.getWidth();
            endY = map.getHeight();
        }

        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;

        if (endX > map.getWidth()) {
            endX = map.getWidth();
        }

        if (endY > map.getHeight()) {
            endY = map.getHeight();
        }

        try {
            for (y = startY; y < endY; y++) {
                for (x = startX; x < endX; x++) {
                    paintTile(x, y, g);
                }
            }

            drawRivers((Graphics2D)g);

            // Now draw labels. Need to draw these last so that they don't
            // get overwritten by tiles. No labels are drawn for LOCAL maps.
            if (map.getType() == Map.WORLD) {
                for (y = startY; y < endY; y++) {
                    yp = y * tileYSize;
                    for (x = startX; x < endX; x++) {
                        if (map.isSite(x, y)) {
                            // Show name, should be configurable.
                            xp = x * tileXSize;
                            ypp = yp + ((x%2 == 0)?0:tileYOffset);
                            g.drawString(map.getSite(x, y).getName(), xp, ypp);
                        }
                    }
                }
            }

            if (showLargeGrid) {
                int x1, x2, y1, y2;
                Graphics2D  g2 = (Graphics2D)g;

                // Vertical lines
                y1 = 0;
                y2 = endY * tileYSize;
                for (x = 0; x < endX; x+=6) {
                    x1 = x2 = (int) (x * tileXSize + (iconWidth * 0.5));
                    GeneralPath gp = new GeneralPath();
                    Line2D      line = new Line2D.Float(x1, y1, x2, y2);
                    gp.append(line, true);
                    g2.draw(gp);
                }

                // Horizontal lines
                x1 = 0;
                x2 = endX * tileXSize;
                for (y = 0; y < endY; y+=5) {
                    y1 = y2 = (int) (y * tileYSize);
                    GeneralPath gp = new GeneralPath();
                    Line2D      line = new Line2D.Float(x1, y1, x2, y2);
                    gp.append(line, true);
                    g2.draw(gp);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to paint tile "+x+", "+y);
        }
    }

    /**
     * Paint a single tile on the map. Tile is referenced by its
     * coordinate. Tile is painted to the graphics object of the
     * MapViewer component.
     */
    public void
    paintTile(int x, int y) {
        Graphics    g = this.getGraphics();

        paintTile(x, y, g);
    }

    /**
     * Paint a single tile on the map. Tile is referenced by its
     * coordinate. Tile is painted to the supplied graphics object.
     */
    public void
    paintTile(int x, int y, Graphics g) {
        int         xp, yp;

        if (map.getTileShape() == Map.SQUARE) {
            xp = x * tileXSize;
            yp = y * tileYSize;
        } else {
            xp = x * tileXSize;
            yp = y * tileYSize + ((x%2 == 0)?0:tileYOffset);
        }

        try {
            short   t = map.getTerrain(x, y);
            Image   icon = iconSet.getIcon(t);
            g.drawImage(icon, xp, yp, this);

            if (map.getHills(x, y) > 0) {
                icon = hillSet.getIcon(map.getHills(x, y));
                g.drawImage(icon, xp, yp, this);
            }

            if (map.isSite(x, y)) {
                int     sx=0, sy=0, rot=0;

                icon = siteSet.getIcon(map.getSiteMask(x, y));
                sx = map.getSite(x, y).getX();
                sy = map.getSite(x, y).getY();
                rot = map.getSite(x, y).getRotation();

                if (sx !=0 || sy !=0) {
                    xp += (int)((iconWidth/200.0) * sx);
                    yp += (int)((iconHeight/200.0) * sy);
                }

                g.drawImage(icon, xp, yp, this);
                // Show name, should be configurable.
                if (map.getType() != Map.LOCAL) {
                    g.drawString(map.getSite(x, y).getName(), xp, yp);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to paint tile "+x+", "+y);
        }

    }

    /**
     * Convert the map to an Image, ready for saving to disc.
     */
    public BufferedImage
    toImage() {
        BufferedImage   image = null;
        Graphics2D      graphics = null;
        int             w, h;

        try {
            w = map.getWidth() * tileXSize;
            h = map.getHeight() * tileYSize;

            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            System.out.println("Painting image...");
            paintComponent(image.getGraphics());
            System.out.println("...finished painting");

        } catch (Exception e) {
            System.out.println("toImage: "+e.getMessage());
            e.printStackTrace();
        }

        return image;
    }

    public void
    saveImage(String filename) {
        OutputStream    out = null;
        BufferedImage   image = null;

        System.out.println("Saving image as ["+filename+"]");
        try {
            Thread.sleep(5000);
        } catch (Exception ee) { }

        try {
            image = toImage();

            File file = new File(filename);

            out = new BufferedOutputStream(new FileOutputStream(file));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

            encoder.encode(image);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable t) {}
        }
    }

    /**
     * Main method for use when testing.
     */
    public static void
    main(String args[]) {
    /*
        JFrame      frame = new JFrame("Map Viewer");
        MapViewer   view = new MapViewer("maps/harn.map", "images");

        frame.getContentPane().add(view);
        frame.setSize(new Dimension(600,700));
        frame.setVisible(true);
    */
    }
}
