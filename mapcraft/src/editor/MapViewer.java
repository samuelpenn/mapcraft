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
    protected IconSet     thingSet = null;
    protected IconSet     featureSet = null;

    private boolean       showGrid = true;
    private boolean       showLargeGrid = true;
    private boolean       showThings = true;
    private boolean       showFeatures = true;
    private boolean       showCoasts = true;
    private boolean       showRivers = true;
    private boolean       showRoads = true;
    private boolean       showAreas = true;

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
                //debug("Loading icon ["+path+"]");
                Image   icon = toolkit.getImage(path);
                toolkit.prepareImage(icon, -1, -1, this);
                iconSet.add(id, icon);
            }
        }

        outlineIcon = toolkit.getImage(views[view].getPath()+"/outline.png");

        return iconSet;
    }

    protected void
    readAllIcons() {
        iconSet = readIcons("terrain", map.getTerrainSet());
        thingSet = readIcons("things", map.getThingSet());
        featureSet = readIcons("features", map.getFeatureSet());
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

            views = new ViewProperties[8];
            views[0] = getNewViewProperties(path+"/usmall");
            views[1] = getNewViewProperties(path+"/xxsmall");
            views[2] = getNewViewProperties(path+"/xsmall");
            views[3] = getNewViewProperties(path+"/small");
            views[4] = getNewViewProperties(path+"/medium");
            views[5] = getNewViewProperties(path+"/large");
            views[6] = getNewViewProperties(path+"/xlarge");
            views[7] = getNewViewProperties(path+"/xxlarge");
            setView(4);

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
            thingSet = readIcons("things", map.getThingSet());

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
     * Is the map currently displaying things?
     */
    public boolean
    isShowThings() { return showThings; }

    /**
     * Set whether the map should show things.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowThings(boolean show) {
        showThings = show;
        paintComponent();
    }

    /**
     * Set whether the map should show area borders.
     */
    public void
    setShowAreas(boolean show) {
        showAreas = show;
        paintComponent();
    }

    /**
     * Is the map displaying area borders?
     */
    public boolean
    isShowAreas() {
        return showAreas;
    }

    /**
     * Is the map currently displaying hills?
     */
    public boolean
    isShowFeatures() { return showFeatures; }

    /**
     * Set whether the map should show hills.
     * Force a redraw of the map to update the display.
     */
    public void
    setShowFeatures(boolean show) {
        showFeatures = show;
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
    drawRivers() {
        drawRivers((Graphics2D)this.getGraphics());
    }

    public void
    drawRivers(Graphics2D g) {
        int     i = 0, e = 0;

        for (i=0; i < map.getRivers().size(); i++) {
            Path    path = (Path)map.getRivers().elementAt(i);
            Shape   shape = path.getGraphicsShape(g, tileXSize, tileYSize, tileYOffset,
                                                  iconWidth, iconHeight);

            g.setStroke(new BasicStroke(path.getWidth()));
            if (path.isHighlighted()) {
                g.setColor(new Color(60, 60, 255));
            } else {
                g.setColor(new Color(184, 253, 253));
            }
            g.draw(shape);
        }
    }

    public void
    drawLargeGrid(Graphics2D g) {
    }



    public void
    paintComponent() {
        if (this.getGraphics() == null) {
            return;
        }
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

            if (showRivers) {
                drawRivers((Graphics2D)g);
            }

            // Now draw labels. Need to draw these last so that they don't
            // get overwritten by tiles. No labels are drawn for LOCAL maps.
            if (showThings) {
                Vector   things = map.getThings();
                for (int i=0; i < things.size(); i++) {
                    Thing    thing = (Thing)things.elementAt(i);
                    if (thing.getX() < (100 * startX) || thing.getX() > (100 * endX)) {
                        continue;
                    }
                    if (thing.getY() < (100 * startY) || thing.getY() > (100 * endY)) {
                        continue;
                    }
                    paintThing(thing, g);

                }
            }

            if (showLargeGrid) {
                int x1, x2, y1, y2;
                Graphics2D  g2 = (Graphics2D)g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));

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
     * Return a point representing the top left corner of thhis tile.
     * For hexagonal maps, this is non-trivial to work out.
     */
    private Point
    getPosition(int x, int y) {
        int     px = 0;
        int     py = 0;

        if (map.getTileShape() == Map.SQUARE) {
            px = x * tileXSize;
            py = y * tileYSize;
        } else {
            px = x * tileXSize;
            py = y * tileYSize;
            if (x%2 == 1) {
                py += tileYOffset;
            }
        }

        return new Point(px, py);
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

            if (map.getFeature(x, y) > 0) {
                icon = featureSet.getIcon(map.getFeature(x, y));
                g.drawImage(icon, xp, yp, this);
            }

            // Area borders should be drawn if the neighbouring tiles belong
            // to a different area. Borders are not drawn on the edge of the
            // map, and are only drawn on three sides (the neighbours will
            // draw their own border for the other three sides).
            if (showAreas && map.getTileShape() == Map.HEXAGONAL) {
                Graphics2D  g2 = (Graphics2D)g;
                int         area = map.getTile(x, y).getArea();
                int         x1, y1, x2, y2;

                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke((float)(0.5 * view)));

                // Top neighbour (only if not top row).
                if (y > 0) {
                    if (area != map.getTile(x, y-1).getArea()) {
                        Point   p = getPosition(x, y);

                        x1 = (int)p.getX() + (int)(iconWidth * 0.3);
                        x2 = (int)p.getX() + (int)(iconWidth * 0.7);
                        y1 = y2 = (int)p.getY();

                        GeneralPath gp = new GeneralPath();
                        Line2D      line = new Line2D.Float(x1, y1, x2, y2);
                        gp.append(line, true);
                        g2.draw(gp);
                    }
                }

                // Now left top neighbour. For hexagonal maps, this is
                // complicated by the uneven columns.
                if (x > 0 && y > 0) {
                    int     n = 0;

                    if (x%2 == 0) {
                        n = map.getTile(x-1, y-1).getArea();
                    } else {
                        n = map.getTile(x-1, y).getArea();
                    }
                    if (area != n) {
                        Point   p = getPosition(x, y);

                        x1 = (int)p.getX();
                        x2 = (int)p.getX() + (int)(iconWidth * 0.3);
                        y1 = (int)p.getY() + (int)(tileYSize * 0.5);
                        y2 = (int)p.getY();

                        GeneralPath gp = new GeneralPath();
                        Line2D      line = new Line2D.Float(x1, y1, x2, y2);
                        gp.append(line, true);
                        g2.draw(gp);
                    }
                }

                // Now left bottom neighbour.
                if (x > 0 && y < map.getHeight()) {
                    int     n = 0;

                    if (x%2 == 0) {
                        n = map.getTile(x-1, y).getArea();
                    } else if (y+1 < map.getHeight()) {
                        n = map.getTile(x-1, y+1).getArea();
                    } else {
                        // We don't want to display border along edge of map.
                        n = area;
                    }
                    if (area != n) {
                        Point   p = getPosition(x, y);

                        x1 = (int)p.getX();
                        x2 = (int)p.getX() + (int)(iconWidth * 0.3);
                        y1 = (int)p.getY() + (int)(tileYSize * 0.5);
                        y2 = (int)p.getY() + tileYSize;

                        GeneralPath gp = new GeneralPath();
                        Line2D      line = new Line2D.Float(x1, y1, x2, y2);
                        gp.append(line, true);
                        g2.draw(gp);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to paint tile "+x+", "+y);
        }
    }

    private double
    getFontScale() {
        double fontScale = 1.0;

        switch (view) {
            case 0: // usmall
                fontScale = 0.6;
                break;
            case 1: // xxsmall
                fontScale = 0.75;
                break;
            case 2: // xsmall
                fontScale = 0.85;
                break;
            case 3: // small
                fontScale = 1.0;
                break;
            case 4: // medium
                fontScale = 1.0;
                break;
            case 5: // large
                fontScale = 1.3;
                break;
            case 6: // xlarge
                fontScale = 1.6;
                break;
            case 7: // xxlarge
                fontScale = 2.0;
                break;
        }

        return fontScale;
    }

    public void
    paintThing(Thing thing, Graphics g) {
        int     x=0, y=0;
        int     fontSize = 10;
        Image   icon = thingSet.getIcon(thing.getType());

        debug("paintThing: ["+thing.getName()+"]");

        // If scale is too large, and thing not importance enough,
        // then don't display it.
        switch (thing.getImportance()) {
            case Thing.LOW:
                if (view < 4) return;
                break;
            case Thing.NORMAL:
                if (view < 2) return;
                break;
            case Thing.HIGH:
                break;
        }

        x = thing.getX() * tileXSize / 100 - tileXSize/2;
        y = thing.getY() * tileYSize / 100 - tileYSize/2;

        g.drawImage(icon, x, y, this);
        g.setColor(Color.BLACK);

        switch (thing.getFontSize())  {
        case Thing.SMALL:
            fontSize = 8;
            break;
        case Thing.MEDIUM:
            fontSize = 10;
            break;
        case Thing.LARGE:
            fontSize = 14;
            break;
        case Thing.HUGE:
            fontSize = 18;
            break;
        }
        fontSize = (int) (getFontScale() * fontSize);
        Font    font = new Font("Helvetica", Font.PLAIN, fontSize);
        g.setFont(font);
        // Show name, should be configurable.
        if (map.getType() != Map.LOCAL) {
            g.drawString(thing.getName(), x, y);
        }
    }

    public boolean
    crop(int x, int y, int w, int h) {
        map.crop(map.getCurrentSet(), x, y, w, h);
        paintComponent();

        return true;
    }

    public boolean
    cropToArea(String area, int margin) {
        short       a = (short)map.getAreaByName(area).getId();

        return cropToArea(a, margin);
    }

    public boolean
    cropToArea(short area, int margin) {
        map.cropToArea(map.getCurrentSet(), area, margin);
        paintComponent();

        return true;
    }

    public boolean
    rescale(int newScale) {
        map.rescale(map.getCurrentSet(), newScale);
        paintComponent();

        return true;
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
