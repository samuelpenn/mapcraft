
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

import uk.co.demon.bifrost.utils.Options;

/**
 * A version of the MapViewer class which is designed for generating
 * standalone images from the Map, such as a JPEG file. Does not
 * support any form of interactive editing.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class MapImage extends MapViewer {
    private int     thumbnail = 0;

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
    toImage(int scale, boolean unwrap) {
        BufferedImage   image = null;
        Graphics2D      graphics = null;
        int             w, h;

        setView(scale);
        forceImageLoad();
        try {
            Thread.sleep(1000);
        } catch (Exception ie) {
        }

        try {
            w = map.getWidth() * tileXSize;
            h = map.getHeight() * tileYSize + tileYOffset;

            if (unwrap) {
                map.unwrapWorld();
            }
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
    // This method returns a buffered image with the contents of an image
    private BufferedImage
    getBufferedImage(Image image) {
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        /*
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        */

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
    
        return bimage;
    }


    private void
    saveAsJPEG(BufferedImage image, String filename) throws IOException {
        File          file = new File(filename);
        OutputStream  out = new BufferedOutputStream(new FileOutputStream(file));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        encoder.encode(image);
    }

    public void
    saveImage(String filename, int scale, boolean unwrap) {
        OutputStream    out = null;
        BufferedImage   image = null;

        System.out.println("Saving image as ["+filename+"]");

        try {
            image = toImage(scale, unwrap);
            saveAsJPEG(image, filename);

            if (thumbnail > 0) {
                Image   thumb = image.getScaledInstance(thumbnail, -1,
                                                Image.SCALE_SMOOTH);
                String  thumbFile = filename.replaceAll("\\.jpg", "-t.jpg");
                saveAsJPEG(getBufferedImage(thumb), thumbFile);
            }

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

    public void
    setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public static void
    main(String args[]) {
        Options     options = new Options(args);
        try {
            MapImage    map = null;
            String      filename = options.getString("-image");
            String      outfile = options.getString("-out");
            Properties  properties = new Properties();

            properties.setProperty("path.run", System.getProperty("user.dir"));
            properties.setProperty("path.images", System.getProperty("user.dir")+"/images");

            map = new MapImage(properties, filename);
            map.setShowThings(true);
            map.setShowAreas(true);
            map.setShowLargeGrid(false);
            map.saveImage(outfile, 2, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
