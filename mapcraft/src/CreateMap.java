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
import java.awt.event.*;
import java.awt.*;
import java.util.*;

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
    private Hashtable           types;

    /**
     * Holds information about a map type. We present a drop down list
     * of types to the user, then fetch the resource from the jar when
     * the map is created.
     */
    private class MapType {
        String        name;
        String        resource;
        String        tileShape;

        MapType(String name, String resource, String tileShape) {
            this.name = name;
            this.resource = resource;
            this.tileShape = tileShape;
        }
    }

    public void
    actionPerformed(ActionEvent event) {
        setVisible(false);
    }

    public
    CreateMap(MapCraft application) {
        super(application.getWindow(), "Create New Map", true);

        this.application = application;

        types = new Hashtable();
        types.put("Wilderness (Hexagonal)",
                  new MapType("Wilderness (Hexagonal)", "hexagonal.xml",
                              "hexagonal"));

        types.put("Building (Square)",
                  new MapType("Building (Square)", "square.xml",
                              "square"));

        types.put("Town (Square)",
                  new MapType("Town (Square)", "town.xml",
                              "square"));

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
        add(mapName = new JTextField("NewMap", 24), 1, 0, 3, 1);

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

        add(okay = new JButton("Create"), 2, 5, 1, 1);
        add(cancel = new JButton("Cancel"), 3, 5, 1, 1);

        okay.setActionCommand("createmap.save");
        okay.addActionListener(this);

        cancel.setActionCommand("createmap.cancel");
        cancel.addActionListener(this);

        setVisible(true);
    }

    /**
     * Get a list of all the map types we have templates for.
     */
    private String[]
    getTypes() {
        String[]    names = new String[types.size()];
        int         i = 0;

        for (Enumeration e = types.keys(); e.hasMoreElements(); i++) {
            names[i] = (String) e.nextElement();
        }

        return names;
    }

    /**
     * Get the name of the map, as defined in the map name textfield.
     */
    public String
    getMapName() {
        if (mapName == null) {
            return "map";
        }
        return mapName.getText();
    }

    /**
     * Return the name of the author, as defined in the author name textfield.
     */
    public String
    getAuthorName() {
        if (authorName == null) {
            return "anonymous";
        }
        return authorName.getText();
    }

    /**
     * Return the type of the map. This is the name of the default terrain
     * file to use, which defines the graphics, as well as the shape of the
     * tiles in the map.
     */
    public String
    getMapType() {
        if (mapType == null) {
            return "hexagonal";
        }

        String      name = (String)mapType.getSelectedItem();

        MapType     type = (MapType)types.get(name);

        return type.resource;
    }

    public int
    getScale() {
        int     scale = 1;
        String  value = "";

        if (mapScale != null) {
            try {
                value = mapScale.getText();
                scale = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
            }
        }

        return scale;
    }

    public int
    getMapWidth() {
        int     num = 50;
        String  value = "";

        if (mapWidth != null) {
            try {
                value = mapWidth.getText();
                num = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
            }
        }

        return num;
    }

    public int
    getMapHeight() {
        int     num = 50;
        String  value = "";

        if (mapHeight != null) {
            try {
                value = mapHeight.getText();
                num = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
            }
        }

        return num;
    }

}
