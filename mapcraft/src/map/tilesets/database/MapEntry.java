/*
 * Copyright (C) 2005 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.map.tilesets.database;

import java.sql.*;

/**
 * Represents a map descriptor in the database. Holds all the properties
 * of a map row in the 'mapcraft' table.
 * 
 * @author Samuel Penn
 */
class MapEntry {
    private String      name;
    private String      shape;
    private String      template;
    private String      description;
    private int         width;
    private int         height;
    private int         scale;
    
    MapEntry(ResultSet results) throws SQLException {
        name = results.getString("name");
        shape = results.getString("shape");
        template = results.getString("template");
        width = results.getInt("width");
        height = results.getInt("height");
        scale = results.getInt("scale");
        description = results.getString("description");
    }
    
    String getName() { return name; }
    String getShape() { return shape; }
    String getTemplate() { return template; }
    String getDescription() { return description; }
    
    int getWidth() { return width; }
    int getHeight() { return height; }
    int getScale() { return scale; }
    
    
}
