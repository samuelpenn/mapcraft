
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
import java.awt.event.*;

/**
 * Create and handle the Edit menu in the main application window.
 * Constructors return back a JMenu which can be added directly to
 * the menubar for the application.
 *
 * Implements the ActionListener class, and handles all events for
 * this menu, including those which are fired from the Toolbar rather
 * than from the menu directly.
 */
public class ToolMenu extends JMenu implements ActionListener {
    protected Actions     actions = new Actions(null);
    protected MapCraft    application = null;


    public
    ToolMenu(MapCraft application) {
        super("Tools");

        JMenu       zoom;

        this.application = application;

        addItem(Actions.TOOL_CROP);
        addItem(Actions.TOOL_RESIZE);
        addItem(Actions.TOOL_RESCALE);
        addItem(Actions.TOOL_MERGE);
        addItem(Actions.TOOL_EDITAREAS);
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

        System.out.println("TOOLMENU ["+cmd+"]");

        if (cmd.equals(Actions.TOOL_CROP)) {
            application.getEditor().crop();
        } else if (cmd.equals(Actions.TOOL_RESIZE)) {
            application.getEditor().resize();
        } else if (cmd.equals(Actions.TOOL_RESCALE)) {
            application.getEditor().rescale();
        } else if (cmd.equals(Actions.TOOL_MERGE)) {
            application.getEditor().merge();
        } else if (cmd.equals(Actions.TOOL_EDITAREAS)) {
            application.getEditor().editAreas();
        }
    }



}
