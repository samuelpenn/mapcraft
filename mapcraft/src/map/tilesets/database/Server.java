/*
 * Copyright (C) 2005 Samuel Penn, sam@glendale.org.uk
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

import java.net.URL;
import java.sql.*;
import java.util.*;

/**
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Server {
    private String      url = null;
    private String      driverName = "com.mysql.jdbc.Driver";
    
    private Connection  cx = null;
    
    /**
     * Create a new server instance ready to connect to the database.
     * Does not actually connect, just tries to load the right driver.
     * 
     * @param url           URL of the database to connect to.
     * @param driverName    Name of the driver class to use.
     */
    public Server(String url, String driverName) {
        this.url = url;
        this.driverName = driverName;
        
        loadDriver();
    }
    
    /**
     * Create a new server instance ready to connect to the database.
     * Does not actually connect, just tries to load the right driver.
     * By default assumes a MySQL database.
     * 
     * @param url           URL of the database to connect to.
     */
    public Server(String url) {
        this.url = url;
        
        loadDriver();
    }
    
    private void loadDriver() {
        try {
            Class.forName(driverName).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
        
    public void connect(Properties properties) throws SQLException {
        
        try {
            Driver  driver = DriverManager.getDriver(url);
            
            cx = driver.connect(url, properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void disconnect() throws SQLException {
        cx.close();
        cx = null;
    }
    
    public MapEntry[] getAllMaps() throws SQLException {
    	ArrayList      list = new ArrayList();
        
        String      sql = "select * from mapcraft";
        
        Statement   stmnt = cx.createStatement();
        ResultSet   results = stmnt.executeQuery(sql);
        
        try {
            while (results.next()) {
                MapEntry    entry = null;
                entry = new MapEntry(results);
                list.add(entry);
            }
        } finally {
            results.close();
            results = null;
        }
        
        return (MapEntry[])list.toArray(new MapEntry[1]);
    }
    
    public Hashtable getTerrainList(URL url) {
    	return MapBuilder.getTerrainList(url);   
    }

    /**
     * Create a new map database. The properties object should contain
     * at least the following:
     *  width       Width of the map.
     *  height      Height of the map.
     *  scale       Scale of the map.
     *  shape       Shape, either 'square' or 'hexagonal'
     * 
     * @param name
     * @param properties
     */
    public void create(String name, Properties properties) throws SQLException {
       int      width = 64;
       int      height = 64;
       int      scale = 1;
       String   shape = "hexagonal";
       String   template = "standard";
       String   description = name;
       
       width = Integer.parseInt(properties.getProperty("width", "64"));
       height = Integer.parseInt(properties.getProperty("height", "64"));
       scale = Integer.parseInt(properties.getProperty("scale", "1"));
       
       shape = properties.getProperty("shape", "hexagonal");
       template = properties.getProperty("template", "standard");
       description = properties.getProperty("description", name);
       
       StringBuffer     query = new StringBuffer("insert into mapcraft (");
       
       query.append("name, description, shape, template, ");
       query.append("width, height, scale) values (");
       query.append("'").append(name).append("', ");
       query.append("'").append(description).append("', ");
       query.append("'").append(shape).append("', ");
       query.append("'").append(template).append("', ");
       query.append(width).append(", ");
       query.append(height).append(", ");
       query.append(scale).append(")");
       
       Statement   stmnt = cx.createStatement();
       stmnt.executeUpdate(query.toString());
    }
    
    MapEntry find(String name) throws SQLException {
        MapEntry    entry = null;
        String      sql = "select * from mapcraft where name='"+name+"'";
        
        Statement   stmnt = cx.createStatement();
        ResultSet   results = stmnt.executeQuery(sql);
        
        try {
            if (results.next()) {
                entry = new MapEntry(results);
            }
        } finally {
            results.close();
            results = null;
        }

        return entry;
    }
    
    
    
    /**
     * Create a new set of map databases from scratch.
     */
    void createEverything() {
        
    }
    
}
