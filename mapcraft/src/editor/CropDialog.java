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

    private JComboBox           cropType;
    private Map                 map;


    public void
    itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == e.SELECTED) {
        }
    }



    private JComboBox
    createTypeCombo() {
        String[]    labels = { "Highlighted", "Area", "Thing" };
        JComboBox   box = new JComboBox(labels);
        box.setSelectedItem("Highlighted");

        return box;
    }

    private JComboBox
    createAreaCombo() {
        String[]    labels = null;
        JComboBox   box = null;
        AreaSet     areas = map.getAreaSet();

        box = new JComboBox(areas.toNameArray());

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
        super(frame, "Crop map", true);

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

        add(new Label("Margin"), 0, 1, 1, 1);
        add(new TextField(5), 1, 1, 2, 1);

        add(new Label("Crop type"), 0, 0, 1, 1);
        add(cropType = createTypeCombo(), 1, 0, 2, 1);

        add(new Label("Area"), 0, 2, 1, 1);
        add(createAreaCombo(), 1, 2, 2, 1);


        setSize(new Dimension(300, 400));
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

    public static void
    main(String args[]) {
    }
}
