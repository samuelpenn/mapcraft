/*
 * Copyright (C) 2002 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package uk.co.demon.bifrost.rpg.mapcraft.map;

import junit.framework.*;

public class TileSetTest extends TestCase {
    private int     width, height, scale;
    private String  name;

    public void
    setUp() {
        width = 100;
        height = 100;
        scale = 25;

        name = "Test";
    }

    /**
     * Ensure that it is possibel to create a simple TileSet.
     */
    public void
    testCreate() {
        TileSet     set = null;

        try {
            set = new TileSet(name, width, height, scale);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Assert.assertTrue(true);
    }

    /**
     * Test cropping of a simple tileset.
     */
    public void
    testCrop() {
        TileSet     set = null;

        try {
            set = new TileSet(name, width, height, scale);
            set.crop(10, 10, 20, 15);

            assertEquals("Width of cropped map is wrong", 20, set.getWidth());
            assertEquals("Height of cropped map is wrong", 15, set.getHeight());
        } catch (MapOutOfBoundsException moobe) {
            fail("Map out of bounds error thrown");
        } catch (Exception e) {
            fail("Unexpected exception: "+e.getMessage());
        }
        Assert.assertTrue(true);
    }

    /**
     * Test adding rivers and roads.
     */
    public void
    testPaths() {
        TileSet     set = null;
        Path        path = null;
        int         id = 0;
        String      pathName = "Path one";

        try {
            set = new TileSet(name, width, height, scale);

            id = set.addPath(pathName, Path.RIVER, Path.PLAIN, 100, 95);
            assertEquals("Id of first path must be one", 1, id);
            set.extendPath(id, 120, 120);
            set.extendPath(id, 150, 200);

            path = set.getPath(id);
            assertEquals("Name of path has changed", pathName, path.getName());
            assertEquals("Path does not have right number of elements", 3, path.getSize());
            assertEquals("Left edge of bounding box is wrong", 100, path.getMinX());
            assertEquals("Right edge of bounding box is wrong", 150, path.getMaxX());
            assertEquals("Top edge of bounding box is wrong", 200, path.getMaxY());
            assertEquals("Bottom edge of bounding box is wrong", 95, path.getMinY());
        } catch (Exception e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }
}