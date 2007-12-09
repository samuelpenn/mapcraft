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

import net.sourceforge.mapcraft.map.elements.Path;


/**
 * The Brush class provides support for a 'brush' - the concept
 * used for painting to the map. A brush has a type (which may
 * be changed by the user), which defines which layer of the map
 * is painted to, and a selection, which is the type of object
 * painted.
 */
public class Brush {
    public static final int TERRAIN = 1;
    public static final int THINGS = 2;
    public static final int RIVERS = 3;
    public static final int FEATURES = 4;
    public static final int HEIGHT = 5;
    public static final int AREAS = 6;
    public static final int ROADS = 7;
    public static final int HIGHLIGHT = 8;

    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int LARGE = 4;

    public static final int SELECT = 1;
    public static final int NEW = 2;
    public static final int EDIT = 3;
    public static final int INSERT = 4;
    public static final int DELETE = 5;

    public static final int LEFT = 1;
    public static final int MIDDLE = 2;
    public static final int RIGHT = 3;

    private int brush = TERRAIN;
    private int size = SMALL;
    private int mode = SELECT;

    private int terrain = 1;
    private int thing = 1;
    private int feature = 0;
    private int height = 0;
    private int area = 0;
    
    private Path    road = null;
    private Path    river = null;

    private int     rawX = 0;
    private int     rawY = 0;
    private int     button = 1;
    private short   rotation = 0;

    private int     lastMouseX = 0;
    private int     lastMouseY = 0;

    Brush() {
        brush = TERRAIN;
        size = SMALL;
        mode = SELECT;
    }

    int
    getType() {
        return brush;
    }

    int
    getSize() {
        return size;
    }

    int
    getMode() {
        return mode;
    }

    int getX() { return rawX; }
    int getY() { return rawY; }
    int getButton() { return button; }
    short getRotation() { return rotation; }

    void setX(int x) { this.rawX = x; }
    void setY(int y) { this.rawY = y; }
    void setButton(int button) { this.button = button; }
    void setRotation(short rotation) { this.rotation = rotation; }

    Path
    getSelectedPath() {
        switch (brush) {
        case RIVERS:    return river;
        case ROADS:     return road;
        default:
            return null;
        }
    }
    
    int
    getSelected() {
        switch (brush) {
        case TERRAIN:   return terrain;
        case THINGS:    return thing;
        case FEATURES:  return feature;
        case HEIGHT:    return height;
        case AREAS:     return area;
        case HIGHLIGHT: return 1;
        default:
            return 0;
        }
    }

    void
    setType(int type) {
        this.brush = type;
        rotation = 0;

        switch (brush) {
        case RIVERS:
        case ROADS:
            mode = SELECT;
            road = river = null;
            break;
        default:
            break;
        }
    }

    void
    setSize(int size) {
        if (size == SMALL || size == MEDIUM || size == LARGE) {
            this.size = size;
        }
    }

    void
    setMode(int mode) {
        this.mode = mode;
    }
    
    void
    setSelected(int type, Path selected) {
        switch (type) {
        case RIVERS:
            river = selected;
            break;
        case ROADS:
            road = selected;
            break;
        default:
            return;
        }
    }

    void
    setSelected(int type, int selected) {
        switch (type) {
        case TERRAIN:
            terrain = selected;
            break;
        case THINGS:
            thing = selected;
            break;
        case FEATURES:
            feature = selected;
            break;
        case HEIGHT:
            height = selected;
            break;
        case AREAS:
            area = selected;
            break;
        default:
            return;
        }
        rotation = 0;
    }

    public void
    setLastMousePosition(int x, int y) {
        lastMouseX = x;
        lastMouseY = y;
    }

    public int
    getLastMouseX() { return lastMouseX; }

    public int
    getLastMouseY() { return lastMouseY; }
}
