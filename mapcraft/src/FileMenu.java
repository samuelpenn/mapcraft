
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.filechooser.FileFilter;

public class FileMenu extends JMenu implements ActionListener {
    Actions     actions = new Actions(null);
    MapCraft    application = null;
    

    private class MapFileFilter extends FileFilter {
        private String MAP_EXTENSION = ".map";
        
        public
        MapFileFilter() {
        }
        
        public String
        getDescription() {
            return "Mapcraft map files";
        }

        public String
        getExtension(File f) {
            if(f != null) {
                String filename = f.getName();
                int i = filename.lastIndexOf('.');
                if(i>0 && i<filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                };
            }
            return null;
        }

        public boolean
        accept(File f) {
            if(f != null) {
                if(f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if(extension != null && extension.equals("map")) {
                    return true;
                }
            }
            return false;
        }
    }

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

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getName();

            System.out.println("Opening file ["+filename+"]");
            application.load(filename);
        }
    }

    private void
    saveas() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new MapFileFilter());

        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getName();

            System.out.println("Save file as ["+filename+"]");
            application.save(filename);
        }
    }

}
