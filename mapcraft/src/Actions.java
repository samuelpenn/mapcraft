
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
 * This class keeps track of all the major actions used by the editor.
 * It loads the icons for the buttons and menu items, and should be
 * called when setting up toolbars and menus.
 */
public class Actions {
    private static Hashtable    actions;
    private String              path="icons/toolbar/";
    private ActionListener      globalListener;
    
    public final static String  FILE_NEW            = "file.new";
    public final static String  FILE_OPEN           = "file.open";
    public final static String  FILE_SAVE           = "file.save";
    public final static String  FILE_SAVEAS         = "file.saveas";
    public final static String  FILE_PRINT          = "file.print";
    public final static String  FILE_EXIT           = "file.exit";
    
    public final static String  VIEW_ZOOMIN         = "view.zoomin";
    public final static String  VIEW_ZOOMOUT        = "view.zoomout";
    public final static String  VIEW_XXSMALL        = "view.xxsmall";
    public final static String  VIEW_XSMALL         = "view.xsmall";
    public final static String  VIEW_SMALL          = "view.small";
    public final static String  VIEW_MEDIUM         = "view.medium";
    public final static String  VIEW_LARGE          = "view.large";
    public final static String  VIEW_XLARGE         = "view.xlarge";
    public final static String  VIEW_XXLARGE        = "view.xxlarges";
    public final static String  VIEW_SHOWTERRAIN    = "view.showterrain";
    public final static String  VIEW_SHOWFEATURES   = "view.showfeatures";
    public final static String  VIEW_GRID           = "view.grid";
    public final static String  VIEW_LARGEGRID      = "view.largegrid";

    public final static String  EDIT_TERRAIN        = "edit.terrain";
    public final static String  EDIT_FEATURES       = "edit.features";
    public final static String  EDIT_RIVERS         = "edit.rivers";

    public class Actor extends AbstractAction {
        private ActionListener  listener;
        
        public
        Actor(String name, String label) {
            putValue(Action.NAME, label);
            putValue(Action.ACTION_COMMAND_KEY, name);
        }

        public
        Actor(String name, String label, String image) {
            ImageIcon icon = new ImageIcon(image);

            putValue(Action.NAME, label);
            putValue(Action.SMALL_ICON, icon);
            putValue(Action.ACTION_COMMAND_KEY, name);
        }

        public void
        addListener(ActionListener listener) {
            this.listener = listener;
        }

        public void
        actionPerformed(ActionEvent e) {
            if (listener != null) {
                listener.actionPerformed(e);
            } else {
                globalListener.actionPerformed(e);
            }
        }
    }

    public
    Actions(ActionListener listener) {
        Action      action;

        if (listener != null) {
            this.globalListener = listener;
        }

        if (actions == null) {
            actions = new Hashtable();

            // File actions
            add(FILE_NEW, "New map", "general/New");
            add(FILE_OPEN, "Open...", "general/Open");
            add(FILE_SAVE, "Save", "general/Save");
            add(FILE_SAVEAS, "Save As...", "general/SaveAs");
            add(FILE_EXIT, "Exit", "general/Stop");

            // View actions
            add(VIEW_ZOOMIN, "Zoom in", "general/ZoomIn");
            add(VIEW_ZOOMOUT, "Zoom out", "general/ZoomOut");
            addLocal(VIEW_SHOWTERRAIN, "Terrain palette", "terrain");
            addLocal(VIEW_SHOWFEATURES, "Feature palette", "features");
            addLocal(VIEW_GRID, "Toggle grid", "grid");
            addLocal(VIEW_LARGEGRID, "Toggle rulers", "biggrid");

            add(VIEW_XXSMALL, "XX-Small");
            add(VIEW_XSMALL, "X-Small");
            add(VIEW_SMALL, "Small");
            add(VIEW_MEDIUM, "Medium");
            add(VIEW_LARGE, "Large");
            add(VIEW_XLARGE, "X-Large");
            add(VIEW_XXLARGE, "XX-Large");

            // Palettes
            addLocal(EDIT_TERRAIN, "Terrain Brush", "terrain");
            addLocal(EDIT_FEATURES, "Features Brush", "features");
            addLocal(EDIT_RIVERS, "Rivers Brush", "rivers");

        }
    }
    
    private void
    add(String name, String label) {
        Action      action = new Actor(name, label);
        actions.put(name, action);
    }

    private void
    add(String name, String label, String image) {
        Action      action = new Actor(name, label, path + image + "24.gif");
        actions.put(name, action);
    }
    
    private void
    addLocal(String name, String label, String image) {
        Action      action = new Actor(name, label, "icons/mapcraft/"+image+".png");
        actions.put(name, action);
    }

    public Action
    get(String name) {
        Actor actor = (Actor)actions.get(name);
        return (Action)actor;
    }
    
    public Action
    get(String name, ActionListener listener) {
        Actor actor = (Actor)actions.get(name);
        actor.addListener(listener);
        return (Action)actor;
    }

}