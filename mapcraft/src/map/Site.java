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


public class Site {
    private short       type;
    private String      name;
    private String      description;
    
    public
    Site(short type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
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
}
