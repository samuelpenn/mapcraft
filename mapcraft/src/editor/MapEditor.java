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
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

import uk.co.demon.bifrost.utils.Options;


public class MapEditor extends MapViewer
                       implements ActionListener {


    private JFrame      frame;
    private JMenuBar    menuBar;
    private ScrollPane  scrollpane;

    private Pane        terrainPane = null;
    private Pane        riverPane = null;
    private Pane        placePane = null;
    private Pane        hillPane = null;
    private Pane        areaPane = null;

    private Brush       brush = new Brush();

    /**
     * Event handler for the terrain palette. Provides logic for
     * when an item in the terrain palette (terrainPane) is selected.
     * Changes the 'terrainSelected' field, and changes the painting
     * mode to be MODE_BRUSH.
     */
    public class
    TerrainPalette implements ListSelectionListener {
        private MapEditor editor;

        public
        TerrainPalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (terrainPane.isSelected(last)) {
                index = last;
            }

            System.out.println("TerrainPalette: "+index);
            Terrain ta[] = map.getTerrainSet().toArray();
            brush.setSelected(Brush.TERRAIN, ta[index].getId());
            System.out.println("Terrain selected: "+brush.getSelected());

            brush.setType(Brush.TERRAIN);
        }
    }

    public class
    PlacePalette implements ListSelectionListener {
        private MapEditor editor;

        public
        PlacePalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;
            int type = Brush.SITES;

            if (map.getType() == Map.LOCAL) {
                type = Brush.FEATURES;
            }

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (placePane.isSelected(last)) {
                index = last;
            }

            System.out.println("PlacePalette: "+index);
            Terrain ta[] = map.getPlaceSet().toArray();
            brush.setSelected(type, ta[index].getId());
            System.out.println("Place selected: "+brush.getSelected());

            brush.setType(type);
        }
    }

    public class
    HillPalette implements ListSelectionListener {
        private MapEditor editor;

        public
        HillPalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;
            int type = Brush.HILLS;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (hillPane.isSelected(last)) {
                index = last;
            }

            System.out.println("HillPalette: "+index);
            brush.setSelected(type, (short)index);
            System.out.println("Hill selected: "+brush.getSelected());

            brush.setType(type);
        }
    }

    /**
     * Displays a list of all defined areas. An area has a name (no icons).
     */
    public class
    AreaPalette implements ListSelectionListener {
        private MapEditor editor;

        public
        AreaPalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;
            int type = Brush.AREAS;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (areaPane.isSelected(last)) {
                index = last;
            }

            System.out.println("AreaPalette: "+index);
            brush.setSelected(type, (short)index);
            System.out.println("Area selected: "+brush.getSelected());

            brush.setType(type);
        }
    }


    public void
    applyBrush(int x, int y) {
        try {
            switch (brush.getType()) {
            case Brush.TERRAIN:
                info("Applying terrain brush "+brush.getSelected());
                switch (brush.getSize()) {
                case Brush.MEDIUM:
                    // A medium sized brush is 3 tiles across.
                    for (int px = x - 1; px <= x+1; px++) {
                        for (int py = y - 1; py <= y+1; py++) {
                            map.setTerrain(px, py, brush.getSelected());
                            paintTile(px, py);
                        }
                    }
                    break;
                case Brush.LARGE:
                    // A large sized brush is 7 tiles across.
                    for (int px = x - 3; px <= x+3; px++) {
                        for (int py = y - 3; py <= y+3; py++) {
                            map.setTerrain(px, py, brush.getSelected());
                            paintTile(px, py);
                        }
                    }
                    break;
                default:
                    map.setTerrain(x, y, brush.getSelected());
                    paintTile(x, y);
                    break;
                }
                break;
            case Brush.HILLS:
                info("Applying hill brush");
                map.getTile(x, y).setHills(brush.getSelected());
                paintTile(x, y);
                break;
            case Brush.FEATURES:
                info("Applying feature brush");
                if (brush.getSelected() == 0) {
                    map.getTile(x, y).setSite((Site)null);
                } else {
                    Site site = new Site(brush.getSelected(), "Unnamed", "Unnamed");
                    map.getTile(x, y).setSite(site);
                }
                paintTile(x, y);
                break;
            case Brush.SITES:
                if (brush.getSelected() == 0) {
                    map.getTile(x, y).setSite((Site)null);
                } else if (!map.isSite(x, y)) {
                    // Only set site if no site currently present.
                    Site    site = new Site(brush.getSelected(), "Unnamed", "Unknown");
                    info("Applying site brush "+brush.getSelected());
                    map.getTile(x, y).setSite(site);
                    info("Opening dialog");
                    SiteDialog dialog = new SiteDialog(site, frame);
                    info("Finished dialog");
                    site.setName(dialog.getName());
                    site.setDescription(dialog.getDescription());
                } else {
                    // Do nothing.
                }
                paintTile(x, y);
                break;
            case Brush.AREAS:
                map.getTile(x, y).setArea(brush.getSelected());
                paintTile(x, y);
                break;
            case Brush.RIVERS:
                info("Distance: "+map.distance(2, 2, x, y));
                if (brush.getSelected() == 0) {
                    // Have not yet selected a river.
                    String  name = "River "+(map.getRivers().size()+1);
                    info("Creating new river ["+name+"] at "+x+","+y);
                    int     id = map.addRiver(name, x, y);
                    brush.setSelected(Brush.RIVERS, (short)id);
                    drawRivers();
                } else if (map.isRiver(x, y)) {
                    debug("There is already a river here");
                } else {
                    Path    river = map.getRiver(brush.getSelected());
                    debug("Adding to river ["+river.getName()+"]");
                    // Add to a current river.
                    info("Adding new element to river");
                    Path.Element    end = river.getEndPoint();
                    Path.Element    start = river.getStartPoint();
                    if (map.isNextTo(end.getX(), end.getY(), x, y)) {
                        debug("Next to end, so adding");
                        map.extendRiver(brush.getSelected(), x, y);
                        drawRivers();
                    } else if (map.isNextTo(start.getX(), start.getY(), x, y)) {
                        debug("Next to start, so adding");
                        map.extendRiver(brush.getSelected(), x, y);
                        drawRivers();
                    }
                }
                break;
            }
        } catch (MapOutOfBoundsException moobe) {
            warn("Out of bounds!");
        }

    }

    /**
     * Set the size of the brush.
     */
    public void
    setBrushSize(int size) {
        brush.setSize(size);
    }

    /**
     * Set the type of the brush. This defines whether we are painting
     * terrain, sites, rivers, hills etc. Allowable brush types are
     * defined in the Brush class.
     */
    public void
    setBrushType(int type) {
        debug("Setting brush type to be "+type);
        brush.setType(type);
    }


    /**
     * Class to handle mouse click events.
     */
    public class
    MouseHandler extends MouseAdapter {
        public void
        mousePressed(MouseEvent e) {
            int     x,y;
            int     yp;

            if (map.getTileShape() == Map.SQUARE) {
                x = e.getX()/tileXSize;
                y = e.getY()/tileYSize;
            } else {
                x = e.getX()/tileXSize;
                yp = e.getY();

                if (x%2 != 0) {
                    yp -= tileYOffset;
                }
                y = yp/tileYSize;
            }

            if (e.getButton() == e.BUTTON1) {
                // Left mouse button
                applyBrush(x, y);
            } else if (e.getButton() == e.BUTTON3) {
                // Right mouse button
                debug("Right mouse");
                try {
                    if (map.isSite(x, y)) {
                        Site        site = map.getSite(x, y);
                        SiteDialog  dialog = new SiteDialog(site, frame);

                        site.setName(dialog.getName());
                        site.setDescription(dialog.getDescription());
                        paintTile(x, y);
                    }
                } catch (MapOutOfBoundsException oobe) {
                    warn("Mouse click out of bounds");
                }
            }
        }
    }

    /**
     * Class to handle mouse motion events.
     */
    public class
    MouseMotionHandler extends MouseMotionAdapter {
        public void
        mouseDragged(MouseEvent e) {
            int     x,y;
            int     yp;

            if (brush.getType() == Brush.SITES) {
                // Don't allow drag paint for painting of sites.
                return;
            }

            if (map.getTileShape() == Map.SQUARE) {
                x = e.getX()/tileXSize;
                y = e.getY()/tileYSize;
            } else {
                x = e.getX()/tileXSize;
                yp = e.getY();

                if (x%2 != 0) {
                    yp -= tileYOffset;
                }
                y = yp/tileYSize;
            }

            applyBrush(x, y);
        }
    }

    public class
    KeyEventHandler extends KeyAdapter {
        public void
        KeyPressed(KeyEvent e) {
            System.out.println("Pressed!");
        }
    }

    /**
     * Class to listen for events from the Edit menu.
     */
    protected class
    MenuEditListener implements ActionListener {
        public void
        actionPerformed(ActionEvent e) {
            String  command = e.getActionCommand();

            info("ActionEvent: Menu edit."+command);

            if (command.equals("double")) {
                map.rescaleMap(map.getScale()/2);
            }
        }
    }

    /**
     * Class to listen for events from the File menu.
     */
    protected class
    MenuFileListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String command = ev.getActionCommand();

            info("ActionEvent: Menu file."+command);

            if (command.equals("load")) {
                // Load a map file.
                try {
                    map.load("kanday.map");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("save")) {
                // Save current map file.
                try {
                    map.save(map.getFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Unimplemented menu option.
                warn("ActionEvent: Unsupported menu file."+command);
            }
        }
    }
    
    public void
    save(String filename) throws IOException {
        map.save(filename);
    }
    
    public void
    save() throws IOException {
        save(map.getFilename());
    }

    /**
     * Class to listen for events from the File menu.
     */
    protected class
    MenuViewListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String command = ev.getActionCommand();

            info("ActionEvent: MenuViewListener."+command);

            if (command.equals("showGrid")) {
                info("ShowGrid");
            } else if (command.equals("0")) {
                setView(0);
            } else if (command.equals("1")) {
                setView(1);
            } else if (command.equals("2")) {
                setView(2);
            } else if (command.equals("3")) {
                setView(3);
            } else if (command.equals("4")) {
                setView(4);
            } else if (command.equals("5")) {
                setView(5);
            } else if (command.equals("6")) {
                setView(6);
            } else {
                // Unimplemented menu option.
                warn("ActionEvent: Unsupported MenuViewListener."+command);
            }
        }
    }

    /**
     * Class to listen for events from the Palette menu.
     */
    protected class
    MenuPaletteListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String  command = ev.getActionCommand();

            info("ActionEvent: Menu palette."+command);
            try {
                brush.setSelected(Brush.TERRAIN, (short) Integer.parseInt(command));
            } catch (Exception e) {
                warn("ActionEvent: Palette option not an integer");
            }
        }
    }
    
    public ScrollPane
    getScrollPane() {
        return scrollpane;
    }


    /**
     * Main constructor.
     */
    public
    MapEditor(Properties properties, String filename) {
        super(properties, filename);


        this.setSize(new Dimension(2000, 1200));

        addMouseMotionListener(new MouseMotionHandler());
        addMouseListener(new MouseHandler());

    }

    public
    MapEditor(int width, int height, int scale) {
    }
    
    public void
    showTerrainPalette() {
        terrainPane = new Pane(new TerrainPalette(this), "Terrain");
        terrainPane.setImagePath(properties.getProperty("path.images")+"/"+map.getImageDir()+"/medium");
        terrainPane.setPalette(map.getTerrainSet().toArray(), false);
        terrainPane.makeFrame();
    }

    public void
    showFeaturePalette() {
        if (map.getType() == Map.LOCAL) {
            placePane = new Pane(new PlacePalette(this), "Features");
        } else {
            placePane = new Pane(new PlacePalette(this), "Places");
        }
        placePane.setImagePath(properties.getProperty("path.images")+"/"+map.getImageDir()+"/medium");
        placePane.setPalette(map.getPlaceSet().toArray(), false);
        placePane.makeFrame();
    }

    public void
    showHillPalette() {
        hillPane = new Pane(new HillPalette(this), "Hills");
        hillPane.setImagePath(properties.getProperty("path.images")+"/"+map.getImageDir()+"/medium");

        /*
        Terrain[]   set = new Terrain[6];

        set[0] = new Terrain((short)0, "clear", "Clear", "0.png");
        set[1] = new Terrain((short)1, "lowhills", "Low hills", "1.png");
        set[2] = new Terrain((short)2, "highhills", "High hills", "2.png");
        set[3] = new Terrain((short)3, "foothills", "Foot hills", "3.png");
        set[4] = new Terrain((short)4, "lowmnts", "Low mountains", "4.png");
        set[5] = new Terrain((short)5, "highmnts", "High mountains", "5.png");
*/
        hillPane.setPalette(map.getHillSet().toArray(), false);
        hillPane.makeFrame();
    }

    public void
    showAreaPalette() {
        areaPane = new Pane(new AreaPalette(this), "Areas");
        areaPane.setPalette(map.getAreaSet().toTerrainArray(), false);
        areaPane.makeFrame();
    }

    public void
    actionPerformed(ActionEvent e) {
        JMenuItem   source = (JMenuItem)(e.getSource());
        String      option = source.getText();
        System.out.println(source.getText());

        processEvent(option);
    }

    public void
    processEvent(String option) {
    }


    /**
     * Create the 'Edit' menu for the frame.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createEditMenu() {
        JMenu               menu = new JMenu("Edit");
        JMenuItem	        item;
        MenuEditListener    editListener = new MenuEditListener();

        menu.setMnemonic(KeyEvent.VK_E);

        item = new JMenuItem("Clear", KeyEvent.VK_C);
        menu.add(item);

        menu.add(item = new JMenuItem("Double detail", KeyEvent.VK_D));
        item.setActionCommand("double");
        item.addActionListener(editListener);

        return menu;
    }
    
    public JMenuItem
    createItem(JMenuItem item, String command, ActionListener l) {
        item.setActionCommand(command);
        item.addActionListener(l);

        return item;
    }

    /**
     * Create the 'View' menu for the frame.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createViewMenu() {
        JMenu               menu = new JMenu("View");
        JMenuItem	        item;
        MenuViewListener    listener = new MenuViewListener();
        int                 i = 0;

        menu.setMnemonic(KeyEvent.VK_V);
        item = new JCheckBoxMenuItem("Show grid", true);
        item.addItemListener(new ItemListener() {
                public void
                itemStateChanged(ItemEvent e) {
                    boolean s = (e.getStateChange() == ItemEvent.SELECTED);
                    setShowGrid(s);
                }
            });

        //item.setAction(handler.getShowGridAction());
        menu.add(item);

        menu.addSeparator();

        for (i=0; i < views.length; i++) {
            if (views[i] != null) {
                menu.add(createItem(new JMenuItem(views[i].toString()), ""+i, listener));
            }
        }


/*
        menu.add(createItem(new JCheckBoxMenuItem("Show grid", true),
                            "showGrid", listener), KeyEvent.VK_G);
        menu.add(createItem(new JCheckBoxMenuItem("Show sites", true),
                            "showSites", listener), KeyEvent.VK_S);
        menu.add(createItem(new JCheckBoxMenuItem("Show hills", true),
                            "showHills", listener), KeyEvent.VK_H);
        menu.add(createItem(new JCheckBoxMenuItem("Show coasts", true),
                            "showCoasts", listener), KeyEvent.VK_C);
*/
        return menu;
    }

    JMenu
    createPaletteMenu() {
        JMenu       menu = new JMenu("Palette");
        JMenuItem   item;
        MenuPaletteListener listener = new MenuPaletteListener();

        menu.setMnemonic(KeyEvent.VK_P);
        
        TerrainSet  set = map.getTerrainSet();
        Iterator    it = set.iterator();
        
        while (it.hasNext()) {
            Terrain t = (Terrain)it.next();
            menu.add(item = new JMenuItem(t.getDescription()));
            item.setActionCommand(""+t.getId());
            item.addActionListener(listener);

        }

        return menu;
    }


    public static void
    main(String args[]) {
        Options options = new Options(args);
        MapEditor   editor = null;
        Map         map = null;
/*
        try {
            options = new Options(args);

            if (options.isOption("-create")) {
                int width = options.getInt("-width");
                int height = options.getInt("-height");
                int scale = options.getInt("-scale");
                String name = options.getString("-create");
                String terrain = options.getString("-terrain");
                boolean square = options.isOption("-square");
                boolean local = options.isOption("-local");


                System.out.println("Creating map "+name+" "+width+"x"+height);
                map = new Map(name, width, height, scale);
                if (square) map.setTileShape(Map.SQUARE);
                if (local) map.setType(Map.LOCAL);
                map.loadTerrainSet(terrain);
                map.save(name+".map");
            } else if (options.isOption("-load")) {
                String  images = "images";

                if (options.isOption("-images")) {
                    images = options.getString("-images");
                }
                editor = new MapEditor(options.getString("-load"), images);
            } else if (options.isOption("-rewrite")) {
                map = new Map(options.getString("-rewrite"));
                map.save("new.map");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }

}
