
/*
 * Copyright (C) 2003 Samuel Penn, sam@bifrost.demon.co.uk
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

import java.util.*;

/**
 * A set of areas for a map.
 */
 public class AreaSet implements Cloneable {

    protected ArrayList    list;

    public
    AreaSet() {
        list = new ArrayList();
    }

    public Object
    clone() throws CloneNotSupportedException {
        AreaSet    a = new AreaSet();
        return (Object)a;
    }

    public int
    size() {
        return list.size();
    }

    public Iterator
    iterator() {
        return list.iterator();
    }

    public void
    add(Area area) {
        list.add(area);
    }

    public void
    add(int id, String name) {
        Area    a = new Area(id, name);
        list.add(a);
    }

    public void
    add(int id, String name, int parent) {
        Area    a = new Area(id, name, parent);
        list.add(a);
    }

    public Area
    getArea(String name) {
        Iterator    it = list.iterator();
        Area        a = null;
        boolean     found = false;

        while (it.hasNext()) {
            a = (Area)it.next();
            if (a.getName().equalsIgnoreCase(name)) {
                found = true;
                break;
            }
        }

        if (!found) {
            a = null;
        }

        return a;
    }

    public Area
    getArea(int id) {
        Iterator    it = list.iterator();
        Area        a = null;
        boolean     found = false;

        while (it.hasNext()) {
            a = (Area)it.next();
            if (a.getId() == id) {
                found = true;
                break;
            }
        }

        if (!found) {
            a = null;
        }

        return a;
    }

    public Area[]
    toArray() {
        Area[]      array = new Area[list.size()];
        Iterator    iter = iterator();
        int         i = 0;

        while (iter.hasNext()) {
            Area    a = (Area)iter.next();
            array[i++] = a;
        }

        return array;
    }

    /**
     * Needed so that we can use the general Pane class for displaying the
     * areas. Just pretend to be terrain.
     */
    public Terrain[]
    toTerrainArray() {
        Terrain[]   array = new Terrain[list.size()+1];
        Iterator    iter = iterator();
        int         i = 0;

        array[i++] = new Terrain((short)0, "Undefined", "Undefined", null);

        while (iter.hasNext()) {
            Area    a = (Area)iter.next();
            array[i++] = new Terrain((short)a.getId(), a.getName(), a.getName(), null);
        }

        return array;
    }
 }
