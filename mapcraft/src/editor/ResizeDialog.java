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
public class ResizeDialog extends JDialog  {
    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    private int                 orgWidth, orgHeight;
    private JTextField          width, height;
    private JCheckBox           left, top;
    private JButton             okay, cancel;

    private Map                 map;

    private boolean             isOkay = false;




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
    ResizeDialog(int orgWidth, int orgHeight, JFrame frame) {
        super(frame, "Resize map", true);

        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }
        this.orgWidth = orgWidth;
        this.orgHeight = orgHeight;

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        int     y = 0;
        add(new Label("Current"), 1, 0, 1, 1);
        add(new Label("New"), 2, 0, 1, 1);

        add(new Label("Width"), 0, 1, 1, 1);
        add(new Label("Height"), 0, 2, 1, 1);

        add(new Label(""+orgWidth), 1, 1, 1, 1);
        add(new Label(""+orgHeight), 1, 2, 1, 1);

        add(width = new JTextField(""+orgWidth), 2, 1, 1, 1);
        add(height = new JTextField(""+orgHeight), 2, 2, 1, 1);


        add(okay = new JButton("Resize"), 1, 5, 1, 1);
        add(cancel = new JButton("Cancel"), 2, 5, 1, 1);

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
