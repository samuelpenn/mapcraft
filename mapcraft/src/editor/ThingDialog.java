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

package net.sourceforge.mapcraft.editor;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.Terrain;
import net.sourceforge.mapcraft.map.elements.Thing;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.net.URL;

public class ThingDialog extends JDialog implements ItemListener {
    private Thing       thing;
    private JTextField  name;
    private JTextArea   description;
    private JButton     okay;
    private JButton     cancel;
    private ImageIcon   icon = null;
    private JLabel      picture;
    private TerrainSet  icons = null;
    private String      basePath = null;
    private JComboBox   type = null;
    private JComboBox   fontSize = null;
    private JComboBox   importance = null;
    private JTable      table = null;

    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    private Vector              keys, values;

    private class PropertyModel extends AbstractTableModel {
        public int
        getColumnCount() {
            return 2;
        }

        public String
        getColumnName(int column) {
            if (column == 0) {
                return "Property";
            }
            return "Value";
        }

        public int
        getRowCount() {
            return keys.size()+1;
        }

        public boolean
        isCellEditable(int x, int y) {
            return true;
        }

        public void
        setValueAt(Object value, int y, int x) {
            String  s = (String)value;
            System.out.println("setValueAt: "+x+","+y);

            if (x == 0) {
                if (y >= keys.size()) {
                    keys.add(s);
                    values.add("");
                } else {
                    keys.setElementAt(s, y);
                }
            } else {
                if (y >= keys.size()) {
                    values.add(s);
                    keys.add("");
                } else {
                    values.setElementAt(s, y);
                }
            }
            table.revalidate();
            table.repaint();
        }

        public Object
        getValueAt(int row, int column) {
            String      v = null;

            if (row >= keys.size()) {
                v = "";
            } else if (column == 0) {
                v = (String)keys.elementAt(row);
            } else {
                v = (String)values.elementAt(row);
            }

            return (Object)v;
        }
    }

    private void
    fetchProperties() {
        keys = new Vector();
        values = new Vector();

        if (thing.getProperties() == null) {
            return;
        }
        Enumeration     e = thing.getProperties().keys();
        while (e.hasMoreElements()) {
            String  k = (String) e.nextElement();
            keys.add((String)k);
            values.add((String)thing.getProperty(k));
        }
    }

    public void
    itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            Terrain     t = (Terrain)e.getItem();
            String path = basePath;
            URL    url = ThingDialog.class.getResource(path + "/" + t.getImagePath());
            icon = new ImageIcon(url);
            picture.setIcon(icon);
            //picture.setText(t.getDescription());
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

        if (keys.size() > 0) {
            Properties      props = new Properties();

            for (int i=0; i<keys.size(); i++) {
                String      k = (String)keys.elementAt(i);
                if (k.length() > 0) {
                    String  v = (String)values.elementAt(i);
                    if (v.length() > 0) {
                        props.put(k, v);
                    }
                }
            }
            if (props.size() > 0) {
                thing.setProperties(props);
            } else {
                thing.setProperties(null);
            }
        } else {
            thing.setProperties(null);
        }

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
        description = new JTextArea(thing.getDescription(), 5, 30);
        String path = basePath;
        URL    url = ThingDialog.class.getResource(path + "/" +
                       icons.getTerrain(thing.getType()).getImagePath());
        icon = new ImageIcon(url, "");

        picture = new JLabel(icon);

        add(picture, 0, 0, 3, 3);
        add(name, 2, 0, 2, 1);
        add(type = createTypeCombo(), 2, 1, 2, 1);

        add(new JLabel("Description"), 0, 3, 1, 1);
        c.weighty = 1.0;
        add(description, 0, 4, 5, 3);
        c.weighty = 0.0;

        add(new JLabel("Font size"), 0, 7, 1, 1);
        add(fontSize = createFontCombo(), 2, 7, 3, 1);
        add(new JLabel("Importance"), 0, 8, 1, 1);
        add(importance = createImportanceCombo(), 2, 8, 3, 1);

        fetchProperties();
        table = new JTable(new PropertyModel());
        table.setCellSelectionEnabled(true);
        table.setCellEditor(new DefaultCellEditor(new JTextField()));

        add(new JLabel("Meta data"), 0, 9, 1, 1);
        c.weighty = 1.0;
        add(new JScrollPane(table), 0, 10, 5, 3);


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
        Thing        thing = new Thing((short)1, "London", "Large city");
        //ThingDialog  dialog = new ThingDialog(thing);
    }
}
