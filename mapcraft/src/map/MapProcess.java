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

import java.io.*;
import java.util.*;

import net.sourceforge.mapcraft.utils.Options;

/**
 * A class object which describes a region as one or more maps.
 * Each view of the map is held as a TileSet. The "root" TileSet
 * is the top level map, normally at the largest scale. Other
 * TileSets can be stored as part of the same map object, all
 * views, possibly at higher resolution, of regions within the
 * "root" TileSet.
 *
 * This class does not provide any display functionality, it
 * is merely a holder for the map data.
 *
 * @author  Samuel Penn
 * @version $Revision$
 */
public class MapProcess extends Map {

    /**
     * Load a map from an existing XML file and construct
     * the necessary data sets.
     *
     * @param filename  Filename of map to load.
     * @throws          MapException
     */
    public
    MapProcess(String filename) throws MapException {
        super(filename);
    }

    public void
    convert(Properties table) {
        short       terrain = 0;
        String      value = null;

        for (int x=0; x < getWidth(); x++) {
            for (int y=0; y < getHeight(); y++) {
                try {
                    terrain = getTerrain(x, y);
                    value = table.getProperty(""+terrain, ""+terrain);

                    setTerrain(x, y, Short.parseShort(value));

                } catch (MapOutOfBoundsException moobe) {
                } catch (NumberFormatException nfe) {
                }
            }
        }
    }

    public static void
    main(String args[]) {
        MapProcess  map = null;
        Options     options;
        String      infile = null;
        String      outfile = null;

        try {
            options = new Options(args);

            if (options.isOption("-in")) {
                infile = options.getString("-in");
            }
            if (options.isOption("-out")) {
                outfile = options.getString("-out");
            }

            if (options.isOption("-retile")) {
                String      table = null;
                Properties  props = null;

                table = options.getString("-retile");
                props = new Properties();
                props.load(new FileInputStream(new File(table)));

                map = new MapProcess(infile);
                map.convert(props);
                map.loadTerrainSet("terrain/hexagonal.xml");
                map.save(outfile);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
