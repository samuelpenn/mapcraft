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
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.*;

/**
 * Implements a GUI dialog for controlling rescaling of a map.
 * Rescaling involves changing the map resolution, not just the size
 * represented by each time. Changing a map from 1:5km to 1:1km would
 * increase the number of tiles by a factor of 25.
 */
public class RescaleDialog extends JDialog  {
    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    private int                 orgScale, orgWidth, orgHeight;
    private JLabel              width, height;
    private JSpinner            scale;
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
    RescaleDialog(int orgScale, int orgWidth, int orgHeight, JFrame frame) {
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
        this.orgScale = orgScale;

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        int     y = 0;
        add(new JLabel("Current"), 1, 0, 1, 1);
        add(new JLabel("New"), 2, 0, 1, 1);

        add(new JLabel("Width"), 0, 1, 1, 1);
        add(new JLabel("Height"), 0, 2, 1, 1);
        add(new JLabel("Scale"), 0, 3, 1, 1);

        add(new JLabel(""+orgWidth), 1, 1, 1, 1);
        add(new JLabel(""+orgHeight), 1, 2, 1, 1);
        add(new JLabel(""+orgScale), 1, 3, 1, 1);

        add(width = new JLabel(""+orgWidth), 2, 1, 1, 1);
        add(height = new JLabel(""+orgHeight), 2, 2, 1, 1);
        add(scale = new JSpinner(new SpinnerNumberModel(orgScale, 1, 1000, 1)), 2, 3, 1, 1);

        //add(left = new JCheckBox("Insert/remove columns at left edge"), 0, 4, 3, 1);
        //add(top = new JCheckBox("Insert/remove rows at top edge"), 0, 5, 3, 1);

        add(okay = new JButton("Rescale"), 1, 6, 1, 1);
        add(cancel = new JButton("Cancel"), 2, 6, 1, 1);

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

        scale.addChangeListener(new ChangeListener() {
                                    public void
                                    stateChanged(ChangeEvent e) {
                                        scaleChanged();
                                    }
                                 });

        setSize(new Dimension(300, 200));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Recalculates the new width and height of the map.
     * Called when the scale is changed, so the user gets instant feedback
     * on how big their new map is going to be.
     */
    private void
    scaleChanged() {
        int     s = getNewScale();
        int     w = (this.orgWidth * this.orgScale) / s;
        int     h = (this.orgHeight * this.orgScale) / s;

        width.setText(""+w);
        height.setText(""+h);
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

    /**
     * Return true if columns should be inserted on the left of map.
     */
    public boolean
    isLeftInsert() {
        return left.isSelected();
    }

    /**
     * Return true if rows should be inserted at the top of map.
     */
    public boolean
    isTopInsert() {
        return top.isSelected();
    }

    /**
     * Get the new scale of the map.
     */
    public int
    getNewScale() {
        Integer  i = (Integer)scale.getValue();

        return i.intValue();
    }

}
