
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

import uk.co.demon.bifrost.rpg.mapcraft.editor.*;

/**
 * Create and handle the Edit menu in the main application window.
 * Constructors return back a JMenu which can be added directly to
 * the menubar for the application.
 *
 * Implements the ActionListener class, and handles all events for
 * this menu, including those which are fired from the Toolbar rather
 * than from the menu directly.
 */
public class EditMenu extends JMenu implements ActionListener {
    protected Actions     actions = new Actions(null);
    protected MapCraft    application = null;


    public
    EditMenu(MapCraft application) {
        super("Edit");

        JMenu       zoom;

        this.application = application;

        addItem(Actions.EDIT_SMALL);
        addItem(Actions.EDIT_MEDIUM);
        addItem(Actions.EDIT_LARGE);

        addItem(Actions.EDIT_TERRAIN);
        addItem(Actions.EDIT_HILLS);
        addItem(Actions.EDIT_FEATURES);
        addItem(Actions.EDIT_AREAS);
        addItem(Actions.EDIT_RIVERS);

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

        System.out.println("EDITMENU ["+cmd+"]");

        if (cmd.equals(Actions.EDIT_SMALL)) {
            application.getEditor().setBrushSize(Brush.SMALL);
        } else if (cmd.equals(Actions.EDIT_MEDIUM)) {
            application.getEditor().setBrushSize(Brush.MEDIUM);
        } else if (cmd.equals(Actions.EDIT_LARGE)) {
            application.getEditor().setBrushSize(Brush.LARGE);
        } else if (cmd.equals(Actions.EDIT_RIVERS)) {
            application.getEditor().setBrushType(Brush.RIVERS);
        } else if (cmd.equals(Actions.EDIT_TERRAIN)) {
            application.getEditor().setBrushType(Brush.TERRAIN);
        } else if (cmd.equals(Actions.EDIT_FEATURES)) {
            application.getEditor().setBrushType(Brush.FEATURES);
        } else if (cmd.equals(Actions.EDIT_HILLS)) {
            application.getEditor().setBrushType(Brush.HILLS);
        } else if (cmd.equals(Actions.EDIT_AREAS)) {
            application.getEditor().setBrushType(Brush.AREAS);
        }
    }



}
