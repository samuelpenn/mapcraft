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



 /**
 * An area is a continguous named region of the map.
 */
public class Area implements Cloneable {
    private int         id;
    private String      name;


    public
    Area(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Object
    clone() throws CloneNotSupportedException {
        Area    a = new Area(id, name);
        return (Object)a;
    }

    public void
    setId(int id) {
        this.id = id;
    }

    public void
    setName(String name) {
        this.name = name;
    }

    public int
    getId() {
        return id;
    }

    public String
    getName() {
        return name;
    }
}
