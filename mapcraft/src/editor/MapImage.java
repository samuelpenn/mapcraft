
/*
 * Copyright (C) 2002 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.editor;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import com.sun.image.codec.jpeg.*;

import net.sourceforge.mapcraft.utils.Options;

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

    private void
    clearToWhite(BufferedImage image) {
        Graphics2D  g = image.createGraphics();
        int         width = image.getWidth();
        int         height = image.getHeight();
        Rectangle   rectangle;

        rectangle = new Rectangle(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.fill(rectangle);
    }

    /**
     * Convert the map to an Image, ready for saving to disc.
     *
     * @param scale     Scale to display at, from 0 (xsmall), 
     *                  2 (medium), 4 (xlarge)
     */
    public BufferedImage
    toImage(int scale, boolean unwrap) {
        BufferedImage   image = null;
        Graphics2D      graphics = null;
        int             w, h;

        setView(scale);
        try {
            Thread.sleep(1000);
        } catch (Exception ie) {
        }

        try {
            w = map.getWidth() * tileXSize + (iconWidth - tileXSize);
            h = map.getHeight() * tileYSize + tileYOffset;

            if (unwrap) {
                map.unwrapWorld();
            }
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            System.out.println("Painting image...");
            clearToWhite(image);
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
    getBufferedImage(Image image, int crop) {
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null)-crop*2,
                    image.getHeight(null)-crop*2, type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0-crop, 0-crop, null);
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
    saveCelestia(String filename, int scale, boolean unwrap) {
        OutputStream    out = null;
        BufferedImage   image = null;
        Image           texture = null;
        int             crop = 16;

        System.out.println("Saving image as ["+filename+"]");

        try {
            image = toImage(scale, unwrap);
            System.out.println("Original image is "+image.getWidth()+
                               "x"+image.getHeight());
            
            int     w = 2048 + crop * 2;
            int     h = 1024 + crop * 2;
            texture = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            // Need to crop the image, to remove 'half hexes'.
            image = getBufferedImage(texture, crop);
            texture = null; // Free up some memory.
            System.out.println("Final image is "+image.getWidth()+
                    "x"+image.getHeight());
            saveAsJPEG(image, filename);

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
                saveAsJPEG(getBufferedImage(thumb, 0), thumbFile);
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
            properties.setProperty("path.images", "");

            map = new MapImage(properties, filename);

            if (options.isOption("-thing")) {
                String  thing = options.getString("-thing");
                int     radius = options.getInt("-radius");

                map.cropToThing(thing, radius);
            }

            if (options.isOption("-area")) {
                String  area = options.getString("-area");
                int     margin = options.getInt("-margin");

                map.cropToArea(area, margin);
            }
            boolean unwrap = options.isOption("-unwrap");
            boolean celestia = options.isOption("-celestia");
            int scale = 2;
            if (options.isOption("-scale")) {
                scale = options.getInt("-scale");
            }

            map.setShowThings(true);
            map.setShowAreas(true);
            map.setShowLargeGrid(false);
            if (celestia) {
                map.saveCelestia(outfile, scale, unwrap);
            } else {
                map.saveImage(outfile, scale, unwrap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
