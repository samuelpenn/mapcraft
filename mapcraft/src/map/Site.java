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
 * A Site is a place of interest on a map, often a town or castle. For
 * local area maps, sites are 'features', such as a door or a table.
 * A Site supports sub-tile placement, so that multiple sites can be
 * placed on a single tile. The image can also be rotated - this latter
 * is of more use for 'features' in local area maps than it is for towns
 * in world area maps.
 */
public class Site implements Cloneable {
    private short       type;
    private String      name;
    private String      description;

    // Sub-hex placement.
    private int         x;
    private int         y;

    public String
    toString() {
        String      string;

        string = "Type ("+type+") Name ("+name+") X ("+x+") Y ("+y+")";

        return string;
    }

    /**
     * Constructor for a Site, giving its type, a name and a long
     * description for the place.
     */
    public
    Site(short type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructor designed for local area maps, where a Site doesn't
     * have a name or description.
     */
    public
    Site(short type, short rotation) {
        this.type = type;
        this.name = "";
        this.description = "";
        this.x = 0;
        this.y = 0;
    }

    /**
     * Full constructor for a Site, allowing all components to be specified.
     */
    public
    Site(short type, String name, String description, int x, int y) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
    }

    public Object
    clone() throws CloneNotSupportedException {
        Site    s = new Site(type, name, description);

        s.x = x;
        s.y = y;

        return (Object)s;
    }

    public short
    getType() {
        return type;
    }

    public String
    getName() {
        return name;
    }

    public String
    getDescription() {
        return description;
    }

    public void
    setType(short type) { this.type = type; }

    public void
    setName(String name) { this.name = name; }

    public void
    setDescription(String description) { this.description = description; }

    /**
     * The X position of the site within a tile. Possible values range
     * from -100 to +100, with the default being zero.
     */
    public int
    getX() { return x; }

    /**
     * The Y position of the site within a tile. Possible values range
     * from -100 to +100, with the default being zero.
     */
    public int
    getY() { return y; }


    public void
    setX(int x) {
        this.x = x;
    }

    public void
    setY(int y) {
        this.y = y;
    }

}
