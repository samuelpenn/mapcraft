
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
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;


/**
 * Create and handle the View menu in the main application window.
 * Constructors return back a JMenu which can be added directly to
 * the menubar for the application.
 *
 * Implements the ActionListener class, and handles all events for
 * this menu, including those which are fired from the Toolbar rather
 * than from the menu directly.
 */
public class ViewMenu extends JMenu implements ActionListener {
    protected Actions     actions = new Actions(null);
    protected MapCraft    application = null;


    public
    ViewMenu(MapCraft application) {
        super("View");

        JMenu       zoom;

        this.application = application;

        addItem(Actions.VIEW_SHOWTERRAIN);
        addItem(Actions.VIEW_SHOWTHINGS);
        addItem(Actions.VIEW_SHOWFEATURES);
        addItem(Actions.VIEW_SHOWAREAS);
        addSeparator();
        addItem(Actions.VIEW_ZOOMIN);
        addItem(Actions.VIEW_ZOOMOUT);

        zoom = new JMenu("Zoom");
        addItem(zoom, Actions.VIEW_XXSMALL);
        addItem(zoom, Actions.VIEW_XSMALL);
        addItem(zoom, Actions.VIEW_SMALL);
        addItem(zoom, Actions.VIEW_MEDIUM);
        addItem(zoom, Actions.VIEW_LARGE);
        addItem(zoom, Actions.VIEW_XLARGE);
        addItem(zoom, Actions.VIEW_XXLARGE);
        add(zoom);

        addSeparator();

        addItem(Actions.VIEW_GRID);
        addItem(Actions.VIEW_LARGEGRID);
    }


    private void
    addItem(String name) {
        JMenuItem       item = new JMenuItem();
        item.setAction(actions.get(name, this));
        add(item);
    }

    private void
    addItem(JMenu menu, String name) {
        JMenuItem       item = new JMenuItem();
        item.setAction(actions.get(name, this));
        menu.add(item);
    }

    public void
    actionPerformed(ActionEvent e) {
        String  cmd = e.getActionCommand();

        System.out.println("VIEWMENU ["+cmd+"]");

        if (cmd.equals(Actions.VIEW_XXSMALL)) {
            application.getEditor().setView(0);
        } else if (cmd.equals(Actions.VIEW_XSMALL)) {
            application.getEditor().setView(1);
        } else if (cmd.equals(Actions.VIEW_SMALL)) {
            application.getEditor().setView(2);
        } else if (cmd.equals(Actions.VIEW_MEDIUM)) {
            application.getEditor().setView(3);
        } else if (cmd.equals(Actions.VIEW_LARGE)) {
            application.getEditor().setView(4);
        } else if (cmd.equals(Actions.VIEW_XLARGE)) {
            application.getEditor().setView(5);
        } else if (cmd.equals(Actions.VIEW_XXLARGE)) {
            application.getEditor().setView(6);
        } else if (cmd.equals(Actions.VIEW_ZOOMIN)) {
            application.getEditor().zoomIn();
        } else if (cmd.equals(Actions.VIEW_ZOOMOUT)) {
            application.getEditor().zoomOut();
        } else if (cmd.equals(Actions.VIEW_GRID)) {
            // Toggle grid display.
            application.getEditor().setShowLargeGrid(!application.getEditor().isShowLargeGrid());
        } else if (cmd.equals(Actions.VIEW_LARGEGRID)) {
            // Toggle large grid display.
            application.getEditor().setShowLargeGrid(!application.getEditor().isShowLargeGrid());
        } else if (cmd.equals(Actions.VIEW_SHOWTERRAIN)) {
            application.getEditor().showTerrainPalette();
        } else if (cmd.equals(Actions.VIEW_SHOWTHINGS)) {
            application.getEditor().showThingPalette();
        } else if (cmd.equals(Actions.VIEW_SHOWFEATURES)) {
            application.getEditor().showFeaturePalette();
        } else if (cmd.equals(Actions.VIEW_SHOWAREAS)) {
            application.getEditor().showAreaPalette();
        }
    }



}
