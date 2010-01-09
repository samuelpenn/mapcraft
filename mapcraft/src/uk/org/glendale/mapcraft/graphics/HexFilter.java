package uk.org.glendale.mapcraft.graphics;

import java.awt.image.RGBImageFilter;

/**
 * Image filter which turns a rectangle into a hexagon.
 * Filters on an image, forcing any pixels outside the hexagonal region
 * to be transparent. The visible hexagon will be in to the top left
 * hand corner of the image, which often results in the bottom part of
 * a rectangular image being blank.
 * 
 * @author Samuel Penn.
 */
public class HexFilter extends RGBImageFilter {
    private int     width;
    private int     h;

    private double  SQRTHREE;

    public
    HexFilter(int width) {
        this.width = width;

        canFilterIndexColorModel = true;

        SQRTHREE = Math.sqrt(3);
        h = (int)((SQRTHREE/4)*width+0.5);
        
        System.out.println(width+", "+h);
    }

    private int lastY = -1;
    public int
    filterRGB(int x, int y, int rgb) {
        int     dx = x;
        int     dy = y;
        if (y != lastY) {
        	System.out.println(y);
        	lastY = y;
        }

        if (y > h*2) {
            return 0;
        }

        if (x > width/2) {
            dx = width - x;
        }
        if (y > h) {
            dy = (h*2) - y;
        }

        int  a = (int)(SQRTHREE*dx + 0.5);
        if ((h-dy) > a) {
            rgb = 0;
        }

        return rgb;
    }


}
