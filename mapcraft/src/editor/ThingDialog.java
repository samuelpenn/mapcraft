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

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class ThingDialog extends JDialog implements ItemListener {
    private Thing       thing;
    private JTextField  name;
    private JTextField  description;
    private JButton     okay;
    private JButton     cancel;
    private ImageIcon   icon = null;
    private JLabel      picture;
    private TerrainSet  icons = null;
    private String      basePath = null;
    private JComboBox   type = null;
    private JComboBox   fontSize = null;
    private JComboBox   importance = null;

    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    public void
    itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == e.SELECTED) {
            Terrain     t = (Terrain)e.getItem();
            String path = basePath;
            path = path + "/" + t.getImagePath();
            icon = new ImageIcon(path);
            picture.setIcon(icon);
            picture.setText(t.getDescription());
        }
    }

    public String
    getName() {
        if (name != null) {
            return name.getText();
        }

        return "";
    }

    public String
    getDescription() {
        if (description != null) {
            return description.getText();
        }

        return "";
    }

    /**
     * Get the selected terrain type for this thing.
     */
    public short
    getType() {
        Terrain     t = (Terrain)type.getSelectedItem();
        return t.getId();
    }

    /**
     * Get the selected font size for this thing.
     */
    public int
    getFontSize() {
        String  s = (String)fontSize.getSelectedItem();
        int     size = Thing.MEDIUM;

        if (s.equals("Small")) {
            size = Thing.SMALL;
        } else if (s.equals("Medium")) {
            size = Thing.MEDIUM;
        } else if (s.equals("Large")) {
            size = Thing.LARGE;
        } else if (s.equals("Huge")) {
            size = Thing.HUGE;
        }

        return size;
    }

    public int
    getImportance() {
        String  s = (String)fontSize.getSelectedItem();
        int     importance = Thing.NORMAL;

        if (s.equals("Low")) {
            importance = Thing.LOW;
        } else if (s.equals("Normal")) {
            importance = Thing.NORMAL;
        } else if (s.equals("High")) {
            importance = Thing.HIGH;
        }

        return importance;
    }

    public Thing
    getThing() {
        thing.setType(getType());
        thing.setName(getName());
        thing.setDescription(getDescription());
        thing.setFontSize(getFontSize());
        thing.setImportance(getImportance());

        return thing;
    }

    private JComboBox
    createFontCombo() {
        String[]    label = { "Small", "Medium", "Large", "Huge" };
        JComboBox   box = new JComboBox(label);
        box.setSelectedItem("Medium");

        return box;
    }

    private JComboBox
    createImportanceCombo() {
        String[]    label = { "Low", "Medium", "High" };
        return new JComboBox(label);
    }

    /**
     * Create a JComboBox for the terrain type. Set up this class
     * to be the itemListener for the box.
     */
    private JComboBox
    createTypeCombo() {
        JComboBox   box = new JComboBox(icons.toArray());
        box.addItemListener(this);
        box.setSelectedItem(icons.getTerrain(thing.getType()));

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
    ThingDialog(Thing thing, JFrame frame, TerrainSet icons, String basePath) {
        super(frame, "Edit thing", true);
        this.thing = thing;
        this.icons = icons;
        this.basePath = basePath;

        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        JLabel              label = null;
        ImageIcon           icon = null;

        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        name = new JTextField(thing.getName(), 20);
        description = new JTextField(thing.getDescription(), 30);
        String path = basePath;
        path = path + "/" + icons.getTerrain(thing.getType()).getImagePath();
        icon = new ImageIcon(path);

        picture = new JLabel(icons.getTerrain(thing.getType()).getDescription(),
                             icon, SwingConstants.LEFT);


        add(picture, 0, 0, 1, 1);
        add(type = createTypeCombo(), 0, 1, 1, 1);
        add(new JLabel("Name"), 1, 0, 1, 1);
        add(name, 1, 1, 1, 1);

        add(new JLabel("Description"), 0, 2, 1, 1);
        add(description, 0, 3, 2, 3);

        add(fontSize = createFontCombo(), 0, 6, 1, 1);
        add(importance = createImportanceCombo(), 1, 6, 1, 1);
        /*
        c.gridy = 2;
        c.gridx = 1;
        c.weightx = 0.0;
        okay = new JButton("Okay");
        gridbag.setConstraints(okay, c);
        getContentPane().add(okay);
        */
        setSize(new Dimension(300,150));
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
        Thing        thing = new Thing((short)1, "London", "Large city");
        //ThingDialog  dialog = new ThingDialog(thing);
    }
}
