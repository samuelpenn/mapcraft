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
import net.sourceforge.mapcraft.utils.ImageUtils;
import net.sourceforge.mapcraft.xml.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.*;

public class Pane extends JPanel implements ListSelectionListener {
    private ListSelectionListener   editor;
    private Toolkit     toolkit;
    private Terrain[]   terrains;
    private JFrame      frame;
    private String      title;
    private JList       list;
    private JScrollPane scrollPane;
    private String      imagePath = "images/medium";


    /**
     * Class which implements cell rendering for an IconSet.
     * Used in the JList component.
     */
    public class
    ListRenderer extends JLabel implements ListCellRenderer {

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.

        public Component
        getListCellRendererComponent(JList list, Object value,
                                    int index, boolean isSelected,
                                    boolean cellHasFocus) {
            Image       image = null;
            ImageIcon   icon = null;

            Terrain t = (Terrain)value;

            if (t.getImagePath() != null) {
                ImageUtils  iu = new ImageUtils(this, imagePath);
                image = iu.getImage(t.getImagePath(), 24, 24);
                /*
                URL         url = Pane.class.getResource(imagePath+"/"+t.getImagePath());
                image = toolkit.getImage(url);
                image = image.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                */
                icon = new ImageIcon(image);
                setIcon(icon);
            }
            setText(t.getDescription());

            setEnabled(list.isEnabled());
            setOpaque(true);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    public String
    toString() {
        return title;
    }

    public void
    valueChanged(ListSelectionEvent e) {
    }

    public
    Pane(ListSelectionListener editor, String title) {
        super(true);

        this.editor = editor;
        this.title = title;

        toolkit = Toolkit.getDefaultToolkit();

    }
    
    public void
    setImagePath(String path) {
        this.imagePath = path;
    }

    public boolean
    isSelected(int index) {
        return list.isSelectedIndex(index);
    }

    public void
    makeFrame() {
        frame = new JFrame(title);
        frame.getContentPane().add(this);
        frame.setSize(new Dimension(120, 300));
        frame.setVisible(true);
    }

    public void
    setPalette(Terrain[] set, boolean label) {
        this.terrains = set;

        list = new JList(set);
        list.setCellRenderer(new ListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (editor != null) {
            list.addListSelectionListener(editor);
        } else {
            list.addListSelectionListener(this);
        }
        scrollPane = new JScrollPane(list);

        GridBagLayout       gridbag = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);
        scrollPane.setSize(new Dimension(120, 300));
    }


    public static void
    main(String args[]) {
        Pane        pane = new Pane((ListSelectionListener)null, "Test");
        TerrainSet  items;

        try {
            MapXML      xml = new MapXML("terrain.xml");
            Terrain[]   terrains;

            items = xml.getTerrainSet("basic");
            terrains = items.toArray();

            pane.setPalette(terrains, true);
            pane.makeFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
