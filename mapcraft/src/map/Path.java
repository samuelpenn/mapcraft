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
        public int getType() { return type; }

        public void setType(int type) { this.type = type; }
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
        int         pos = elements.size();
        Element     end = (Element)elements.lastElement();

        if (isAtEnd(x, y) || elements.size()<2) {
            if (end.getType() == END) {
                end.setType(PATH);
            }
            add(END, x, y);
        } else if (isAtStart(x, y)) {
            Element     start = (Element)elements.elementAt(0);
            start.setType(PATH);
            elements.insertElementAt(new Element(START, x, y, 1), 0);
        }
    }

    public void
    add(int type, int x, int y) {
        Element e = new Element(type, x, y, 1);
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

        System.out.println("Scales are "+xs+","+ys);

        for (i=0; i < elements.size(); i++) {
            Element e = (Element)elements.elementAt(i);
            x = (float)e.getX() * xscale + (float)iconWidth/2;
            y = (float)e.getY() * yscale + (float)iconHeight/2;

            if (e.getX()%2 == 1) {
                y += offset;
            }

            //x = x * (float)xs;
            //y = y * (float)ys;

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

    /**
     * Return the name of this path.
     */
    public String
    getName() {
        return name;
    }

    /**
     * Sets the name of the path to the desired value.
     */
    public void
    setName(String name) {
        this.name = name;
    }


    /**
     * Returns a Vector of all the elements in this path. The Vector
     * contains objects of type Path.Element.
     */
    public Vector
    getElements() {
        return elements;
    }

    /**
     * Returns the end point element in this path.
     */
    public Element
    getEndPoint() {
        if (elements.size() > 0) {
            return (Element)elements.elementAt(elements.size()-1);
        } else {
            return null;
        }
    }

    /**
     * Returns the start point element for this path.
     */
    public Element
    getStartPoint() {
        if (elements.size() > 0) {
            return (Element)elements.elementAt(0);
        } else {
            return null;
        }
    }

    public boolean
    isAtEnd(int x, int y) {
        Element     e = (Element)elements.lastElement();
        if (Map.isNextTo(x, y, e.getX(), e.getY())) {
            return true;
        }

        return false;
    }

    public boolean
    isAtStart(int x, int y) {
        Element     e = (Element)elements.elementAt(0);
        if (Map.isNextTo(x, y, e.getX(), e.getY())) {
            return true;
        }

        return false;
    }

}

