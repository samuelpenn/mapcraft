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
package net.sourceforge.mapcraft.map;

import java.util.*;

/**
 * Manages all the different types of terrain.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class TerrainSet implements Cloneable {
    protected ArrayList     terrainList;
    protected String        id;
    protected String        path;
    protected String        basePath;
    protected boolean       anySize;

    public
    TerrainSet(String id, String path) {
        this.id = id;
        this.path = path;
        this.basePath = ".";
        terrainList = new ArrayList();
        anySize = false;
    }

    public
    TerrainSet(String id, String basePath, String path) {
        this.id = id;
        this.path = path;
        this.basePath = basePath;
        terrainList = new ArrayList();
        anySize = false;
    }

    public String getId() { return id; }
    public String getPath() { return path; }
    public String getBasePath() { return basePath; }
    public boolean isAnySize() { return anySize; }

    public void
    setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * If anySize is set to be true, then graphics in this TerrainSet are
     * not limited to being the same size as the underlying map tiles.
     * They can be any size and shape.
     */
    public void
    setAnySize(boolean anySize) {
        this.anySize = anySize;
    }

    /**
     * Get terrain identified by its id.
     *
     * @param id    Unique id of the terrain to get.
     * @return      The Terrain object.
     */
    public Terrain
    getTerrain(short id) {
        Terrain     t;
        for (int i = 0; i < terrainList.size(); i++) {
            t = (Terrain)terrainList.get(i);
            if (t.getId() == id) {
                return t;
            }
        }

        return null;
    }

    public Terrain[]
    toArray() {
        Terrain[]   array = new Terrain[terrainList.size()];
        Iterator    it = iterator();
        int i = 0;

        while (it.hasNext()) {
            Terrain t = (Terrain) it.next();
            array[i++] = t;
        }

        return array;
    }

    public Iterator
    iterator() {
        return terrainList.iterator();
    }

    public int
    size() {
        return terrainList.size();
    }

    /**
     * Add the predefined terrain to the set of terrains.
     *
     * @param terrain   Terrain to be added.
     */
    public void
    add(Terrain terrain) {
        if (getTerrain(terrain.getId()) == null) {
            terrainList.add(terrain);
        }
    }

    /**
     * Add this description of a terrain to the set of
     * all terrains.
     *
     * @param id            Unique id for this terrain.
     * @param name          Unique name of the terrain.
     * @param description   Description of the terrain.
     * @param imagePath     Path to the image file to use.
     */
    public void
    add(short id, String name, String description, String imagePath) {
        if (getTerrain(id) == null) {
            add(new Terrain(id, name, description, imagePath));
        }
    }

    /**
     * Perform a deep copy of this object.
     */
    public Object
    clone() throws CloneNotSupportedException {
        TerrainSet      ts = new TerrainSet(id, path);
        int             i = 0;

        for (i=0; i < terrainList.size(); i++) {
            Terrain     t = (Terrain)terrainList.get(i);
            ts.add((Terrain)t.clone());
        }

        return (Object)ts;
    }

}
