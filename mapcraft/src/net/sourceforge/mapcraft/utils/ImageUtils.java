/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import net.sourceforge.mapcraft.editor.Pane;

/**
 * Utilities for image manipulation.
 * 
 * @author Samuel Penn
 */
public class ImageUtils {
    private Component   component = null;
    private String      resourcePath = null;
    
    public
    ImageUtils() {
        this.resourcePath = "";
    }

    public
    ImageUtils(String path) {
        this.resourcePath = path+"/";
    }
    
    public
    ImageUtils(Component component) {
        this.component = component;
        this.resourcePath = "";
    }
    
    public
    ImageUtils(Component component, String path) {
        this.component = component;
        this.resourcePath = path+"/";
    }
    
    /**
     * Create a new blank image.
     * 
     * @param width     Width of the image.
     * @param height    Height of the image.
     * 
     * @return          Created image.
     */
    public BufferedImage
    createImage(int width, int height) {
        BufferedImage       image = null;
        
        image = new BufferedImage(width, height, 
                                  BufferedImage.TYPE_INT_RGB);
        return image;
        /*
        return image.getScaledInstance(width, height, 
                                       BufferedImage.SCALE_FAST);
        */
    }
    
    /**
     * Create a new plain image with the specified colour. The
     * colour is of the form '#rrggbb' or '#rrggbbaa' where aa
     * is the alpha value. The hash is optional.
     * 
     * @param width     Width of the image.
     * @param height    Height of the image.
     * @param colour    RGB or RGBA colour string.
     * 
     * @return          Created image.
     */
    public Image
    createImage(int width, int height, String colour) {
        BufferedImage   image = createImage(width, height);
        Graphics        g = image.createGraphics();
        int             red=0, green=0, blue=0, alpha=255;
        
        if (colour.startsWith("#")) {
            colour = colour.substring(1);
        }
        
        red = Integer.parseInt(colour.substring(0, 2), 16);
        green = Integer.parseInt(colour.substring(2, 4), 16);
        blue = Integer.parseInt(colour.substring(4, 6), 16);
        if (colour.length() == 8) {
            alpha = Integer.parseInt(colour.substring(6, 8));
        }

        g.setColor(new Color(red, green, blue, alpha));
        g.fillRect(0, 0, width, height);
        
        return image;
    }
    
    /**
     * Return the specified image. If the path starts with '#',
     * then the image is taken to be an RGB colour code, and a
     * new plain image is created of that colour. Otherwise, it
     * is assumed to be a filename and is loaded from the Jar
     * file.
     * 
     * @param path      Path to file or RGB colour code.
     * @param width     Width of image to return.
     * @param height    Height of image to return.
     * 
     * @return          Image scaled to the specified size.
     */
    public Image
    getImage(String path, int width, int height) {
        Image       image = null;
        
        if (path.startsWith("#")) {
            image = createImage(width, height, path);
        } else {
            URL         url = Pane.class.getResource(resourcePath+path);
            image = component.getToolkit().getImage(url);
            image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        
        return image;
    }
}
