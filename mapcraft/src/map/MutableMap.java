
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
     *
     * @param set       TileSet to be cropped.
     * @param x         X coordinate of left side of crop window.
     * @param y         Y coordinate of top side of crop window.
     * @param w         Width of the crop window.
     * @param h         Height of the crop window.
     *
    public void
    crop(int set, int x, int y, int w, int h) throws MapOutOfBoundsException {
        tileSets[set].crop(x, y, w, h);
    }
    */

    /**
     * Crop the map to the given area. The map is searched for all tiles
     * which match the area, and a rectangle is formed which encloses all
     * these tiles. The rectangle will then be grown in each direction by
     * the size of the margin, if it is positive.
     *
     * The margin will not take the cropped area out beyond the edges of
     * the original map.
     *
     * @param set       TileSet to be cropped.
     * @param area      Area id to be cropped to.
     * @param margin    Number of tiles to add as a margin.
     *
    public void
    cropToArea(int set, short area, int margin) throws MapOutOfBoundsException {
        int     minX, minY, maxX, maxY;
        int     x, y;
        boolean found = false;

        minX = minY = maxX = maxY = -1;
        for (x=0; x < getWidth(set); x++) {
            for (y=0; y < getHeight(set); y++) {
                try {
                    if (getArea(set, x, y).getId() == area) {
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

        if (margin > 0) {
            minX -= margin;
            minY -= margin;
            maxX += margin;
            maxY += margin;

            if (minX < 0) minX = 0;
            if (minY < 0) minY = 0;
            if (maxX >= getWidth(set)) maxX = getWidth(set)-1;
            if (maxY >= getHeight(set)) maxY = getHeight(set)-1;
        }

        crop(set, minX, minY, maxX-minX, maxY-minY);
    }
*/

    public void
    cropToThing(int set, int thing) {
    }
}
