
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
package uk.co.demon.bifrost.rpg.mapcraft.editor;

import uk.co.demon.bifrost.rpg.mapcraft.map.*;
import uk.co.demon.bifrost.rpg.mapcraft.map.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;

import java.util.*;
import java.awt.geom.Line2D;

import com.sun.image.codec.jpeg.*;


/**
 * A version of the MapViewer class which is designed for generating
 * standalone images from the Map, such as a JPEG file. Does not
 * support any form of interactive editing.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class MapImage extends MapViewer {

    public
    MapImage() {
        super();

        toolkit = Toolkit.getDefaultToolkit();
    }

    public
    MapImage(Properties properties, String filename) {
        super(properties, filename);
    }

    /**
     * Forces all images to be loaded into memory. If this is not done,
     * when we generate the off-screen image, none of the images will
     * have been loaded from disc, so we get a blank image.
     */
    private void
    forceImageLoad() {
        int     i = 0;

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert the map to an Image, ready for saving to disc.
     *
     * @param scale     Scale to display at, from 0 (xx-small), 3 (medium), 6 (xxlarge)
     */
    public BufferedImage
    toImage(int scale) {
        BufferedImage   image = null;
        Graphics2D      graphics = null;
        int             w, h;

        setView(scale);
        forceImageLoad();

        try {
            w = map.getWidth() * tileXSize;
            h = map.getHeight() * tileYSize;

            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            System.out.println("Painting image...");
            paintComponent(image.createGraphics());
            System.out.println("...finished painting");

        } catch (Exception e) {
            System.out.println("toImage: "+e.getMessage());
            e.printStackTrace();
        }

        return image;
    }

    public void
    saveImage(String filename, int scale) {
        OutputStream    out = null;
        BufferedImage   image = null;

        System.out.println("Saving image as ["+filename+"]");

        try {
            image = toImage(scale);

            File file = new File(filename);

            out = new BufferedOutputStream(new FileOutputStream(file));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

            encoder.encode(image);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable t) {}
        }
    }

    public static void
    main(String args[]) {
        try {
            MapImage    map = null;
            String      filename = "maps/Euressa.map";
            Properties  properties = new Properties();

            properties.setProperty("path.run", System.getProperty("user.dir"));
            properties.setProperty("path.images", System.getProperty("user.dir")+"/images");

            map = new MapImage(properties, filename);
            map.setShowSites(false);
            map.setShowAreas(false);
            map.setShowLargeGrid(false);
            map.saveImage("foo.jpg", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
