
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

import junit.framework.*;

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

    public void
    testCreate() {
        try {
            map = new Map(name, width, height, scale);
            map.loadTerrainSet(terrain);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Assert.assertTrue(true);
    }

    public void
    testSave() {
        try {
            map.save(filename);
            map = null;
        } catch (Exception e)  {
            fail(e.getMessage());
        }
        Assert.assertTrue(true);
    }

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

