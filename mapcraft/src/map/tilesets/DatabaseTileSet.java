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
package net.sourceforge.mapcraft.map.tilesets;

import java.sql.*;
import java.util.*;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.*;
import net.sourceforge.mapcraft.map.interfaces.ITileSet;
import net.sourceforge.mapcraft.map.tilesets.database.Server;

/**
 * Implementation of a TileSet which stores information in a database.
 * This should allow very large maps which can't otherwise fit into
 * memory.
 * 
 * @author Samuel Penn
 */
public class DatabaseTileSet extends AbstractTileSet {
    private String      url = null;
    private String      driver = "my.sql.Driver";
    private Server      server = null;
    
    public DatabaseTileSet(String name, int x, int y, int scale, String url, Properties properties) {
        this.url = url;
        
        try {
            Driver  driver = new com.mysql.jdbc.Driver();
            
            Connection  cx = driver.connect(url, properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#crop(int, int, int, int)
     */
    public void crop(int x, int y, int width, int height) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#rescale(int)
     */
    public void rescale(int newScale) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToArea(net.sourceforge.mapcraft.map.elements.Area, int)
     */
    public void cropToArea(Area area, int margin) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToHighlighted()
     */
    public void cropToHighlighted() throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToThing(java.lang.String, short)
     */
    public void cropToThing(String name, short radius) throws MapException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#cropToPath(java.lang.String, short)
     */
    public void cropToPath(String name, short margin) throws MapException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setPaths(net.sourceforge.mapcraft.map.elements.Path[])
     */
    public void setPaths(Path[] paths) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#setThings(net.sourceforge.mapcraft.map.elements.Thing[])
     */
    public void setThings(Thing[] things) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getPaths()
     */
    public Path[] getPaths() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getThings()
     */
    public Thing[] getThings() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getThingNames()
     */
    public String[] getThingNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getThing(java.lang.String)
     */
    public Thing getThing(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getPathNames(short)
     */
    public String[] getPathNames(short type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#getPath(java.lang.String)
     */
    public Path getPath(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#addPath(java.lang.String, short, short, int, int)
     */
    public Path addPath(String name, short type, short style, int x, int y) throws MapException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#removeThing(net.sourceforge.mapcraft.map.elements.Thing)
     */
    public void removeThing(Thing thing) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#addThing(net.sourceforge.mapcraft.map.elements.Thing)
     */
    public void addThing(Thing thing) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#copy(int, int, int, int)
     */
    public void copy(int fromX, int fromY, int toX, int toY) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mapcraft.map.interfaces.ITileSet#copy(net.sourceforge.mapcraft.map.interfaces.ITileSet, int, int, int, int)
     */
    public void copy(ITileSet source, int fromX, int fromY, int toX, int toY) throws MapOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }


    public static void main(String[] args) throws Exception {
        DatabaseTileSet     ts = null;
        String              url = "jdbc:mysql://wotan/mapcraft";
        Properties          properties = new Properties();
        
        properties.setProperty("user", "mapcraft");
        properties.setProperty("password", "mapcraft");
        
        Server server = new Server(url);
        server.connect(properties);
        
        properties.setProperty("width", "256");
        properties.setProperty("height", "256");
        server.create("mapone", properties);
        
        //ts = new DatabaseTileSet("foo", 640, 480, 1, url, properties);
    }
}
