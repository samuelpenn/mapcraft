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
    private Pane        thingPane = null;
    private Pane        featurePane = null;
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
    ThingPalette implements ListSelectionListener {
        private MapEditor editor;

        public
        ThingPalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;
            int type = Brush.THINGS;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (thingPane.isSelected(last)) {
                index = last;
            }

            System.out.println("ThingPalette: "+index);
            Terrain ta[] = map.getThingSet().toArray();
            brush.setSelected(type, ta[index].getId());
            System.out.println("Thing selected: "+brush.getSelected());

            brush.setType(type);
        }
    }

    public class
    FeaturePalette implements ListSelectionListener {
        private MapEditor editor;

        public
        FeaturePalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;
            int type = Brush.FEATURES;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (featurePane.isSelected(last)) {
                index = last;
            }

            System.out.println("FeaturePalette: "+index);
            brush.setSelected(type, (short)index);
            System.out.println("Feature selected: "+brush.getSelected());

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
            case Brush.FEATURES:
                map.getTile(x, y).setFeature(brush.getSelected());
                paintTile(x, y);
                break;
            case Brush.THINGS:
                switch (brush.getButton()) {
                case MouseEvent.BUTTON1:
                    if (brush.getSelected() == 0) {
                        int     s = map.getNearestThingIndex(brush.getX(), brush.getY(), 100);
                        if (s >= 0) {
                            map.removeThing(s);
                        }
                    } else {
                        // Only set Thing if no Thing currently present.
                        Thing   thing = new Thing(brush.getSelected(), "Unnamed", "Unknown",
                                                brush.getX(), brush.getY());
                        map.addThing(thing);
                        ThingDialog dialog = new ThingDialog(thing, frame,
                                        map.getThingSet(), views[5].getPath());
                        dialog.getThing();
                    }
                    break;
                case MouseEvent.BUTTON2:
                    info("Moving thing");
                    Thing    thing = map.getNearestThing(brush.getX(), brush.getY(), 100);
                    if (thing != null) {
                        thing.setPosition(brush.getX(), brush.getY());
                    }
                    break;
                }
                paintComponent();
                break;
            case Brush.AREAS:
                map.getTile(x, y).setArea((short)brush.getSelected());
                paintTile(x, y);
                break;
            case Brush.RIVERS:
                applyBrushRivers(brush.getX(), brush.getY());
                break;
            }
        } catch (MapOutOfBoundsException moobe) {
            warn("Out of bounds!");
        }

    }

    private void
    applyBrushRivers(int x, int y) throws MapOutOfBoundsException {
        switch (brush.getMode()) {
        case Brush.SELECT:
            info("Selecting river");
            map.unselectRivers();
            int     nearest = 0;
            int     min = -1;
            for (int r = 1; r <= map.getRivers().size(); r++) {
                Path    river = (Path) map.getRiver(r);
                int     v = river.getNearestVertex(x, y, 250);
                if (v > -1) {
                    int     d = river.getDistanceToVertex(x, y, v);
                    if (min == -1 || d < min) {
                        min = d;
                        nearest = r;
                    }
                }
            }
            if (nearest > 0) {
                debug("Nearest river is "+nearest);
                map.getRiver(nearest).setHighlighted(true);
                brush.setSelected(Brush.RIVERS, (short)nearest);
            }
            drawRivers();
            break;
        case Brush.NEW:
            info("New river");
            // Have not yet selected a river.
            String  name = "River "+(map.getRivers().size()+1);
            info("Creating new river ["+name+"] at "+x+","+y);
            int     id = map.addRiver(name, x, y);
            info("created");
            brush.setSelected(Brush.RIVERS, (short)id);
            brush.setMode(Brush.EDIT);
            map.unselectRivers();
            map.selectRiver(id);
            drawRivers();
            break;
        case Brush.EDIT:
            info("Edit river "+brush.getSelected());

            Path    river = map.getRiver(brush.getSelected());
            debug("Adding to river ["+river.getName()+"]");

            if (brush.getButton() == Brush.LEFT) {
                // Add to a current river.
                info("Adding new element to river");
                Path.Element    end = river.getEndPoint();
                Path.Element    start = river.getStartPoint();
                if (river.isAtEnd(brush.getX(), brush.getY())) {
                    debug("Next to end, so adding");
                    map.extendRiver(brush.getSelected(), brush.getX(), brush.getY());
                    drawRivers();
                } else if (river.isAtStart(brush.getX(), brush.getY())) {
                    debug("Next to start, so adding");
                    map.extendRiver(brush.getSelected(), brush.getX(), brush.getY());
                    drawRivers();
                }
            } else if (brush.getButton() == Brush.MIDDLE) {
                info("Middle button (move)");
                int     v = river.getNearestVertex(x, y, 50);
                river.setVertexPosition(v, x, y);
                drawRivers();
            } else if (brush.getButton() == Brush.RIGHT) {
                info("Right button");
            }
            break;
        default:
            info("applyBrushRiver: Unknown action");
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

    public void
    setBrushMode(int mode) {
        brush.setMode(mode);
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

            // Record actual X/Y coordinate of mouse click.
            brush.setX((e.getX()*100)/tileXSize);
            brush.setY((e.getY()*100)/tileYSize);
            brush.setButton(e.getButton());

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

    /**
     * Class to handle mouse motion events.
     */
    public class
    MouseMotionHandler extends MouseMotionAdapter {
        public void
        mouseDragged(MouseEvent e) {
            int     x,y;
            int     yp;

            if (brush.getType() == Brush.THINGS) {
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
                //map.rescaleMap(map.getScale()/2);
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
        terrainPane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/medium");
        terrainPane.setPalette(map.getTerrainSet().toArray(), false);
        terrainPane.makeFrame();
    }

    public void
    showThingPalette() {
        thingPane = new Pane(new ThingPalette(this), "Things");
        thingPane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/medium");
        thingPane.setPalette(map.getThingSet().toArray(), false);
        thingPane.makeFrame();
    }

    public void
    showFeaturePalette() {
        featurePane = new Pane(new FeaturePalette(this), "Hills");
        featurePane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/medium");

        featurePane.setPalette(map.getFeatureSet().toArray(), false);
        featurePane.makeFrame();
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


    public void
    fork() {
        map.fork();
    }

    public void
    merge(String mergePath) {
        try {
            Map     merge = new Map(mergePath);

            map.merge(merge);
        } catch (MapException e) {
            e.printStackTrace();
        }
    }

    public static void
    main(String args[]) {
        Options options = new Options(args);
        MapEditor   editor = null;
        Map         map = null;

    }

}
