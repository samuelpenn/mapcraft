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

package uk.co.demon.bifrost.rpg.mapcraft.map;

import java.util.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * A class which describes a vector object on the map, such as
 * a river or a road. A path consists of a number of elements,
 * each centred on a tile in the map.
 */
public class Path {
    public static final short   START = 1;
    public static final short   END = 2;
    public static final short   PATH = 3;
    public static final short   JOIN = 4;
    

    protected String    name;
    protected Vector    elements;

    /**
     * Inner class which describes an element of a path.
     */
    public class Element {
        protected int       type;
        protected int       x;
        protected int       y;
        protected int       width;

        public
        Element(int type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = 1;
        }

        public
        Element(int type, int x, int y, int w) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = w;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
    }

    public
    Path(String name, int x, int y) {
        this.name = name;

        Element e = new Element(START, x, y);
        elements = new Vector();
        elements.add(e);
    }
    
    
    public void
    add(int x, int y) {
        add(PATH, x, y);
    }
    
    public void
    add(int type, int x, int y) {
        Element e = new Element(type, x, y);
        elements.add(e);
    }
    
    /**
     * Return a Graphics2D shape representing this path.
     * This can then be used to draw directly to a Java
     * Graphics2D object.
     */
    public Shape
    getGraphicsShape(Graphics2D g, int xscale, int yscale, int offset, int iconWidth, int iconHeight) {
        GeneralPath     gp = new GeneralPath();
        Line2D          line = null;
        int             i;
        Point2D         p1 = null, p2 = null;

        float           x, y;
        double          xs, ys;

        GraphicsConfiguration gc = g.getDeviceConfiguration();

        AffineTransform transform = gc.getNormalizingTransform();

        xs = transform.getScaleX();
        ys = transform.getScaleY();

        System.out.println("This path as "+elements.size()+" elements");
        System.out.println("Scaling factors are "+xs+" and "+ys);

        for (i=0; i < elements.size(); i++) {
            Element e = (Element)elements.elementAt(i);
            x = (float)e.getX() * xscale + (float)iconWidth/2;
            y = (float)e.getY() * xscale + (float)iconHeight/2;

            if (e.getX()%2 == 1) {
                System.out.println("Offset is "+offset);
                y += offset;
            }
            
            x = x * (float)xs;
            y = y * (float)ys;

            System.out.println("Point "+x+","+y);

            if (p1 == null) {
                p1 = new Point2D.Float(x, y);
            } else {
                p2 = new Point2D.Float(x, y);

                line = new Line2D.Float(p1, p2);
                gp.append(line, true);
                p1 = p2;
            }
        }

        return gp;

    }


}

