/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
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

import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.Area;

/**
 * Displays a list of all the areas in the map's AreaSet. Allows the user
 * to edit, add and delete areas.
 * 
 * @author Samuel Penn
 */
public class AreaSetDialog extends JDialog {
    private JPanel              centrePane;
    private JPanel              rightPane;
    private JPanel              bottomPane;
    
    private JList               list;
    private JButton             add, modify, delete;
    private JButton             okay, cancel;
    
    private boolean             isOkay = false;
    private Map                 map;
    private JFrame              frame;
    
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
    
    /**
     * Create and setup widgets for the RHS of the dialog.
     * This includes the buttons to add, edit and delete areas from the list.
     */
    private void
    setupRightPane() {
        GridBagLayout       g = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        
        //rightPane.setLayout(new GridLayout(6, 1));
        
        rightPane.setLayout(g);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTH;
        
        add(rightPane, g, c, add = new JButton("New"), 0, 0, 1, 1);
        add(rightPane, g, c, modify = new JButton("Edit"), 0, 1, 1, 1);
        add(rightPane, g, c, delete = new JButton("Delete"), 0, 2, 1, 1);
        c.weighty = 1.0;
        add(rightPane, g, c, new JPanel(), 0, 3, 1, 1);

        add.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        add();
                                    }
                               });

        modify.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        modify();
                                    }
                               });

        delete.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        delete();
                                    }
                               });

    }

    private Object[]
    getListData(AreaSet set) {
        Area        a;
        ArrayList   array = new ArrayList();
        
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            Area    area = (Area) iter.next();
            String  name = area.getName();
            
            if (area.getParent() > 0) {
                String  parent = set.getArea(area.getParent()).getName();
                name += " ("+parent+")";
            }
            array.add(name);
        }
        
        return array.toArray();
    }
    /**
     * Setup and display the centre pane. This contains the list of areas
     * currently available.
     */    
    private void
    setupCentrePane() {
        GridBagLayout       g = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        
        centrePane.setLayout(g);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        
        AreaSet     set = map.getAreaSet();
        list = new JList(getListData(set));
        add(centrePane, g, c, new JScrollPane(list), 0, 0, 6, 6);
    }
    
    private void
    setupBottomPane() {
        System.out.println("setupBottomPane:");
        bottomPane.add(cancel = new JButton("Cancel"));
        bottomPane.add(okay = new JButton("Okay"));
        
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
    
    
    /**
     * Instantiate the dialog, and setup the widgets for it.
     * 
     * @param map
     * @param frame
     */
    public
    AreaSetDialog(Map map, JFrame frame) {
        super(frame, "Add/Edit areas", true);
        this.map = map;
        this.frame = frame;
        
        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(centrePane = new JPanel(), BorderLayout.CENTER);
        getContentPane().add(rightPane = new JPanel(), BorderLayout.EAST);
        getContentPane().add(bottomPane = new JPanel(), BorderLayout.SOUTH);
        
        setupCentrePane();
        setupRightPane();
        setupBottomPane();
        
        // Set size of dialog, and display.
        setSize(new Dimension(300, 350));
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Add a new area to the list of areas.
     */
    private void
    add() {
        AreaDialog      dialog;
        AreaSet         set = map.getAreaSet();
        
        Area            area = new Area(0, "Unnamed", "unnamed");
        
        dialog = new AreaDialog(area, set, frame);
        if (dialog.isOkay()) {
            area.setName(dialog.getName());
            area.setUri(dialog.getUri());
            
            String  parent = dialog.getParentName();
            if (parent.length() > 0) {
                int     id = set.getArea(parent).getId();
                area.setParent(id);
            }
            set.add(area);
            // Redisplay the new data.
            list.setListData(getListData(set));
        }
        
    }
    
    /**
     * Modify the selected area.
     */
    private void
    modify() {
        AreaDialog      dialog;
        AreaSet         set = map.getAreaSet();
        
        String          selected = (String)list.getSelectedValue();
        selected = selected.replaceAll(" \\(.*\\)", "");
        Area            area = set.getArea(selected);
        
        dialog = new AreaDialog(area, set, frame);
        if (dialog.isOkay()) {
            area.setName(dialog.getName());
            area.setUri(dialog.getUri());
            
            String  parent = dialog.getParentName();
            if (parent.length() > 0) {
                int     id = set.getArea(parent).getId();
                area.setParent(id);
            } else {
                area.setParent(0);
            }
            // Redisplay the new data.
            list.setListData(getListData(set));
        }
    }
    
    /**
     * Delete the currently selected area. Prompt the user for confirmation
     * first.
     */
    private void
    delete() {
        AreaSet         set = map.getAreaSet();
        
        String          selected = (String)list.getSelectedValue();
        selected = selected.replaceAll(" \\(.*\\)", "");
        Area            area = set.getArea(selected);
        String          message;

        message = "Do you really want to delete area '"+selected+"'";
        int option = JOptionPane.showConfirmDialog(null, message,
                "Are you sure?",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // Do this via the Map, since it also needs to clean up any
            // parts of the map set to this area.
            map.deleteArea((short)area.getId());
            list.setListData(getListData(set));
        }
    }
    
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

    public static void
    main(String[] args) throws Exception {
        new AreaSetDialog(null, null);
    }
}
