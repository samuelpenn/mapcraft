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


/**
 * The Brush class provides support for a 'brush' - the concept
 * used for painting to the map. A brush has a type (which may
 * be changed by the user), which defines which layer of the map
 * is painted to, and a selection, which is the type of object
 * painted.
 */
public class Brush {
    public static final int SELECT = 0;
    public static final int TERRAIN = 1;
    public static final int SITES = 2;
    public static final int RIVERS = 3;
    public static final int HILLS = 4;
    public static final int HEIGHT = 5;
    public static final int FEATURES = 6;
    public static final int AREAS = 7;
    
    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int LARGE = 4;

    public static final int NEW = 1;
    public static final int MOVE = 2;
    public static final int EDIT = 3;

    private int brush = TERRAIN;
    private int size = SMALL;
    private int mode = NEW;

    private short terrain = 1;
    private short river = 0;
    private short site = 1;
    private short hill = 0;
    private short height = 0;
    private short area = 0;

    private int     rawX = 0;
    private int     rawY = 0;
    private int     button = 1;

    Brush() {
        brush = TERRAIN;
    }

    int
    getType() {
        return brush;
    }

    int
    getSize() {
        return size;
    }

    int getX() { return rawX; }
    int getY() { return rawY; }
    int getButton() { return button; }

    void setX(int x) { this.rawX = x; }
    void setY(int y) { this.rawY = y; }
    void setButton(int button) { this.button = button; }

    short
    getSelected() {
        switch (brush) {
        case TERRAIN: return terrain;
        case SITES:
        case FEATURES:
            return site;
        case RIVERS:  return river;
        case HILLS:   return hill;
        case HEIGHT:  return height;
        case AREAS:   return area;
        default:
            return 0;
        }
    }

    void
    setType(int type) {
        this.brush = type;
        river = 0;
    }
    
    void
    setSize(int size) {
        if (size == SMALL || size == MEDIUM || size == LARGE) {
            this.size = size;
        }
    }

    void
    setSelected(int type, short selected) {
        switch (type) {
        case TERRAIN:
            terrain = selected;
            break;
        case FEATURES:
        case SITES:
            site = selected;
            break;
        case RIVERS:
            river = selected;
            break;
        case HILLS:
            hill = selected;
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
    }
}
