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
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.*;

/**
 * Implements a GUI dialog for controlling cropping of the map.
 */
public class CropDialog extends JDialog implements ItemListener {
    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    private JComboBox           cropBox, areaBox, thingBox, riverBox, roadBox;
    private JTextField          margin, radius;
    private JButton             okay, cancel;

    private Map                 map;

    private boolean             doCrop = false;

    private final static String HIGHLIGHT = "Highlighted";
    private final static String AREA = "Named area";
    private final static String THING = "Thing";
    private final static String RIVER = "River";
    private final static String ROAD = "Road";

    public void
    itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == e.SELECTED) {
        }
    }

    private void
    deselectAll() {
        areaBox.setEnabled(false);
        thingBox.setEnabled(false);
        riverBox.setEnabled(false);
        roadBox.setEnabled(false);
        margin.setEnabled(false);
        radius.setEnabled(false);
    }

    private void
    selectedType() {
        String  selection = (String)cropBox.getSelectedItem();

        deselectAll();
        if (selection.equals(HIGHLIGHT)) {
        } else if (selection.equals(AREA)) {
            areaBox.setEnabled(true);
            margin.setEnabled(true);
        } else if (selection.equals(THING)) {
            thingBox.setEnabled(true);
            radius.setEnabled(true);
        } else if (selection.equals(RIVER)) {
            riverBox.setEnabled(true);
            margin.setEnabled(true);
        } else if (selection.equals(ROAD)) {
            roadBox.setEnabled(true);
            margin.setEnabled(true);
        }
    }

    /**
     * Create a JComboBox listing all the possible selection targets.
     */
    private JComboBox
    createTypeCombo() {
        String[]    labels = { HIGHLIGHT, AREA, THING, RIVER, ROAD };
        JComboBox   box = new JComboBox(labels);
        box.setSelectedItem(HIGHLIGHT);
        box.addItemListener(new ItemListener() {
                                public void
                                itemStateChanged(ItemEvent e) {
                                    if (e.getStateChange() == ItemEvent.SELECTED) {
                                        selectedType();
                                    }
                                }
                            });

        return box;
    }

    /**
     * Create a JComboBox listing all the named areas in the map.
     */
    private JComboBox
    createAreaCombo() {
        JComboBox   box = null;
        AreaSet     areas = map.getAreaSet();
        String[]    labels = areas.toNameArray();

        if (labels != null) {
            box = new JComboBox(labels);
        } else {
            box = new JComboBox();
            box.setEnabled(false);
            cropBox.removeItem(AREA);
        }

        return box;
    }

    /**
     * Create a JComboBox listing all the things in the map.
     */
    private JComboBox
    createThingCombo() {
        JComboBox   box = null;
        String[]    labels = map.getThingNames();

        if (labels != null) {
            box = new JComboBox(labels);
        } else {
            box = new JComboBox();
            box.setEnabled(false);
            cropBox.removeItem(THING);
        }

        return box;
    }

    private JComboBox
    createRiverCombo() {
        JComboBox   box = null;
        String[]    labels = map.getRiverNames();

        if (labels != null) {
            box = new JComboBox(labels);
        } else {
            box = new JComboBox();
            box.setEnabled(false);
            cropBox.removeItem(RIVER);
        }

        return box;
    }

    private JComboBox
    createRoadCombo() {
        JComboBox   box = null;
        String[]    labels = map.getRoadNames();

        if (labels != null) {
            box = new JComboBox(labels);
        } else {
            box = new JComboBox();
            box.setEnabled(false);
            cropBox.removeItem(ROAD);
        }

        return box;
    }

    /**
     * Create a modal thing editor dialog. When constructor returns, all
     * the data has been set up for the thing.
     *
     * @param thing     Thing to be edited.
     * @param frame     Frame thing is attached to.
     * @param icons     Terrain set for thing icons.
     * @param basePath  Base path to thing icons to use in dialog.
     */
    public
    CropDialog(Map map, JFrame frame) {
        super(frame, "Select region", true);

        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }
        this.map = map;

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        JLabel              label = null;
        ImageIcon           icon = null;

        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        int     y = 0;
        add(new Label("Crop type"), 0, y, 1, 1);
        add(cropBox = createTypeCombo(), 1, y++, 2, 1);

        add(new Label("Area"), 0, y, 1, 1);
        add(areaBox = createAreaCombo(), 1, y++, 2, 1);

        add(new Label("Thing"), 0, y, 1, 1);
        add(thingBox = createThingCombo(), 1, y++, 2, 1);

        add(new Label("River"), 0, y, 1, 1);
        add(riverBox = createRiverCombo(), 1, y++, 2, 1);

        add(new Label("Road"), 0, y, 1, 1);
        add(roadBox = createRoadCombo(), 1, y++, 2, 1);

        add(new Label("Margin"), 0, y, 1, 1);
        add(margin = new JTextField("1", 5), 1, y++, 1, 1);

        add(new Label("Radius"), 0, y, 1, 1);
        add(radius = new JTextField("5", 5), 1, y++, 1, 1);

        add(okay = new JButton("Crop"), 1, y, 1, 1);
        add(cancel = new JButton("Cancel"), 2, y, 1, 1);

        okay.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        crop();
                                    }
                               });

        cancel.addActionListener(new ActionListener() {
                                    public void
                                    actionPerformed(ActionEvent e) {
                                        doCrop = false;
                                        setVisible(false);
                                    }
                                 });


        selectedType();

        setSize(new Dimension(300, 250));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void
    add(Component cmp, int x, int y, int w, int h) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.gridheight = h;
        gridbag.setConstraints(cmp, c);
        getContentPane().add(cmp);
    }

    public void
    crop() {
        doCrop = true;
        setVisible(false);
    }

    /**
     * Return true if user clicked 'Okay', false otherwise.
     */
    public boolean
    isOkay() {
        return doCrop;
    }

    /**
     * Does the user want to crop to the highlighted region?
     */
    public boolean
    isHighlight() {
        String  selection = (String)cropBox.getSelectedItem();

        return selection.equals(HIGHLIGHT);
    }

    /**
     * Does the user want to crop to the named area?
     */
    public boolean
    isArea() {
        String  selection = (String)cropBox.getSelectedItem();

        return selection.equals(AREA);
    }

    /**
     * Does the user want to crop to a thing?
     */
    public boolean
    isThing() {
        String  selection = (String)cropBox.getSelectedItem();

        return selection.equals(THING);
    }

    /**
     * Does the user want to crop to a river?
     */
    public boolean
    isRiver() {
        String  selection = (String)cropBox.getSelectedItem();

        return selection.equals(RIVER);
    }

    /**
     * Does the user want to crop to a road?
     */
    public boolean
    isRoad() {
        String  selection = (String)cropBox.getSelectedItem();

        return selection.equals(ROAD);
    }

    /**
     * Get the name of the region to crop to. This may be a named area,
     * a thing, river or road, depending on what type the user has selected.
     * Use the various isRoad() methods etc to find out what.
     *
     * For highlighted regions, null is returned.
     */
    public String
    getSelection() {
        String      name = null;

        if (isThing()) {
            name = (String)thingBox.getSelectedItem();
        } else if (isArea()) {
            name = (String)areaBox.getSelectedItem();
        } else if (isRiver()) {
            name = (String)riverBox.getSelectedItem();
        } else if (isRoad()) {
            name = (String)roadBox.getSelectedItem();
        }

        return name;
    }

    public short
    getMargin() {
        String  value = (String)margin.getText();
        short   i = 0;

        try {
            i = Short.parseShort(value);
        } catch (Exception e) {
        }
        return i;
    }

    public short
    getRadius() {
        String  value = (String)radius.getText();
        short   i = 0;

        try {
            i = Short.parseShort(value);
        } catch (Exception e) {
        }
        return i;
    }

    public static void
    main(String args[]) {
    }
}
