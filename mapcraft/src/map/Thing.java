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

import java.util.Properties;

/**
 * A Thing is a place of interest on a map, often a town or castle. For
 * local area maps, things are 'features', such as a door or a table.
 * A Thing supports sub-tile placement, so that multiple things can be
 * placed on a single tile. The image can also be rotated - this latter
 * is of more use for 'features' in local area maps than it is for towns
 * in world area maps.
 */
public class Thing implements Cloneable {
    private short       type;
    private String      name;
    private String      description;
    private int         x;
    private int         y;
    private int         fontSize = MEDIUM;
    private int         importance = NORMAL;
    private boolean     bold;
    private boolean     italic;
    private boolean     underlined;
    private short       rotation;

    private Properties  properties;


    public static final int   SMALL = 1;  // 8pt
    public static final int   MEDIUM = 2; // 12pt
    public static final int   LARGE = 3;  // 16pt
    public static final int   HUGE = 4;   // 24pt

    public static final int   LOW = 1;
    public static final int   NORMAL = 2;
    public static final int   HIGH = 3;

    public String
    toString() {
        String      string;

        string = "Type ("+type+") Name ("+name+") X ("+x+") Y ("+y+")";

        return string;
    }

    /**
     * Constructor for a Thing, giving its type, a name and a long
     * description for the place.
     */
    public
    Thing(short type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.x = 0;
        this.y = 0;
        this.rotation = 0;
        this.properties = null;
    }

    /**
     * Constructor designed for local area maps, where a Thing doesn't
     * have a name or description.
     */
    public
    Thing(short type, short rotation) {
        this.type = type;
        this.name = "";
        this.description = "";
        this.x = 0;
        this.y = 0;
        this.rotation = 0;
        this.properties = null;
    }

    /**
     * Full constructor for a Thing, allowing all components to be specified.
     */
    public
    Thing(short type, String name, String description, int x, int y) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.properties = null;
    }

    public Object
    clone() throws CloneNotSupportedException {
        Thing    s = new Thing(type, name, description);

        s.x = x;
        s.y = y;
        s.rotation = rotation;
        s.properties = properties;

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

    public int
    getFontSize() {
        return fontSize;
    }

    public int
    getImportance() {
        return importance;
    }

    public short
    getRotation() {
        return rotation;
    }

    public boolean
    isBold()  { return bold; }

    public boolean
    isItalic() { return italic; }

    public boolean
    isUnderlined() { return underlined; }

    public void
    setType(short type) { this.type = type; }

    public void
    setName(String name) { this.name = name; }

    public void
    setDescription(String description) { this.description = description; }

    /**
     * Set the font size for the label. Font size is one of SMALL, MEDIUM,
     * LARGE and HUGE. Font size scales according to the scale of the map,
     * so it does not represent any particular point size.
     */
    public void
    setFontSize(int fontSize) {
        if (fontSize >= SMALL && fontSize <= HUGE) {
            this.fontSize = fontSize;
        }
    }

    /**
     * Set the importance of this Thing. Things of LOW importance are not
     * displayed when the map is zoomed out. HIGH importance sites are
     * always displayed, regardless of zoom factor. NORMAL importance
     * falls somewhere inbetween.
     */
    public void
    setImportance(int importance) {
        if (importance >= LOW && importance <= HIGH) {
            this.importance = importance;
        }
    }

    public void
    setRotation(short rotation) {
        this.rotation = rotation;
    }

    public void
    setBold(boolean bold) {
        this.bold = bold;
    }

    public void
    setItalic(boolean italic) {
        this.italic = italic;
    }

    public void
    setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

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

    public void
    setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get all the properties of this Thing, as a Properties object.
     * Properties are user defined values, which can be added to any Thing.
     */
    public Properties
    getProperties() {
        return properties;
    }

    /**
     * Set the properties for this Thing.
     */
    public void
    setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Get the value of the named property. If it doesn't exist, then null
     * is returned.
     */
    public String
    getProperty(String key) {
        if (properties == null) {
            return null;
        }

        return properties.getProperty(key);
    }

    /**
     * Set a new property, or replace an existing one.
     */
    public void
    setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }

        properties.setProperty(key, value);
    }

    public int
    getPropertyCount() {
        if (properties == null) {
            return 0;
        }
        return properties.size();
    }
}
