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

public class SiteDialog extends JDialog implements ItemListener {
    private Site        site;
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
     * Get the selected terrain type for this site.
     */
    public short
    getType() {
        Terrain     t = (Terrain)type.getSelectedItem();
        return t.getId();
    }

    /**
     * Get the selected font size for this site.
     */
    public int
    getFontSize() {
        String  s = (String)fontSize.getSelectedItem();
        int     size = Site.MEDIUM;

        if (s.equals("Small")) {
            size = Site.SMALL;
        } else if (s.equals("Medium")) {
            size = Site.MEDIUM;
        } else if (s.equals("Large")) {
            size = Site.LARGE;
        } else if (s.equals("Huge")) {
            size = Site.HUGE;
        }

        return size;
    }

    public int
    getImportance() {
        String  s = (String)fontSize.getSelectedItem();
        int     importance = Site.NORMAL;

        if (s.equals("Low")) {
            importance = Site.LOW;
        } else if (s.equals("Normal")) {
            importance = Site.NORMAL;
        } else if (s.equals("High")) {
            importance = Site.HIGH;
        }

        return importance;
    }

    public Site
    getSite() {
        site.setType(getType());
        site.setName(getName());
        site.setDescription(getDescription());
        site.setFontSize(getFontSize());
        site.setImportance(getImportance());

        return site;
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
        box.setSelectedItem(icons.getTerrain(site.getType()));

        return box;
    }

    /**
     * Create a modal site editor dialog. When constructor returns, all
     * the data has been set up for the site.
     *
     * @param site      Site to be edited.
     * @param frame     Frame site is attached to.
     * @param icons     Terrain set for site icons.
     * @param basePath  Base path to site icons to use in dialog.
     */
    public
    SiteDialog(Site site, JFrame frame, TerrainSet icons, String basePath) {
        super(frame, "Edit site", true);
        this.site = site;
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

        name = new JTextField(site.getName(), 20);
        description = new JTextField(site.getDescription(), 30);
        String path = basePath;
        path = path + "/" + icons.getTerrain(site.getType()).getImagePath();
        icon = new ImageIcon(path);

        picture = new JLabel(icons.getTerrain(site.getType()).getDescription(),
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
        Site        site = new Site((short)1, "London", "Large city");
        //SiteDialog  dialog = new SiteDialog(site);
    }
}
