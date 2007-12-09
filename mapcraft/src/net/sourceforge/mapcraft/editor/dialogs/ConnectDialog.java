/*
 * Copyright (C) 2005 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.editor.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.sourceforge.mapcraft.map.tilesets.database.MapEntry;

/**
 * Display a dialog listing all the available maps that the user can load,
 * and also give an option to create a new map. Needs to get list of current
 * maps from the database, and a list of possible terrain sets for new maps
 * from a resource URL.
 * 
 * @author Samuel Penn
 */
public class ConnectDialog extends JDialog {
    Entry[]      list;

    private JPanel          topPane;
    private JPanel          bottomPane;
    
    private boolean         isOkay = false;
    private JButton         okay, cancel;
    private JTextField      name, uri;
    private JComboBox       parent;
    private String[]        terrainList = null;
     
    private class Entry {
        String      name;
        String      shape;
        String      template;
        String      description;
        int         width;
        int         height;
        int         scale;
        boolean     editable;
        
    	Entry(MapEntry map, boolean editable) {
    		this.name = map.getName();
            this.width = map.getWidth();
            this.height = map.getHeight();
            this.description = map.getDescription();
            this.scale = map.getScale();
            this.template = map.getTemplate();
            this.shape = map.getShape();
            this.editable = editable;
        }
        
        Entry(String name, boolean editable) {
        	this.name = name;
            this.width = 600;
            this.height = 400;
            this.scale = 1;
            this.description = "New map";
            this.template = "World";
            this.shape = "Hexagonal";
            this.editable = editable;
        }
    }

    /**
     * Helper method to add a component to a GridBagLayout.
     * 
     * @param con       Container to add to.
     * @param g         GridBagLayout of container.
     * @param c         Constraints of the GridBagLayout.
     * @param cmp       Component to be added.
     * @param x         X coordinate to position component.
     * @param y         Y coordinate to position component.
     * @param w         Width of component.
     * @param h         Height of component.
     */
    private void
    add(Container con, GridBagLayout g, GridBagConstraints c, Component cmp,
        int x, int y, int w, int h) {
            
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.gridheight = h;
        g.setConstraints(cmp, c);
        con.add(cmp);
    }

    private void
    setupTopPane() {
        GridBagLayout       g = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        
        //rightPane.setLayout(new GridLayout(6, 1));
        
        topPane.setLayout(g);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTH;


        add(topPane, g, c, new JLabel("Select map"), 0, 0, 1, 1);
        add(topPane, g, c, new JLabel("Terrain"), 0, 1, 1, 1);
        add(topPane, g, c, new JLabel("Shape"), 0, 2, 1, 1);
        
        add(topPane, g, c, name = new JTextField(list[0].name), 1, 0, 2, 1);
        add(topPane, g, c, uri = new JTextField(list[0].shape), 1, 2, 2, 1);
        
        // Set up a combo box displaying all the possible maps.
        // The list must include a 'null' item, and must not contain
        // this area's name.
        ArrayList   nameList = new ArrayList();
        nameList.add("");
        for (int i=0; i < list.length; i++) {
            nameList.add(list[i].name);
        }
        int     selection = 0;
        
        parent = new JComboBox(nameList.toArray());
        parent.setSelectedIndex(selection);
        add(topPane, g, c, parent, 1, 1, 2, 1);

    }
    
    private void
    setupBottomPane() {
        bottomPane.add(okay = new JButton("Apply"));
        bottomPane.add(cancel = new JButton("Cancel"));
        
        okay.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        okay();
                                    }
                               });

        cancel.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        isOkay = false;
                                        setVisible(false);
                                    }
                                 });
    }
    
    
    
	public ConnectDialog(MapEntry[] maps, String[] terrain, JFrame frame) {
        super(frame, "Database Connection", true);
        
        terrainList = terrain;
        list = new Entry[maps.length + 1];
        list[0] = new Entry("<create new map>", true);
        for (int i=0; i < maps.length; i++) {
        	list[i+1] = new Entry(maps[i], false);
        }
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPane = new JPanel(), BorderLayout.NORTH);
        getContentPane().add(bottomPane = new JPanel(), BorderLayout.SOUTH);
        
        setupTopPane();
        setupBottomPane();
        
        // Set size of dialog, and display.
        setSize(new Dimension(300, 150));
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    /**
     * User clicked 'Okay', hide dialog and return to caller.
     */
    public void
    okay() {
        isOkay = true;
        setVisible(false);
    }

    /**
     * Return true if user clicked 'Okay', false otherwise.
     */
    public boolean
    isOkay() {
        return isOkay;
    }
    
}
