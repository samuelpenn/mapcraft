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
 * Describes a single type of terrain.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class Terrain implements Cloneable {
    private short   id;
    private String  name;
    private String  description;
    private String  imagePath;

    public
    Terrain(short id, String name, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }

    public short getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }

    /**
     * Performs a deep copy of this object, returning a new object which
     * is identicle, but unrelated, to the current object.
     */
    public Object
    clone() throws CloneNotSupportedException {
        Terrain t;

        t = new Terrain(id, name, description, imagePath);

        return (Object)t;
    }

    public String
    toString() {
        return description;
    }
}
