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

package uk.co.demon.bifrost.rpg.mapcraft;

import uk.co.demon.bifrost.rpg.mapcraft.editor.*;
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


public class MapCraft implements ActionListener {
    private JFrame      window;
    private MapEditor   editor;
    private JScrollPane scrollpane;

    // Tools and stuff.
    private Actions     actions = new Actions(this);
    private JToolBar    toolbar;
    private JMenuBar    menubar;

    // Global vars
    private Properties  properties;


    protected void
    shutdown() {
        System.exit(0);
    }


    public
    MapCraft(Properties properties, String map) {
        this.properties = properties;

        setupWindows();
        load(map);
    }

    public
    MapCraft(Properties properties) {
        this.properties = properties;

        setupWindows();
    }


    private void
    setupWindows() {
        Toolkit     toolkit = Toolkit.getDefaultToolkit();
        Dimension   screen = toolkit.getScreenSize();
        int         width, height;

        window = new JFrame("Map Craft");

        width = (int)screen.getWidth()/2;
        height = (int)screen.getHeight()/2;

        window.setSize(new Dimension(width, height));
        window.setLocationRelativeTo(null); // Centre on screen.

        scrollpane = new JScrollPane(editor,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Make sure closing the window will close the application.
        window.addWindowListener(new WindowAdapter() {
                public void
                windowClosing(WindowEvent e) {
                    shutdown();
                }
            });
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(createToolbar(), BorderLayout.NORTH);

        menubar = new JMenuBar();
        menubar.add(new FileMenu(this));
        menubar.add(new ViewMenu(this));
        menubar.setVisible(true);

        window.setJMenuBar(menubar);
        window.setVisible(true);
    }
    
    public MapEditor
    getEditor() {
        return editor;
    }


    /**
     * Load a map into the application and display it.
     *
     * @param map   Filename of map to load and display.
     */
    public void
    load(String map) {
        editor = new MapEditor(properties, map);

        scrollpane = new JScrollPane(editor,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        window.getContentPane().add(scrollpane, BorderLayout.CENTER);
        scrollpane.setVisible(true);
    }
    
    /**
     * Save the currently displayed map back to its original filename.
     */
    public void
    save() {
        try {
            editor.save();
        } catch (IOException ioe) {
            new JOptionPane("Cannot save file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save the currently displau map to the specified file. This is the
     * equivalent of a 'Save as' operation.
     */
    public void
    save(String filename) {
        try {
            editor.save(filename);
        } catch (IOException ioe) {
            new JOptionPane("Cannot save file", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton
    createToolbarButton(String action) {
        JButton         button = new JButton();

        button.setAction(actions.get(action));
        button.setText("");

        return button;
    }

    /**
     * Create the toolbar ready for it to be added to our frame.
     */
    private JToolBar
    createToolbar() {
        JButton     button;

        this.toolbar = new JToolBar();

        toolbar.add(createToolbarButton(Actions.FILE_NEW));
        toolbar.add(createToolbarButton(Actions.FILE_OPEN));
        toolbar.add(createToolbarButton(Actions.FILE_SAVE));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton(Actions.VIEW_ZOOMIN));
        toolbar.add(createToolbarButton(Actions.VIEW_ZOOMOUT));
        toolbar.add(createToolbarButton(Actions.VIEW_GRID));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton(Actions.EDIT_TERRAIN));
        toolbar.add(createToolbarButton(Actions.EDIT_FEATURES));
        toolbar.add(createToolbarButton(Actions.EDIT_RIVERS));

        return this.toolbar;
    }

    
    public void
    actionPerformed(ActionEvent e) {
        System.out.println("ACTION ["+e.getActionCommand()+"]");
    }

    public static void
    main(String args[]) {
        MapCraft        map = null;
        Options         options = new Options(args);
        String          mapfile = null;
        Properties      properties = new Properties();


        if (options.isOption("-rundir")) {
            properties.setProperty("path.run", options.getString("-rundir"));
            properties.setProperty("path.images", options.getString("-rundir")+"/images");
        } else {
            properties.setProperty("path.run", System.getProperty("user.dir"));
            properties.setProperty("path.images", System.getProperty("user.dir")+"/images");
        }
        
        if (options.isOption("-create")) {
            String      name = options.getString("-create");
            int         width = options.getInt("-width");
            int         height = options.getInt("-height");
            int         scale = options.getInt("-scale");
            String      terrain = options.getString("-terrain");
            String      imagedir = options.getString("-images");
            boolean     square = options.isOption("-square");
            boolean     local = options.isOption("-local");

            try {
                Map newmap = new Map(name, width, height, scale);

                if (square) {
                    newmap.setTileShape(Map.SQUARE);
                }
                if (local) {
                    newmap.setType(Map.LOCAL);
                }
                if (imagedir != null) {
                    newmap.setImageDir(imagedir);
                } else {
                    newmap.setImageDir(".");
                }
                newmap.loadTerrainSet(terrain);
                newmap.save(name+".map");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }


        
        if (options.isOption("-load")) {
            mapfile = options.getString("-load");
        }

        if (options.isOption("-imagedir")) {
            properties.setProperty("path.images", options.getString("-imagedir"));
        }
        
        if (mapfile == null) {
            map = new MapCraft(properties);
        } else {
            map = new MapCraft(properties, mapfile);
        }


    }
}