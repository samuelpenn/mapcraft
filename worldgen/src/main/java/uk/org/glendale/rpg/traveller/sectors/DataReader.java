/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */

package uk.org.glendale.rpg.traveller.sectors;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.*;

public class DataReader {
    public DataReader(File file) {
    	ObjectFactory		factory = new ObjectFactory();
        FileReader          reader = null;
        LineNumberReader    input = null;
        try {
            String    line = null;
            
            reader = new FileReader(file);
            input = new LineNumberReader(reader);
            
            for (line = input.readLine(); line != null; line = input.readLine()) {
            	System.out.println(input.getLineNumber()+": "+line);
            	UWP		uwp = new UWP(line);
            	
            	String	sectorName = uwp.getSectorName();
            	int		sectorX = uwp.getSectorX();
            	int	    sectorY = uwp.getSectorY();
            	
            	Sector	sector = new Sector(sectorName, sectorX, sectorY);
            	StarSystem		ss = new StarSystem(factory, sector.getId(),uwp);
            }
                
        } catch (IOException e) {
        }
    }
    
    public static void main(String[] args) {
    	DataReader		dr = new DataReader(new File("data/allworld.txt"));
    }
    
    

}
