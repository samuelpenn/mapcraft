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
 * An area is a continguous named region of the map. Each area is
 * identified by a unique id (which is used internally for a quick
 * lookup), a unique uri, and a name. Each area can also have a
 * parent.
 */
public class Area implements Cloneable {
    private int         id;
    private String      name;
    private String      uri;
    private int         parent;


    public
    Area(int id, String name, String uri) {
        this.id = id;
        this.name = name;
        this.uri = uri;
    }

    public
    Area(int id, String name, String uri, int parent) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.parent = parent;
    }

    public Object
    clone() throws CloneNotSupportedException {
        Area    a = new Area(id, name, uri, parent);
        return (Object)a;
    }

    /**
     * Set the unique id for this area. Should be a positive
     * (non-zero) integer.
     */
    public void
    setId(int id) {
        this.id = id;
    }

    /**
     * Sets the name of this area. Should be unique for all areas.
     * This is the human-readable name possibly displayed on the map.
     */
    public void
    setName(String name) {
        this.name = name;
    }

    /**
     * Set the parent of this area, or zero if no parent. The parent area
     * is inclusive of all its children. For example, the counties of
     * England would have 'England' as their parent.
     */
    public void
    setParent(int parent) {
        this.parent = parent;
    }
    
    /**
     * Set the uri of this area. The uri consists of "[a-z\-]*", and is
     * used to uniquely identify the area from external applications.
     * If Yagsbook Encyclopedia is being used, the uri should match the
     * uri of the area's corresponding article.
     *  
     * @param uri   The uri to be set.
     */
    public void
    setUri(String uri) {
        this.uri = uri;
    }

    public int
    getId() {
        return id;
    }

    public String
    getName() {
        return name;
    }

    public int
    getParent() {
        return parent;
    }
    
    public String
    getUri() {
        return uri;
    }
}
