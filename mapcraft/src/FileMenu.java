
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

import javax.swing.*;
import java.awt.event.*;
import java.net.*;

import net.sourceforge.mapcraft.map.Map;
import net.sourceforge.mapcraft.utils.MapFileFilter;

public class FileMenu extends JMenu implements ActionListener {
    Actions     actions = new Actions(null);
    MapCraft    application = null;

    public
    FileMenu(MapCraft application) {
        super("File");

        JMenuItem       item;

        this.application = application;

        addItem(Actions.FILE_NEW, "New...", KeyEvent.VK_N);
        addItem(Actions.FILE_OPEN, "Open...", KeyEvent.VK_L);
        addItem(Actions.FILE_SAVE, "Save", KeyEvent.VK_S);
        addItem(Actions.FILE_SAVEAS, "Save As...", KeyEvent.VK_A);
        addSeparator();
        addItem(Actions.FILE_EXIT, "Exit", KeyEvent.VK_X);
    }


    private void
    addItem(String name, String label, int key) {
        JMenuItem       item = new JMenuItem();
        item.setAction(actions.get(name, this));
        add(item);
    }

    public void
    actionPerformed(ActionEvent e) {
        String  cmd = e.getActionCommand();

        System.out.println("FILEMENU ["+cmd+"]");

        if (cmd.equals(Actions.FILE_NEW)) {
            create();
        } else if (cmd.equals(Actions.FILE_OPEN)) {
            open();
        } else if (cmd.equals(Actions.FILE_SAVE)) {
            application.save();
        } else if (cmd.equals(Actions.FILE_SAVEAS)) {
            saveas();
        }
    }

    /**
     * Opens an existing map file.
     */
    private void
    open() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new MapFileFilter());
        if (MapFileFilter.getLastLocation() != null) {
	        chooser.setCurrentDirectory(MapFileFilter.getLastLocation());
        }

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getAbsolutePath();

            System.out.println("Opening file ["+filename+"]");
            MapFileFilter.setLastLocation(chooser.getSelectedFile());
            application.load(filename);
        }
    }

    /**
     * Save map in a new place.
     */
    private void
    saveas() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new MapFileFilter());

        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getAbsolutePath();

            if (!filename.endsWith(".map")) {
                filename = filename + ".map";
            }

            System.out.println("Save file as ["+filename+"]");
            application.save(filename);
        }
    }

    /**
     * Create a new map.
     */
    private void
    create() {
        CreateMap   create = new CreateMap(application);
        System.out.println("Done");

        int         width = create.getMapWidth();
        int         height = create.getMapHeight();
        int         scale = create.getScale();

        String      name = create.getMapName();
        String      author = create.getAuthorName();
        String      terrain = create.getMapType();

        try {
            System.out.println("Creating new map of "+width+"x"+height);
            Map         newMap = new Map(name, width, height, scale);

            URL     url = FileMenu.class.getResource("/"+terrain);
            newMap.loadTerrainSet(url);
            newMap.save(name+".map");
            newMap = null;
            application.load(name+".map");
        } catch (Exception e) {
            e.printStackTrace();
        }
   }

}
