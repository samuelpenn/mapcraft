
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
package uk.co.demon.bifrost.rpg.mapcraft.map;

import uk.co.demon.bifrost.rpg.mapcraft.xml.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.*;

import uk.co.demon.bifrost.utils.Options;

/**
 * A mutable map is one which can be rescaled, resized and cropped.
 * It is otherwise identical to a standard Map. The real reason it
 * is a new class, is that smaller classes are easier to work with.
 *
 * All operations which effect the entire map are done on a MutableMap.
 */
public class MutableMap extends Map {
    public
    MutableMap(Map map) {
        super(map);
    }

    /**
     * Crop the given tileset to the given size.
     */
    public void
    crop(int set, int x, int y, int w, int h) {
        tilesets[set].crop(x, y, w, h);
    }

    public void
    cropToArea(int set, short area) {
        Map     map = null;
        int     minX, minY, maxX, maxY;
        int     x, y;
        boolean found = false;

        minX = minY = maxX = maxY = -1;
        for (x=0; x < map.getWidth(); x++) {
            for (y=0; y < map.getHeight(); y++) {
                try {
                    if (getArea(x, y).getId() == area) {
                        if (!found || x < minX) {
                            minX = x;
                        }
                        if (!found || x > maxX) {
                            maxX = x;
                        }
                        if (!found || y < minY) {
                            minY = y;
                        }
                        if (!found || y > maxY) {
                            maxY = y;
                        }
                        found = true;
                    }
                } catch (MapOutOfBoundsException moobe) {
                }
            }
        }
        crop(minX, minY, maxX-minX, maxY-minY);
    }


    public void
    cropToThing(int set, int thing) {
    }
}
