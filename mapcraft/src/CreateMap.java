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


public class CreateMap extends JDialog implements ActionListener {
    private JFrame      window;
    private MapCraft    application;

    private GridBagLayout       gridbag;
    private GridBagConstraints  constraints;

    private JTextField          mapName;
    private JTextField          authorName;
    private JComboBox           mapType;
    private JTextField          mapScale;
    private JTextField          mapWidth;
    private JTextField          mapHeight;
    private JButton             okay, cancel;

    public void
    actionPerformed(ActionEvent event) {
        setVisible(false);
    }

    public
    CreateMap(MapCraft application) {
        super(application.getWindow(), "Create New Map", true);

        this.application = application;
        setupWindows();
    }

    private void
    add(Component cmp, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        gridbag.setConstraints(cmp, constraints);
        getContentPane().add(cmp);
    }

    private void
    setupWindows() {
        Toolkit     toolkit = Toolkit.getDefaultToolkit();
        Dimension   screen = toolkit.getScreenSize();
        int         w, h;

        w = (int)screen.getWidth()/4;
        h = (int)screen.getHeight()/4;

        setSize(new Dimension(w, h));
        setLocationRelativeTo(null); // Centre on screen.

        gridbag = new GridBagLayout();
        constraints = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;

        add(new JLabel("Map name"), 0, 0, 1, 1);
        add(mapName = new JTextField("New map", 24), 1, 0, 3, 1);

        add(new JLabel("Author name"), 0, 1, 1, 1);
        add(authorName = new JTextField("anonymous", 24), 1, 1, 3, 1);

        add(new JLabel("Map type"), 0, 2, 2, 1);
        add(mapType = new JComboBox(getTypes()), 1, 2, 3, 1);

        add(new JLabel("Scale"), 0, 3, 1, 1);
        add(mapScale = new JTextField("1", 5), 1, 3, 1, 1);

        add(new JLabel("Width"), 2, 3, 1, 1);
        add(mapWidth = new JTextField("100"), 3, 3, 1, 1);

        add(new JLabel("Height"), 2, 4, 1, 1);
        add(mapHeight = new JTextField("70"), 3, 4, 1, 1);

        add(okay = new JButton("Save..."), 2, 5, 1, 1);
        add(cancel = new JButton("Cancel"), 3, 5, 1, 1);

        okay.setActionCommand("createmap.save");
        okay.addActionListener(this);

        cancel.setActionCommand("createmap.cancel");
        cancel.addActionListener(this);

        setVisible(true);
    }

    private String[]
    getTypes() {
        String[]    types = { "Outdoor (hexagons)", "Indoor (square)" };

        return types;
    }


}
