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

package net.sourceforge.mapcraft.editor;

import net.sourceforge.mapcraft.editor.dialogs.AreaSetDialog;
import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.Map;
import net.sourceforge.mapcraft.map.elements.Area;
import net.sourceforge.mapcraft.map.elements.Path;
import net.sourceforge.mapcraft.map.elements.Terrain;
import net.sourceforge.mapcraft.map.elements.Thing;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import net.sourceforge.mapcraft.utils.Options;


public class MapEditor extends MapViewer
                       implements ActionListener {


    private JFrame      frame;
    private JMenuBar    menuBar;
    private ScrollPane  scrollpane;

    private Pane        terrainPane = null;
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

            Terrain ta[] = map.getTerrainSet().toArray();
            brush.setSelected(Brush.TERRAIN, ta[index].getId());

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

            Terrain ta[] = map.getThingSet().toArray();
            brush.setSelected(type, ta[index].getId());

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

            brush.setSelected(type, (short)index);

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
            Area a[] = map.getAreaSet().toArray();
            brush.setSelected(type, (short)a[index-1].getId());
            System.out.println("Area selected: "+brush.getSelected());

            brush.setType(type);
        }
    }


    public void
    applyBrush(int x, int y) {
        try {
            int     width = 0;
            
            switch (brush.getType()) {
            case Brush.TERRAIN:
                switch (brush.getSize()) {
                case Brush.MEDIUM:
                    // A medium sized brush is 3 tiles across.
                    width = 1;
                    break;
                case Brush.LARGE:
                    // A large sized brush is 7 tiles across.
                    width = 3;
                    break;
                default:
                    width = 0;
                    break;
                }
                for (int px = x - width; px <= x+width; px++) {
                    for (int py = y - width; py <= y+width; py++) {
                        currentSet.setTerrain(px, py, 
                                currentSet.getTerrainSet().getTerrain(brush.getSelected()));
                        currentSet.setTerrainRotation(px, py, brush.getRotation());
                        paintTile(px, py);
                    }
                }
                break;
            case Brush.FEATURES:
                currentSet.setFeature(x, y, 
                        currentSet.getFeatureSet().getTerrain(brush.getSelected()));
                currentSet.setFeatureRotation(x, y, brush.getRotation());
                paintTile(x, y);
                break;
            case Brush.THINGS:
                applyBrushThings();
                paintComponent();
                break;
            case Brush.AREAS:
                currentSet.setArea(x, y, currentSet.getAreaSet().getArea(brush.getSelected()));
                paintTile(x, y);
                break;
            case Brush.RIVERS:
                applyBrushPaths(brush.getX(), brush.getY(), Path.RIVER);
                break;
            case Brush.ROADS:
                applyBrushPaths(brush.getX(), brush.getY(), Path.ROAD);
                break;
            case Brush.HIGHLIGHT:
                if (brush.getMode() == Brush.INSERT) {
                    currentSet.setHighlighted(x, y, true);
                } else {
                    currentSet.setHighlighted(x, y, false);
                }
                paintTile(x, y);
                break;
            }
        } catch (MapException moobe) {
            warn("Out of bounds!");
        }

    }

    /**
     * Apply the actions of the current brush to the nearest Thing.
     */
    private void
    applyBrushThings() throws MapOutOfBoundsException {
        Thing       thing = null;
        ThingDialog dialog = null;

        switch (brush.getMode()) {
        case Brush.NEW:
            if (brush.getSelected() > 0) {
                // Only set Thing if no Thing currently present.
                thing = new Thing(brush.getSelected(), "Unnamed", "Unknown",
                                        brush.getX(), brush.getY());
                currentSet.addThing(thing);
                if (map.getType() == Map.WORLD) {
                    dialog = new ThingDialog(thing, frame, map.getThingSet(),
                                  views[ViewProperties.EDITICON].getPath());
                    dialog.getThing();
                } else {
                    thing.setImportance(Thing.HIGH);
                }
            }
            break;
        case Brush.DELETE:
            Thing     t = map.getNearestThing(brush.getX(),
                                    brush.getY(), 100);
            if (t != null) {
                currentSet.removeThing(t);
            }
            break;
        case Brush.SELECT:
            thing = map.getNearestThing(brush.getX(), brush.getY(), 100);
            if (thing != null) {
                thing.setPosition(brush.getX(), brush.getY());
            }
            break;
        case Brush.EDIT:
            thing = map.getNearestThing(brush.getX(), brush.getY(), 100);
            dialog = new ThingDialog(thing, frame, map.getThingSet(),
                                    views[ViewProperties.EDITICON].getPath());
            dialog.getThing();
            break;
        }
    }

    private void
    applyBrushPaths(int x, int y, short type) throws MapException {
        switch (brush.getMode()) {
        case Brush.SELECT:
            info("Selecting path");
            map.unselectPaths();
            Path    nearest = null;
            int     min = -1;
            Path[]  paths = currentSet.getPaths();
            for (int r = 0; r < paths.length; r++) {
                Path    path = paths[r];
                int     v = path.getNearestVertex(x, y, 250);
                if (v > -1) {
                    int     d = path.getDistanceToVertex(x, y, v);
                    if (min == -1 || d < min) {
                        min = d;
                        nearest = path;
                    }
                }
            }
            if (nearest != null) {
                debug("Nearest river is "+nearest);
                nearest.setHighlighted(true);
                //TODO: Fix this so apply paths works
                //brush.setSelected(Brush.RIVERS, nearest. );
            }
            drawPaths();
            break;
        case Brush.NEW:
            info("New path");
            // Have not yet selected a river.
            String  name = "Path "+(currentSet.getPaths().length+1);
            info("Creating new path ["+name+"] at "+x+","+y);
            Path    path = currentSet.addPath(name, type, Path.PLAIN,  x, y);
            info("created");
            brush.setSelected(brush.getType(), path);
            brush.setMode(Brush.EDIT);
            map.unselectPaths();
            map.selectPath(path);
            drawPaths();
            break;
        case Brush.EDIT:
            info("Edit river "+brush.getSelected());

            Path    river = brush.getSelectedPath();
            debug("Adding to river ["+river.getName()+"]");

            if (brush.getButton() == Brush.LEFT) {
                // Add to a current river.
                info("Adding new element to river");
                Path.Element    end = river.getEndPoint();
                Path.Element    start = river.getStartPoint();
                if (river.isAtEnd(brush.getX(), brush.getY())) {
                    debug("Next to end, so adding");
                    river.add(brush.getX(), brush.getY());
                    drawPaths();
                } else if (river.isAtStart(brush.getX(), brush.getY())) {
                    debug("Next to start, so adding");
                    river.add(brush.getX(), brush.getY());
                    drawPaths();
                }
            } else if (brush.getButton() == Brush.MIDDLE) {
                info("Middle button (move)");
                int     v = river.getNearestVertex(x, y, 50);
                river.setVertexPosition(v, x, y);
                drawPaths();
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
            
            if (map == null) {
                return;
            }

            requestFocus();
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
            
            if (map == null) {
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

        public void
        mouseMoved(MouseEvent e) {
            int     x, y, yp;
            brush.setLastMousePosition(e.getX(), e.getY());
            
            if (map == null) {
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
            try {
                String  area = currentSet.getArea(x, y).getName();
                String  terrain, feature;
                String  message = null;
                
                terrain = currentSet.getTerrain(x, y).getName();
                feature = currentSet.getFeature(x, y).getName();

                message = "("+x+","+y+") "+terrain+"/"+feature+" ["+area+"]";
                application.setMessage(message);
            } catch (MapOutOfBoundsException moobe) {
            }
        }
    }

    public class
    KeyEventHandler extends KeyAdapter {
        public void
        keyPressed(KeyEvent e) {
            int     key = e.getKeyCode();
            char    ch = e.getKeyChar();
            int     x, y, angle;
            Thing   thing = null;
            
            if (map == null) {
                return;
            }

            if (map.getTileShape() == Map.SQUARE) {
                x = brush.getLastMouseX()/tileXSize;
                y = brush.getLastMouseY()/tileYSize;
                angle = 90;
            } else {
                x = brush.getLastMouseX()/tileXSize;
                int yp = brush.getLastMouseY();

                if (x%2 != 0) {
                    yp -= tileYOffset;
                }
                y = yp/tileYSize;

                angle = 60;
            }
            if (brush.getType() == Brush.THINGS) {
                x = (brush.getLastMouseX()*100)/tileXSize;
                y = (brush.getLastMouseY()*100)/tileYSize;
                thing = map.getNearestThing(x, y, 100);
                angle = 30;
            }

            try {
                short   r = -1;

                switch (ch) {
                case '[':
                    if (brush.getType() == Brush.TERRAIN) {
                        r = (short) (currentSet.getTerrainRotation(x, y) - angle);
                        if (r < 0) r = (short) (360-angle);
                        currentSet.setTerrainRotation(x, y, r);
                        paintTile(x, y);
                    } else if (brush.getType() == Brush.FEATURES) {
                        r = (short) (currentSet.getFeatureRotation(x, y) - angle);
                        if (r < 0) r = (short) (360-angle);
                        currentSet.setFeatureRotation(x, y, r);
                        paintTile(x, y);
                    } else if (brush.getType() == Brush.THINGS && thing != null) {
                        r = (short) (thing.getRotation() - angle);
                        if (r < 0) r = (short) (360 - angle);
                        thing.setRotation(r);
                        paintComponent();
                    }
                    brush.setRotation(r);
                    break;
                case ']':
                    if (brush.getType() == Brush.TERRAIN) {
                        r = (short) (currentSet.getTerrainRotation(x, y) + angle);
                        if (r >= 360) r = (short) 0;
                        currentSet.setTerrainRotation(x, y, r);
                        paintTile(x, y);
                    } else if (brush.getType() == Brush.FEATURES) {
                        r = (short) (currentSet.getFeatureRotation(x, y) + angle);
                        if (r >= 360) r = (short) 0;
                        currentSet.setFeatureRotation(x, y, r);
                        paintTile(x, y);
                    } else if (brush.getType() == Brush.THINGS && thing != null) {
                        r = (short) (thing.getRotation() + angle);
                        if ( r >= 360) r = (short) 0;
                        thing.setRotation(r);
                        paintComponent();
                    }
                    brush.setRotation(r);
                    break;
                }
            } catch (MapOutOfBoundsException moobe) {
                error("keyPressed: Map out of bounds ("+x+","+y+")");
            }
        }

        public void
        keyTyped(KeyEvent e) {
        }

        public void
        keyReleased(KeyEvent e) {
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

    /**
     * Set the filename of the map to be the new file.
     * Should be used after a SaveAs operation.
     */
    public void
    setFilename(String filename) {
        map.setFilename(filename);
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
        addKeyListener(new KeyEventHandler());

    }

    public
    MapEditor(int width, int height, int scale) {
    }

    public void
    showTerrainPalette() {
        terrainPane = new Pane(new TerrainPalette(this), "Terrain");
        terrainPane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/96x96");
        terrainPane.setPalette(map.getTerrainSet().toArray(), false);
        terrainPane.makeFrame();
    }

    public void
    showThingPalette() {
        thingPane = new Pane(new ThingPalette(this), "Things");
        thingPane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/96x96");
        thingPane.setPalette(map.getThingSet().toArray(), false);
        thingPane.makeFrame();
    }

    public void
    showFeaturePalette() {
        featurePane = new Pane(new FeaturePalette(this), "Hills");
        featurePane.setImagePath(properties.getProperty("path.images")+
                "/"+map.getImageDir()+"/96x96");

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

    /**
     * Called before cropping a map. Enables a reference to
     * the original map to be kept for future merger.
     */
    public void
    fork() {
        map.fork();
    }

    public void
    merge() {
        try {
            MergeDialog dialog = new MergeDialog(map, frame);
            if (dialog.isOkay()) {
                map.merge(dialog.getMergeMap());
            }
        } catch (MapException e) {
            e.printStackTrace();
        }
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

    /**
     * Use the Crop dialog to crop the map. The map is forked, and cropped
     * according to the criteria chosen by the user. It is possible to crop
     * to the currently selected region, a named area, near to a thing, or
     * along the length of a road or river.
     */
    public void
    crop() {
        try {
            CropDialog  dialog = new CropDialog(map, frame);
            if (dialog.isOkay()) {
                fork();
                if (dialog.isHighlight()) {
                    map.cropToHighlighted();
                } else if (dialog.isArea()) {
                    short    margin = dialog.getMargin();
                    Area     area = currentSet.getAreaSet().getArea(dialog.getSelection());
                    map.cropToArea(area, margin);
                } else if (dialog.isThing()) {
                    short   radius = dialog.getRadius();
                    String  name = dialog.getSelection();

                    map.cropToThing(name, radius);
                } else if (dialog.isRiver() || dialog.isRoad()) {
                    short   margin = dialog.getMargin();
                    String  name = dialog.getSelection();

                    map.cropToPath(name, margin);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resize the map, changing its width and height. The scale of the map
     * is preserved, and blank rows and columns are inserted at the edges
     * of the map. No other changes are performed.
     */
    public void
    resize() {
        try {
            ResizeDialog    dialog = new ResizeDialog(currentSet.getMapWidth(),
                                            currentSet.getMapHeight(), frame);

            if (dialog.isOkay()) {
                int     width = dialog.getNewWidth();
                int     height = dialog.getNewHeight();
                boolean left = dialog.isLeftInsert();
                boolean top = dialog.isTopInsert();

                map.resize(width, height, left, top);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void
    rescale() {
        try {
            RescaleDialog   dialog = new RescaleDialog(currentSet.getScale(),
                                    currentSet.getMapWidth(), 
                                    currentSet.getMapHeight(), frame);

            if (dialog.isOkay()) {
                map.rescale(dialog.getNewScale());
                setView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void
    editAreas() {
        try {
            AreaSetDialog   dialog = new AreaSetDialog(map, frame);
        } catch (Exception e) {
        }
    }

    public static void
    main(String args[]) {
        Options options = new Options(args);
        MapEditor   editor = null;
        Map         map = null;

    }

}
