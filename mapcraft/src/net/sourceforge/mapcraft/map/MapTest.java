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
package net.sourceforge.mapcraft.map;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MapTest extends TestCase {
    private Map     map = null;
    private int     width, height, scale;
    private String  name = null;
    private String  terrain = null;
    private String  filename = null;

    public void
    setUp() {
        map = null;
        name = "Test Map One";
        width = 30;
        height = 20;
        scale = 5;
        terrain = "terrain/hexagonal.xml";
        filename = "test.map";
    }

    /**
     * Make sure that it is possible to create a new map from scratch.
     * A Hexagonal world map is created, and saved to a file.
     */
    public void
    testCreate() {
        try {
            map = new Map(name, width, height, scale);
            map.setTileShape(Map.HEXAGONAL);
            map.loadTerrainSet(terrain);
            map.save(filename);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Assert.assertTrue(true);
    }

    /**
     * Try to load the map created and saved in the previous test.
     * If this works, then the file format is probably sane.
     */
    public void
    testLoad() {
        try {
            map = new Map(filename);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Assert.assertTrue(true);
    }

}

