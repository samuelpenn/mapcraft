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
public class Site {
    private short       type;
    private String      name;
    private String      description;

    // Sub-hex placement.
    private short       sx;
    private short       sy;
    private short       rotation;
    
    public String
    toString() {
        String      string;
        
        string = "Type ("+type+") Name ("+name+") X ("+sx+") Y ("+sy+") R ("+rotation+")";

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
        this.sx = 0;
        this.sy = 0;
        this.rotation = 0;
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
        this.sx = 0;
        this.sy = 0;
        this.rotation = (short)(rotation%360);
    }

    /**
     * Full constructor for a Site, allowing all components to be specified.
     */
    public
    Site(short type, String name, String description, short x, short y, short rotation) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.sx = x;
        this.sy = y;
        this.rotation = (short)(rotation%360);
        
        if (sx < -100) sx = -100;
        if (sy < -100) sy = -100;
        if (sx > +100) sx = +100;
        if (sy > +100) sy = +100;
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
    public short
    getX() { return sx; }
    
    /**
     * The Y position of the site within a tile. Possible values range
     * from -100 to +100, with the default being zero.
     */
    public short
    getY() { return sy; }
    
    /**
     * The rotation of the image for this site, in degrees clockwise.
     */
    public short
    getRotation() { return rotation; }

    public void
    setX(short x) {
        this.sx = x;
    }

    public void
    setY(short y) {
        this.sy = y;
    }
    
    public void
    setRotation(short r) {
        this.rotation = (short)(r%360);
    }
}
