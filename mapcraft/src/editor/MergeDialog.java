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

import uk.co.demon.bifrost.rpg.mapcraft.map.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Implements a GUI dialog for controlling merging of two maps.
 */
public class MergeDialog extends JDialog  {
    private GridBagLayout       gridbag;
    private GridBagConstraints  c;

    public static final int     NOTHING = 0;
    public static final int     SYNC = 1;
    public static final int     ADD = 2;


    private JTextField          mergeMap;
    private int                 orgScale, orgWidth, orgHeight;
    private JLabel              width, height;
    private JSpinner            scale;
    private JCheckBox           left, top;
    private JButton             okay, cancel;


    private Map                 map;

    private boolean             isOkay = false;


    /**
     * Create a modal thing editor dialog. When constructor returns, all
     * the data has been set up for the merge.
     *
     * @param thing     Thing to be edited.
     * @param frame     Frame thing is attached to.
     * @param icons     Terrain set for thing icons.
     * @param basePath  Base path to thing icons to use in dialog.
     */
    public
    MergeDialog(Map map, JFrame frame) {
        super(frame, "Resize map", true);

        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        int     y = 0;
        add(new JLabel("Merge from map"), 0, y, 2, 1);
        add(mergeMap = new JTextField(""), 2, y, 3, 1);
        y++;

        add(new JCheckBox("Only merge known areas"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge things"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge terrain and features"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge areas"), 0, y, 3, 1); y++;


        add(okay = new JButton("Rescale"), 2, y, 1, 1);
        add(cancel = new JButton("Cancel"), 3, y, 1, 1);

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


        setSize(new Dimension(300, 200));
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
