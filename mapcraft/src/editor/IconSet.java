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

import java.awt.*;

/**
 * Holds all the icons for a particular view.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class IconSet {
    private short[]     ids;
    private Image[]     icons;
    private String      name;

    private static final int    defaultSize = 64;
    private static final int    growStep = 32;
    private int                 currentSize = 0;

    /**
     * Create a new, empty, icon set.
     *
     * @param name  Name used to identify this icon set.
     */
    public
    IconSet(String name) {
        this.name = name;
        ids = new short[defaultSize];
        icons = new Image[defaultSize];

        currentSize = 0;
    }
    
    public String getName() { return name; }
    public int getSize() { return currentSize; }
    
    private void
    grow() {
        int     size = ids.length;
        int     newSize = size + growStep;
        
        short   newIds[] = new short[newSize];
        Image   newIcons[] = new Image[newSize];
        
        for (int i=0; i<size; i++) {
            newIds[i] = ids[i];
            newIcons[i] = icons[i];
        }
        
        ids = newIds;
        icons = newIcons;

        return;
    }

    /**
     * Add an icon to the set of icons to be used.
     * Icons are identified by an id, which must be
     * unique within the IconSet.
     *
     * @param id    Unique id for this icon.
     * @param icon  Image to use as the icon.
     */
    public void
    add(short id, Image icon) {
        if (currentSize >= ids.length) {
            grow();
        }
        ids[currentSize] = id;
        icons[currentSize] = icon;

        currentSize++;
    }

    /**
     * Returns the Image icon identified by its unique id.
     *
     * @param id    Id of the icon to return.
     * @return      Image for this icon, or null if not found.
     */
    public Image
    getIcon(short id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                return icons[i];
            }
        }
        
        return null;
    }

    /**
     * Make sure that the images for all the icons are fully loaded. Will
     * call prepareImage() on each icon, and if any are not completed it
     * waits 300ms and tries again. It will try a total of 3 times.
     *
     * This should be quicker than calling prepareImage() on each icon
     * individually and waiting for that icon to complete before moving onto
     * the next one.
     *
     * @return      True if all images loaded, false otherwise.
     */
    public boolean
    prepareImages(Component jc) {
        int         count = 3;
        boolean     allDone = false;

        while (!allDone) {
            allDone = true;
            for (int i = 0; i < currentSize; i++) {
                allDone = jc.prepareImage(icons[i], -1, -1, jc) && allDone;
            }
            if (!allDone) {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                }
                if (count-- <= 0) {
                    break;
                }
            }
        }

        return allDone;
    }
}
