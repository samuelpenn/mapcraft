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
class Brush {
    public static final int NULL = 0;
    public static final int TERRAIN = 1;
    public static final int SITES = 2;
    public static final int RIVERS = 3;
    public static final int HILLS = 4;
    public static final int HEIGHT = 5;
    public static final int FEATURES = 6;

    private int brush = TERRAIN;

    private short terrain = 1;
    private short river = 1;
    private short site = 0;
    private short hill = 0;
    private short height = 0;

    Brush() {
        brush = TERRAIN;
    }

    int
    getType() {
        return brush;
    }

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
        default:
            return 0;
        }
    }

    void
    setType(int type) {
        this.brush = type;
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
        default:
            return;
        }
    }
}
