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
import uk.co.demon.bifrost.rpg.mapcraft.utils.MapFileFilter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Implements a GUI dialog for controlling merging of two maps.
 */
public class MergeDialog extends JDialog  {
    private JPanel              topPanel;
    private JPanel              centrePanel;
    private JPanel              bottomPanel;
    
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
    private JButton             okay, cancel, load;
    private JList				ourAreas, otherAreas;


    private Map                 map;
    private Map					otherMap;

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
        super(frame, "Merge maps", true);

        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel = new JPanel(), BorderLayout.NORTH);
        getContentPane().add(centrePanel = new JPanel(), BorderLayout.CENTER);
        getContentPane().add(bottomPanel = new JPanel(), BorderLayout.SOUTH);

        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        centrePanel.setLayout(gridbag);
        
        // First, add the top panel components. These consist of the map
        // loader buttons and label.
        topPanel.add(new JLabel("Merge from"));
        topPanel.add(mergeMap = new JTextField("", 15));
        topPanel.add(load = new JButton("..."));
        mergeMap.setEditable(false);

        // Second, add the bottom panel components.
        bottomPanel.add(okay = new JButton("Merge"));
        bottomPanel.add(cancel = new JButton("Cancel"), BorderLayout.LINE_END);

        // Lastly, add the main central components. This uses a GridBagLayout.
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        int     y = 0;
        add(new JCheckBox("Only merge known areas"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge things"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge terrain and features"), 0, y, 3, 1); y++;
        add(new JCheckBox("Merge areas"), 0, y, 3, 1); y++;

        if (map != null) {
            ourAreas = new JList(map.getAreaSet().toNameArray());
        } else {
            ourAreas = new JList(new String[] { "Foo", "Bar", "Baz", 
                                                "Boing", "Ching"});
        }
        add(new JScrollPane(ourAreas), 0, y, 2, 4);


        // Set up action listeners for the buttons.
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

		load.addActionListener(new ActionListener() {
									public void
									actionPerformed(ActionEvent e) {
										browseForMap();
									}
								 });

        setSize(new Dimension(300, 350));
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Load the map that is going to be merged from.
     */
    private void
    browseForMap() {
    	String[]		areas = null;
		JFileChooser 	chooser = new JFileChooser();
		chooser.setFileFilter(new MapFileFilter());
		if (MapFileFilter.getLastLocation() != null) {
			chooser.setCurrentDirectory(MapFileFilter.getLastLocation());
		}

		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = chooser.getSelectedFile().getAbsolutePath();

			System.out.println("Opening file ["+filename+"]");
			MapFileFilter.setLastLocation(chooser.getSelectedFile());
			try {
				otherMap = new Map(filename);
				mergeMap.setText(filename);
				
				areas = otherMap.getAreaSet().toNameArray();
				otherAreas = new JList(areas);
				add(new JScrollPane(otherAreas), 3, 5, 2, 4);
				
			} catch (MapException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"Error loading map",
						JOptionPane.ERROR_MESSAGE); 
				e.printStackTrace();
			}
		}
    	
    }


    private void
    add(Component cmp, int x, int y, int w, int h) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.gridheight = h;
        gridbag.setConstraints(cmp, c);
        centrePanel.add(cmp);
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

    public static void
    main(String[] args) {
        try {
            MergeDialog     dialog;
            
            dialog = new MergeDialog(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
