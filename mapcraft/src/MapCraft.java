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

package net.sourceforge.mapcraft;

import net.sourceforge.mapcraft.editor.*;
import net.sourceforge.mapcraft.map.Map;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import net.sourceforge.mapcraft.utils.Options;
import net.sourceforge.mapcraft.xml.SanityCheck;

/**
 * The top level object for the application. This handles command line
 * arguments when the application is started, opens necessary windows and
 * loads any requested maps.
 *   
 * @author Samuel Penn.
 */
public class MapCraft implements ActionListener {
	// Location of global properties file.
	private static String	PROPERTY_FILE = ".mapcraftrc";
	
    private JFrame      window;
    private MapEditor   editor;
    private JScrollPane scrollpane;
    private JLabel      statusBar;

    // Tools and stuff.
    private Actions     actions = new Actions(this);
    private JToolBar    toolbar;
    private JMenuBar    menubar;

    // Global vars
    private Properties  properties;

	/**
	 * Get a reference to the file which holds the application properties.
	 * This is normally in the user's home directory, and stores various
	 * preferences for the user.
	 * 
	 * @return	Reference to the old properties file, or a new empty file
	 * 			if one didn't exist.
	 */
	private static File
	getPropertyFile() {
		String 	path = System.getProperty("user.home") + "/"+PROPERTY_FILE;
		File	file = new File(path);
		
		if (!file.exists()) {
			try {
				file.createNewFile();
				file = new File(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return file;
	}
	
	/**
	 * Load the application properties file.
	 * 
	 * @return	Properties for the application.
	 */
	public static Properties
	loadProperties() {
		File		file = getPropertyFile();
		Properties 	properties = new Properties();
		
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return properties;
	}

	/**
	 * Store the application properties in a file. The application's current
	 * property settings are saved in a file in the user's home directory.
	 * 
	 * @param properties
	 */
	public static void
	storeProperties(Properties properties) {
		File		file = getPropertyFile();
		
		try {
			properties.store(new FileOutputStream(file), "Mapcraft properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the named application property. The property is loaded from the
	 * user's application property file. The file is loaded each time, so
	 * obtaining such a property is not guaranteed to be efficient.
	 * 
	 * @param property		Name of property to fetch.
	 * @param defaultValue	Default value of property.
	 * 
	 * @return				Value of the named property, or the default if
	 * 						no such property was located.
	 */
	public static String
	getProperty(String property, String defaultValue) {
		Properties	properties = loadProperties();
		
		return properties.getProperty(property, defaultValue);
	}
	
	/**
	 * Set an application property. The property value will be stored along
	 * with all the other properties whenever one is set. The properties file
	 * is first loaded, the property set, then all properties are saved back
	 * to the file.
	 * 
	 * @param property		Name of property to set.
	 * @param value			The value to set the property to.
	 */
	public static void
	setProperty(String property, String value) {
		Properties	properties = loadProperties();
		properties.setProperty(property, value);
		
		storeProperties(properties);
	}


    protected void
    shutdown() {
        System.exit(0);
    }
    
    private void
    error(String message) {
        System.err.println("*** "+message);
    }
    
    private void
    check() {
        SanityCheck     sc = new SanityCheck();
        if (!sc.isSane()) {
            error("Cannot parse XML documents.");
            error("It is most likely that xalan.jar and xercesImpl.jar "+
                  "are missing from your CLASSPATH.");
            error("Have you set XALAN_HOME in the start script?");
            System.exit(1);
        }
    }


    public
    MapCraft(Properties properties, String map) {
        check();
        this.properties = properties;

        setupWindows();
        load(map);
    }

    public
    MapCraft(Properties properties) {
        check();
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
        
        window.getContentPane().add(statusBar = new JLabel("No map"),
                                    BorderLayout.SOUTH);
        
        window.getContentPane().add(createToolbar(), BorderLayout.NORTH);

        menubar = new JMenuBar();
        menubar.add(new FileMenu(this));
        menubar.add(new EditMenu(this));
        menubar.add(new ViewMenu(this));
        menubar.add(new ToolMenu(this));
        menubar.setVisible(true);

        window.setJMenuBar(menubar);
        window.setVisible(true);
    }

    MapEditor
    getEditor() {
        return editor;
    }

    JFrame
    getWindow() {
        return window;
    }

    /**
     * Load a map into the application and display it.
     *
     * @param map   Filename of map to load and display.
     */
    public void
    load(String map) {
        editor = new MapEditor(properties, map);
        editor.setApplication(this);
        scrollpane = new JScrollPane(editor,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        window.getContentPane().add(scrollpane, BorderLayout.CENTER);
        scrollpane.setVisible(true);
        //scrollpane.addKeyListener(editor);
        editor.setVisible(true);
        
        // Components refuse to draw themselves until the scrollpane is
        // resized, so force a resize. There has to be a better way of
        // doing this. repaint() etc doesn't seem to work.
        Dimension dim = window.getSize();
        window.setSize(new Dimension((int)dim.getWidth()+1, (int)dim.getHeight()+1));

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
            editor.setFilename(filename);
            editor.save(filename);
        } catch (IOException ioe) {
            new JOptionPane("Cannot save file", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void
    setMessage(String message) {
        statusBar.setText(message);
    }


    public void
    create() {
        try {
            CreateMap   create = new CreateMap(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void
    crop(int x, int y, int w, int h) {
        editor.crop(x, y, w, h);
    }

    public void
    cropToArea(String area, int margin) {
        editor.cropToArea(area, margin);
    }

    public void
    rescale(int scale) {
        editor.rescale(scale);
    }

    public void
    merge(String map) {
        editor.merge(map);
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
        toolbar.add(createToolbarButton(Actions.EDIT_THINGS));
        toolbar.add(createToolbarButton(Actions.EDIT_FEATURES));
        toolbar.add(createToolbarButton(Actions.EDIT_RIVERS));
        toolbar.add(createToolbarButton(Actions.EDIT_ROADS));
        toolbar.add(createToolbarButton(Actions.EDIT_HIGHLIGHT));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton(Actions.EDIT_SMALL));
        toolbar.add(createToolbarButton(Actions.EDIT_MEDIUM));
        toolbar.add(createToolbarButton(Actions.EDIT_LARGE));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton(Actions.EDIT_SELECT));
        toolbar.add(createToolbarButton(Actions.EDIT_NEW));
        toolbar.add(createToolbarButton(Actions.EDIT_EDIT));
        toolbar.add(createToolbarButton(Actions.EDIT_INSERT));
        toolbar.add(createToolbarButton(Actions.EDIT_DELETE));


        return this.toolbar;
    }

    public void
    actionCreate(ActionEvent e) {
    }


    public void
    actionPerformed(ActionEvent e) {
        String      cmd = e.getActionCommand();
        System.out.println("ACTION ["+cmd+"]");

        if (cmd.equals("createmap.save")) {
        } else if (cmd.equals("createmap.cancel")) {
        }
    }
    
    private static void
    print(String msg) {
        System.out.println(msg);
    }
    
    /**
     * Print out a message detailing command line usage.
     */
    private static void
    usage() {
        print("Mapcraft 0.3pre");
        print("Usage:");
        print("mapcraft -help");
        print("mapcraft -load <filename>");
        print("mapcraft -create <name> -width <w> -height <h> -scale <s>");
        print("         -terrain <terrain_file> [-square] [-local]"); 
    }

    public static void
    main(String args[]) {
        MapCraft        map = null;
        Options         options = new Options(args);
        String          mapfile = null;
        Properties      properties = new Properties();
        
        if (options.isOption("-help")) {
            usage();
            System.exit(0);
        }

        if (options.isOption("-map")) {
            Map.main(args);
            System.exit(0);
        }

        if (options.isOption("-rundir")) {
            properties.setProperty("path.run", options.getString("-rundir"));
            properties.setProperty("path.images",
                                 options.getString("-rundir")+"/images");
        } else {
            properties.setProperty("path.run", System.getProperty("user.dir"));
            properties.setProperty("path.images", "");
        }

        if (options.isOption("-create")) {
            String      name = options.getString("-create");
            int         width = options.getInt("-width");
            int         height = options.getInt("-height");
            int         scale = options.getInt("-scale");
            String      terrain = options.getString("-terrain");
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
                newmap.loadTerrainSet(terrain);
                newmap.save(name+".map");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        if (options.isOption("-image")) {
            MapImage.main(args);
            System.exit(0);
        }

        if (options.isOption("-load")) {
            mapfile = options.getString("-load");
        }

        if (options.isOption("-imagedir")) {
            properties.setProperty("path.images",
                        options.getString("-imagedir"));
        }

        boolean     crop = false;
        boolean     area = false;
        boolean     rescale = false;
        boolean     fork = false;

        int         x=0, y=0, w=0, h=0;
        String      areaName = null;
        int         margin = 0;
        int         newScale = 1;

        String      mergeMap = null;

        if (options.isOption("-crop")) {
            x = options.getInt("-x");
            y = options.getInt("-y");
            w = options.getInt("-w");
            h = options.getInt("-h");

            fork = crop = true;
        }

        if (options.isOption("-area")) {
            areaName = options.getString("-area");
            if (options.isOption("-margin")) {
                margin = options.getInt("-margin");
            }

            fork = area = true;
        }

        if (options.isOption("-rescale")) {
            newScale = options.getInt("-rescale");
            fork = rescale = true;
        }

        if (options.isOption("-merge")) {
            mergeMap = options.getString("-merge");
        }

        if (mapfile == null) {
            map = new MapCraft(properties);
        } else {
            map = new MapCraft(properties, mapfile);
            if (fork) {
                map.getEditor().fork();
            }
            if (crop) {
                map.crop(x, y, w, h);
            }
            if (area) {
                map.cropToArea(areaName, margin);
            }
            if (rescale) {
                map.rescale(newScale);
            }
            if (mergeMap != null) {
                map.merge(mergeMap);
            }
        }
    }
}
